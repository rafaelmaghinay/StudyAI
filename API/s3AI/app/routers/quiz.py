from fastapi import APIRouter, HTTPException
import logging
from app.services.gemini_service import GeminiService
from app.services.s3_document_service import S3DocumentService
from app.models.schemas import (
    QuizGenerationRequest, 
    QuizGenerationResponse
)
from app.config import settings

router = APIRouter(prefix="/quiz", tags=["quiz"])
logger = logging.getLogger(__name__)

gemini_service = GeminiService()
s3_service = S3DocumentService()

@router.post("/generate", response_model=QuizGenerationResponse)
async def generate_quiz(request: QuizGenerationRequest):
    """
    Generate quiz questions from documents stored in S3
    
    Flow:
    1. Receives document metadata (s3_key, file_type) from SpringBoot
    2. Downloads documents from S3
    3. Extracts text from PDFs/DOCX files
    4. Combines extracted text
    5. Generates quiz using Gemini

    Args:
        request: QuizGenerationRequest containing documents, num_questions, quiz_title

    Returns:
        QuizGenerationResponse with generated questions
    """
    try:
        logger.info(
            "Received quiz generation request: title=%s, num_questions=%d, num_documents=%d",
            request.quiz_title,
            request.num_questions,
            len(request.documents) if request.documents else 0,
        )

        # Validate request
        if not request.documents or len(request.documents) == 0:
            logger.warning(
                "Quiz generation request rejected: empty documents, title=%s, num_questions=%d",
                request.quiz_title,
                request.num_questions,
            )
            raise HTTPException(status_code=400, detail="Documents list cannot be empty")

        if request.num_questions <= 0 or request.num_questions > settings.max_num_questions:
            logger.warning(
                "Quiz generation request rejected: invalid num_questions=%d (max=%d), title=%s",
                request.num_questions,
                settings.max_num_questions,
                request.quiz_title,
            )
            raise HTTPException(
                status_code=400,
                detail=f"Number of questions must be between 1 and {settings.max_num_questions}"
            )

        logger.info("Step 1-2: Downloading and extracting text from documents")
        # Step 1-4: Download documents from S3 and extract text
        combined_text = ""
        successful_docs = 0
        failed_docs = []
        
        for doc in request.documents:
            try:
                logger.debug(f"Processing document: {doc.s3_key} ({doc.file_type})")
                
                # Download and extract text
                extracted_text = s3_service.get_document_text(doc.s3_key, doc.file_type)
                
                if extracted_text:
                    combined_text += extracted_text + "\n\n"
                    successful_docs += 1
                    logger.debug(f"Successfully extracted {len(extracted_text)} chars from {doc.s3_key}")
                else:
                    logger.warning(f"Document {doc.s3_key} returned empty text")
                    failed_docs.append((doc.s3_key, "Empty text extracted"))
                    
            except Exception as e:
                logger.error(f"Error processing document {doc.s3_key}: {str(e)}")
                failed_docs.append((doc.s3_key, str(e)))
        
        # Check if we have any text
        if not combined_text or len(combined_text.strip()) == 0:
            error_msg = f"Failed to extract text from all documents. Details: {failed_docs}"
            logger.error(error_msg)
            raise HTTPException(
                status_code=400, 
                detail=f"Could not extract text from documents. {error_msg}"
            )
        
        if failed_docs:
            logger.warning(
                f"Successfully processed {successful_docs} documents, {len(failed_docs)} failed: {failed_docs}"
            )
        else:
            logger.info(f"Successfully extracted text from all {successful_docs} documents")
        
        logger.info("Step 3: Generating quiz using Gemini")
        # Step 3: Generate quiz using Gemini
        quiz_response = await gemini_service.generate_quiz_from_text(
            text=combined_text,
            num_questions=request.num_questions,
            quiz_title=request.quiz_title
        )
        
        logger.info(
            "Successfully generated quiz: title=%s, requested_questions=%d, generated_questions=%d, used_docs=%d",
            quiz_response['quiz_title'],
            request.num_questions,
            len(quiz_response['questions']),
            successful_docs
        )

        return QuizGenerationResponse(
            quiz_title=quiz_response['quiz_title'],
            questions=quiz_response['questions'],
            total_questions=len(quiz_response['questions']),
            generation_status="success",
            message=f"Successfully generated {len(quiz_response['questions'])} quiz questions"
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error generating quiz: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Error generating quiz: {str(e)}")
