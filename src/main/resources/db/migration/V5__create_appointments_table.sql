CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    pet_id UUID,
    veterinarian_id UUID,
    tutor_id UUID,
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_appointment_pet FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_appointment_vet FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(id),
    CONSTRAINT fk_appointment_tutor FOREIGN KEY (tutor_id) REFERENCES tutors(id)
);
CREATE INDEX idx_appointments_vet_id ON appointments(veterinarian_id);
CREATE INDEX idx_appointments_tutor_id ON appointments(tutor_id);
CREATE INDEX idx_appointments_scheduled_at ON appointments(scheduled_at);
