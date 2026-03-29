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

    /**
     * Récupère une image depuis un chemin complet
     */
    public byte[] getImage(String fullPath) throws IOException {
        Path filePath = Paths.get(fullPath);
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