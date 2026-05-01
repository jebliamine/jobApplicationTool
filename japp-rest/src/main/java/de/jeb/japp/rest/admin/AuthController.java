package de.jeb.japp.rest.admin;

import de.jeb.japp.model.user.dto.AuthResponse;
import de.jeb.japp.model.user.dto.LoginRequest;
import de.jeb.japp.model.user.dto.RegisterRequest;
import de.jeb.japp.security.service.AuthServiceInterface;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthServiceInterface authServiceInterface;


    public AuthController(AuthServiceInterface authServiceInterface) {
        this.authServiceInterface = authServiceInterface;
    }


    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest registerRequest) {
        return authServiceInterface.register(registerRequest);
    }


    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        return authServiceInterface.login(loginRequest);
    }

}
