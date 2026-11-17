# Yatrik Authentication Setup Guide

## Issues Fixed

### 1. Gateway Routing Bug (Critical)
**Problem**: The API Gateway was routing auth requests to `http://localhost:8095`, but the Identity Service runs on port `8081`.

**Fixed**: Updated `gateway/src/main/resources/application.yml` to route to `http://localhost:8081`.

### 2. Default OTP Providers Only Log to Console
**Problem**: Both SMS and Email providers defaulted to `"log"` which only prints OTPs to the console instead of delivering them to users.

**Fixed**: 
- Added clear documentation in `application.yml` and `application-docker.yml`
- Created `.env.example` with all required configuration variables
- Providers now clearly show how to enable production delivery

### 3. Phone Number Normalization (Twilio)
**Problem**: The Twilio SMS sender assumed all numbers were Indian (+91), breaking international numbers.

**Fixed**: Enhanced `TwilioSmsSender.e164()` method to:
- Accept numbers with existing country codes (e.g., +1, +44, +91)
- Detect common country codes automatically
- Default to +91 only for numbers without country codes

### 4. Email Validation
**Problem**: Basic email check (`contains("@") && contains(".")`) was too permissive.

**Fixed**: Improved `OtpService.isEmail()` with proper validation:
- `@` must be present and not at start
- `.` must come after `@` 
- `.` must not be the last character

---

## Quick Start (Development)

For local development, the default "log" providers work out of the box:

1. Start the services:
   ```bash
   # Terminal 1 - Identity Service
   cd identity-service && mvn spring-boot:run
   
   # Terminal 2 - Gateway
   cd gateway && mvn spring-boot:run
   ```

2. Request OTP (check console for the code):
   ```bash
   # Phone OTP
   curl -X POST http://localhost:8080/auth/otp/request \
     -H "Content-Type: application/json" \
     -d '{"phone": "+919876543210"}'
   
   # Email OTP
   curl -X POST http://localhost:8080/auth/otp/request \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com"}'
   ```

3. Verify OTP (use code from console):
   ```bash
   curl -X POST http://localhost:8080/auth/otp/verify \
     -H "Content-Type: application/json" \
     -d '{"phone": "+919876543210", "otp": "123456", "name": "John Doe"}'
   ```

4. Email/Password Login:
   ```bash
   # First signup
   curl -X POST http://localhost:8080/auth/signup \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "securePass123", "phone": "+919876543210"}'
   
   # Then login
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "securePass123"}'
   ```

---

## Production Configuration

### SMS (Twilio)

1. Create a Twilio account at https://www.twilio.com
2. Get your Account SID, Auth Token, and a verified phone number
3. Set environment variables:
   ```bash
   export SMS_PROVIDER=twilio
   export TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   export TWILIO_AUTH_TOKEN=your_auth_token
   export TWILIO_FROM_NUMBER=+15551234567
   ```

### Email (SMTP)

1. Configure SMTP credentials (Gmail example):
   ```bash
   export EMAIL_PROVIDER=smtp
   export SMTP_HOST=smtp.gmail.com
   export SMTP_PORT=587
   export SMTP_USERNAME=your-email@gmail.com
   export SMTP_PASSWORD=your-app-password  # Use App Password for Gmail
   export SMTP_FROM=noreply@yourdomain.com
   ```

2. For other providers, adjust host/port accordingly:
   - Outlook: smtp.office365.com:587
   - SendGrid: smtp.sendgrid.net:587
   - Amazon SES: email-smtp.us-east-1.amazonaws.com:587

### JWT Secret

**Important**: Change the default JWT secret in production:
```bash
export JWT_SECRET=$(openssl rand -base64 32)
```

---

## Docker Deployment

```bash
# Copy and configure environment
cp identity-service/.env.example identity-service/.env
# Edit .env with your values

# Start with Docker Compose
cd identity-service
docker-compose up -d

# For full stack (requires all service docker-compose files)
# See infra/docker/ for database setup
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/otp/request` | Request OTP via phone or email |
| POST | `/auth/otp/verify` | Verify OTP and get tokens |
| POST | `/auth/login` | Email/password login |
| POST | `/auth/signup` | Register new user |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Revoke refresh token |

---

## Request/Response Examples

### Request OTP
```json
// Phone
{"phone": "+919876543210"}

// Email
{"email": "user@example.com"}
```

### Verify OTP
```json
{
  "phone": "+919876543210",
  "otp": "123456",
  "name": "John Doe",
  "organisationName": "John's Fleet"
}
```

### Login
```json
{"email": "user@example.com", "password": "securePass123"}
```

### Signup
```json
{"email": "user@example.com", "password": "securePass123", "phone": "+919876543210"}
```

### Response (AuthResponse)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
  "expiresIn": 1800,
  "userId": "uuid",
  "organisationId": "uuid",
  "role": "OWNER"
}
```

---

## Troubleshooting

### "Connection refused" on auth endpoints
- Verify Identity Service is running on port 8081
- Check Gateway routes point to `http://localhost:8081`

### OTP not received
- Check console logs if using "log" provider
- Verify Twilio/SMTP credentials if using production providers
- Check Twilio console for delivery status
- Verify phone number format (E.164: +[country][number])

### Email/Password login fails
- Ensure user exists (check signup was successful)
- Verify password is correct
- Check user status is ACTIVE

### CORS errors
- Gateway CORS allows: localhost:5173, 5174, 5175, 3000
- Add your frontend origin to `SecurityConfig.corsConfigurationSource()`

---

## Security Notes

- OTPs expire in 5 minutes
- Max 5 verification attempts per OTP
- Refresh tokens valid for 30 days with rotation
- Access tokens valid for 30 minutes (configurable)
- Rate limiting should be added at Gateway level for production