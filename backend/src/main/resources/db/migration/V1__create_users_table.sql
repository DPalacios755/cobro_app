CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,

                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_users_email UNIQUE (email)
);