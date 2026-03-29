package com.example.security.cahierdeCharge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reception_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "charge_sheet_id")
    @JsonIgnore
    private ChargeSheet chargeSheet;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ChargeSheetItem item;

    private int quantityReceived;
    private int previousTotalReceived;
    private int newTotalReceived;
    private int quantityOrdered;

    private String deliveryNoteNumber;
    private LocalDate receptionDate;
    private String receivedBy;
    private String comments;

    @Column(name = "created_at")
    private LocalDate createdAt;
}