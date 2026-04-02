package com.example.security.cahierdeCharge;

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

@RestController
@RequestMapping("/api/v1/charge-sheets")
@Tag(name = "Gestion des images", description = "Upload, consultation et suppression des images des items")
@RequiredArgsConstructor
public class ChargeSheetImageController {

    private final ChargeSheetService chargeSheetService;
    private final ChargeSheetItemRepository itemRepository;
    private final ImageStorageService imageStorageService;

    private static final String CHARGE_SHEET_IMAGE_FOLDER = "charge-sheets";

    /**
     * Upload une image pour un item spécifique
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
     * Récupère l'image d'un item spécifique
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
     * Supprime l'image d'un item spécifique
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
}