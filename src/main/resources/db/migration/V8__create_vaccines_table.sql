CREATE TABLE vaccines (
    id UUID PRIMARY KEY,
    pet_id UUID NOT NULL,
    veterinarian_id UUID NOT NULL,
    vaccine_name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255),
    batch_number VARCHAR(100),
    applied_at DATE NOT NULL,
    next_dose_at DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_vac_pet FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_vac_vet FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(id)
);
