package com.example.security.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentative d'authentification avec identifiants invalides: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "Email ou mot de passe incorrect",
                HttpStatus.UNAUTHORIZED.value()  // ← 401 au lieu de 403
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ✅ NOUVEAU : Gestion des erreurs d'authentification (utile pour d'autres cas)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Erreur d'authentification: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "Authentification échouée",
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ✅ Gestion des erreurs de validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return new ApiError(fieldName, errorMessage);
                })
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error(
                "Erreur de validation des données",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // ✅ Gestion des erreurs d'accès (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                "Accès non autorisé",
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ✅ Gestion des ResponseStatusException (tes exceptions personnalisées)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                ex.getReason() != null ? ex.getReason() : "Erreur serveur",
                ex.getStatusCode().value()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    // ✅ Gestion des RuntimeException génériques
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Erreur serveur: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage() != null ? ex.getMessage() : "Une erreur interne est survenue",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ✅ Gestion des erreurs 404 (NotFoundException générique)
    @ExceptionHandler({jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
        ApiResponse<Void> response = ApiResponse.error(
                "Ressource non trouvée",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}