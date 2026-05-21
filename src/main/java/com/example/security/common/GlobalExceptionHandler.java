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
/**
 * Gestionnaire global des exceptions pour l'ensemble des contrôleurs REST.
 * <p>
 * Cette classe intercepte les exceptions levées par les contrôleurs et les
 * convertit en réponses HTTP structurées au format {@link ApiResponse}.
 Elle garantit une expérience utilisateur cohérente en cas d'erreur.
 * </p>
 *
 * <p><strong>Exceptions gérées :</strong></p>
 * <ul>
 *     <li>{@link BadCredentialsException} → 401 Unauthorized (identifiants invalides)</li>
 *     <li>{@link AuthenticationException} → 403 Forbidden (authentification échouée)</li>
 *     <li>{@link MethodArgumentNotValidException} → 400 Bad Request (validation @Valid)</li>
 *     <li>{@link AccessDeniedException} → 403 Forbidden (accès non autorisé)</li>
 *     <li>{@link ResponseStatusException} → Code HTTP personnalisé</li>
 *     <li>{@link RuntimeException} → 500 Internal Server Error</li>
 * </ul>
 *
 * <p><strong>Structure des réponses d'erreur :</strong></p>
 * <pre>
 * {
 *     "success": false,
 *     "message": "Email ou mot de passe incorrect",
 *     "statusCode": 401,
 *     "errors": null
 * }
 * </pre>
 *
 * <p><strong>Pour les erreurs de validation :</strong></p>
 * <pre>
 * {
 *     "success": false,
 *     "message": "Erreur de validation des données",
 *     "statusCode": 400,
 *     "errors": [
 *         {"field": "email", "message": "L'email est obligatoire"},
 *         {"field": "password", "message": "Le mot de passe doit contenir au moins 6 caractères"}
 *     ]
 * }
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ApiResponse
 * @see ApiError
 * @since Sprint 2
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
// ============================================================
    // GESTION DES ERREURS D'AUTHENTIFICATION
    // ============================================================

    /**
     * Gère les erreurs d'identifiants incorrects lors de l'authentification.
     * <p>
     * Cette exception est levée par Spring Security lorsque l'utilisateur
     * soumet un email ou un mot de passe invalide.
     * </p>
     *
     * @param ex L'exception {@link BadCredentialsException}
     * @return ResponseEntity avec une {@link ApiResponse} contenant un message
     *         d'erreur générique et le code HTTP 401 (Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentative d'authentification avec identifiants invalides: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "Email ou mot de passe incorrect",
                HttpStatus.UNAUTHORIZED.value()  // ← 401 au lieu de 403
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère les erreurs d'authentification générales.
     * <p>
     * Cette exception peut être levée pour divers problèmes d'authentification
     * (token expiré, token invalide, utilisateur non authentifié, etc.).
     * </p>
     *
     * @param ex L'exception {@link AuthenticationException}
     * @return ResponseEntity avec une {@link ApiResponse} contenant un message
     *         d'erreur générique et le code HTTP 403 (Forbidden)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Erreur d'authentification: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "Authentification échouée",
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ============================================================
    // GESTION DES ERREURS DE VALIDATION
    // ============================================================

    /**
     * Gère les erreurs de validation des données (@Valid).
     * <p>
     * Cette exception est levée lorsque les données envoyées par le client
     * ne respectent pas les contraintes de validation définies dans les DTO
     * (ex: {@code @NotBlank}, {@code @Email}, {@code @NotNull}, etc.).
     * </p>
     *
     * <p><strong>Exemple de corps de requête invalide :</strong></p>
     * <pre>
     * {
     *     "email": "email_invalide",
     *     "password": ""  // vide
     * }
     * </pre>
     *
     * @param ex L'exception {@link MethodArgumentNotValidException}
     * @return ResponseEntity avec une {@link ApiResponse} contenant la liste
     *         des champs en erreur avec leurs messages, et le code HTTP 400
     */
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

    // ============================================================
    // GESTION DES ERREURS D'ACCÈS
    // ============================================================

    /**
     * Gère les erreurs d'accès non autorisé.
     * <p>
     * Cette exception est levée par Spring Security lorsqu'un utilisateur
     * tente d'accéder à une ressource pour laquelle il n'a pas les permissions
     * nécessaires (via {@code @PreAuthorize}).
     * </p>
     *
     * @param ex L'exception {@link AccessDeniedException}
     * @return ResponseEntity avec une {@link ApiResponse} contenant un message
     *         d'erreur et le code HTTP 403 (Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                "Accès non autorisé",
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ============================================================
    // GESTION DES EXCEPTIONS PERSONNALISÉES
    // ============================================================

    /**
     * Gère les exceptions {@link ResponseStatusException}.
     * <p>
     * Cette exception est utilisée dans les services pour retourner des erreurs
     * avec des codes HTTP spécifiques (ex: 404, 409, 400).
     * </p>
     *
     * @param ex L'exception {@link ResponseStatusException}
     * @return ResponseEntity avec une {@link ApiResponse} contenant le message
     *         d'erreur et le code HTTP correspondant
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException ex) {
        ApiResponse<Void> response = ApiResponse.error(
                ex.getReason() != null ? ex.getReason() : "Erreur serveur",
                ex.getStatusCode().value()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    // ============================================================
    // GESTION DES EXCEPTIONS GÉNÉRIQUES
    // ============================================================

    /**
     * Gère toutes les {@link RuntimeException} non capturées par les autres handlers.
     * <p>
     * Cette méthode sert de filet de sécurité pour toutes les exceptions
     * inattendues. Elle logge l'erreur complète (stack trace) pour le débogage
     * et retourne un message générique à l'utilisateur.
     * </p>
     *
     * @param ex L'exception {@link RuntimeException}
     * @return ResponseEntity avec une {@link ApiResponse} contenant le message
     *         d'erreur (ou un message générique) et le code HTTP 500
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Erreur serveur: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage() != null ? ex.getMessage() : "Une erreur interne est survenue",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * <p>
     * Cette exception est levée par JPA/Hibernate lorsqu'une entité recherchée
     * n'existe pas en base de données.
     * </p>
     *

     * @return ResponseEntity avec une {@link ApiResponse} contenant un message
     *         d'erreur et le code HTTP 404 (Not Found)
     */
    @ExceptionHandler({jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
        ApiResponse<Void> response = ApiResponse.error(
                "Ressource non trouvée",
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}