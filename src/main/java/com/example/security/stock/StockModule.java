package com.example.security.stock;

import com.example.security.fichierTechnique.TechnicalFile;
import com.example.security.fichierTechnique.TechnicalFileItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "stock_module")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation avec le dossier technique
    @ManyToOne
    @JoinColumn(name = "technical_file_id")
    private TechnicalFile technicalFile;

    // Relation avec l'item spécifique
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "technical_file_item_id")
    private TechnicalFileItem technicalFileItem;

    // Informations sur l'item
    @Column(name = "charge_sheet_item_id")
    private Long chargeSheetItemId;

    @Column(name = "item_number")
    private String itemNumber;

    @Column(name = "position")
    private String position;

    // Valeurs finales
    @Column(name = "final_displacement")
    private Double finalDisplacement;

    @Column(name = "final_programmed_sealing")
    private Double finalProgrammedSealing;

    @Column(name = "final_detection", length = 20)
    private String finalDetection;

    // Métadonnées
    private String movedBy;
    private LocalDate movedAt;

    @Enumerated(EnumType.STRING)
    private StockStatus status;

    public enum StockStatus {
        AVAILABLE,
        USED,
        SCRAPPED
    }
}