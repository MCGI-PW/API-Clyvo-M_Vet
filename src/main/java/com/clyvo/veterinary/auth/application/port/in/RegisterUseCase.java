package com.clyvo.veterinary.auth.application.port.in;

import com.clyvo.veterinary.auth.application.dto.RegisterRequest;
import com.clyvo.veterinary.user.domain.model.User;

public interface RegisterUseCase {
    User register(RegisterRequest request);
}
