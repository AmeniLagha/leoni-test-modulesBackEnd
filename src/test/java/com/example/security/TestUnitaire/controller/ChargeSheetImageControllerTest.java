package com.example.security.TestUnitaire.controller;

import com.example.security.TestUnitaire.BaseIntegrationTest;
import com.example.security.cahierdeCharge.*;
import com.example.security.cahierdeCharge.ImageStorageService;
import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.reclamation.Claim;
import com.example.security.reclamation.ClaimRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.token.Token;
import com.example.security.token.TokenRepository;
import com.example.security.token.TokenType;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChargeSheetImageControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChargeSheetRepository chargeSheetRepository;

    @Autowired
    private ChargeSheetItemRepository chargeSheetItemRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Mock file system — no real disk writes in tests
    @MockBean
    private ImageStorageService imageStorageService;

    private String ppToken;
    private String ptToken;
    private String ingToken;
    private Site testSite;
    private Projet testProjet;
    private ChargeSheet testSheet;
    private ChargeSheetItem testItem;
    private ChargeSheetItem itemWithImage;
    private Claim testClaim;

    @BeforeEach
    void setUp() throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        claimRepository.deleteAll();
        chargeSheetItemRepository.deleteAll();
        chargeSheetRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        siteRepository.deleteAll();
        projetRepository.deleteAll();

        testSite = new Site();
        testSite.setName("MH1_" + uid);
        testSite.setActive(true);
        testSite = siteRepository.save(testSite);

        testProjet = new Projet();
        testProjet.setName("FORD_" + uid);
        testProjet.setActive(true);
        testProjet = projetRepository.save(testProjet);

        User pp = createUser("pp_" + uid + "@leoni.com", Role.PP, 60001);
        User pt = createUser("pt_" + uid + "@leoni.com", Role.PT, 60002);
        User ing = createUser("ing_" + uid + "@leoni.com", Role.ING, 60003);

        ppToken = jwtService.generateToken(pp);
        ptToken = jwtService.generateToken(pt);
        ingToken = jwtService.generateToken(ing);

        saveToken(pp, ppToken);
        saveToken(pt, ptToken);
        saveToken(ing, ingToken);

        testSheet = ChargeSheet.builder()
                .plant(testSite.getName())
                .project(testProjet.getName())
                .orderNumber("ORD-IMG-" + uid)
                .status(ChargeSheetStatus.VALIDATED_PT)
                .createdBy(pp.getEmail())
                .createdAt(LocalDate.now())
                .build();
        testSheet = chargeSheetRepository.save(testSheet);

        testItem = ChargeSheetItem.builder()
                .chargeSheet(testSheet)
                .itemNumber("ITEM-001")
                .quantityOfTestModules(5)
                .build();
        testItem = chargeSheetItemRepository.save(testItem);

        itemWithImage = ChargeSheetItem.builder()
                .chargeSheet(testSheet)
                .itemNumber("ITEM-002")
                .quantityOfTestModules(3)
                .realConnectorPicture("uploads/charge-sheets/existing-image.jpg")
                .build();
        itemWithImage = chargeSheetItemRepository.save(itemWithImage);

        testClaim = Claim.builder()
                .chargeSheetId(testSheet.getId())
                .title("Claim with image")
                .description("Claim description")
                .priority(Claim.Priority.HIGH)
                .category("TEST")
                .status(Claim.ClaimStatus.ASSIGNED)
                .reportedBy(pp.getEmail())
                .reportedDate(LocalDate.now())
                .assignedTo(pt.getEmail())
                .createdBy(pp.getEmail())
                .createdAt(LocalDate.now())
                .plant(testSite.getName())
                .imagePath("uploads/claims/existing-claim.jpg")
                .build();
        testClaim = claimRepository.save(testClaim);

        // Default mocks for image service
        when(imageStorageService.saveImage(any(), anyString()))
                .thenReturn("uploads/charge-sheets/new-image-uuid.jpg");
        when(imageStorageService.getImage(anyString()))
                .thenReturn("fake-image-bytes".getBytes());
    }

    private User createUser(String email, Role role, int matricule) {
        Set<Projet> projets = new HashSet<>();
        projets.add(testProjet);
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("pass123"))
                .firstname("Test").lastname("User")
                .matricule(matricule)
                .role(role)
                .site(testSite).projets(projets)
                .build();
        return userRepository.save(user);
    }

    private void saveToken(User user, String token) {
        tokenRepository.deleteAll(tokenRepository.findByUser(user));
        Token t = Token.builder()
                .token(token).tokenType(TokenType.BEARER)
                .expired(false).revoked(false).user(user)
                .build();
        tokenRepository.save(t);
    }





    @Test
    void uploadItemImage_WithWrongSheetId_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/charge-sheets/99999"
                        + "/items/" + testItem.getId() + "/upload-image")
                        .file(file)
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void uploadItemImage_WithoutToken_ShouldReturnForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/charge-sheets/" + testSheet.getId()
                        + "/items/" + testItem.getId() + "/upload-image")
                        .file(file))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /{sheetId}/items/{itemId}/image ====================

    @Test
    void getItemImage_WithExistingImage_ShouldReturnImageBytes() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets/" + testSheet.getId()
                        + "/items/" + itemWithImage.getId() + "/image")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk())
                .andExpect(content().bytes("fake-image-bytes".getBytes()));
    }

    @Test
    void getItemImage_WithNoImage_ShouldReturnNotFound() throws Exception {
        // testItem has no image set
        mockMvc.perform(get("/api/v1/charge-sheets/" + testSheet.getId()
                        + "/items/" + testItem.getId() + "/image")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemImage_WithWrongItemId_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets/" + testSheet.getId()
                        + "/items/99999/image")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().is5xxServerError());
    }

    // ==================== GET /{id}/image (Claim image) ====================

    @Test
    void getClaimImage_WithExistingImage_ShouldReturnImageBytes() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets/" + testClaim.getId() + "/image")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().isOk());
    }

    @Test
    void getClaimImage_WithNonExistentClaim_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/v1/charge-sheets/99999/image")
                        .header("Authorization", "Bearer " + ppToken))
                .andExpect(status().is5xxServerError());
    }

    // ==================== DELETE /{sheetId}/items/{itemId}/image ====================
    @Test
    void deleteItemImage_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/charge-sheets/" + testSheet.getId()
                        + "/items/" + itemWithImage.getId() + "/image"))
                .andExpect(status().isForbidden());
    }
    @Test
    void uploadItemImage_WithValidToken_ShouldReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/charge-sheets/" + testSheet.getId()
                        + "/items/" + testItem.getId() + "/upload-image")
                        .file(file)
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").exists());
    }
    @Test
    void deleteItemImage_WithExistingImage_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/charge-sheets/" + testSheet.getId()
                        + "/items/" + itemWithImage.getId() + "/image")
                        .header("Authorization", "Bearer " + ingToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }
}