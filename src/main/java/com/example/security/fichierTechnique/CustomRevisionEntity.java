package com.example.security.fichierTechnique;

import lombok.Setter;
import lombok.Getter;
import org.hibernate.envers.RevisionEntity;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "revinfo")
@RevisionEntity(CustomRevisionListener.class)
@Getter
@Setter
public class CustomRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    private long timestamp;

    private String username; // ⭐ QUI MODIFIE
}