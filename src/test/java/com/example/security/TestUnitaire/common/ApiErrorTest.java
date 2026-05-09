package com.example.security.TestUnitaire.common;

import com.example.security.common.ApiError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorTest {

    @Test
    void constructor_WithFieldAndMessage_ShouldCreateApiError() {
        ApiError error = new ApiError("email", "L'email est requis");

        assertThat(error.getField()).isEqualTo("email");
        assertThat(error.getMessage()).isEqualTo("L'email est requis");
        assertThat(error.getCode()).isNull();
    }

    @Test
    void builder_ShouldCreateApiErrorWithAllFields() {
        ApiError error = ApiError.builder()
                .field("password")
                .message("Le mot de passe doit contenir au moins 6 caractères")
                .code("PASSWORD_TOO_SHORT")
                .build();

        assertThat(error.getField()).isEqualTo("password");
        assertThat(error.getMessage()).isEqualTo("Le mot de passe doit contenir au moins 6 caractères");
        assertThat(error.getCode()).isEqualTo("PASSWORD_TOO_SHORT");
    }

    @Test
    void multipleApiErrors_ShouldBeStoredInList() {
        List<ApiError> errors = List.of(
                new ApiError("email", "Email invalide"),
                new ApiError("password", "Mot de passe requis")
        );

        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).getField()).isEqualTo("email");
        assertThat(errors.get(1).getField()).isEqualTo("password");
    }

    @Test
    void apiError_WithCode_ShouldReturnCode() {
        ApiError error = ApiError.builder()
                .field("user")
                .message("Utilisateur non trouvé")
                .code("USER_NOT_FOUND")
                .build();

        assertThat(error.getCode()).isEqualTo("USER_NOT_FOUND");
    }
}