from app.config import settings
import boto3
from botocore.exceptions import ClientError
from typing import Optional
import logging

logger = logging.getLogger(__name__)

class S3Service:
    def __init__(self):
        self.s3_client = boto3.client(
            's3',
            region_name=settings.aws_region,
            aws_access_key_id=settings.aws_access_key_id,
            aws_secret_access_key=settings.aws_secret_access_key
        )
        self.bucket_name = settings.aws_s3_bucket

    async def upload_file(self, file_path: str, s3_key: str, content_type: str = "application/octet-stream") -> dict:
        """
        Upload a file to S3

        Args:
            file_path: Local file path
            s3_key: S3 object key (path in bucket)
            content_type: MIME type

        Returns:
            dict with upload status and S3 key
        """
        try:
            self.s3_client.upload_file(
                file_path,
                self.bucket_name,
                s3_key,
                ExtraArgs={'ContentType': content_type}
            )
            logger.info(f"Successfully uploaded file to S3: {s3_key}")
            return {
                "status": "success",
                "s3_key": s3_key,
                "bucket": self.bucket_name
            }
        except ClientError as e:
            logger.error(f"Error uploading file to S3: {str(e)}")
            raise Exception(f"Failed to upload file to S3: {str(e)}")

    async def generate_presigned_url(self, s3_key: str, expiration: int = 3600) -> str:
        """
        Generate a presigned URL for accessing S3 object

        Args:
            s3_key: S3 object key
            expiration: URL expiration time in seconds

        Returns:
            Presigned URL string
        """
        try:
            url = self.s3_client.generate_presigned_url(
                'get_object',
                Params={'Bucket': self.bucket_name, 'Key': s3_key},
                ExpiresIn=expiration
            )
            logger.info(f"Generated presigned URL for S3 object: {s3_key}")
            return url
        except ClientError as e:
            logger.error(f"Error generating presigned URL: {str(e)}")
            raise Exception(f"Failed to generate presigned URL: {str(e)}")
