from fastapi import APIRouter, UploadFile, File, HTTPException, Form
from fastapi.responses import FileResponse
import tempfile
import os
import logging
from app.services.s3_service import S3Service
from app.services.document_processor import DocumentProcessor
from app.models.schemas import DocumentUploadResponse, DocumentDownloadResponse
from app.utils.helpers import FileManager
from app.config import settings

router = APIRouter(prefix="/documents", tags=["documents"])
logger = logging.getLogger(__name__)

s3_service = S3Service()
doc_processor = DocumentProcessor()

@router.post("/upload", response_model=DocumentUploadResponse)
async def upload_document(
    file: UploadFile = File(...),
    user_id: str = Form(...),
    subject_id: str = Form(...),
    note_id: str = Form(...)
):
    """
    Upload a document (PDF/DOCX) and extract text

    Args:
        file: Uploaded file
        user_id: User ID
        subject_id: Subject ID
        note_id: Note ID

    Returns:
        DocumentUploadResponse with extracted text and S3 location
    """
    try:
        logger.info(
            "Received document upload request: user_id=%s, subject_id=%s, note_id=%s, filename=%s, content_type=%s",
            user_id,
            subject_id,
            note_id,
            file.filename,
            file.content_type,
        )

        # Validate filename
        if not FileManager.validate_filename(file.filename):
            logger.warning(
                "Invalid filename for document upload: user_id=%s, subject_id=%s, note_id=%s, filename=%s",
                user_id,
                subject_id,
                note_id,
                file.filename,
            )
            raise HTTPException(status_code=400, detail="Invalid filename")

        # Get file extension
        file_extension = FileManager.get_file_extension(file.filename)
        if file_extension.lower() not in settings.allowed_file_types:
            logger.warning(
                "Unsupported file type for upload: user_id=%s, subject_id=%s, note_id=%s, filename=%s, extension=%s",
                user_id,
                subject_id,
                note_id,
                file.filename,
                file_extension,
            )
            raise HTTPException(
                status_code=400,
                detail=f"File type '.{file_extension}' not supported. Allowed: {', '.join(settings.allowed_file_types)}"
            )

        # Save uploaded file to temp location
        with tempfile.NamedTemporaryFile(delete=False, suffix=f".{file_extension}") as tmp_file:
            content = await file.read()
            file_size_bytes = len(content)
            logger.debug(
                "Writing uploaded file to temp path for user_id=%s, subject_id=%s, note_id=%s, filename=%s, size_bytes=%d",
                user_id,
                subject_id,
                note_id,
                file.filename,
                file_size_bytes,
            )
            tmp_file.write(content)
            tmp_file_path = tmp_file.name

        try:
            # Validate file
            is_valid, validation_msg = await doc_processor.validate_file(
                tmp_file_path,
                file_extension,
                settings.max_file_size
            )

            if not is_valid:
                logger.warning(
                    "File validation failed for upload: user_id=%s, subject_id=%s, note_id=%s, filename=%s, reason=%s",
                    user_id,
                    subject_id,
                    note_id,
                    file.filename,
                    validation_msg,
                )
                raise HTTPException(status_code=400, detail=validation_msg)

            # Extract text
            extracted_text = await doc_processor.extract_text(tmp_file_path, file_extension)

            logger.debug(
                "Extracted text from uploaded document: user_id=%s, subject_id=%s, note_id=%s, filename=%s, extracted_length=%d",
                user_id,
                subject_id,
                note_id,
                file.filename,
                len(extracted_text) if extracted_text is not None else 0,
            )

            # Generate S3 key
            s3_key = FileManager.generate_s3_key(user_id, subject_id, note_id, file.filename)

            logger.info(
                "Uploading document to S3: user_id=%s, subject_id=%s, note_id=%s, s3_key=%s, filename=%s, size_bytes=%d",
                user_id,
                subject_id,
                note_id,
                s3_key,
                file.filename,
                file_size_bytes,
            )

            # Upload to S3
            upload_result = await s3_service.upload_file(
                tmp_file_path,
                s3_key,
                content_type=file.content_type or "application/octet-stream"
            )

            file_id = FileManager.generate_file_id()
            file_size = os.path.getsize(tmp_file_path)

            logger.info(
                "Document upload completed: file_id=%s, user_id=%s, subject_id=%s, note_id=%s, s3_key=%s, file_name=%s, file_type=%s, file_size_bytes=%d",
                file_id,
                user_id,
                subject_id,
                note_id,
                s3_key,
                file.filename,
                file_extension,
                file_size,
            )

            return DocumentUploadResponse(
                file_id=file_id,
                file_name=file.filename,
                file_type=file_extension,
                s3_key=s3_key,
                file_size=file_size,
                extracted_text=extracted_text[:10000],  # Return first 10k chars for preview
                extraction_status="success",
                message="Document uploaded and processed successfully"
            )

        finally:
            # Clean up temp file
            if os.path.exists(tmp_file_path):
                os.remove(tmp_file_path)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error uploading document: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error processing document: {str(e)}")

