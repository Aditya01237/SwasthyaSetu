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
    id BIGINT PRIMARY KEY,
    patient_id BIGINT,
    hospital_id VARCHAR(255),
    doctor_id BIGINT,
    appointment_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    CONSTRAINT uk_appointments_doctor_time UNIQUE (doctor_id, appointment_time)
);

CREATE TABLE IF NOT EXISTS medical_records (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT,
    appointment_id BIGINT UNIQUE,
    diagnosis VARCHAR(255),
    record_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medicines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    dosage VARCHAR(255),
    frequency VARCHAR(255),
    medical_record_id BIGINT
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT,
    patient_id BIGINT,
    appointment_id BIGINT,
    action VARCHAR(255),
    timestamp TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_medical_records_patient ON medical_records(patient_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_patient ON audit_logs(patient_id);
