package com.example.security.reclamation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "claim")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === LIEN AVEC LE CAHIER DES CHARGES ===
    @Column(name = "charge_sheet_id", nullable = false)
    private Long chargeSheetId;

    // === LIEN AVEC LA SECTION CONCERNÉE ===
    @Column(name = "related_to", length = 50)
    private String relatedTo; // "CHARGE_SHEET", "COMPLIANCE", "TECHNICAL_FILE"

    @Column(name = "related_id")
    private Long relatedId; // ID de la section concernée

    // === INFORMATIONS DE LA RÉCLAMATION ===
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    @Column(name = "image_path", length = 255)
    private String imagePath; // Chemin de l'image
    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "priority", length = 20)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "category", length = 50)
    private String category; // "TECHNICAL", "QUALITY", "LOGISTIC", "OTHER"

    // === ÉTAT DE LA RÉCLAMATION ===
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    // === INFORMATIONS DU SIGNALEMENT ===
    @Column(name = "reported_by", length = 100)
    private String reportedBy; // Email de l'utilisateur

    @Column(name = "reported_date")
    private LocalDate reportedDate;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo; // Email de la personne assignée

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    // === TRAITEMENT ===
    @Lob
    @Column(name = "action_taken")
    private String actionTaken;

    @Lob
    @Column(name = "resolution")
    private String resolution;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_date")
    private LocalDate resolvedDate;

    // === SUIVI ===
    @Column(name = "estimated_resolution_date")
    private LocalDate estimatedResolutionDate;

    @Column(name = "actual_resolution_date")
    private LocalDate actualResolutionDate;

    @Column(name = "closed_by", length = 100)
    private String closedBy;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    // === CHAMPS D'AUDIT ===
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    public enum Priority {
        LOW,        // Basse priorité
        MEDIUM,     // Priorité moyenne
        HIGH,       // Haute priorité
        CRITICAL    // Critique
    }

    public enum ClaimStatus {
        NEW,                // Nouvelle réclamation
        IN_PROGRESS,        // En cours de traitement
        ASSIGNED,           // Assignée à quelqu'un
        ON_HOLD,            // En attente
        RESOLVED,           // Résolue
        CLOSED,             // Fermée
        REJECTED            // Rejetée
    }
}