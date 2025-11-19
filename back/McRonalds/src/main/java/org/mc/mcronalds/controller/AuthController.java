package org.mc.mcronalds.controller;

import lombok.Data;
import org.mc.mcronalds.model.User;
import org.mc.mcronalds.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepo.findByUsername(request.getUsername()).get();

        return new AuthResponse(user.getUsername(), user.getRole().name());
    }
}

@Data
class LoginRequest {
    private String username;
    private String password;
}

@Data
class AuthResponse {
    private String username;
    private String role;

    public AuthResponse(String u, String r) {
        this.username = u;
        this.role = r;
    }
}
