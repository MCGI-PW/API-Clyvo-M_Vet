package com.clyvo.veterinary.appointment.domain.service;

import com.clyvo.veterinary.appointment.domain.model.Appointment;
import com.clyvo.veterinary.appointment.domain.model.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class AppointmentDomainService {

    public boolean hasConflict(List<Appointment> existingAppointments, LocalDateTime proposedDateTime) {
        LocalDateTime windowStart = proposedDateTime.minusMinutes(59);
        LocalDateTime windowEnd = proposedDateTime.plusMinutes(59);

        return existingAppointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .anyMatch(a -> a.getScheduledAt().isAfter(windowStart) && a.getScheduledAt().isBefore(windowEnd));
    }

    public void validateSchedulingRules(LocalDateTime scheduledAt) {
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment must be scheduled in the future.");
        }

        DayOfWeek day = scheduledAt.getDayOfWeek();
        LocalTime time = scheduledAt.toLocalTime();

        if (day == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Cannot schedule on Sunday.");
        }

        if (day == DayOfWeek.SATURDAY) {
            if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(12, 0))) {
                throw new IllegalArgumentException("Saturday business hours are 08:00 to 13:00.");
            }
        } else {
            if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 0))) {
                throw new IllegalArgumentException("Weekday business hours are 08:00 to 18:00.");
            }
        }
    }
}
