package com.clyvo.veterinary.integration.orchestrator.infrastructure.http;

import com.clyvo.veterinary.integration.orchestrator.application.port.OrchestratorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class OrchestratorHttpAdapter implements OrchestratorPort {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorHttpAdapter.class);

    private final RestTemplate restTemplate;

    @Value("${orchestrator.base-url:http://localhost:5000}")
    private String baseUrl;

    @Value("${orchestrator.api-key:secret}")
    private String apiKey;

    public OrchestratorHttpAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void notifyAppointmentCreated(UUID appointmentId, UUID veterinarianId, LocalDateTime scheduledAt) {
        String url = baseUrl + "/api/appointments/notify";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", apiKey);

        Map<String, Object> body = Map.of(
                "appointmentId", appointmentId,
                "veterinarianId", veterinarianId,
                "scheduledAt", scheduledAt.toString()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, Void.class);
            log.info("Notified orchestrator of new appointment: {}", appointmentId);
        } catch (HttpClientErrorException e) {
            log.error("Failed to notify orchestrator of appointment {}: {}", appointmentId, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling orchestrator: {}", e.getMessage());
        }
    }

    @Override
    public void notifyPaymentRequired(UUID userId, String planName, BigDecimal amount) {
        String url = baseUrl + "/api/payments/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", apiKey);

        Map<String, Object> body = Map.of(
                "userId", userId,
                "planName", planName,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, Void.class);
            log.info("Requested payment from orchestrator for user: {}", userId);
        } catch (HttpClientErrorException e) {
            log.error("Failed to request payment for user {}: {}", userId, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling orchestrator for payment: {}", e.getMessage());
        }
    }
}

@Configuration
class OrchestratorConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
