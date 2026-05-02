package com.example.security.TestUnitaire.repository;

  // Import your test config
import com.example.security.auth.AuthenticationService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.main.allow-bean-definition-overriding=true"
})
class UserRepositoryTest {
    @MockBean  // This will create a mock and prevent the real bean from being loaded
    private AuthenticationService authenticationService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Site testSite;
    private Projet testProjet;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Nettoyage
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        testSite = new Site();
        testSite.setName("MH1");
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        testProjet = new Projet();
        testProjet.setName("FORD");
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        testUser = User.builder()
                .email("user@test.com")
                .password("password")
                .firstname("Test")
                .lastname("User")
                .matricule(12345)
                .role(Role.ING)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
        testUser = userRepository.save(testUser);

        adminUser = User.builder()
                .email("admin@test.com")
                .password("admin123")
                .firstname("Admin")
                .lastname("User")
                .matricule(10000)
                .role(Role.ADMIN)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
        adminUser = userRepository.save(adminUser);

        // Flush pour s'assurer que les données sont en base
        entityManager.flush();
        entityManager.clear();
    }

    // Vos tests existants restent identiques...
    @Test
    void existsByEmail_ShouldReturnTrueForExistingEmail() {
        boolean exists = userRepository.existsByEmail("user@test.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalseForNonExistentEmail() {
        boolean exists = userRepository.existsByEmail("nonexistent@test.com");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByMatricule_ShouldReturnTrueForExistingMatricule() {
        boolean exists = userRepository.existsByMatricule(12345);
        assertThat(exists).isTrue();
    }

    @Test
    void existsByMatricule_ShouldReturnFalseForNonExistentMatricule() {
        boolean exists = userRepository.existsByMatricule(99999);
        assertThat(exists).isFalse();
    }

    @Test
    void findAllUserEmails_ShouldReturnAllEmails() {
        List<String> emails = userRepository.findAllUserEmails();

        assertThat(emails).hasSize(2);
        assertThat(emails).contains("user@test.com", "admin@test.com");
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        User found = userRepository.findByEmail("user@test.com").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void findActiveUserEmailsByProjet_ShouldReturnEmails() {
        List<String> emails = userRepository.findActiveUserEmailsByProjet("FORD");

        assertThat(emails).hasSize(2);
    }

    @Test
    void findActiveUserEmailsByProjetExcludingCurrent_ShouldExcludeUser() {
        List<String> emails = userRepository.findActiveUserEmailsByProjetExcludingCurrent(
                "FORD", "user@test.com");

        assertThat(emails).hasSize(1);
        assertThat(emails.get(0)).isEqualTo("admin@test.com");
    }

    @Test
    void findMcAndMpByProject_ShouldReturnMCAndMP() {
        User mcUser = User.builder()
                .email("mc@test.com")
                .password("password")
                .firstname("MC")
                .lastname("User")
                .matricule(12346)
                .role(Role.MC)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
        userRepository.save(mcUser);

        List<User> users = userRepository.findMcAndMpByProject("FORD");

        assertThat(users).isNotEmpty();
    }

    @Test
    void findEmailsByProjectsAndSite_ShouldReturnFilteredEmails() {
        List<String> emails = userRepository.findEmailsByProjectsAndSite(
                List.of("FORD"), "MH1");

        assertThat(emails).hasSize(2);
    }

    @Test
    void findPpUsersByProjectAndSite_ShouldReturnPPUsers() {
        User ppUser = User.builder()
                .email("pp@test.com")
                .password("password")
                .firstname("PP")
                .lastname("User")
                .matricule(12347)
                .role(Role.PP)
                .site(testSite)
                .projets(Set.of(testProjet))
                .build();
        userRepository.save(ppUser);

        List<User> users = userRepository.findPpUsersByProjectAndSite("FORD", "MH1");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("pp@test.com");
    }

    @Test
    void findEmailsByProjectAndSite_ShouldReturnEmails() {
        List<String> emails = userRepository.findEmailsByProjectAndSite("FORD", "MH1");

        assertThat(emails).hasSize(2);
    }
}