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

/**
 * Service pour la gestion du stockage des images sur disque.
 * <p>
 * Cette classe fournit les opérations nécessaires pour sauvegarder, récupérer
 * et supprimer des images associées aux différents modules de l'application
 * (cahiers des charges, réclamations, etc.).
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Sauvegarde d'images avec génération de noms de fichiers uniques (UUID)</li>
 *     <li>Récupération d'images pour affichage (bytes)</li>
 *     <li>Suppression d'images</li>
 *     <li>Organisation en sous-dossiers par entité (charge-sheets, claims, etc.)</li>
 *     <li>Nettoyage automatique des chemins pour compatibilité DEV et PROD</li>
 * </ul>
 *
 * <p><strong>Configuration :</strong></p>
 * <ul>
 *     <li>Le dossier racine d'upload est configurable via {@code file.upload-dir} (défaut: "uploads")</li>
 *     <li>Les images sont stockées dans des sous-dossiers comme {@code charge-sheets/}, {@code claims/}</li>
 *     <li>Le service crée automatiquement les dossiers manquants au démarrage</li>
 * </ul>
 *
 * <p><strong>Formats d'image supportés :</strong></p>
 * <ul>
 *     <li>JPEG/JPG (.jpg, .jpeg)</li>
 *     <li>PNG (.png)</li>
 *     <li>GIF (.gif)</li>
 *     <li>BMP (.bmp)</li>
 *     <li>WEBP (.webp)</li>
 * </ul>
 *
 * <p><strong>Compatibilité :</strong>
 * Le service fonctionne aussi bien en développement local que dans les environnements
 * de production (Docker, Render, Clever Cloud). Les chemins sont automatiquement
 * nettoyés pour s'adapter aux différents contextes.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheetImageController
 * @see com.example.security.reclamation.ClaimController
 * @since Sprint 4
 */
@Service
public class ImageStorageService {
    /**
     * Répertoire racine de stockage des images.
     * <p>
     * Configurable via la propriété {@code file.upload-dir} dans
     * {@code application.properties} ou {@code application.yml}.
     * Valeur par défaut : "uploads".
     * </p>
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *     <li>DEV local : "uploads"</li>
     *     <li>Docker : "/app/uploads"</li>
     *     <li>Clever Cloud : "./uploads"</li>
     * </ul>
     */
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    /**
     * Initialise le service au démarrage de l'application.
     * <p>
     * Cette méthode est exécutée automatiquement après l'injection des dépendances.
     * Elle crée le dossier racine d'upload s'il n'existe pas.
     * </p>
     *
     * <p><strong>Actions effectuées :</strong></p>
     * <ul>
     *     <li>Vérifie l'existence du dossier d'upload</li>
     *     <li>Crée le dossier et ses parents si nécessaire</li>
     *     <li>Affiche des logs pour le débogage</li>
     * </ul>
     */
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
     * Sauvegarde une image sur le disque.
     * <p>
     * Cette méthode génère un nom de fichier unique (UUID) tout en conservant
     * l'extension d'origine. L'image est stockée dans un sous-dossier spécifié.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Crée le sous-dossier s'il n'existe pas</li>
     *     <li>Extrait l'extension du fichier original</li>
     *     <li>Génère un nom unique avec UUID</li>
     *     <li>Sauvegarde le fichier sur le disque</li>
     *     <li>Retourne le chemin relatif (sous-dossier + nom)</li>
     * </ol>
     *
     * @param file Le fichier image à sauvegarder (MultipartFile)
     * @param subFolder Le nom du sous-dossier (ex: "charge-sheets", "claims")
     * @return Le chemin relatif de l'image sauvegardée (ex: "charge-sheets/uuid.jpg")
     * @throws IOException En cas d'erreur d'écriture du fichier ou de création du dossier
     */
  /*  public String saveImage(MultipartFile file, String subFolder) throws IOException {
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
    }*/
    // ✅ Sauvegarder sur le bucket aussi
public String saveImage(MultipartFile file, String subFolder) throws IOException {
    String filename = UUID.randomUUID().toString() + getExtension(file);
    String relativePath = subFolder + "/" + filename;
    
    if (bucketHost != null && !bucketHost.isEmpty()) {
        // ✅ Sauvegarder sur le bucket
        String bucketUrl = "https://" + bucketHost + "/" + relativePath;
        // Upload via HTTP PUT ou FTP
        // ...
    } else {
        // Sauvegarde locale
        Path uploadPath = Paths.get(uploadDir, subFolder);
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
    }
    
    return relativePath;
}

