CREATE TABLE IF NOT EXISTS hospitals (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    email VARCHAR(255),
    rating DOUBLE PRECISION,
    total_reviews INTEGER,
    is_open24x7 BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hospital_images (
    hospital_id VARCHAR(255) NOT NULL,
    image_url VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS hospital_services (
    hospital_id VARCHAR(255) NOT NULL,
    service VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS hospital_specializations (
    hospital_id VARCHAR(255) NOT NULL,
    specialization VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    uhid VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INTEGER NOT NULL,
    phone VARCHAR(255) NOT NULL UNIQUE,
    gender VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS auth_otp_verification (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(255),
    uhid VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE,
    otp VARCHAR(255),
    expiry_time TIMESTAMP,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_sent_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT,
    name VARCHAR(255),
    specialization VARCHAR(255),
    experience INTEGER NOT NULL DEFAULT 0,
    fee INTEGER NOT NULL DEFAULT 0,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    hospital_id VARCHAR(255)
);

ALTER TABLE doctors ADD COLUMN IF NOT EXISTS profile_id BIGINT;
UPDATE doctors SET profile_id = id WHERE profile_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_doctors_profile_id
    ON doctors(profile_id) WHERE profile_id IS NOT NULL;

ALTER TABLE auth_otp_verification
    ADD COLUMN IF NOT EXISTS attempt_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE auth_otp_verification
    ADD COLUMN IF NOT EXISTS last_sent_at TIMESTAMP;
