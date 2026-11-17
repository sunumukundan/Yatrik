-- Identity Service Schema Migration (H2 compatible)
-- Owns: app_user, organisation, subscription, otp_request, refresh_token, subscription_plan
-- Note: The OTP delivery is handled by identity-service, but the actual OTP table
-- is named 'otp_request' to match the domain entity

-- H2 has built-in UUID support with RANDOM_UUID()
-- No CREATE EXTENSION needed for H2

-- App User table - stores app users with their authentication details
-- Matches @Table(name = "app_user") in AppUser entity
CREATE TABLE app_user (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(150) NOT NULL,
    name VARCHAR(150) NOT NULL,
    organisation_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    language VARCHAR(5) NOT NULL DEFAULT 'en',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0,

    -- Ensure unique constraint for phone within an organisation
    CONSTRAINT uq_app_user_phone_org UNIQUE (organisation_id, phone)
);

-- Organisation table - tenant root entity
-- Matches @Table(name = "organisation") in Organisation entity
CREATE TABLE organisation (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    gst_no VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL',
    plan VARCHAR(20) NOT NULL DEFAULT 'FREE',
    default_language VARCHAR(5) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0
);

-- OTP Request table - for phone/email-based authentication
-- Matches @Table(name = "otp_request") in OtpRequest entity
CREATE TABLE otp_request (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    identifier_type VARCHAR(20) NOT NULL, -- 'phone' or 'email'
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    consumed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refresh Token table - for JWT token rotation
-- Matches @Table(name = "refresh_token") in RefreshToken entity
CREATE TABLE refresh_token (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(100),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    replaced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Subscription table - tracks organisation subscriptions and vehicle limits
-- Matches @Table(name = "subscription") in Subscription entity
CREATE TABLE subscription (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    organisation_id UUID NOT NULL,
    plan VARCHAR(20) NOT NULL,
    vehicle_limit INTEGER NOT NULL,
    valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_till DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_subscription_org FOREIGN KEY (organisation_id) REFERENCES organisation(id)
);

-- Index for better query performance
CREATE INDEX idx_app_user_organisation_id ON app_user(organisation_id);
CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_app_user_phone ON app_user(phone);
CREATE INDEX idx_organisation_id ON organisation(id);
CREATE INDEX idx_otp_request_identifier ON otp_request(identifier);
CREATE INDEX idx_otp_request_expires ON otp_request(expires_at);
CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_hash ON refresh_token(token_hash);
CREATE INDEX idx_subscription_org_id ON subscription(organisation_id);

-- updated_at is handled in the application layer (JPA @PreUpdate)
-- Function to clean up expired security tokens is handled by a scheduled job in the application layer
-- (see com.fleetapp.identity.scheduler.SecurityTokenCleanupScheduler)