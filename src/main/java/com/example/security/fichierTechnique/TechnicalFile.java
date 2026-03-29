package com.example.security.fichierTechnique;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "technical_file")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TechnicalFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", length = 100)
    private String reference;
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;
    @JsonIgnore
    @OneToMany(mappedBy = "technicalFile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TechnicalFileItem> technicalFileItems = new ArrayList<>();

    // getters setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }

    public void setReference(String reference) { this.reference = reference; }

    public List<TechnicalFileItem> getTechnicalFileItems() {
        return technicalFileItems;
    }

    public void setTechnicalFileItems(List<TechnicalFileItem> technicalFileItems) {
        this.technicalFileItems = technicalFileItems;
    }
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addTechnicalFileItem(TechnicalFileItem item) {
        technicalFileItems.add(item);
        item.setTechnicalFile(this);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TechnicalFile that = (TechnicalFile) obj;
        return Objects.equals(id, that.id);
    }
}