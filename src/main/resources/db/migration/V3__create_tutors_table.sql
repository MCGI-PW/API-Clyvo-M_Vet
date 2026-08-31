CREATE TABLE tutors (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    document VARCHAR(14) UNIQUE,
    profile_picture_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_tutor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
