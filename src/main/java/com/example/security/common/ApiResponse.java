package com.example.security.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Structure de réponse standardisée pour toutes les API de l'application.
 * <p>
 * Cette classe générique garantit une structure unifiée pour toutes les réponses
 * HTTP, que la requête aboutisse ou non. Elle permet au frontend de traiter * les réponses de manière cohérente, avec un indicateur de succès, un message,
 * un code HTTP et des données facultatives.
 * </p>
 *
 * <p><strong>Structure de la réponse :</strong></p>
 * <pre>
 * {
 *     "success": true/false,
 *     "message": "Message explicatif",
 *     "statusCode": 200,
 *     "data": { ... },           // Optionnel (succès uniquement)
 *     "errors": [ ... ],         // Optionnel (erreurs de validation)
 *     "timestamp": "2024-01-15T14:30:00"
 * }
 * </pre>
 *
 * <p><strong>Types de réponses :</strong></p>
 * <ul>
 *     <li><strong>Succès sans données</strong> : {@link #success(String)}</li>
 *     <li><strong>Succès avec données</strong> : {@link #success(String, Object)}</li>
 *     <li><strong>Succès avec code personnalisé</strong> : {@link #success(String, Object, int)}</li>
 *     <li><strong>Erreur simple</strong> : {@link #error(String, int)}</li>
 *     <li><strong>Erreur avec détails</strong> : {@link #error(String, int, List)}</li>
 *     <li><strong>Erreur avec un champ spécifique</strong> : {@link #error(String, int, String, String)}</li>
 * </ul>
 *
 * <p><strong>Exemple d'utilisation dans un contrôleur :</strong></p>
 * <pre>
 * // Réponse de succès avec données
 * UserDto user = userService.findById(1L);
 * return ResponseEntity.ok(ApiResponse.success("Utilisateur trouvé", user));
 *
 * // Réponse de succès sans données
 * userService.delete(1L);
 * return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé"));
 *
 * // Réponse d'erreur
 * return ResponseEntity.status(HttpStatus.NOT_FOUND)
 *         .body(ApiResponse.error("Utilisateur non trouvé", 404));
 *
 * // Réponse avec validation
 * return ResponseEntity.badRequest()
 *         .body(ApiResponse.error("Validation échouée", 400, "email", "Format invalide"));
 * </pre>
 *
 * @param <T> Le type des données retournées dans la réponse
 * @author LAGHA AMENI
 * @version 1.0
 * @see GlobalExceptionHandler
 * @see ApiError
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    /**
     * Indique si la requête a abouti avec succès.
     * <p>
     * {@code true} lorsque l'opération est réussie, {@code false} en cas d'erreur.
     * Permet au frontend de déterminer rapidement l'état de la réponse.
     * </p>
     */
    private boolean success;
    /**
     * Message explicatif de la réponse.
     * <p>
     * En cas de succès, message confirmatif (ex: "Utilisateur créé avec succès").
     * En cas d'erreur, description du problème (ex: "Email déjà utilisé").
     * </p>
     */
    private String message;
    /**
     * Code HTTP de la réponse.
     * <p>
     * Permet de connaître le statut HTTP même si la réponse est encapsulée.
     * Valeurs typiques : 200, 201, 400, 401, 403, 404, 409, 500.
     * </p>
     */
    private int statusCode;
    /**
     * Données retournées par la requête (optionnel).
     * <p>
     * Présent uniquement en cas de succès. Peut contenir n'importe quel type
     * d'objet (DTO, liste, map, etc.) grâce au générique {@code T}.
     * </p>
     *
     * @param <T> Le type des données
     */
    private T data;
    /**
     * Liste des erreurs détaillées (optionnel).
     * <p>
     * Présent uniquement en cas d'erreur, généralement pour les validations
     * de formulaire. Chaque erreur contient le champ concerné et le message
     * d'erreur associé.
     * </p>
     *
     * @see ApiError
     */
    private List<ApiError> errors;
    /**
     * Horodatage de la réponse.
     * <p>
     * Date et heure exactes de génération de la réponse, au format
     * ISO-8601 (ex: 2024-01-15T14:30:00).
     * </p>
     */
    private LocalDateTime timestamp;

    // ============================================================
    // MÉTHODES STATIQUES DE CONSTRUCTION
    // ============================================================

    /**
     * Crée une réponse de succès sans données.
     * <p>
     * Utilisée lorsque l'opération est réussie mais qu'il n'y a pas de
     * données à retourner (ex: suppression, mise à jour simple).
     * </p>
     *
     * @param message Le message de confirmation
     * @param <T> Le type générique (inféré automatiquement)
     * @return Une réponse {@link ApiResponse} avec success=true et code HTTP 200
     *
     * @example
     * <pre>
     * return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé"));
     * </pre>
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crée une réponse de succès avec données.
     * <p>
     * Utilisée lorsque l'opération est réussie et retourne des données
     * (ex: recherche, création, mise à jour).
     * </p>
     *
     * @param message Le message de confirmation
     * @param data Les données à retourner (DTO, liste, etc.)
     * @param <T> Le type des données
     * @return Une réponse {@link ApiResponse} avec success=true, code HTTP 200 et les données
     *
     * @example
     * <pre>
     * UserDto user = userService.findById(1L);
     * return ResponseEntity.ok(ApiResponse.success("Utilisateur trouvé", user));
     * </pre>
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crée une réponse de succès avec données et code HTTP personnalisé.
     * <p>
     * Utilisée pour les créations (code 201) ou autres cas nécessitant
     * un code HTTP spécifique.
     * </p>
     *
     * @param message Le message de confirmation
     * @param data Les données à retourner
     * @param statusCode Le code HTTP personnalisé (ex: 201 pour CREATED)
     * @param <T> Le type des données
     * @return Une réponse {@link ApiResponse} avec success=true et le code spécifié
     *
     * @example
     * <pre>
     * UserDto newUser = userService.create(userDto);
     * return ResponseEntity.status(HttpStatus.CREATED)
     *         .body(ApiResponse.success("Utilisateur créé", newUser, 201));
     * </pre>
     */
    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(statusCode)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crée une réponse d'erreur simple.
     * <p>
     * Utilisée pour les erreurs sans détails supplémentaires
     * (ex: ressource non trouvée, accès non autorisé).
     * </p>
     *
     * @param message Le message d'erreur
     * @param statusCode Le code HTTP de l'erreur (ex: 404, 403, 500)
     * @param <T> Le type générique (inféré automatiquement)
     * @return Une réponse {@link ApiResponse} avec success=false et le code spécifié
     *
     * @example
     * <pre>
     * return ResponseEntity.status(HttpStatus.NOT_FOUND)
     *         .body(ApiResponse.error("Utilisateur non trouvé", 404));
     * </pre>
     */
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crée une réponse d'erreur avec liste d'erreurs détaillées.
     * <p>
     * Utilisée principalement pour les erreurs de validation de formulaire,
     * où plusieurs champs peuvent être en erreur simultanément.
     * </p>
     *
     * @param message Le message d'erreur général
     * @param statusCode Le code HTTP de l'erreur (généralement 400)
     * @param errors La liste des erreurs détaillées (champ + message)
     * @param <T> Le type générique
     * @return Une réponse {@link ApiResponse} avec success=false, le code spécifié
     *         et la liste des erreurs
     *
     * @example
     * <pre>
     * List&lt;ApiError&gt; errors = Arrays.asList(
     *     new ApiError("email", "L'email est obligatoire"),
     *     new ApiError("password", "Le mot de passe est trop court")
     * );
     * return ResponseEntity.badRequest()
     *         .body(ApiResponse.error("Erreur de validation", 400, errors));
     * </pre>
     */
    public static <T> ApiResponse<T> error(String message, int statusCode, List<ApiError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crée une réponse d'erreur avec un seul champ en erreur.
     * <p>
     * Version simplifiée de {@link #error(String, int, List)} pour les cas
     * où un seul champ est problématique.
     * </p>
     *
     * @param message Le message d'erreur général
     * @param statusCode Le code HTTP de l'erreur
     * @param field Le nom du champ en erreur
     * @param errorMessage Le message d'erreur pour ce champ
     * @param <T> Le type générique
     * @return Une réponse {@link ApiResponse} avec success=false, le code spécifié
     *         et une liste contenant une seule erreur
     *
     * @example
     * <pre>
     * return ResponseEntity.badRequest()
     *         .body(ApiResponse.error("Validation échouée", 400, "email", "Format d'email invalide"));
     * </pre>
     */
    public static <T> ApiResponse<T> error(String message, int statusCode, String field, String errorMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .errors(List.of(new ApiError(field, errorMessage)))
                .timestamp(LocalDateTime.now())
                .build();
    }
}