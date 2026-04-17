package com.example.security.reclamation;

import com.example.security.cahierdeCharge.ChargeSheet;
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

    // ========== NOUVEAUX CHAMPS POUR LE FORMULAIRE RÉCLAMATION ==========

    // === INFORMATIONS PLANT ===
    @Column(name = "plant", length = 100)
    private String plant;  // Ex: "Manzel Hayet MH1"

    // === INFORMATIONS CLIENT ===
    @Column(name = "customer", length = 100)
    private String customer;  // Ex: "Ahmed Bouhlel"

    @Column(name = "contact_person", length = 100)
    private String contactPerson;  // Ex: "Kaouther Aifa"

    @Column(name = "customer_email", length = 100)
    private String customerEmail;  // Ex: "Ahmed.Bouhlel@leoni.com"

    @Column(name = "customer_phone", length = 50)
    private String customerPhone;  // Ex: "20644955"

    // === INFORMATIONS FOURNISSEUR ===
    @Column(name = "supplier", length = 100)
    private String supplier;  // Ex: "EMDEP"

    @Column(name = "supplier_contact_person", length = 100)
    private String supplierContactPerson;

    // === INFORMATIONS COMMANDE ===
    @Column(name = "order_number", length = 100)
    private String orderNumber;  // Ex: "2023-5320 / / 2024-1989"

    @Column(name = "test_module_number", length = 100)
    private String testModuleNumber;

    @Column(name = "test_module_quantity")
    private Integer testModuleQuantity;  // Ex: 30

    // === SIGNATURE ===
    @Column(name = "ppo_signature", length = 100)
    private String ppoSignature;  // Ex: "Ahmed Bouhlel"

    // === DESCRIPTION DU PROBLÈME ===
    @Column(name = "problem_what_happened", columnDefinition = "TEXT")
    private String problemWhatHappened;  // Ex: "insertion problem"

    @Column(name = "problem_why", columnDefinition = "TEXT")
    private String problemWhy;  // Ex: "can't detect clips"

    @Column(name = "problem_when_detected", length = 200)
    private String problemWhenDetected;  // Ex: "conformity test"

    @Column(name = "problem_who_detected", length = 100)
    private String problemWhoDetected;  // Ex: "Leoni technician"

    @Column(name = "problem_where_detected", length = 200)
    private String problemWhereDetected;  // Ex: "in the test table"

    @Column(name = "problem_how_detected", columnDefinition = "TEXT")
    private String problemHowDetected;  // Ex: "During the conformity test"

    // === DATE ===
    @Column(name = "claim_date")
    private LocalDate claimDate;  // Ex: "4/26/2024"

    // === INFORMATIONS DE LA RÉCLAMATION (existants) ===
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "priority", length = 20)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "category", length = 50)
    private String category;

    // === ÉTAT DE LA RÉCLAMATION ===
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    // === INFORMATIONS DU SIGNALEMENT ===
    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "reported_date")
    private LocalDate reportedDate;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

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
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ClaimStatus {
        NEW, IN_PROGRESS, ASSIGNED, ON_HOLD, RESOLVED, CLOSED, REJECTED
    }
}