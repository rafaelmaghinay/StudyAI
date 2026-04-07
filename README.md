# StudyAI

A comprehensive learning platform that generates quiz questions from notes using AI, allows users to take quizzes, view results, and track their performance.

## Project Structure

```
StudyAI/
├── API/
│   ├── s3AI/                 # Python FastAPI - Document processing & AI quiz generation
│   └── studyai/              # Spring Boot Java - Main backend API
├── Webapp/                   # React TypeScript - Frontend web application
└── README.md
```

## Tech Stack

### Frontend
- **React 18** with TypeScript
- **Vite** - Build tool
- **Axios** - HTTP client
- **CSS** - Styling

### Backend API (Spring Boot)
- **Spring Boot** - Java REST API
- **JPA/Hibernate** - ORM
- **PostgreSQL** - Database
- **JWT** - Authentication

### Document Processing (FastAPI)
- **FastAPI** - Python API
- **Google Gemini AI** - Quiz generation
- **Grok AI** - Fallback provider
- **AWS S3** - File storage
- **boto3** - AWS SDK

## Features

- 📝 **Note Management** - Upload and organize study notes
- 🤖 **AI Quiz Generation** - Automatically generate quiz questions from notes
- 📊 **Quiz Attempts** - Take quizzes with real-time scoring
- 📈 **Performance Tracking** - View detailed quiz results and progress
- 🎯 **Subject Organization** - Organize quizzes and notes by subject
- 🔐 **User Authentication** - JWT-based secure authentication

## Prerequisites

- **Node.js** 18+ (for frontend)
- **Java 17+** (for Spring Boot)
- **Python 3.8+** (for FastAPI)
- **PostgreSQL** (database)
- **AWS S3** (file storage)
- **Google Gemini API Key** (AI quiz generation)

## Setup Instructions

### 1. Frontend Setup (React)

```bash
cd Webapp
npm install
npm run dev
```

The frontend runs on `http://localhost:5173`

### 2. Backend API Setup (Spring Boot)

Create `studyai/src/main/resources/application.properties`:

```properties
spring.application.name=studyai
spring.datasource.url=jdbc:postgresql://localhost:5432/studyai
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=your_jwt_secret_key
jwt.expiration=86400000

# S3 Configuration
aws.region=your_region
aws.s3.bucket=your_bucket_name
aws.accessKeyId=your_access_key
aws.secretAccessKey=your_secret_key

# FastAPI Document Service
document.service.url=http://localhost:8000/api
```

Build and run:

```bash
cd studyai
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080`

### 3. FastAPI Setup (Document Processing)

Create `.env` file in `API/s3AI/`:

```
GEMINI_API_KEY=your_gemini_api_key
GROK_API_KEY=your_grok_api_key
AWS_REGION=your_region
AWS_S3_BUCKET=your_bucket_name
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

Setup and run:

```bash
cd API/s3AI
python -m venv venv

# On Windows
venv\Scripts\activate
# On macOS/Linux
source venv/bin/activate

pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8000
```

The FastAPI service runs on `http://localhost:8000`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/refresh` - Refresh JWT token

### Quizzes
- `GET /api/quizzes/user/{userId}` - Get user's quizzes
- `GET /api/quizzes/{id}` - Get quiz details
- `GET /api/quizzes/{id}/questions` - Get quiz questions
- `POST /api/quizzes` - Create quiz
- `PUT /api/quizzes/{id}` - Update quiz
- `DELETE /api/quizzes/{id}` - Delete quiz

### Quiz Attempts
- `GET /api/quiz-attempts/user/{userId}` - Get user's attempts
- `POST /api/quiz-attempts` - Start new attempt
- `GET /api/quiz-attempts/{id}` - Get attempt details
- `GET /api/quiz-attempts/{id}/answers` - Get user's answers
- `PUT /api/quiz-attempts/{id}/submit` - Submit quiz
- `DELETE /api/quiz-attempts/{id}` - Delete attempt

### Notes
- `GET /api/notes/user/{userId}` - Get user's notes
- `POST /api/notes` - Upload note
- `GET /api/notes/{id}/download-url` - Get note download URL
- `DELETE /api/notes/{id}` - Delete note

### Subjects
- `GET /api/subjects/user/{userId}` - Get user's subjects
- `POST /api/subjects` - Create subject
- `PUT /api/subjects/{id}` - Update subject
- `DELETE /api/subjects/{id}` - Delete subject

## Environment Variables

### Spring Boot (.env)
```
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
JWT_SECRET=
AWS_REGION=
AWS_S3_BUCKET=
AWS_ACCESSKEYID=
AWS_SECRETACCESSKEY=
```

### FastAPI (.env in API/s3AI/)
```
GEMINI_API_KEY=
GROK_API_KEY=
AWS_REGION=
AWS_S3_BUCKET=
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
```

### Frontend (.env in Webapp/)
```
VITE_API_URL=http://localhost:8080/api
```

## Database Setup

Create PostgreSQL database:

```sql
CREATE DATABASE studyai;
```

The application will automatically create tables on first run.

## Building for Production

### Frontend
```bash
cd Webapp
npm run build
```

Output: `dist/` directory

### Backend
```bash
cd studyai
./mvnw clean package
```

Output: `target/studyai-0.0.1-SNAPSHOT.jar`

### FastAPI
```bash
cd API/s3AI
# Create production requirements
pip freeze > requirements.txt
```

## Deployment

### Frontend (Vercel/Netlify)
```bash
npm run build
# Deploy dist/ folder
```

### Backend (AWS/Heroku/Railway)
```bash
java -jar target/studyai-0.0.1-SNAPSHOT.jar
```

### FastAPI (Render/Railway/AWS Lambda)
```bash
gunicorn app.main:app --workers 4 --worker-class uvicorn.workers.UvicornWorker
```

## Contributing

1. Create a feature branch (`git checkout -b feature/AmazingFeature`)
2. Commit changes (`git commit -m 'Add AmazingFeature'`)
3. Push to branch (`git push origin feature/AmazingFeature`)
4. Open a Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support

For issues and questions, please open an GitHub issue.
