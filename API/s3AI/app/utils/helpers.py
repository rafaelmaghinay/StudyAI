import logging
import uuid
from datetime import datetime

logger = logging.getLogger(__name__)

class FileManager:
    """Utility class for managing file operations"""

    @staticmethod
    def generate_s3_key(user_id: str, subject_id: str, note_id: str, filename: str) -> str:
        """
        Generate S3 key for file storage

        Args:
            user_id: User ID
            subject_id: Subject ID
            note_id: Note ID
            filename: Original filename

        Returns:
            S3 key path
        """
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        return f"users/{user_id}/subjects/{subject_id}/notes/{note_id}/{timestamp}_{filename}"

    @staticmethod
    def generate_file_id() -> str:
        """Generate unique file ID"""
        return str(uuid.uuid4())

    @staticmethod
    def get_file_extension(filename: str) -> str:
        """Extract file extension from filename"""
        return filename.split('.')[-1].lower() if '.' in filename else ""

    @staticmethod
    def validate_filename(filename: str) -> bool:
        """Validate filename"""
        if not filename or len(filename) > 255:
            return False
        invalid_chars = ['\\', '/', ':', '*', '?', '"', '<', '>', '|']
        return not any(char in filename for char in invalid_chars)

class Logger:
    """Centralized logging configuration"""

    @staticmethod
    def get_logger(name: str) -> logging.Logger:
        """Get configured logger"""
        return logging.getLogger(name)

    @staticmethod
    def configure_logging(level: str = "INFO"):
        """Configure logging for the application"""
        logging.basicConfig(
            level=getattr(logging, level.upper(), logging.INFO),
            format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
            handlers=[
                logging.StreamHandler(),
            ]
        )
