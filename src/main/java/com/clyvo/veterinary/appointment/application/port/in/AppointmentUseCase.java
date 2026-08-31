package com.clyvo.veterinary.appointment.application.port.in;

import com.clyvo.veterinary.appointment.application.dto.AppointmentResponse;
import com.clyvo.veterinary.appointment.application.dto.ScheduleAppointmentRequest;
import java.util.List;
import java.util.UUID;

public interface AppointmentUseCase {
    AppointmentResponse scheduleAppointment(UUID tutorId, ScheduleAppointmentRequest request);
    AppointmentResponse confirmAppointment(UUID id);
    AppointmentResponse cancelAppointment(UUID id);
    AppointmentResponse completeAppointment(UUID id, String finalNotes);
    AppointmentResponse getAppointment(UUID id);
    List<AppointmentResponse> listByVeterinarian(UUID vetId);
    List<AppointmentResponse> listByTutor(UUID tutorId);
    List<AppointmentResponse> listByPet(UUID petId);
    List<AppointmentResponse> listAll();
}
