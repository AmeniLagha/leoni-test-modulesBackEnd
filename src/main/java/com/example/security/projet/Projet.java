// Projet.java
package com.example.security.projet;

import com.example.security.site.Site;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;  // "BMW", "Mercedes", "Audi", etc.

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "active")
    private boolean active = true;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "site_projet",
            joinColumns = @JoinColumn(name = "projet_id"),
            inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    @Builder.Default
    private List<Site> sites = new ArrayList<>();
}