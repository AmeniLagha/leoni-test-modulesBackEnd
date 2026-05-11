package com.example.security.cahierdeCharge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Dossier d'upload créé: " + uploadPath.toAbsolutePath());
            }
            System.out.println("📁 Dossier d'upload configuré: " + uploadPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Erreur création dossier upload: " + e.getMessage());
        }
    }

    /**
     * Sauvegarde une image - Fonctionne en DEV et PROD
     */
    public String saveImage(MultipartFile file, String subFolder) throws IOException {
        // Créer le dossier spécifique
        Path uploadPath = Paths.get(uploadDir, subFolder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        // ✅ Retourner le chemin RELATIF (sans uploadDir) - fonctionne partout
        return subFolder + "/" + filename;
    }

    /**
     * Récupère une image - Fonctionne en DEV et PROD
     */
    public byte[] getImage(String storedPath) throws IOException {
        // storedPath est comme "charge-sheets/uuid.jpg"

        // Nettoyer le chemin (supprimer les préfixes indésirables)
        String cleanPath = storedPath;

        // Supprimer "uploads/" si présent (anciennes données)
        if (cleanPath.startsWith("uploads/")) {
            cleanPath = cleanPath.substring(8);
        }

        // Supprimer "./" si présent
        if (cleanPath.startsWith("./")) {
            cleanPath = cleanPath.substring(2);
        }

        // Supprimer "/app/uploads/" si présent (pour les cas particuliers)
        if (cleanPath.startsWith("/app/uploads/")) {
            cleanPath = cleanPath.substring(12);
        }

        // Construire le chemin complet
        Path filePath = Paths.get(uploadDir, cleanPath);

        // Logs de débogage (utiles pour identifier les problèmes)
        if (System.getProperty("spring.profiles.active", "").contains("docker") ||
                System.getenv("SPRING_PROFILES_ACTIVE") != null) {
            System.out.println("📸 [DEBUG] storedPath: " + storedPath);
            System.out.println("📸 [DEBUG] cleanPath: " + cleanPath);
            System.out.println("📸 [DEBUG] uploadDir: " + uploadDir);
            System.out.println("📸 [DEBUG] fullPath: " + filePath.toAbsolutePath());
            System.out.println("📸 [DEBUG] fileExists: " + Files.exists(filePath));
        }

        if (!Files.exists(filePath)) {
            throw new IOException("Image non trouvée: " + filePath.toAbsolutePath());
        }

        return Files.readAllBytes(filePath);
    }

    /**
     * Supprime une image
     */
    public void deleteImage(String storedPath) throws IOException {
        if (storedPath != null && !storedPath.isEmpty()) {
            String cleanPath = storedPath;
            if (cleanPath.startsWith("uploads/")) {
                cleanPath = cleanPath.substring(8);
            }
            Path filePath = Paths.get(uploadDir, cleanPath);
            Files.deleteIfExists(filePath);
        }
    }

    /**
     * Récupère une image depuis un dossier spécifique
     */
    public byte[] getImageFromFolder(String subFolder, String filename) throws IOException {
        Path filePath = Paths.get(uploadDir, subFolder, filename);
        return Files.readAllBytes(filePath);
    }
}