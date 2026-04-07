from fastapi import APIRouter
from app.models.schemas import HealthCheckResponse
from app.config import settings

router = APIRouter(tags=["health"])

@router.get("/health", response_model=HealthCheckResponse)
async def health_check():
    """
    Health check endpoint

    Returns:
        HealthCheckResponse with service status
    """
    return HealthCheckResponse(
        status="healthy",
        service="StudyAI FastAPI Service",
        version="1.0.0",
        environment=settings.environment
    )

@router.get("/status")
async def get_status():
    """
    Get service status

    Returns:
        Service status details
    """
    return {
        "status": "running",
        "service": "StudyAI FastAPI",
        "environment": settings.environment,
        "debug": settings.debug,
        "s3_bucket": settings.aws_s3_bucket,
        "openai_model": settings.quiz_generation_model
    }
