CREATE TABLE veterinarians (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    crm VARCHAR(20) UNIQUE NOT NULL,
    specialty VARCHAR(100),
    bio TEXT,
    phone VARCHAR(20),
    profile_picture_url TEXT,
    subscription_plan VARCHAR(50) DEFAULT 'FREE',
    subscription_status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_vet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
