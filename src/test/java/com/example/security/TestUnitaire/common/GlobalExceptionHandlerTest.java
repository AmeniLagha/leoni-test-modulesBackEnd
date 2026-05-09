package com.example.security.TestUnitaire.common;

import com.example.security.common.ApiError;
import com.example.security.common.ApiResponse;
import com.example.security.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadCredentials_ShouldReturnUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email ou mot de passe incorrect");
        assertThat(response.getBody().getStatusCode()).isEqualTo(401);
    }

    @Test
    void handleAuthenticationException_ShouldReturnForbidden() {
        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("Authentication failed");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Authentification échouée");
        assertThat(response.getBody().getStatusCode()).isEqualTo(403);
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("objectName", "email", "L'email est requis");
        FieldError fieldError2 = new FieldError("objectName", "password", "Le mot de passe est requis");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Erreur de validation des données");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().getErrors()).hasSize(2);
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("email");
        assertThat(response.getBody().getErrors().get(1).getField()).isEqualTo("password");
    }

    @Test
    void handleAccessDenied_ShouldReturnForbidden() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Accès non autorisé");
        assertThat(response.getBody().getStatusCode()).isEqualTo(403);
    }

    @Test
    void handleResponseStatus_WithConflict_ShouldReturnConflict() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResponseStatus(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email déjà utilisé");
        assertThat(response.getBody().getStatusCode()).isEqualTo(409);
    }

    @Test
    void handleResponseStatus_WithNotFound_ShouldReturnNotFound() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource non trouvée");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResponseStatus(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Ressource non trouvée");
        assertThat(response.getBody().getStatusCode()).isEqualTo(404);
    }

    @Test
    void handleResponseStatus_WithBadRequest_ShouldReturnBadRequest() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requête invalide");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResponseStatus(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Requête invalide");
        assertThat(response.getBody().getStatusCode()).isEqualTo(400);
    }

    @Test
    void handleRuntimeException_ShouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Erreur inattendue");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Erreur inattendue");
        assertThat(response.getBody().getStatusCode()).isEqualTo(500);
    }

    @Test
    void handleRuntimeException_WithNullMessage_ShouldReturnDefaultMessage() {
        RuntimeException ex = new RuntimeException();

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getBody().getMessage()).isEqualTo("Une erreur interne est survenue");
    }

    @Test
    void handleEntityNotFoundException_ShouldReturnNotFound() {
        jakarta.persistence.EntityNotFoundException ex = new jakarta.persistence.EntityNotFoundException();

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Ressource non trouvée");
        assertThat(response.getBody().getStatusCode()).isEqualTo(404);
    }
}