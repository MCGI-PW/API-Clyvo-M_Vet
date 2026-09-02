package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    java.util.Optional<User> findByEmail(String email);
}
