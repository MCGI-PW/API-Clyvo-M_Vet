package com.clyvo.veterinary.dto;
import java.time.LocalDateTime;
import java.util.UUID;
public class ScheduleAppointmentRequest {
    private LocalDateTime appointmentDate;
    private UUID veterinarianId;
    private UUID petId;
    private String modality;
    public LocalDateTime getAppointmentDate() { return appointmentDate; } public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public UUID getVeterinarianId() { return veterinarianId; } public void setVeterinarianId(UUID veterinarianId) { this.veterinarianId = veterinarianId; }
    public UUID getPetId() { return petId; } public void setPetId(UUID petId) { this.petId = petId; }
    public String getModality() { return modality; } public void setModality(String modality) { this.modality = modality; }
}
