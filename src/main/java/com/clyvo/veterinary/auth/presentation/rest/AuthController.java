package com.clyvo.veterinary.auth.presentation.rest;

import com.clyvo.veterinary.auth.application.dto.AuthResponse;
import com.clyvo.veterinary.auth.application.dto.LoginRequest;
import com.clyvo.veterinary.auth.application.dto.RegisterRequest;
import com.clyvo.veterinary.auth.application.port.in.LoginUseCase;
import com.clyvo.veterinary.auth.application.port.in.RegisterUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = loginUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}
