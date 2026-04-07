#!/usr/bin/env python3
"""
FastAPI Integration Verification Script

This script verifies that OpenAI API and AWS S3 are properly configured
and connected. Run this to test your setup before using the full application.

Usage:
    python verify_setup.py
"""

import sys
import os
from pathlib import Path

# Handle Unicode on Windows
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Add app to path
sys.path.insert(0, str(Path(__file__).parent))

def check_env_file():
    """Check if .env file exists"""
    print("🔍 Checking .env file...")
    env_path = Path(__file__).parent / ".env"
    if env_path.exists():
        print("✅ .env file found")
        return True
    else:
        print("❌ .env file NOT found")
        print(f"   Expected at: {env_path}")
        print("   Create .env file from .env.example")
        return False

def check_dependencies():
    """Check if all required packages are installed"""
    print("\n🔍 Checking dependencies...")
    required_packages = {
        'fastapi': 'FastAPI',
        'uvicorn': 'Uvicorn',
        'boto3': 'boto3 (AWS SDK)',
        'openai': 'OpenAI Python client',
        'dotenv': 'python-dotenv',
        'pydantic': 'Pydantic',
    }

    all_installed = True
    for module, name in required_packages.items():
        try:
            __import__(module)
            print(f"✅ {name}")
        except ImportError:
            print(f"❌ {name} - NOT installed")
            all_installed = False

    return all_installed

def check_config():
    """Check if configuration loads correctly"""
    print("\n🔍 Checking configuration...")
    try:
        from app.config import settings
        print(f"✅ Configuration loaded")
        print(f"   Environment: {settings.environment}")
        print(f"   Debug: {settings.debug}")
        print(f"   Server: {settings.server_host}:{settings.server_port}")
        print(f"   AWS Region: {settings.aws_region}")
        print(f"   S3 Bucket: {settings.aws_s3_bucket}")
        print(f"   OpenAI Model: {settings.quiz_generation_model}")
        return True
    except Exception as e:
        print(f"❌ Configuration error: {str(e)}")
        return False

def check_openai():
    """Check OpenAI API connection"""
    print("\n🔍 Checking OpenAI API connection...")
    try:
        from app.config import settings

        if not settings.openai_api_key:
            print("❌ OpenAI API key is empty")
            return False

        from openai import OpenAI
        client = OpenAI(api_key=settings.openai_api_key)

        # Try to list models (lightweight call)
        models = client.models.list()
        model_count = len(models.data)

        print(f"✅ OpenAI API connected")
        print(f"   Available models: {model_count}")

        # Check if GPT-4 is available
        model_ids = [m.id for m in models.data]
        if 'gpt-4' in model_ids or any('gpt-4' in m_id for m_id in model_ids):
            print(f"   ✅ GPT-4 is available")
        else:
            print(f"   ⚠️  GPT-4 not found in available models")
            print(f"   Available: {', '.join(model_ids[:5])}...")

        return True
    except Exception as e:
        print(f"❌ OpenAI API connection failed")
        print(f"   Error: {str(e)}")
        return False

def check_s3():
    """Check AWS S3 connection"""
    print("\n🔍 Checking AWS S3 connection...")
    try:
        from app.config import settings

        if not settings.aws_access_key_id or not settings.aws_secret_access_key:
            print("❌ AWS credentials are empty")
            return False

        import boto3
        from botocore.exceptions import ClientError

        s3_client = boto3.client(
            's3',
            region_name=settings.aws_region,
            aws_access_key_id=settings.aws_access_key_id,
            aws_secret_access_key=settings.aws_secret_access_key
        )

        # Try to access bucket
        s3_client.head_bucket(Bucket=settings.aws_s3_bucket)

        print(f"✅ AWS S3 connected")
        print(f"   Bucket: {settings.aws_s3_bucket}")
        print(f"   Region: {settings.aws_region}")

        # Try to list objects (check permissions)
        response = s3_client.list_objects_v2(
            Bucket=settings.aws_s3_bucket,
            MaxKeys=1
        )
        print(f"   ✅ Read permissions verified")

        return True
    except ClientError as e:
        error_code = e.response['Error']['Code']
        if error_code == 'NoSuchBucket':
            print(f"❌ Bucket not found: {settings.aws_s3_bucket}")
        elif error_code == 'AccessDenied':
            print(f"❌ Access denied to bucket (check IAM permissions)")
        else:
            print(f"❌ AWS S3 error: {error_code}")
        return False
    except Exception as e:
        print(f"❌ AWS S3 connection failed")
        print(f"   Error: {str(e)}")
        return False

def check_services():
    """Check if services can be initialized"""
    print("\n🔍 Checking services...")
    try:
        from app.services.s3_service import S3Service
        from app.services.openai_service import OpenAIService
        from app.services.document_processor import DocumentProcessor

        s3_service = S3Service()
        print("✅ S3Service initialized")

        openai_service = OpenAIService()
        print("✅ OpenAIService initialized")

        doc_processor = DocumentProcessor()
        print("✅ DocumentProcessor initialized")

        return True
    except Exception as e:
        print(f"❌ Service initialization failed")
        print(f"   Error: {str(e)}")
        return False

def main():
    """Run all checks"""
    print("=" * 60)
    print("StudyAI FastAPI Integration Verification")
    print("=" * 60)

    checks = [
        ("Environment File", check_env_file),
        ("Dependencies", check_dependencies),
        ("Configuration", check_config),
        ("OpenAI API", check_openai),
        ("AWS S3", check_s3),
        ("Services", check_services),
    ]

    results = {}
    for check_name, check_func in checks:
        try:
            results[check_name] = check_func()
        except Exception as e:
            print(f"\n❌ Unexpected error in {check_name}: {str(e)}")
            results[check_name] = False

    # Summary
    print("\n" + "=" * 60)
    print("SUMMARY")
    print("=" * 60)

    passed = sum(1 for v in results.values() if v)
    total = len(results)

    for check_name, result in results.items():
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{status}: {check_name}")

    print(f"\nTotal: {passed}/{total} checks passed")

    if passed == total:
        print("\n🎉 All checks passed! Your setup is ready.")
        print("\nYou can now run:")
        print("  python -m uvicorn app.main:app --reload")
        return 0
    else:
        print(f"\n⚠️  {total - passed} check(s) failed. Please fix the issues above.")
        return 1

if __name__ == "__main__":
    sys.exit(main())
