package com.example.security.cahierdeCharge;

import com.example.security.reclamation.Claim;
import com.example.security.reclamation.ClaimRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des images associées aux cahiers des charges et réclamations.
 * <p>
 * Ce contrôleur expose les endpoints permettant d'uploader, consulter et supprimer
 * les images des items techniques (connecteurs) et des réclamations.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li><strong>Upload d'image</strong> : Ajout d'une image pour un item de cahier des charges</li>
 *     <li><strong>Consultation d'image</strong> : Récupération et affichage d'une image associée à un item ou une réclamation</li>
 *     <li><strong>Suppression d'image</strong> : Suppression de l'image associée à un item</li>
 * </ul>
 *
 * <p><strong>Formats d'image supportés :</strong></p>
 * <ul>
 *     <li>JPEG/JPG</li>
 *     <li>PNG</li>
 *     <li>GIF</li>
 *     <li>BMP</li>
 *     <li>WEBP</li>
 * </ul>
 *
 * <p><strong>Sécurité et permissions :</strong></p>
 * <ul>
 *     <li>{@code charge_sheet:basic:write} : Upload et suppression d'images pour items</li>
 *     <li>{@code charge_sheet:tech:create} : Alternative pour upload/suppression</li>
 *     <li>{@code charge_sheet:all:read} : Consultation des images d'items</li>
 *     <li>{@code claim:read} : Consultation des images de réclamations</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheetService
 * @see ImageStorageService
 * @since Sprint 4
 */
@RestController
@RequestMapping("/api/v1/charge-sheets")
@Tag(name = "Gestion des images", description = "Upload, consultation et suppression des images des items")
@RequiredArgsConstructor
public class ChargeSheetImageController {

    private final ChargeSheetService chargeSheetService;
    private final ChargeSheetItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final ImageStorageService imageStorageService;
    /** Dossier de stockage pour les images des cahiers des charges. */
    private static final String CHARGE_SHEET_IMAGE_FOLDER = "charge-sheets";

    // ============================================================
    // ENDPOINTS - GESTION DES IMAGES POUR ITEMS DE CAHIER
    // ============================================================

