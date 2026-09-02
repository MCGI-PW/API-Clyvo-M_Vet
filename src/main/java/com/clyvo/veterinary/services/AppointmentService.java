package com.clyvo.veterinary.services;

import com.clyvo.veterinary.dto.CompleteAppointmentRequest;
import com.clyvo.veterinary.dto.ScheduleAppointmentRequest;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;
    private final NotificationRepository notificationRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, VeterinarianRepository veterinarianRepository, TutorRepository tutorRepository, PetRepository petRepository, NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.tutorRepository = tutorRepository;
        this.petRepository = petRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Appointment scheduleAppointment(UUID tutorUserId, ScheduleAppointmentRequest request) {
        Tutor tutor = tutorRepository.findByUserId(tutorUserId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        Veterinarian vet = veterinarianRepository.findById(request.getVeterinarianId())
                .orElseThrow(() -> new RuntimeException("Veterinarian not found"));

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        if (!pet.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Pet does not belong to the tutor");
        }

        Appointment app = new Appointment();
        app.setAppointmentDate(request.getAppointmentDate());
        app.setModality(request.getModality());
        app.setStatus("SCHEDULED");
        app.setTutor(tutor);
        app.setVeterinarian(vet);
        app.setPet(pet);
        
        Appointment saved = appointmentRepository.save(app);

        // Dispara notificacao
        Notification notif = new Notification();
        notif.setUser(tutor.getUser());
        notif.setSentAt(LocalDateTime.now());
        notif.setMessage("Sua consulta para o pet " + pet.getName() + " foi agendada com " + vet.getName() + " para " + request.getAppointmentDate());
        notificationRepository.save(notif);

        return saved;
    }

    @Transactional
    public Appointment completeAppointment(UUID vetUserId, UUID appointmentId, CompleteAppointmentRequest request) {
        Veterinarian vet = veterinarianRepository.findByUserId(vetUserId)
                .orElseThrow(() -> new RuntimeException("Veterinarian not found"));

        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!app.getVeterinarian().getId().equals(vet.getId())) {
            throw new RuntimeException("You cannot complete another veterinarian's appointment");
        }

        app.setStatus("COMPLETED");
        app.setClinicalNotes(request.getClinicalNotes());
        
        Appointment saved = appointmentRepository.save(app);

        // Dispara notificacao
        Notification notif = new Notification();
        notif.setUser(app.getTutor().getUser());
        notif.setSentAt(LocalDateTime.now());
        notif.setMessage("A consulta de " + app.getPet().getName() + " foi concluida. Nota clinica: " + request.getClinicalNotes());
        notificationRepository.save(notif);

        return saved;
    }
}
