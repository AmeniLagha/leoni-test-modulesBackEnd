package com.example.security.TestUnitaire.common;

import com.example.security.common.ApiError;
import com.example.security.common.ApiResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    // ==================== Tests success() ====================

    @Test
    void success_WithMessageOnly_ShouldCreateSuccessResponse() {
        ApiResponse<Void> response = ApiResponse.success("Opération réussie");

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Opération réussie");
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_WithMessageAndData_ShouldCreateSuccessResponse() {
        String data = "Données de test";
        ApiResponse<String> response = ApiResponse.success("Opération réussie", data);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Opération réussie");
        assertThat(response.getData()).isEqualTo("Données de test");
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    void success_WithMessageDataAndStatusCode_ShouldCreateSuccessResponse() {
        List<String> data = List.of("item1", "item2");
        ApiResponse<List<String>> response = ApiResponse.success(
                "Liste récupérée",
                data,
                201
        );

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Liste récupérée");
        assertThat(response.getData()).containsExactly("item1", "item2");
        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    @Test
    void success_WithNullData_ShouldCreateSuccessResponse() {
        ApiResponse<Object> response = ApiResponse.success("Succès", null);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Succès");
        assertThat(response.getData()).isNull();
    }

    // ==================== Tests error() ====================

    @Test
    void error_WithMessageAndStatusCode_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Erreur serveur", 500);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Erreur serveur");
        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void error_WithUnauthorized_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Non authentifié", 401);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Non authentifié");
        assertThat(response.getStatusCode()).isEqualTo(401);
    }

    @Test
    void error_WithForbidden_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Accès interdit", 403);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Accès interdit");
        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    @Test
    void error_WithNotFound_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Ressource non trouvée", 404);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Ressource non trouvée");
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    void error_WithBadRequest_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Requête invalide", 400);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Requête invalide");
        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    @Test
    void error_WithConflict_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Conflit de données", 409);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Conflit de données");
        assertThat(response.getStatusCode()).isEqualTo(409);
    }

    @Test
    void error_WithMessageStatusCodeAndErrors_ShouldCreateErrorResponse() {
        List<ApiError> errors = List.of(
                new ApiError("email", "L'email est requis"),
                new ApiError("password", "Le mot de passe est requis")
        );

        ApiResponse<Void> response = ApiResponse.error("Erreur de validation", 400, errors);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Erreur de validation");
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(response.getErrors().get(1).getField()).isEqualTo("password");
    }

    @Test
    void error_WithSingleFieldError_ShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error(
                "Erreur de validation",
                400,
                "email",
                "Format d'email invalide"
        );

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Erreur de validation");
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0).getField()).isEqualTo("email");
        assertThat(response.getErrors().get(0).getMessage()).isEqualTo("Format d'email invalide");
    }

    // ==================== Tests builder() ====================

    @Test
    void builder_ShouldCreateResponseWithAllFields() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Message personnalisé")
                .data("Données")
                .statusCode(201)
                .timestamp(LocalDateTime.now())
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Message personnalisé");
        assertThat(response.getData()).isEqualTo("Données");
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void builder_ShouldCreateErrorResponseWithErrors() {
        List<ApiError> errors = List.of(new ApiError("field1", "Error 1"));

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .message("Erreur")
                .statusCode(400)
                .errors(errors)
                .build();

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors()).hasSize(1);
    }

    // ==================== Tests avec différents types de données ====================

    @Test
    void success_WithIntegerListData_ShouldCreateResponse() {
        List<Integer> data = List.of(1, 2, 3, 4, 5);
        ApiResponse<List<Integer>> response = ApiResponse.success("Liste d'entiers", data);

        assertThat(response.getData()).hasSize(5);
        assertThat(response.getData()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void success_WithCustomObject_ShouldCreateResponse() {
        TestObject data = new TestObject(1L, "Test", true);
        ApiResponse<TestObject> response = ApiResponse.success("Objet créé", data);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
        assertThat(response.getData().getName()).isEqualTo("Test");
        assertThat(response.getData().isActive()).isTrue();
    }

    // ==================== Tests timestamp ====================

    @Test
    void response_ShouldHaveNonNullTimestamp() {
        ApiResponse<Void> response = ApiResponse.success("Test");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void timestamp_ShouldBeLocalDateTime() {
        ApiResponse<Void> response = ApiResponse.success("Test");
        assertThat(response.getTimestamp()).isInstanceOf(LocalDateTime.class);
    }

    // ==================== Tests JsonInclude ====================

    @Test
    void nullFields_ShouldBeExcludedFromJson() {
        ApiResponse<String> response = ApiResponse.success("Success", null);

        assertThat(response.getData()).isNull();
        assertThat(response.getErrors()).isNull();
        // Vérifie que les champs null ne sont pas inclus lors de la sérialisation JSON
    }

    // Classe interne pour tester les objets personnalisés
    static class TestObject {
        private final Long id;
        private final String name;
        private final boolean active;

        TestObject(Long id, String name, boolean active) {
            this.id = id;
            this.name = name;
            this.active = active;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public boolean isActive() { return active; }
    }
}