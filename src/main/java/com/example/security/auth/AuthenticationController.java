package com.example.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }
    /**
     * 🔥 NOUVEAU: Endpoint pour récupérer le token actuel de l'utilisateur connecté
     */
    @GetMapping("/current-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> getCurrentToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Map<String, String> response = new HashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            response.put("token", authHeader.substring(7));
            return ResponseEntity.ok(response);
        }

        response.put("error", "No token found");
        return ResponseEntity.badRequest().body(response);
    }
}
