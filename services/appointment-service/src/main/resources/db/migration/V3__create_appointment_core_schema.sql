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

CREATE TABLE IF NOT EXISTS doctors (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    specialization VARCHAR(255),
    experience INTEGER NOT NULL DEFAULT 0,
    fee INTEGER NOT NULL DEFAULT 0,
    email VARCHAR(255) NOT NULL UNIQUE,
    hospital_id VARCHAR(255)
);
ALTER TABLE doctors DROP COLUMN IF EXISTS password;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT,
    hospital_id VARCHAR(255),
    doctor_id BIGINT,
    appointment_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    CONSTRAINT uk_appointments_doctor_time UNIQUE (doctor_id, appointment_time)
);

CREATE TABLE IF NOT EXISTS qr_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE,
    appointment_id BIGINT,
    patient_id BIGINT,
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    used BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_qr_tokens_appointment ON qr_tokens(appointment_id);
CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments(patient_id);
