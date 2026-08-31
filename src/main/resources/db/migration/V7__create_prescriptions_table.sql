CREATE TABLE prescriptions (
    id UUID PRIMARY KEY,
    medical_record_id UUID,
    pet_id UUID,
    veterinarian_id UUID,
    general_instructions TEXT,
    valid_until DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_rx_mr FOREIGN KEY (medical_record_id) REFERENCES medical_records(id),
    CONSTRAINT fk_rx_pet FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_rx_vet FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(id)
);

CREATE TABLE prescription_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prescription_id UUID NOT NULL,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),
    instructions TEXT,
    CONSTRAINT fk_rx_items_rx FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);
