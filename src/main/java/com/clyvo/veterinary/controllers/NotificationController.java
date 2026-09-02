package com.clyvo.veterinary.controllers;
import com.clyvo.veterinary.models.Notification;
import com.clyvo.veterinary.repositories.NotificationRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repo;
    public NotificationController(NotificationRepository repo) { this.repo = repo; }
    
    @GetMapping
    public List<Notification> getMyNotifications() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return repo.findByUserIdOrderBySentAtDesc(UUID.fromString(userIdStr));
    }
}