@router.get("/download/{file_id}")
async def download_document(file_id: str, s3_key: str):
    """
    Download a document from S3

    Args:
        file_id: File ID
        s3_key: S3 object key

    Returns:
        File for download
    """
    try:
        # Create temp file for download
        with tempfile.NamedTemporaryFile(delete=False) as tmp_file:
            tmp_file_path = tmp_file.name

        # Download from S3
        download_result = await s3_service.download_file(s3_key, tmp_file_path)

        if download_result["status"] == "success":
            logger.info(f"Document downloaded: {file_id}")
            return FileResponse(
                tmp_file_path,
                filename=os.path.basename(s3_key),
                media_type="application/octet-stream"
            )
        else:
            raise HTTPException(status_code=404, detail="File not found")

    except Exception as e:
        logger.error(f"Error downloading document: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error downloading document: {str(e)}")

@router.delete("/delete/{file_id}")
async def delete_document(file_id: str, s3_key: str):
    """
    Delete a document from S3

    Args:
        file_id: File ID
        s3_key: S3 object key

    Returns:
        Success message
    """
    try:
        delete_result = await s3_service.delete_file(s3_key)

        if delete_result["status"] == "success":
            logger.info(f"Document deleted: {file_id}")
            return {"status": "success", "message": "Document deleted successfully", "file_id": file_id}
        else:
            raise HTTPException(status_code=404, detail="File not found")

    except Exception as e:
        logger.error(f"Error deleting document: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error deleting document: {str(e)}")

@router.get("/info/{file_id}")
async def get_document_info(file_id: str, s3_key: str):
    """
    Get document metadata

    Args:
        file_id: File ID
        s3_key: S3 object key

    Returns:
        Document metadata
    """
    try:
        file_info = await s3_service.get_file_info(s3_key)
        logger.info(f"Retrieved info for document: {file_id}")
        return {
            "file_id": file_id,
            "s3_key": s3_key,
            **file_info
        }
    except Exception as e:
        logger.error(f"Error retrieving document info: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error retrieving document info: {str(e)}")

@router.get("/presigned-url/{file_id}")
async def get_presigned_url(file_id: str, s3_key: str, expiration: int = 3600):
    """
    Generate a presigned URL for downloading a document

    Args:
        file_id: File ID
        s3_key: S3 object key
        expiration: URL expiration time in seconds (default 1 hour)

    Returns:
        Presigned URL for downloading the file
    """
    try:
        presigned_url = await s3_service.generate_presigned_url(s3_key, expiration)
        logger.info(f"Generated presigned URL for document: {file_id}")
        return {
            "file_id": file_id,
            "s3_key": s3_key,
            "presigned_url": presigned_url,
            "expiration_seconds": expiration
        }
    except Exception as e:
        logger.error(f"Error generating presigned URL: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error generating presigned URL: {str(e)}")
