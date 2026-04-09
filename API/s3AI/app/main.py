"""StudyAI FastAPI microservice.

This service is responsible for document ingestion, text extraction, and
AI-powered quiz generation. It is called from the Spring Boot backend and
exposes a small set of HTTP endpoints that focus purely on document and
quiz-related tasks.
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
from app.config import settings
from app.utils.helpers import Logger
from app.routers import documents, quiz, health

# Configure logging
Logger.configure_logging(settings.log_level)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="StudyAI FastAPI Service",
    description="Document processing and quiz generation service",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router)
app.include_router(documents.router, prefix="/api")
app.include_router(quiz.router, prefix="/api")

# Global exception handler
@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    logger.error(f"HTTP Exception: {exc.detail}")
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": "HTTP Error",
            "message": exc.detail,
            "status_code": exc.status_code
        }
    )

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    logger.error(f"Unhandled Exception: {str(exc)}")
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal Server Error",
            "message": "An unexpected error occurred",
            "status_code": 500
        }
    )

# Startup event
@app.on_event("startup")
async def startup_event():
    logger.info(f"Starting StudyAI FastAPI Service in {settings.environment} mode")
    logger.info(f"S3 Bucket: {settings.aws_s3_bucket}")
    logger.info(f"OpenAI Model: {settings.quiz_generation_model}")

# Shutdown event
@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Shutting down StudyAI FastAPI Service")

# Root endpoint
@app.get("/")
async def root():
    return {
        "service": "StudyAI FastAPI Service",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app,
        host=settings.server_host,
        port=settings.server_port,
        log_level=settings.log_level.lower(),
        reload=settings.debug
    )
