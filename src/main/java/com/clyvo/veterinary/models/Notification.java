package com.clyvo.veterinary.models;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private String message;
    @Column(nullable = false) private LocalDateTime sentAt;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) private User user;
    
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public String getMessage() { return message; } public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSentAt() { return sentAt; } public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public User getUser() { return user; } public void setUser(User user) { this.user = user; }
}
