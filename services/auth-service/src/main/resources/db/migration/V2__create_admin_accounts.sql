CREATE TABLE IF NOT EXISTS admin_accounts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    hospital_id VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_admin_accounts_role CHECK (role IN ('ADMIN', 'HOSPITAL_ADMIN')),
    CONSTRAINT ck_hospital_admin_scope CHECK (
        (role = 'ADMIN' AND hospital_id IS NULL)
        OR (role = 'HOSPITAL_ADMIN' AND hospital_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_admin_accounts_hospital_id
    ON admin_accounts(hospital_id);
