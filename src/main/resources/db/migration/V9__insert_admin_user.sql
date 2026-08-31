INSERT INTO users (id, name, email, password_hash, role, active) 
VALUES (gen_random_uuid(), 'Administrador', 'admin@clyvovet.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LpMh6eNR6O2', 'ROLE_ADMIN', true);
