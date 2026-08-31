package com.clyvo.veterinary.auth.application.port.in;

import com.clyvo.veterinary.auth.application.dto.AuthResponse;
import com.clyvo.veterinary.auth.application.dto.LoginRequest;

public interface LoginUseCase {
    AuthResponse login(LoginRequest request);
}
