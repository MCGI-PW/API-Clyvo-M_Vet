CREATE TABLE pets (
    id UUID PRIMARY KEY,
    tutor_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(50),
    breed VARCHAR(100),
    birth_date DATE,
    weight DECIMAL(5,2),
    color VARCHAR(100),
    profile_picture_url TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_pet_tutor FOREIGN KEY (tutor_id) REFERENCES tutors(id) ON DELETE CASCADE
);
