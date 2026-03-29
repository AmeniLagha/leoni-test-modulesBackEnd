package com.example.security.fichierTechnique;

import com.example.security.cahierdeCharge.ChargeSheetItem;
import com.example.security.stock.StockModule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "technical_file_item")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TechnicalFileItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @Column(name = "technician_name", length = 100)
    private String technicianName;

    @Column(name = "x_code", length = 50)
    private String xCode;
    // ✅ AJOUT DU CHAMP indexValue
    @Column(name = "index_value")
    private Integer indexValue;

    @Column(name = "leoni_reference_number", length = 100)
    private String leoniReferenceNumber;

    @Column(name = "producer", length = 10)
    private String producer;

    @Column(name = "type", length = 10)
    private String type;

    @Column(name = "reference_pine_push_back", length = 100)
    private String referencePinePushBack;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_sheet_item_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JsonIgnore
    private ChargeSheetItem chargeSheetItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_file_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JsonIgnore
    private TechnicalFile technicalFile;

    @Column(name = "position", length = 20)
    private String position;

    @Column(name = "pin_rigidity_m1", length = 20)
    private String pinRigidityM1;

    @Column(name = "pin_rigidity_m2", length = 20)
    private String pinRigidityM2;

    @Column(name = "pin_rigidity_m3", length = 20)
    private String pinRigidityM3;

    @Column(name = "displacement_path_m1", length = 20)
    private String displacementPathM1;

    @Column(name = "displacement_path_m2", length = 20)
    private String displacementPathM2;

    @Column(name = "displacement_path_m3", length = 20)
    private String displacementPathM3;

    @Column(name = "max_sealing_value_m1", length = 20)
    private String maxSealingValueM1;

    @Column(name = "max_sealing_value_m2", length = 20)
    private String maxSealingValueM2;

    @Column(name = "max_sealing_value_m3", length = 20)
    private String maxSealingValueM3;

    @Column(name = "programmed_sealing_value_m1", length = 20)
    private String programmedSealingValueM1;

    @Column(name = "programmed_sealing_value_m2", length = 20)
    private String programmedSealingValueM2;

    @Column(name = "programmed_sealing_value_m3", length = 20)
    private String programmedSealingValueM3;

    @Column(name = "detections_m1", length = 20)
    private String detectionsM1;

    @Column(name = "detections_m2", length = 20)
    private String detectionsM2;

    @Column(name = "detections_m3", length = 20)
    private String detectionsM3;

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
    @JsonIgnore
    @NotAudited
    @OneToMany(mappedBy = "technicalFileItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockModule> stockModules = new ArrayList<>();
// Dans TechnicalFileItem.java, ajoutez ces champs

    // ===== STATUT DE VALIDATION =====
    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status")
    private TechnicalFileItemStatus validationStatus = TechnicalFileItemStatus.DRAFT;

    // ===== TRACABILITÉ DES VALIDATIONS =====
    @Column(name = "validated_by_pp", length = 100)
    private String validatedByPp;

    @Column(name = "validated_at_pp")
    private LocalDateTime validatedAtPp;

    @Column(name = "validated_by_mc", length = 100)
    private String validatedByMc;

    @Column(name = "validated_at_mc")
    private LocalDateTime validatedAtMc;

    @Column(name = "validated_by_mp", length = 100)
    private String validatedByMp;

    @Column(name = "validated_at_mp")
    private LocalDateTime validatedAtMp;

    // ===== GETTERS ET SETTERS =====
    public TechnicalFileItemStatus getValidationStatus() { return validationStatus; }
    public void setValidationStatus(TechnicalFileItemStatus validationStatus) { this.validationStatus = validationStatus; }

    public String getValidatedByPp() { return validatedByPp; }
    public void setValidatedByPp(String validatedByPp) { this.validatedByPp = validatedByPp; }

    public LocalDateTime getValidatedAtPp() { return validatedAtPp; }
    public void setValidatedAtPp(LocalDateTime validatedAtPp) { this.validatedAtPp = validatedAtPp; }

    public String getValidatedByMc() { return validatedByMc; }
    public void setValidatedByMc(String validatedByMc) { this.validatedByMc = validatedByMc; }

    public LocalDateTime getValidatedAtMc() { return validatedAtMc; }
    public void setValidatedAtMc(LocalDateTime validatedAtMc) { this.validatedAtMc = validatedAtMc; }

    public String getValidatedByMp() { return validatedByMp; }
    public void setValidatedByMp(String validatedByMp) { this.validatedByMp = validatedByMp; }

    public LocalDateTime getValidatedAtMp() { return validatedAtMp; }
    public void setValidatedAtMp(LocalDateTime validatedAtMp) { this.validatedAtMp = validatedAtMp; }
    // Getter et Setter

    public LocalDate getMaintenanceDate() {
        return maintenanceDate;
    }
    public void setMaintenanceDate(LocalDate maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }
    public String getTechnicianName() {
        return technicianName;
    }
    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }
    public String getXCode() {
        return xCode;
    }
    public void setXCode(String xCode) {
        this.xCode = xCode;
    }
    public String getLeoniReferenceNumber() {
        return leoniReferenceNumber;
    }
    public void setLeoniReferenceNumber(String leoniReferenceNumber) {
        this.leoniReferenceNumber = leoniReferenceNumber;

    }
    public String getProducer() {
        return producer;
    }
    public void setProducer(String producer) {
        this.producer = producer;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getReferencePinePushBack() {
        return referencePinePushBack;
    }
    public void setReferencePinePushBack(String referencePinePushBack) {
        this.referencePinePushBack = referencePinePushBack;
    }


    public List<StockModule> getStockModules() {
        return stockModules;
    }

    public void setStockModules(List<StockModule> stockModules) {
        this.stockModules = stockModules;
    }
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ChargeSheetItem getChargeSheetItem() { return chargeSheetItem; }
    public void setChargeSheetItem(ChargeSheetItem chargeSheetItem) { this.chargeSheetItem = chargeSheetItem; }

    public TechnicalFile getTechnicalFile() { return technicalFile; }
    public void setTechnicalFile(TechnicalFile technicalFile) { this.technicalFile = technicalFile; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getPinRigidityM1() { return pinRigidityM1; }
    public void setPinRigidityM1(String pinRigidityM1) { this.pinRigidityM1 = pinRigidityM1; }

    public String getPinRigidityM2() { return pinRigidityM2; }
    public void setPinRigidityM2(String pinRigidityM2) { this.pinRigidityM2 = pinRigidityM2; }

    public String getPinRigidityM3() { return pinRigidityM3; }
    public void setPinRigidityM3(String pinRigidityM3) { this.pinRigidityM3 = pinRigidityM3; }

    public String getDisplacementPathM1() { return displacementPathM1; }
    public void setDisplacementPathM1(String displacementPathM1) { this.displacementPathM1 = displacementPathM1; }

    public String getDisplacementPathM2() { return displacementPathM2; }
    public void setDisplacementPathM2(String displacementPathM2) { this.displacementPathM2 = displacementPathM2; }

    public String getDisplacementPathM3() { return displacementPathM3; }
    public void setDisplacementPathM3(String displacementPathM3) { this.displacementPathM3 = displacementPathM3; }

    public String getMaxSealingValueM1() { return maxSealingValueM1; }
    public void setMaxSealingValueM1(String maxSealingValueM1) { this.maxSealingValueM1 = maxSealingValueM1; }

    public String getMaxSealingValueM2() { return maxSealingValueM2; }
    public void setMaxSealingValueM2(String maxSealingValueM2) { this.maxSealingValueM2 = maxSealingValueM2; }

    public String getMaxSealingValueM3() { return maxSealingValueM3; }
    public void setMaxSealingValueM3(String maxSealingValueM3) { this.maxSealingValueM3 = maxSealingValueM3; }

    public String getProgrammedSealingValueM1() { return programmedSealingValueM1; }
    public void setProgrammedSealingValueM1(String programmedSealingValueM1) { this.programmedSealingValueM1 = programmedSealingValueM1; }

    public String getProgrammedSealingValueM2() { return programmedSealingValueM2; }
    public void setProgrammedSealingValueM2(String programmedSealingValueM2) { this.programmedSealingValueM2 = programmedSealingValueM2; }

    public String getProgrammedSealingValueM3() { return programmedSealingValueM3; }
    public void setProgrammedSealingValueM3(String programmedSealingValueM3) { this.programmedSealingValueM3 = programmedSealingValueM3; }

    public String getDetectionsM1() { return detectionsM1; }
    public void setDetectionsM1(String detectionsM1) { this.detectionsM1 = detectionsM1; }

    public String getDetectionsM2() { return detectionsM2; }
    public void setDetectionsM2(String detectionsM2) { this.detectionsM2 = detectionsM2; }

    public String getDetectionsM3() { return detectionsM3; }
    public void setDetectionsM3(String detectionsM3) { this.detectionsM3 = detectionsM3; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }

    // ✅ Méthodes utilitaires
    public Long getChargeSheetItemId() {
        return chargeSheetItem != null ? chargeSheetItem.getId() : null;
    }

    public Long getTechnicalFileId() {
        return technicalFile != null ? technicalFile.getId() : null;
    }

    public String getItemNumber() {
        return chargeSheetItem != null ? chargeSheetItem.getItemNumber() : null;
    }
    // ✅ GETTER/SETTER POUR indexValue
    public Integer getIndexValue() { return indexValue; }
    public void setIndexValue(Integer indexValue) { this.indexValue = indexValue; }
    // ✅ hashCode basé uniquement sur l'ID
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ✅ equals basé uniquement sur l'ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TechnicalFileItem that = (TechnicalFileItem) obj;
        return Objects.equals(id, that.id);
    }
}