package com.example.security.user;

import com.example.security.projet.Projet;
import com.example.security.site.Site;
import com.example.security.token.Token;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="_user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstname;
    private String lastname;
    private Integer matricule;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_projet",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "projet_id")
    )
    @Builder.Default
    private Set<Projet> projets = new HashSet<>();
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private  Role role;
    @OneToMany(mappedBy = "user")
    private List<Token> tokens;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Override
    public String getPassword() {
        return password;
    }

    public Integer getMatricule(){
        return matricule;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }

    // Raccourci pour obtenir le nom du site
    // User.java - Ajouter ces méthodes

    public String getSiteName() {
        return site != null ? site.getName() : null;
    }

    public Long getSiteId() {
        return site != null ? site.getId() : null;
    }
    // Getters/Setters
    public Set<Projet> getProjets() { return projets; }
    public void setProjets(Set<Projet> projets) { this.projets = projets; }

    // ✅ Raccourci pour obtenir les noms des projets
    public String getProjetsNames() {
        if (projets == null || projets.isEmpty()) return null;
        return projets.stream()
                .map(Projet::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    // ✅ Vérifier si l'utilisateur a accès à un projet
    public boolean hasProjet(String projetName) {
        return projets.stream().anyMatch(p -> p.getName().equals(projetName));
    }
    // User.java - Ajouter cette méthode
    public List<String> getProjetNamesAsList() {
        if (projets == null || projets.isEmpty()) return List.of();
        return projets.stream()
                .map(Projet::getName)
                .collect(Collectors.toList());
    }

}
