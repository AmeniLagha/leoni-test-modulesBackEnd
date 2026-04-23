package com.example.security.auth;

import com.example.security.user.UserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/auth";
        // Nettoyer la base de données avant chaque test
        userRepository.deleteAll();
    }

    @Test
    void testRegister_WithValidData_ShouldCreateUser() {
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("firstname", "John");
        registerRequest.put("lastname", "Doe");
        registerRequest.put("email", "john.doe@test.com");
        registerRequest.put("matricule", "MAT12345");
        registerRequest.put("password", "password123");
        registerRequest.put("role", "USER");
        registerRequest.put("siteName", "Test Site");
        registerRequest.put("projets", List.of("Test Projet"));

        given()
                .contentType("application/json")
                .body(registerRequest)
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void testRegister_WithDuplicateEmail_ShouldReturnError() {
        // Premier enregistrement
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("firstname", "John");
        registerRequest.put("lastname", "Doe");
        registerRequest.put("email", "duplicate@test.com");
        registerRequest.put("matricule", "MAT12346");
        registerRequest.put("password", "password123");
        registerRequest.put("role", "USER");
        registerRequest.put("siteName", "Test Site");
        registerRequest.put("projets", List.of("Test Projet"));

        given()
                .contentType("application/json")
                .body(registerRequest)
                .when()
                .post("/register")
                .then()
                .statusCode(200);

        // Deuxième enregistrement avec même email
        registerRequest.put("matricule", "MAT12347");
        given()
                .contentType("application/json")
                .body(registerRequest)
                .when()
                .post("/register")
                .then()
                .statusCode(500); // RuntimeException "Email déjà utilisé"
    }

    @Test
    void testAuthenticate_WithValidCredentials_ShouldReturnToken() {
        // D'abord créer un utilisateur
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("firstname", "Jane");
        registerRequest.put("lastname", "Smith");
        registerRequest.put("email", "jane.smith@test.com");
        registerRequest.put("matricule", "MAT12348");
        registerRequest.put("password", "password123");
        registerRequest.put("role", "USER");
        registerRequest.put("siteName", "Test Site");
        registerRequest.put("projets", List.of("Test Projet"));

        given()
                .contentType("application/json")
                .body(registerRequest)
                .when()
                .post("/register")
                .then()
                .statusCode(200);

        // Ensuite tester l'authentification
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "jane.smith@test.com");
        loginRequest.put("password", "password123");
        loginRequest.put("siteName", "Test Site");

        given()
                .contentType("application/json")
                .body(loginRequest)
                .when()
                .post("/authenticate")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    void testAuthenticate_WithInvalidCredentials_ShouldReturnError() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "nonexistent@test.com");
        loginRequest.put("password", "wrongpassword");
        loginRequest.put("siteName", "Test Site");

        given()
                .contentType("application/json")
                .body(loginRequest)
                .when()
                .post("/authenticate")
                .then()
                .statusCode(401); // Unauthorized
    }
}