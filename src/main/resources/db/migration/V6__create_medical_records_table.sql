CREATE TABLE medical_records (
    id UUID PRIMARY KEY,
    appointment_id UUID UNIQUE,
    pet_id UUID,
    veterinarian_id UUID,
    symptoms TEXT NOT NULL,
    diagnosis TEXT NOT NULL,
    treatment TEXT NOT NULL,
    observations TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_mr_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    CONSTRAINT fk_mr_pet FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_mr_vet FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(id)
);
