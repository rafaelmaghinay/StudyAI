# Using Environment Variables in Spring Boot

Your Spring Boot application is now configured to automatically load environment variables from the `.env` file.

## How It Works

1. **DotEnvConfig.java** - Spring configuration class that:
   - Loads the `.env` file from your project root at startup
   - Converts each `.env` variable into a Java System property
   - Makes all variables available to `application.properties` and `@Value` annotations

2. **Loading Order** - Spring Boot loads properties in this order (highest to lowest priority):
   - System properties (set by DotEnvConfig from `.env` file)
   - Environment variables
   - application.properties
   - Default values in `@Value` annotations

## Using Environment Variables in Code

### In application.properties

Your `application.properties` already has placeholders for environment variables:

```properties
# JWT Configuration
app.jwtSecret=${JWT_SECRET:study-ai-secret-key-change-in-production-environment}
app.jwtExpirationMs=86400000

# CORS Configuration
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:3000,http://localhost:8080}

# Firebase Configuration
firebase.database-url=${FIREBASE_DATABASE_URL:}
```

The `${VARIABLE_NAME:DEFAULT_VALUE}` syntax means:
- If `VARIABLE_NAME` is set in `.env`, use that value
- Otherwise, use the `DEFAULT_VALUE`

### In Java Code with @Value Annotation

```java
@Component
public class MyService {

    @Value("${JWT_SECRET:default-secret}")
    private String jwtSecret;

    @Value("${FIREBASE_PROJECT_ID}")
    private String firebaseProjectId;
}
```

### In Java Code with Environment Utilities

```java
public class MyService {

    private String jwtSecret = System.getenv("JWT_SECRET");
    // or
    private String jwtSecret = System.getProperty("JWT_SECRET");
}
```

## Your Current .env Variables

All these variables from your `.env` file are now accessible in your application:

```
✓ JWT_SECRET
✓ FIREBASE_PROJECT_ID
✓ FIREBASE_SERVICE_ACCOUNT_PATH
✓ FIREBASE_DATABASE_URL
✓ CORS_ORIGINS
✓ Database credentials (DB_URL, DB_USERNAME, DB_PASSWORD)
✓ AWS credentials (AWS_ACCESS_KEY_ID, etc.)
✓ OpenAI API keys
```

## Example: Accessing Firebase Project ID

Before (manual):
```java
String projectId = System.getenv("FIREBASE_PROJECT_ID"); // ❌ Won't work
```

Now (with DotEnvConfig):
```java
@Value("${FIREBASE_PROJECT_ID}")
private String projectId; // ✅ Works!

// Or in application.properties
firebase.project-id=${FIREBASE_PROJECT_ID}
```

## Running the Application

```bash
# No special configuration needed
# Just run normally
mvn spring-boot:run

# Or using IDE
# The .env file will be automatically loaded
```

## Troubleshooting

### Variables not loading?

1. **Check .env file exists** - Should be in project root:
   ```bash
   ls -la .env
   ```

2. **Check logs** - Look for:
   ```
   INFO: Successfully loaded X environment variables from .env file
   ```

3. **Verify variable names match**:
   - `.env`: `JWT_SECRET=value`
   - `application.properties`: `app.jwtSecret=${JWT_SECRET}`

4. **Restart IDE/Maven** - Sometimes IDEs cache properties

### Using in Production

For production, you have two options:

**Option 1: Use system environment variables directly**
- Set variables directly on your server (don't commit .env)
- Spring Boot will read them automatically

**Option 2: Use application-prod.properties**
```bash
# Create application-prod.properties with production values
mvn spring-boot:run -Dspring.profiles.active=prod
```

## Security Notes

✅ `.env` is in `.gitignore` - won't be committed
✅ Never commit sensitive credentials to Git
✅ Use environment variables for all sensitive data
✅ In production, use proper secrets management (AWS Secrets Manager, etc.)

## Next Step

Your application will now:
1. Load `.env` file on startup
2. Make all variables available to Spring Boot
3. Allow you to use `${VARIABLE_NAME}` in `application.properties`
4. Access variables via `@Value("${VARIABLE_NAME}")` in Java code

No additional configuration needed! Start your application and it should work.
