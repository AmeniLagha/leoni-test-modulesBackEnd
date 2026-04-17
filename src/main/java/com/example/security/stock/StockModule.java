package com.example.security.stock;

import com.example.security.fichierTechnique.TechnicalFile;
import com.example.security.fichierTechnique.TechnicalFileItem;
import com.example.security.site.Site;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnore
    private TechnicalFile technicalFile;

    // Relation avec l'item spécifique
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "technical_file_item_id")
    private TechnicalFileItem technicalFileItem;

    // Informations sur l'item
    @Column(name = "charge_sheet_item_id")
    private Long chargeSheetItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    @JsonIgnoreProperties({"projets", "active", "description"})
    private Site site;

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
    @Column(name = "casier", length = 50)
    private String casier;           // Casier

    @Column(name = "stuff_numr", length = 100)
    private String stuffNumr;        // STUFF NUMR

    @Column(name = "leoni_numr", length = 100)
    private String leoniNumr;        // LEONI NUMR

    @Column(name = "index_value", length = 20)
    private String indexValue;       // INDEX

    @Column(name = "quantite")
    private Integer quantite;        // QUANTITE

    @Column(name = "fournisseur", length = 100)
    private String fournisseur;      // FOURNISSEUR

    @Column(name = "etat", length = 50)
    private String etat;             // ETAT (OK, NOK, etc.)

    @Column(name = "caisse", length = 50)
    private String caisse;           // CAISSE

    @Lob
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;   // Spécifications

    @Column(name = "dernier_maj")
    private LocalDate dernierMaj;    // DERNIER MAJ

    @Lob
    @Column(name = "info_sur_modules", columnDefinition = "TEXT")
    private String infoSurModules;   // Info sur modules

    @Lob
    @Column(name = "demandeur_explication", columnDefinition = "TEXT")
    private String demandeurExplication;  // Demandeur et explication

    @Column(name = "date_creation")
    private LocalDate dateDemande;  // Date

    @Column(name = "new_quantite")
    private Integer newQuantite;

    // StockModule.java - Ajouter ces getters/setters si manquants


    public Double getFinalDisplacement() {
        return finalDisplacement;
    }
    public Double getFinalProgrammedSealing() {
        return finalProgrammedSealing;
    }
    public String getFinalDetection() {
        return finalDetection;
    }
    public String getMovedBy() {
        return movedBy;
    }
    public LocalDate getMovedAt() {
        return movedAt;
    }

    public Long getChargeSheetItemId() {
        return chargeSheetItemId;
    }

    public TechnicalFileItem getTechnicalFileItem() {
        return technicalFileItem;
    }


    public String getCasier() { return casier; }
    public void setCasier(String casier) { this.casier = casier; }

    public String getStuffNumr() { return stuffNumr; }
    public void setStuffNumr(String stuffNumr) { this.stuffNumr = stuffNumr; }

    public String getLeoniNumr() { return leoniNumr; }
    public void setLeoniNumr(String leoniNumr) { this.leoniNumr = leoniNumr; }

    public String getIndexValue() { return indexValue; }
    public void setIndexValue(String indexValue) { this.indexValue = indexValue; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getCaisse() { return caisse; }
    public void setCaisse(String caisse) { this.caisse = caisse; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public LocalDate getDernierMaj() { return dernierMaj; }
    public void setDernierMaj(LocalDate dernierMaj) { this.dernierMaj = dernierMaj; }

    public String getInfoSurModules() { return infoSurModules; }
    public void setInfoSurModules(String infoSurModules) { this.infoSurModules = infoSurModules; }
// StockModule.java - Ajouter ces méthodes

    public Long getTechnicalFileId() {
        return technicalFile != null ? technicalFile.getId() : null;
    }

    public Long getTechnicalFileItemId() {
        return technicalFileItem != null ? technicalFileItem.getId() : null;
    }
    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }



    public Long getSiteId() {
        return site != null ? site.getId() : null;
    }

    public String getSiteName() {
        return site != null ? site.getName() : null;
    }

}