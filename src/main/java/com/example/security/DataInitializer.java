/*package com.example.security;

import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// DataInitializer.java
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProjetRepository projetRepository;

    @Override
    public void run(String... args) throws Exception {
        if (projetRepository.count() == 0) {
            List<Projet> projets = List.of(
                    Projet.builder().name("BMW").description("Projet BMW").active(true).build(),
                    Projet.builder().name("Mercedes").description("Projet Mercedes").active(true).build(),
                    Projet.builder().name("Audi").description("Projet Audi").active(true).build(),
                    Projet.builder().name("Porsche").description("Projet Porsche").active(true).build(),
                    Projet.builder().name("Volkswagen").description("Projet Volkswagen").active(true).build()
            );
            projetRepository.saveAll(projets);
            System.out.println("✅ Projets créés");
        }
    }
}*/