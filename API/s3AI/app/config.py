from pydantic import BaseSettings
from typing import List
import os
from functools import lru_cache

class Settings(BaseSettings):
    # App Settings
    app_name: str = "StudyAI FastAPI Service"
    environment: str = os.getenv("ENVIRONMENT", "development")
    debug: bool = os.getenv("DEBUG", "True").lower() == "true"
    log_level: str = os.getenv("LOG_LEVEL", "INFO")

    # Server Settings
    server_host: str = os.getenv("SERVER_HOST", "0.0.0.0")
    server_port: int = int(os.getenv("SERVER_PORT", "8000"))

    # AWS S3 Configuration
    aws_access_key_id: str = os.getenv("AWS_ACCESS_KEY_ID", "")
    aws_secret_access_key: str = os.getenv("AWS_SECRET_ACCESS_KEY", "")
    aws_region: str = os.getenv("AWS_REGION", "ap-southeast-1")
    aws_s3_bucket: str = os.getenv("AWS_S3_BUCKET", "study-ai-documents-bucket")

    # Google Gemini Configuration (Primary)
    gemini_api_key: str = os.getenv("GEMINI_API_KEY", "")
    quiz_generation_model: str = os.getenv("QUIZ_GENERATION_MODEL", "gemini-2.0-flash")

    # Grok API Configuration (Fallback)
    grok_api_key: str = os.getenv("GROK_API_KEY", "")
    grok_api_url: str = os.getenv("GROK_API_URL", "https://api.x.ai/v1")
    grok_model: str = os.getenv("GROK_MODEL", "grok-beta")

    # SpringBoot Configuration
    springboot_url: str = os.getenv("SPRINGBOOT_URL", "http://localhost:8080")

    # CORS Configuration
    cors_allowed_origins: List[str] = ["http://localhost:3000", "http://localhost:8080", "http://localhost:8000"]

    # File Upload Configuration
    max_file_size: int = int(os.getenv("MAX_FILE_SIZE", "52428800"))  # 50MB
    allowed_file_types: List[str] = ["pdf", "docx"]

    # Quiz Generation Configuration
    default_num_questions: int = int(os.getenv("DEFAULT_NUM_QUESTIONS", "10"))
    max_num_questions: int = int(os.getenv("MAX_NUM_QUESTIONS", "50"))

    class Config:
        env_file = ".env"
        case_sensitive = False

@lru_cache()
def get_settings() -> Settings:
    return Settings()

settings = get_settings()
