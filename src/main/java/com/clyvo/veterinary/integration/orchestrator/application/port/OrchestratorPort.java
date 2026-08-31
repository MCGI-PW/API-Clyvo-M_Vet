package com.clyvo.veterinary.integration.orchestrator.application.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface OrchestratorPort {
    void notifyAppointmentCreated(UUID appointmentId, UUID veterinarianId, LocalDateTime scheduledAt);
    void notifyPaymentRequired(UUID userId, String planName, BigDecimal amount);
}
