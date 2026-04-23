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
        // Créer le site de test s'il n'existe pas
        Site testSite = siteRepository.findByName("Test Site").orElse(null);
        if (testSite == null) {
            testSite = Site.builder()
                    .name("Test Site")
                    .description("Site pour les tests")
                    .active(true)
                    .build();
            testSite = siteRepository.save(testSite);
            System.out.println("✅ Site de test créé: " + testSite.getName());
        } else {
            System.out.println("✅ Site de test existe déjà: " + testSite.getName());
        }

        // Créer le projet de test s'il n'existe pas
        Projet testProjet = projetRepository.findByName("Test Projet").orElse(null);
        if (testProjet == null) {
            testProjet = Projet.builder()
                    .name("Test Projet")
                    .description("Projet pour les tests")
                    .active(true)
                    .build();
            testProjet = projetRepository.save(testProjet);
            System.out.println("✅ Projet de test créé: " + testProjet.getName());
        } else {
            System.out.println("✅ Projet de test existe déjà: " + testProjet.getName());
        }

        // Associer le projet au site
        if (!testSite.getProjets().contains(testProjet)) {
            testSite.getProjets().add(testProjet);
            siteRepository.save(testSite);
            System.out.println("✅ Projet associé au site");
        }
    }
}