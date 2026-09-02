package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    java.util.List<Notification> findByUserIdOrderBySentAtDesc(java.util.UUID userId);
}
