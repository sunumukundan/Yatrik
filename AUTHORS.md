# PROJECT STRUCTURE AND BUILDING

## Project Structure

This project follows a multi-module architecture with the following modules:

### fleet-platform
- **Root pom.xml** - Main project configuration and parent pom
- **Purpose**: Coordinates all sub-modules and project-wide configuration

### common-lib
- **Shared utilities and common components**
- **Purpose**: Provides reusable code across all services

### identity-service
- **Authentication and user management**
- **Core services**: AuthService, JwtService
- **Web layer**: AuthController, DTOs (LoginRequestDto, SignupRequestDto, etc.)
- **Security**: Token management, OTP services

### gateway
- **API Gateway**
- **Purpose**: Routes and proxies requests between services

### fleet-service, trip-service, ledger-service, reporting-service
- **Business domain services**

## Build Process

### Prerequisites
- Java 21+
- Maven 3.9+
- Git

### Building the Project

1. **Full build** - Build all modules:
```bash
mvn clean install
```

2. **Individual module builds**:
```bash
# Build just the identity-service module
mvn clean compile -pl identity-service -am

# Package just the identity-service module
mvn clean install -pl identity-service -am
```

## Key Fixes Applied

### AuthService.java
- Fixed `generateAccessToken()` calls to match method signature:
  - **Before**: `jwtService.generateAccessToken(userId, email, orgId, role)`
  - **After**: `jwtService.generateAccessToken(userId, orgId, role.name())`

### DTO Accessor Methods
- **LoginRequestDto.java**: Added `email()` and `password()` methods to maintain API compatibility with AuthController
- **SignupRequestDto.java**: Added `email()`, `password()`, and `phone()` methods for consistent accessor method names

### AuthController.java
- Maintained existing API by using existing accessor methods (`dto.email()`, `dto.password()`, etc.)

## Authentication Flow

1. **Email/Password Login**
   - POST `/auth/login` with LoginRequestDto (email, password)
   - Returns access token and refresh token

2. **Email/Password Signup**
   - POST `/auth/signup` with SignupRequestDto (email, password, phone)
   - Creates new user with OWNER role and generates organisation

3. **OTP Authentication**
   - GET `/auth/otp/request` with OtpRequestDto (email)
   - POST `/auth/otp/verify` with OtpVerifyDto (email, otp, optional name/organisation)

4. **Token Refresh**
   - POST `/auth/refresh` with RefreshTokenRequestDto (refreshToken)

5. **Logout**
   - POST `/auth/logout` with RefreshTokenRequestDto (refreshToken)

## Notes

- All authentication methods follow the same token structure (access token + refresh token)
- JWT access tokens expire in 30 minutes by default
- Refresh tokens rotate and expire after 30 days
- Email/password authentication added as an alternative to OTP authentication

## Build Process

### Prerequisites
- Java 21+
- Maven 3.9+
- Git

### Building the Project

1. **Full build** - Build all modules:
```bash
mvn clean install
```

2. **Individual module builds**:
```bash
# Build just the identity-service module
mvn clean compile -pl identity-service -am

# Package just the identity-service module
mvn clean install -pl identity-service -am
```

## Key Fixes Applied

### AuthService.java
- Fixed `generateAccessToken()` calls to match method signature:
  - **Before**: `jwtService.generateAccessToken(userId, email, orgId, role)`
  - **After**: `jwtService.generateAccessToken(userId, orgId, role.name())`

### DTO Accessor Methods
- **LoginRequestDto.java**: Added `email()` and `password()` methods to maintain API compatibility with AuthController
- **SignupRequestDto.java**: Added `email()`, `password()`, and `phone()` methods for consistent accessor method names

### AuthController.java
- Maintained existing API by using existing accessor methods (`dto.email()`, `dto.password()`, etc.)

## Authentication Flow

1. **Email/Password Login**
   - POST `/auth/login` with LoginRequestDto (email, password)
   - Returns access token and refresh token

2. **Email/Password Signup**
   - POST `/auth/signup` with SignupRequestDto (email, password, phone)
   - Creates new user with OWNER role and generates organisation

3. **OTP Authentication**
   - GET `/auth/otp/request` with OtpRequestDto (email)
   - POST `/auth/otp/verify` with OtpVerifyDto (email, otp, optional name/organisation)

4. **Token Refresh**
   - POST `/auth/refresh` with RefreshTokenRequestDto (refreshToken)

5. **Logout**
   - POST `/auth/logout` with RefreshTokenRequestDto (refreshToken)

## Notes

- All authentication methods follow the same token structure (access token + refresh token)
- JWT access tokens expire in 30 minutes by default
- Refresh tokens rotate and expire after 30 days
- Email/password authentication added as an alternative to OTP authentication