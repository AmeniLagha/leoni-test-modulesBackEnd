package com.example.security.cahierdeCharge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Sauvegarde une image dans un dossier spécifique
     * @param file Le fichier à sauvegarder
     * @param subFolder Le sous-dossier (ex: "claims", "charge-sheets", "technical-files")
     * @return Le chemin complet du fichier
     */
    public String saveImage(MultipartFile file, String subFolder) throws IOException {
        // Créer le chemin complet: uploads/claims/
        Path uploadPath = Paths.get(uploadDir, subFolder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        // Retourner le chemin relatif
        return uploadDir + "/" + subFolder + "/" + filename;
    }

    public byte[] getImage(String fullPath) throws IOException {
        // fullPath doit être comme "claims/filename.jpg"
        // Nettoyer le chemin au cas où
        String cleanPath = fullPath;
        if (cleanPath.startsWith("./")) {
            cleanPath = cleanPath.substring(2);
        }
        if (cleanPath.startsWith("uploads/")) {
            cleanPath = cleanPath.substring(8); // enlever "uploads/"
        }

        // Construire le chemin complet
        Path filePath = Paths.get(uploadDir, cleanPath);

        System.out.println("=== DÉBOGAGE IMAGE ===");
        System.out.println("fullPath original: " + fullPath);
        System.out.println("cleanPath: " + cleanPath);
        System.out.println("Chemin complet: " + filePath.toAbsolutePath());
        System.out.println("Fichier existe: " + Files.exists(filePath));

        if (!Files.exists(filePath)) {
            throw new IOException("Fichier non trouvé: " + filePath.toAbsolutePath());
        }

        return Files.readAllBytes(filePath);
    }
    /**
     * Supprime une image
     */
    public void deleteImage(String fullPath) throws IOException {
        if (fullPath != null && !fullPath.isEmpty()) {
            Path filePath = Paths.get(fullPath);
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