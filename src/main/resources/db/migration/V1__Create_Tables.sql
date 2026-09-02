CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE tutors (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    phone VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE veterinarians (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    crmv VARCHAR(50) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE pets (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    breed VARCHAR(255) NOT NULL,
    tutor_id UUID NOT NULL,
    FOREIGN KEY (tutor_id) REFERENCES tutors(id)
);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    appointment_date TIMESTAMP NOT NULL,
    modality VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    clinical_notes TEXT,
    veterinarian_id UUID NOT NULL,
    tutor_id UUID NOT NULL,
    pet_id UUID NOT NULL,
    FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(id),
    FOREIGN KEY (tutor_id) REFERENCES tutors(id),
    FOREIGN KEY (pet_id) REFERENCES pets(id)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
