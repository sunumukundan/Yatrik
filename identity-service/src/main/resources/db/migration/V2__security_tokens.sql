-- Identity Service Security Tokens Migration (PostgreSQL)
-- Adds JWT blacklist and additional security token tables for enhanced security

-- JWT Blacklist table - for revoking invalid or compromised JWT tokens
CREATE TABLE jwt_blacklist (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL UNIQUE,  -- SHA-256 hash of the JWT token
    user_id UUID NOT NULL,
    reason VARCHAR(100) NOT NULL,  -- 'logout', 'security', 'compromise', etc.
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,  -- Should match token's expiration
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Reference to app_user table
    CONSTRAINT fk_jwt_blacklist_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Password Reset Tokens table - for secure password reset functionality
CREATE TABLE password_reset_tokens (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,  -- SHA-256 hash of the reset token
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Reference to app_user table
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Session Management table - tracks active user sessions
CREATE TABLE user_sessions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,  -- Random session identifier
    device_info VARCHAR(500),  -- Browser, OS, device details
    ip_address VARCHAR(45),  -- IPv4 or IPv6
    user_agent VARCHAR(500),
    last_accessed TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Reference to app_user table
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

-- Index for better query performance on security tables
CREATE INDEX idx_jwt_blacklist_user_id ON jwt_blacklist(user_id);
CREATE INDEX idx_jwt_blacklist_expires ON jwt_blacklist(expires_at);
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_expires ON user_sessions(expires_at);

-- Cleanup of expired security tokens is handled by a scheduled job in the application layer
-- (see com.fleetapp.identity.scheduler.SecurityTokenCleanupScheduler)

-- Security metrics view
CREATE VIEW security_metrics AS
SELECT
    CURRENT_TIMESTAMP AS snapshot_time,
    (SELECT COUNT(*) FROM user_sessions WHERE is_active) AS active_sessions,
    (SELECT COUNT(*) FROM user_sessions WHERE is_active AND EXTRACT(HOUR FROM last_accessed) BETWEEN 0 AND 6) AS inactive_sessions,
    (SELECT COUNT(*) FROM password_reset_tokens WHERE NOT is_used AND expires_at > CURRENT_TIMESTAMP) AS pending_password_resets;