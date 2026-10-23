-- Dedicated tables for the current simplified backend; does not alter legacy auth.* project scripts.
CREATE TABLE security_users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE security_user_roles (
    user_id UUID NOT NULL REFERENCES security_users(id) ON DELETE CASCADE,
    role VARCHAR(40) NOT NULL CHECK (role IN ('OPERATOR','REVIEWER','RESPONSIBLE','AUDITOR','INTEGRATION','ADMIN')),
    PRIMARY KEY (user_id, role)
);
