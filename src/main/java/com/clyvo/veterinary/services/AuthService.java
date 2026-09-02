package com.clyvo.veterinary.services;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.models.Role;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.models.User;
import com.clyvo.veterinary.models.Veterinarian;
import com.clyvo.veterinary.repositories.TutorRepository;
import com.clyvo.veterinary.repositories.UserRepository;
import com.clyvo.veterinary.repositories.VeterinarianRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, TutorRepository tutorRepository, VeterinarianRepository veterinarianRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tutorRepository = tutorRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user = userRepository.save(user);

        if (request.getRole() == Role.ROLE_TUTOR) {
            Tutor tutor = new Tutor();
            tutor.setUser(user);
            tutor.setName(request.getName());
            tutor.setAge(request.getAge());
            tutor.setPhone(request.getPhone());
            tutorRepository.save(tutor);
        } else if (request.getRole() == Role.ROLE_VETERINARIAN) {
            Veterinarian vet = new Veterinarian();
            vet.setUser(user);
            vet.setName(request.getName());
            vet.setAge(request.getAge());
            vet.setPhone(request.getPhone());
            vet.setCrmv(request.getCrmv());
            veterinarianRepository.save(vet);
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getRole().name());
    }
}
