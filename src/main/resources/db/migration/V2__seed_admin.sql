-- =============================================================================
-- V2__seed_admin.sql
-- Seeds default administrator account
-- =============================================================================
INSERT INTO users (
    id,
    email,
    password_hash,
    first_name,
    last_name,
    role,
    is_active,
    token_version,
    failed_login_attempts,
    account_locked,
    created_at,
    updated_at,
    created_by,
    updated_by,
    version
) VALUES (
    '11111111-2222-3333-4444-555555555555',
    'admin@company.com',
    '$2a$10$/CnNLgVEB4gX3WrjCJPLmO4cca6eJfyDJ6ZDBRYLd3dWC0pee47kq', -- Admin@123
    'Admin',
    'User',
    'ADMIN',
    TRUE,
    1,
    0,
    FALSE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'SYSTEM',
    'SYSTEM',
    1
) ON CONFLICT (email) DO NOTHING;
