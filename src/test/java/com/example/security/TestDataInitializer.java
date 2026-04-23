package com.example.security;

import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import jakarta.annotation.PostConstruct;

@TestConfiguration
@Profile("test")
public class TestDataInitializer {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @PostConstruct
    public void init() {
        Site testSite = Site.builder()
                .name("Test Site")
                .description("Site pour les tests")
                .active(true)
                .build();
        siteRepository.save(testSite);

        Projet testProjet = Projet.builder()
                .name("Test Projet")
                .description("Projet pour les tests")
                .active(true)
                .build();
        projetRepository.save(testProjet);
    }
}