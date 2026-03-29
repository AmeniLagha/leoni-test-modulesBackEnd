package com.example.security.fichierTechnique.TechnicalFileHistory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "technical_file_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalFileHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long technicalFileId;

    @Column(name = "technical_file_item_id")  // ✅ NOUVEAU CHAMP
    private Long technicalFileItemId;
    private String fieldName;
    private String oldValue;
    private String newValue;

    private String modifiedBy;
    private LocalDate modifiedAt;
}
