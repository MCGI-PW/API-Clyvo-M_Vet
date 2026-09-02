package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    java.util.List<Appointment> findByTutorId(java.util.UUID tutorId); java.util.List<Appointment> findByVeterinarianId(java.util.UUID vetId);
}
