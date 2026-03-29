package com.example.security.conformité;

import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "compliance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compliance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === LIEN AVEC LE CAHIER DES CHARGES ===
    @Column(name = "charge_sheet_id", nullable = false)
    private Long chargeSheetId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_sheet_item_id", nullable = false)
    @JsonIgnore
    private ChargeSheetItem item;

    // === GENERAL ===
    @Column(name = "order_number", length = 50)
    private String orderNumber;


    @Column(name = "order_itemnumber", length = 50)
    private String orderitemNumber;

    @Column(name = "test_date_time")
    private LocalDate testDateTime;

    @Column(name = "technician_name", length = 100)
    private String technicianName;

    @Column(name = "rfid_number", length = 50)
    private String rfidNumber;

    // === IDENTITY CODE ===
    @Column(name = "leoni_part_number", length = 100)
    private String leoniPartNumber;

    @Column(name = "index_value")
    private Integer indexValue;

    @Column(name = "producer", length = 10)
    private String producer; // N/T

    @Column(name = "type", length = 10)
    private String type; // t/f

    // === INSPECTION ===
    @Column(name = "sequence_test_pins", length = 10)
    private String sequenceTestPins; // OK/NOK

    @Column(name = "coding_request", length = 10)
    private String codingRequest; // OK/NOK

    @Column(name = "secondary_locking", length = 10)
    private String secondaryLocking; // OK/NOK

    @Column(name = "offset_test_mm")
    private Double offsetTestMm;

    @Column(name = "stable_offset_test_mm")
    private Double stableOffsetTestMm;

    @Column(name = "displacement_path_push_back_mm")
    private Double displacementPathPushBackMm;

    @Column(name = "housing_attachments", length = 10)
    private String housingAttachments; // OK/NOK

    @Column(name = "max_leak_test_mbar")
    private Double maxLeakTestMbar;

    @Column(name = "adjustment_leak_test_mbar")
    private Double adjustmentLeakTestMbar;

    @Column(name = "colour_verification", length = 10)
    private String colourVerification; // OK/NOK

    @Column(name = "terminal_alignment", length = 10)
    private String terminalAlignment; // OK/NOK

    @Column(name = "open_shunts_airbag", length = 10)
    private String openShuntsAirbag; // OK/NOK

    @Column(name = "spacer_closing_unit", length = 10)
    private String spacerClosingUnit; // OK/NOK

    @Column(name = "special_functions", length = 10)
    private String specialFunctions; // OK/NOK

    @Column(name = "contact_problems_percentage")
    private Double contactProblemsPercentage;

    // === QUALIFICATION RESULT ===
    @Column(name = "qualified_test_module")
    private Boolean qualifiedTestModule;

    @Column(name = "conditionally_qualified_test_module")
    private Boolean conditionallyQualifiedTestModule;

    @Column(name = "not_qualified_test_module")
    private Boolean notQualifiedTestModule;

    // === REMARKS ===
    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;
    // Ajoutez ces champs dans votre classe Compliance
    @Column(name = "reception_history_id")
    private Long receptionHistoryId;

    @Column(name = "delivery_note_number", length = 100)
    private String deliveryNoteNumber;

    @Column(name = "unit_number")
    private Integer unitNumber; // Numéro d'unité (1,2,3...)
}
