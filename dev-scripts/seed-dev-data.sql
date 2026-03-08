-- Seed data: demo user for development
-- Credentials: dev@example.com / password
-- Idempotent: skips if user already exists
INSERT INTO users (id, email, password_hash, name, role_id, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'dev@example.com',
    '$2a$10$lcvMfX4RrXJbZm1KfLFzauC0vxuGl7NTIf6pIUNTZ9eo.zocL3Kvi',
    'Dev User',
    1,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;
