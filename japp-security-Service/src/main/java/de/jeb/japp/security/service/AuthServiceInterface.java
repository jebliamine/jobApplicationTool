package de.jeb.japp.security.service;

import de.jeb.japp.model.user.dto.AuthResponse;
import de.jeb.japp.model.user.dto.LoginRequest;
import de.jeb.japp.model.user.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthServiceInterface {

    public AuthResponse login(LoginRequest req);

    public AuthResponse register(RegisterRequest registerRequest);
}