    /**
     * Upload une image pour un item spécifique d'un cahier des charges.
     * <p>
     * Cette méthode permet d'ajouter une photographie illustrant un connecteur
     * (item technique). L'image est sauvegardée sur le disque et son chemin
     * est associé à l'item.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Vérification que l'item existe</li>
     *     <li>Vérification que l'item appartient bien au cahier spécifié</li>
     *     <li>Sauvegarde de l'image dans le dossier {@code charge-sheets/}</li>
     *     <li>Mise à jour de l'item avec le chemin de l'image</li>
     *     <li>Retour des informations de l'image uploadée</li>
     * </ol>
     *
     * @param sheetId L'identifiant du cahier des charges contenant l'item
     * @param itemId L'identifiant de l'item auquel associer l'image
     * @param file Le fichier image à uploader (JPEG, PNG, GIF, etc.)
     * @return ResponseEntity contenant les informations de l'image uploadée
     *         (nom du fichier, chemin, message de succès) ou une erreur
     *
     * @throws RuntimeException si l'item n'existe pas
     * @throws IOException en cas d'erreur lors de la sauvegarde du fichier
     */
    @PostMapping("/{sheetId}/items/{itemId}/upload-image")
    @Operation(
            summary = "Uploader une image",
            description = "Permet d’ajouter une image (ex: connecteur réel) à un item spécifique d’un cahier de charges"
    )
    @PreAuthorize("hasAuthority('charge_sheet:basic:write') or hasAuthority('charge_sheet:tech:create')")
    public ResponseEntity<Map<String, String>> uploadItemImage(
            @PathVariable Long sheetId,
            @PathVariable Long itemId,
            @RequestParam("file") MultipartFile file) {

        try {
            // Vérifier que l'item appartient bien au sheet
            ChargeSheetItem item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!item.getChargeSheet().getId().equals(sheetId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Item does not belong to this charge sheet"));
            }

            // Sauvegarder l'image dans le dossier spécifique "charge-sheets"
            String imagePath = imageStorageService.saveImage(file, CHARGE_SHEET_IMAGE_FOLDER);

            // Mettre à jour l'item avec le chemin de l'image
            item.setRealConnectorPicture(imagePath);
            itemRepository.save(item);

            Map<String, String> response = new HashMap<>();
            response.put("filename", imagePath.substring(imagePath.lastIndexOf("/") + 1));
            response.put("path", imagePath);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Récupère et affiche l'image associée à un item spécifique.
     * <p>
     * Cette méthode retourne l'image stockée pour un connecteur, avec les
     * headers HTTP appropriés (Content-Type, Content-Disposition, Content-Length).
     * </p>
     *
     * @param sheetId L'identifiant du cahier des charges contenant l'item
     * @param itemId L'identifiant de l'item dont on souhaite l'image
     * @return ResponseEntity contenant les données binaires de l'image avec
     *         les headers HTTP appropriés, ou 404 si l'image n'existe pas
     *
     * @throws IOException en cas d'erreur lors de la lecture du fichier
     */
    @GetMapping("/{sheetId}/items/{itemId}/image")
    @Operation(
            summary = "Afficher une image",
            description = "Récupérer et afficher l’image associée à un item"
    )
    @PreAuthorize("hasAuthority('charge_sheet:all:read')")
    public ResponseEntity<byte[]> getItemImage(
            @PathVariable Long sheetId,
            @PathVariable Long itemId) {
        try {
            ChargeSheetItem item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!item.getChargeSheet().getId().equals(sheetId)) {
                return ResponseEntity.notFound().build();
            }

            String imagePath = item.getRealConnectorPicture();

            if (imagePath == null || imagePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageData = imageStorageService.getImage(imagePath);

            // Déterminer le type MIME à partir de l'extension
            String contentType = "image/jpeg";
            String lowerPath = imagePath.toLowerCase();
            if (lowerPath.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerPath.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerPath.endsWith(".bmp")) {
                contentType = "image/bmp";
            } else if (lowerPath.endsWith(".webp")) {
                contentType = "image/webp";
            }

            // Extraire le nom du fichier pour le Content-Disposition
            String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.builder("inline").filename(filename).build());
            headers.setContentLength(imageData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    /**
     * Supprime l'image associée à un item spécifique.
     * <p>
     * Cette méthode supprime le fichier image du disque et met à jour
     * l'item en supprimant la référence au chemin de l'image.
     * </p>
     *
     * @param sheetId L'identifiant du cahier des charges contenant l'item
     * @param itemId L'identifiant de l'item dont on souhaite supprimer l'image
     * @return ResponseEntity confirmant la suppression ou une erreur
     *
     * @throws IOException en cas d'erreur lors de la suppression du fichier
     */
    @DeleteMapping("/{sheetId}/items/{itemId}/image")
    @Operation(
            summary = "Supprimer une image",
            description = "Supprimer l’image associée à un item d’un cahier de charges"
    )
    @PreAuthorize("hasAuthority('charge_sheet:basic:write') or hasAuthority('charge_sheet:tech:create')")
    public ResponseEntity<Map<String, String>> deleteItemImage(
            @PathVariable Long sheetId,
            @PathVariable Long itemId) {
        try {
            ChargeSheetItem item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!item.getChargeSheet().getId().equals(sheetId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Item does not belong to this charge sheet"));
            }

            String imagePath = item.getRealConnectorPicture();
            if (imagePath != null && !imagePath.isEmpty()) {
                imageStorageService.deleteImage(imagePath);
                item.setRealConnectorPicture(null);
                itemRepository.save(item);
            }

            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }
    // ============================================================
    // ENDPOINTS - GESTION DES IMAGES POUR RÉCLAMATIONS
    // ============================================================

    /**
     * Récupère et affiche l'image associée à une réclamation.
     * <p>
     * Cette méthode retourne l'image stockée pour une réclamation, avec les
     * headers HTTP appropriés (Content-Type, Content-Length).
     * </p>
     *
     * @param id L'identifiant de la réclamation dont on souhaite l'image
     * @return ResponseEntity contenant les données binaires de l'image avec
     *         les headers HTTP appropriés, ou 404 si l'image n'existe pas
     *
     * @throws IOException en cas d'erreur lors de la lecture du fichier
     */
    @GetMapping("/{id}/image")
    @Operation(summary = "Afficher image", description = "Afficher l’image d’une réclamation")
    @PreAuthorize("hasAuthority('claim:read')")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            Claim claim = claimRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            String imagePath = claim.getImagePath();
            System.out.println("🔍 Récupération image pour claim " + id + ": " + imagePath);

            if (imagePath == null || imagePath.isEmpty()) {
                System.out.println("⚠️ Aucun chemin d'image trouvé");
                return ResponseEntity.notFound().build();
            }

            byte[] imageData = imageStorageService.getImage(imagePath);

            String contentType = "image/jpeg";
            if (imagePath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (imagePath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);

        } catch (IOException e) {
            System.err.println("❌ Erreur récupération image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Erreur générale: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

}