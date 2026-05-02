package com.example.security.TestUnitaire.repository;

import com.example.security.auth.AuthenticationService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@ActiveProfiles("test")
class ProjetRepositoryTest {

    @MockBean  // Add this to mock the AuthenticationService
    private AuthenticationService authenticationService;

    @Autowired
    private ProjetRepository projetRepository;

    private Projet testProjet;

    @BeforeEach
    void setUp() {
        projetRepository.deleteAll();

        testProjet = new Projet();
        testProjet.setName("FORD");
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);
    }

    @Test
    void findByName_ShouldReturnProjet() {
        Optional<Projet> found = projetRepository.findByName("FORD");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("FORD");
    }

    @Test
    void findByName_WithInvalidName_ShouldReturnEmpty() {
        Optional<Projet> found = projetRepository.findByName("INVALID");

        assertThat(found).isEmpty();
    }

    @Test
    void findAllByActiveTrue_ShouldReturnOnlyActiveProjets() {
        Projet inactiveProjet = new Projet();
        inactiveProjet.setName("TESLA");
        inactiveProjet.setActive(false);
        projetRepository.save(inactiveProjet);

        List<Projet> activeProjets = projetRepository.findAllByActiveTrue();

        assertThat(activeProjets).hasSize(1);
        assertThat(activeProjets.get(0).getName()).isEqualTo("FORD");
    }

    @Test
    void findAllByOrderByNameAsc_ShouldReturnSorted() {
        Projet secondProjet = new Projet();
        secondProjet.setName("BMW");
        secondProjet.setActive(true);
        projetRepository.save(secondProjet);

        List<Projet> projets = projetRepository.findAllByOrderByNameAsc();

        assertThat(projets).hasSize(2);
        assertThat(projets.get(0).getName()).isEqualTo("BMW");
        assertThat(projets.get(1).getName()).isEqualTo("FORD");
    }
}