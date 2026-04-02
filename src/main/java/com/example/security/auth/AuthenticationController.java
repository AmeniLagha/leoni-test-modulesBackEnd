package com.example.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentification", description = "Gestion de l'authentification et des tokens JWT")
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
private final AuthenticationService service;

    @PostMapping("/register")
    @Operation(
            summary = "Inscription",
            description = "Permet de créer un nouveau compte utilisateur avec génération d’un token JWT"
    )
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    @Operation(
            summary = "Connexion",
            description = "Authentifier un utilisateur et retourner un token JWT"
    )
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @Operation(
            summary = "Rafraîchir le token",
            description = "Générer un nouveau token JWT à partir d’un refresh token valide"
    )
    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    @GetMapping("/current-token")
    @Operation(
            summary = "Récupérer le token actuel",
            description = "Retourne le token JWT de l’utilisateur connecté à partir du header Authorization"
    )
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
