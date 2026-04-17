// Site.java
package com.example.security.site;

import com.example.security.projet.Projet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "site")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;  // "Manzel Hayet", "LTN1", "LTN2", "Mateur"

    @Column(name = "description", length = 255)
    private String description;
    @Column(name = "active")
    private boolean active = true;
    // ✅ Relation ManyToMany avec Projet
    @ManyToMany(mappedBy = "sites", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Projet> projets = new ArrayList<>();
}