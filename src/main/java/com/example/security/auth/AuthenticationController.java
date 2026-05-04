package com.example.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.security.common.ApiResponse;

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
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse authResponse = service.register(request);

        ApiResponse<AuthenticationResponse> response = ApiResponse.success(
                "Utilisateur créé avec succès",
                authResponse,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/authenticate")
    @Operation(
            summary = "Connexion",
            description = "Authentifier un utilisateur et retourner un token JWT"
    )
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse authResponse = service.authenticate(request);

        ApiResponse<AuthenticationResponse> response = ApiResponse.success(
                "Authentification réussie",
                authResponse
        );

        return ResponseEntity.ok(response);
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
    public ResponseEntity<ApiResponse<Map<String, String>>> getCurrentToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("token", authHeader.substring(7));

            ApiResponse<Map<String, String>> response = ApiResponse.success(
                    "Token récupéré avec succès",
                    tokenData
            );
            return ResponseEntity.ok(response);
        }

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                "Aucun token trouvé",
                HttpStatus.BAD_REQUEST.value(),
                "Authorization",
                "Le header Authorization est manquant ou invalide"
        );

        return ResponseEntity.badRequest().body(response);
    }
}