package com.example.security;

import com.example.security.auth.AuthenticationService;
import com.example.security.auth.RegisterRequest;
import com.example.security.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
/*
@Component
@RequiredArgsConstructor

public class DataInitializer implements CommandLineRunner {

    private final AuthenticationService authService;

    @Override
    public void run(String... args) throws Exception {
        // Créer des utilisateurs de test pour chaque rôle
        createTestUsers();
    }

    private void createTestUsers() {
        try {
            // ING (Ingénierie)
            RegisterRequest ingRequest = RegisterRequest.builder()
                    .firstname("Ingénieur")
                    .lastname("Test")
                    .email("ing@test.com")
                    .password("password123")
                    .matricule(1001)
                    .role(Role.ING)
                    .build();

            authService.register(ingRequest);
            System.out.println("Utilisateur ING créé: ing@test.com / password123");

            // PT (Production Technology)
            RegisterRequest ptRequest = RegisterRequest.builder()
                    .firstname("Technicien")
                    .lastname("PT")
                    .email("pt@test.com")
                    .password("password123")
                    .matricule(2001)
                    .role(Role.PT)
                    .build();

            authService.register(ptRequest);
            System.out.println("Utilisateur PT créé: pt@test.com / password123");

            // PP (Production Preparation)
            RegisterRequest ppRequest = RegisterRequest.builder()
                    .firstname("Préparateur")
                    .lastname("Production")
                    .email("pp@test.com")
                    .password("password123")
                    .matricule(3001)
                    .role(Role.PP)
                    .build();

            authService.register(ppRequest);
            System.out.println("Utilisateur PP créé: pp@test.com / password123");

            // MC (Maintenance Corrective)
            RegisterRequest mcRequest = RegisterRequest.builder()
                    .firstname("Maintenance")
                    .lastname("Corrective")
                    .email("mc@test.com")
                    .password("password123")
                    .matricule(4001)
                    .role(Role.MC)
                    .build();

            authService.register(mcRequest);
            System.out.println("Utilisateur MC créé: mc@test.com / password123");

            // MP (Maintenance Préventive)
            RegisterRequest mpRequest = RegisterRequest.builder()
                    .firstname("Maintenance")
                    .lastname("Préventive")
                    .email("mp@test.com")
                    .password("password123")
                    .matricule(5001)
                    .role(Role.MP)
                    .build();

            authService.register(mpRequest);
            System.out.println("Utilisateur MP créé: mp@test.com / password123");

            // Admin
            RegisterRequest adminRequest = RegisterRequest.builder()
                    .firstname("Admin")
                    .lastname("System")
                    .email("admin@test.com")
                    .password("admin123")
                    .matricule(0001)
                    .role(Role.ADMIN)
                    .build();

            authService.register(adminRequest);
            System.out.println("Utilisateur Admin créé: admin@test.com / admin123");

        } catch (Exception e) {
            System.out.println("Certains utilisateurs existent déjà: " + e.getMessage());
        }
    }
}*/