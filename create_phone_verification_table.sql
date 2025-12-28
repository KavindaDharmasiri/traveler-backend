-- Create phone_verification table
CREATE TABLE phone_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    user_tenant VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    pin VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP NULL,
    INDEX idx_user_tenant_phone (user_tenant, phone_number),
    INDEX idx_expires_at (expires_at)
);