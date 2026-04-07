from pydantic import BaseModel
from typing import List, Optional

# Request/Response DTOs

class DocumentUploadResponse(BaseModel):
    file_id: str
    file_name: str
    file_type: str
    s3_key: str
    file_size: int
    extracted_text: str
    extraction_status: str
    message: str

class QuestionOption(BaseModel):
    text: str
    is_correct: bool

class GeneratedQuestion(BaseModel):
    question_text: str
    question_type: str = "multiple_choice"  # Always multiple choice
    options: List[str]  # Always exactly 4 options [option1, option2, option3, option4]
    correct_answer: str  # One of: A, B, C, or D
    explanation: Optional[str] = None
    order_index: int

class QuestionDocument(BaseModel):
    """Document metadata for quiz generation"""
    s3_key: str
    file_type: str  # 'pdf' or 'docx'

class QuizGenerationRequest(BaseModel):
    """Request with document metadata from SpringBoot"""
    documents: List[QuestionDocument]  # S3 keys and file types
    num_questions: int = 10
    quiz_title: str = "Generated Quiz"

class QuizGenerationResponse(BaseModel):
    quiz_title: str
    questions: List[GeneratedQuestion]
    total_questions: int
    generation_status: str
    message: str

class DocumentDownloadResponse(BaseModel):
    file_name: str
    file_type: str
    file_size: int
    download_url: str

class ErrorResponse(BaseModel):
    error: str
    message: str
    status_code: int

class HealthCheckResponse(BaseModel):
    status: str
    service: str
    version: str
    environment: str
