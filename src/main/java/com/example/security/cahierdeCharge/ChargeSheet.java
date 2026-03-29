package com.example.security.cahierdeCharge;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "charge_sheet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === INFORMATIONS GÉNÉRALES ===
    @Column(name = "plant", length = 50)
    private String plant;

    @Column(name = "project", length = 50)
    private String project;

    @Column(name = "harness_ref", length = 50)
    private String harnessRef;

    @Column(name = "issued_by", length = 100)
    private String issuedBy;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @Column(name = "cost_center_number", length = 50)
    private String costCenterNumber;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "preferred_delivery_date")
    private LocalDate preferredDeliveryDate;

    // === RELATION AVEC LES ITEMS ===
    @OneToMany(mappedBy = "chargeSheet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChargeSheetItem> items = new ArrayList<>();

    // === CHAMPS DE STATUT ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ChargeSheetStatus status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    // Méthodes utilitaires
    public void addItem(ChargeSheetItem item) {
        items.add(item);
        item.setChargeSheet(this);
    }

    public void removeItem(ChargeSheetItem item) {
        items.remove(item);
        item.setChargeSheet(null);
    }
}