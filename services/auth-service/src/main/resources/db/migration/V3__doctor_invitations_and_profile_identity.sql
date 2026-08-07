CREATE TABLE IF NOT EXISTS doctor_invitations (
    doctor_id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    hospital_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    specialization VARCHAR(255),
    experience INTEGER NOT NULL DEFAULT 0,
    fee INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    CONSTRAINT ck_doctor_invitation_status CHECK (status IN ('PENDING', 'ACCEPTED'))
);

CREATE INDEX IF NOT EXISTS idx_doctor_invitations_hospital
    ON doctor_invitations(hospital_id);
