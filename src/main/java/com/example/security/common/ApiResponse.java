package com.example.security.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Structure de réponse standardisée pour toutes les API
 * @param <T> Type des données retournées
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private int statusCode;
    private T data;
    private List<ApiError> errors;
    private LocalDateTime timestamp;

    // ✅ Constructeur pour réponse succès sans données
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ✅ Constructeur pour réponse succès avec données
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ✅ Constructeur pour réponse succès avec données et code personnalisé
    public static <T> ApiResponse<T> success(String message, T data, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(statusCode)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ✅ Constructeur pour réponse erreur
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ✅ Constructeur pour réponse erreur avec liste d'erreurs détaillées
    public static <T> ApiResponse<T> error(String message, int statusCode, List<ApiError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ✅ Constructeur pour réponse erreur (version simplifiée avec une seule erreur)
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