    /**
     * Récupère une image à partir de son chemin stocké.
     * <p>
     * Cette méthode nettoie automatiquement le chemin pour gérer les différents
     * formats de stockage (chemins relatifs, absolus, préfixes indésirables).
     * Elle retourne les données binaires de l'image pour affichage.
     * </p>
     *
     * @param storedPath Le chemin stocké en base de données
     * @return Les données binaires de l'image (byte array)
     * @throws IOException Si l'image n'existe pas ou ne peut pas être lue
     */
  /*  public byte[] getImage(String storedPath) throws IOException {
        if (storedPath == null || storedPath.isEmpty()) {
            throw new IOException("Chemin d'image vide");
        }

        // Nettoyer le chemin (supprimer les préfixes indésirables)
        String cleanPath = storedPath;

        // ✅ 1. Supprimer "/app/uploads/" (chemin absolu Docker)
        if (cleanPath.startsWith("/app/uploads/")) {
            cleanPath = cleanPath.substring(12);
            System.out.println("📸 [Nettoyage] Supprimé /app/uploads/ → " + cleanPath);
        }

        // ✅ 2. Supprimer "uploads/" au début (anciennes données)
        if (cleanPath.startsWith("uploads/")) {
            cleanPath = cleanPath.substring(8);
            System.out.println("📸 [Nettoyage] Supprimé uploads/ → " + cleanPath);
        }

        // ✅ 3. Supprimer "./" si présent
        if (cleanPath.startsWith("./")) {
            cleanPath = cleanPath.substring(2);
            System.out.println("📸 [Nettoyage] Supprimé ./ → " + cleanPath);
        }

        // ✅ 4. Si le chemin commence encore par "claims/", c'est bon, sinon ajouter "claims/"
        // (Pour les cas où on a juste le nom du fichier)
        if (!cleanPath.startsWith("claims/") && !cleanPath.startsWith("charge-sheets/")) {
            // Si le chemin ressemble à un UUID avec extension, c'est probablement une image de claim
            if (cleanPath.matches("[a-f0-9-]+\\.(jpg|jpeg|png|gif|webp)$")) {
                cleanPath = "claims/" + cleanPath;
                System.out.println("📸 [Nettoyage] Ajouté claims/ → " + cleanPath);
            }
        }

        // Construire le chemin complet
        Path filePath = Paths.get(uploadDir, cleanPath);

        // Logs de débogage
        System.out.println("📸 [DEBUG] storedPath: " + storedPath);
        System.out.println("📸 [DEBUG] cleanPath: " + cleanPath);
        System.out.println("📸 [DEBUG] uploadDir: " + uploadDir);
        System.out.println("📸 [DEBUG] fullPath: " + filePath.toAbsolutePath());
        System.out.println("📸 [DEBUG] fileExists: " + Files.exists(filePath));

        if (!Files.exists(filePath)) {
            throw new IOException("Image non trouvée: " + filePath.toAbsolutePath());
        }

        return Files.readAllBytes(filePath);
    }*/

    /**
     * Supprime une image du disque.
     * <p>
     * Cette méthode supprime le fichier image si son chemin n'est pas nul ou vide.
     * Elle nettoie automatiquement le chemin avant suppression.
     * </p>
     *
     * @param storedPath Le chemin de l'image à supprimer (peut être nul ou vide)
     * @throws IOException En cas d'erreur lors de la suppression du fichier
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
}
