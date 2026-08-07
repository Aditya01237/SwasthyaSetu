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

CREATE TABLE IF NOT EXISTS hospital_images (hospital_id VARCHAR(255) NOT NULL, image_url VARCHAR(255));
CREATE TABLE IF NOT EXISTS hospital_services (hospital_id VARCHAR(255) NOT NULL, service VARCHAR(255));
CREATE TABLE IF NOT EXISTS hospital_specializations (hospital_id VARCHAR(255) NOT NULL, specialization VARCHAR(255));

CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    specialization VARCHAR(255),
    experience INTEGER NOT NULL DEFAULT 0,
    fee INTEGER NOT NULL DEFAULT 0,
    email VARCHAR(255) NOT NULL UNIQUE,
    hospital_id VARCHAR(255)
);
ALTER TABLE doctors DROP COLUMN IF EXISTS password;

CREATE INDEX IF NOT EXISTS idx_doctors_hospital_id ON doctors(hospital_id);
