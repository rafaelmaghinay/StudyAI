# Firebase Authentication Setup Guide

## Overview
The StudyAI backend now uses Firebase for user authentication. This guide walks you through setting up Firebase for local development and production.

## Prerequisites
- A Firebase project (create one at https://console.firebase.google.com)
- Firebase Admin SDK credentials

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add Project"
3. Enter project name: `StudyAI` (or your preferred name)
4. Follow the setup wizard to create the project

## Step 2: Enable Authentication

1. In Firebase Console, go to **Authentication** → **Get Started**
2. Enable **Email/Password** authentication method
3. (Optional) Enable **Google Sign-In** for easier login

## Step 3: Download Service Account Key

1. Go to **Project Settings** → **Service Accounts** tab
2. Click **Generate New Private Key**
3. A JSON file will download - this is your Firebase service account key
4. **Keep this file secure - do NOT commit to Git**

5. Place the downloaded JSON file here:
   ```
   /studyai/src/main/resources/firebase-service-account-key.json
   ```

## Step 4: Configure Environment Variables

Update `/studyai/.env` with your Firebase details:

```properties
# Firebase Configuration
FIREBASE_PROJECT_ID=your-project-id-here
FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/firebase-service-account-key.json
FIREBASE_DATABASE_URL=https://your-project-id.firebaseio.com

# JWT Configuration
JWT_SECRET=your-secure-random-secret-at-least-32-characters-long

# CORS Configuration (update for your frontend URL)
CORS_ORIGINS=http://localhost:3000,http://localhost:8080
```

### Getting Your Project ID
- Look in the Firebase Console → Project Settings
- It's listed as "Project ID"

### Generating a Safe JWT Secret
```bash
# Generate a random 64-character secret (run in terminal)
openssl rand -base64 32
```

## Step 5: Add Service Account Key to .gitignore

The Firebase service account key is already in `.gitignore`, but verify:

```
# .gitignore
/src/main/resources/firebase-service-account-key.json
```

## Step 6: Update Spring Boot Application

No additional code changes needed! The Firebase configuration is already set up:

- `FirebaseConfig.java` - Initializes Firebase Admin SDK
- `JwtAuthenticationFilter.java` - Validates JWT tokens
- `AuthService.java` - Handles authentication logic

## Testing Authentication

### 1. Start Spring Boot
```bash
cd studyai
mvn spring-boot:run
```

### 2. Get a Firebase ID Token
You'll need a Firebase ID token from your frontend. For testing without frontend:

Use Firebase Console → Authentication → Users → Add test user

Or use [Firebase REST API](https://firebase.google.com/docs/reference/rest/auth) to get a test token

### 3. Login Endpoint
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "firebaseToken": "<your-firebase-id-token-here>"
  }'
```

Expected response:
```json
{
  "jwtToken": "eyJhbGc...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "displayName": "User Name",
  "expiresIn": 86400000,
  "tokenType": "Bearer"
}
```

### 4. Using JWT Token for API Calls
```bash
curl http://localhost:8080/api/subjects/user/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGc..."
```

## Authentication Flow

```
React Frontend
    ↓ (User enters credentials)
Firebase Auth SDK (client-side)
    ↓ (Authenticates user, returns ID token)
POST /api/auth/login with Firebase ID token
    ↓
Spring Boot validates Firebase ID token
    ↓ (Creates/retrieves user from database)
    ↓ (Generates JWT token)
Returns JWT token (24 hours expiration)
    ↓
Frontend stores JWT in localStorage/sessionStorage
    ↓
All subsequent API calls include JWT in Authorization header
    ↓
JwtAuthenticationFilter validates JWT
    ↓ (Extracts userId, sets Spring Security context)
Controllers access user via @CurrentUser annotation
```

## Production Deployment

### Before Deploying:

1. **Generate New JWT Secret**
   ```bash
   openssl rand -base64 32
   ```

2. **Update Environment Variables** (on your production server)
   - Never commit service account key to git
   - Use secure environment variable management (e.g., AWS Secrets Manager, Heroku Config Vars)

3. **Enable HTTPS**
   - JWT tokens should only transmit over HTTPS
   - Update `CORS_ORIGINS` to use https://your-domain.com

4. **Configure Firebase**
   - Go to Firebase Console → Authentication → Settings
   - Add your production domain to authorized domains
   - Update CORS settings

5. **Generate Production Service Account Key**
   - Create a separate service account for production
   - Do NOT reuse development credentials

### Environment Variables for Production

```bash
# .env or system environment variables
JWT_SECRET=<use-generated-secret>
FIREBASE_PROJECT_ID=your-prod-project-id
FIREBASE_SERVICE_ACCOUNT_PATH=/secure/path/to/service-account.json
CORS_ORIGINS=https://yourdomain.com,https://api.yourdomain.com
```

## Troubleshooting

### Issue: "Cannot read Firebase service account key"
- Verify file path is correct
- Check file permissions: `chmod 644 firebase-service-account-key.json`
- Ensure the JSON file is valid

### Issue: "Invalid Firebase token"
- Verify token is a valid Firebase ID token (not a refresh token)
- Check token hasn't expired
- Ensure Firebase project ID matches

### Issue: "JWT token expired"
- Current expiration: 24 hours
- Frontend should refresh JWT or re-authenticate
- Can add refresh endpoint if needed

### Issue: CORS errors in frontend
- Update `CORS_ORIGINS` environment variable
- Make sure frontend URL is included
- Restart Spring Boot after changing .env

## Next Steps

1. **Frontend Integration**: Install Firebase SDK in React
   ```bash
   npm install firebase
   ```

2. **Create Login Component**: Use Firebase to authenticate and store JWT

3. **Add to API Calls**: Include JWT in Authorization header for all API requests

4. **Test End-to-End**: Verify full authentication flow works

## Security Checklist

- [ ] Service account key is in .gitignore
- [ ] JWT_SECRET is unique and long (minimum 32 characters)
- [ ] CORS_ORIGINS restricted to specific domains
- [ ] HTTPS enabled in production
- [ ] Firebase Console has appropriate authentication settings
- [ ] Database rules restrict user data access
- [ ] Never log or expose Firebase tokens

## Resources

- [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [OWASP Authentication Guidelines](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

## Support

If you encounter issues:
1. Check Spring Boot logs: `tail -f /path/to/logs/spring.log`
2. Enable debug logging by setting `logging.level.com.example.studyai=DEBUG`
3. Verify Firebase credentials are correct
4. Check that all environment variables are properly set
