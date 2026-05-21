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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Représente un utilisateur du système LEONI.
 *
 * <p>Cette entité gère les informations des utilisateurs, leurs rôles,
 * leurs permissions, et leurs associations avec les projets et sites.
 * Implémente {@link UserDetails} pour l'intégration avec Spring Security.</p>
 *
 * <p>Les utilisateurs peuvent avoir différents rôles (ADMIN, ING, PT, PP, MC, MP)
 * et sont associés à un ou plusieurs projets ainsi qu'à un site unique.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @since 2026
 * @see Role
 * @see Site
 * @see Projet
 */
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
    /**
     * Prénom de l'utilisateur
     */
    private String firstname;
    /**
     * Nom de l'utilisateur
     */
    private String lastname;
    /**
     * Matricule unique de l'utilisateur
     */
    private Integer matricule;
    /**
     * Ensemble des projets auxquels l'utilisateur est affecté.
     * Relation ManyToMany avec chargement EAGER pour un accès immédiat aux permissions.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_projet",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "projet_id")
    )
    @Builder.Default
    private Set<Projet> projets = new HashSet<>();

    /**
     * Email professionnel de l'utilisateur (utilisé comme username pour l'authentification)
     */
    private String email;
    /**
     * Mot de passe hashé de l'utilisateur
     */
    private String password;
    /**
     * Rôle de l'utilisateur (ADMIN, ING, PT, PP, MC, MP)
     */
    @Enumerated(EnumType.STRING)
    private  Role role;
    /**
     * Liste des tokens JWT associés à l'utilisateur
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Token> tokens;
    /**
     * {@inheritDoc}
     * @return les autorités (permissions) de l'utilisateur basées sur son rôle
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }
    /**
     * Site de rattachement de l'utilisateur.
     * Relation ManyToOne avec chargement EAGER.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", nullable = true)
    private Site site;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    /**
     * {@inheritDoc}
     * @return le mot de passe hashé de l'utilisateur
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Retourne le matricule de l'utilisateur.
     *
     * @return le matricule unique de l'utilisateur
     */
    public Integer getMatricule(){
        return matricule;
    }

    /**
     * {@inheritDoc}
     * @return l'email de l'utilisateur (utilisé comme nom d'utilisateur)
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * {@inheritDoc}
     * @return true - le compte n'est jamais expiré dans ce système
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @return true - le compte n'est jamais verrouillé dans ce système
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @return true - les identifiants n'expirent jamais dans ce système
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @return true - le compte est toujours activé
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Retourne le site associé à l'utilisateur.
     *
     * @return le site de l'utilisateur
     */
    public Site getSite() { return site; }

    /**
     * Définit le site de l'utilisateur.
     *
     * @param site le site à associer
     */
    public void setSite(Site site) { this.site = site; }

    /**
     * Retourne le nom du site de l'utilisateur.
     *
     * @return le nom du site, ou null si aucun site n'est défini
     */
    public String getSiteName() {
        return site != null ? site.getName() : null;
    }
    /**
     * Retourne l'ID du site de l'utilisateur.
     *
     * @return l'ID du site, ou null si aucun site n'est défini
     */
    public Long getSiteId() {
        return site != null ? site.getId() : null;
    }
    /**
     * Retourne l'ensemble des projets de l'utilisateur.
     *
     * @return l'ensemble des projets
     */
    public Set<Projet> getProjets() { return projets; }
    /**
     * Définit l'ensemble des projets de l'utilisateur.
     *
     * @param projets l'ensemble des projets à associer
     */
    public void setProjets(Set<Projet> projets) { this.projets = projets; }

    /**
     * Retourne une chaîne contenant les noms des projets séparés par des virgules.
     *
     * @return les noms des projets sous forme de chaîne, ou null si aucun projet
     */
    public String getProjetsNames() {
        if (projets == null || projets.isEmpty()) return null;
        return projets.stream()
                .map(Projet::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    /**
     * Vérifie si l'utilisateur a accès à un projet spécifique.
     *
     * @param projetName le nom du projet à vérifier
     * @return true si l'utilisateur est affecté au projet, false sinon
     */
    public boolean hasProjet(String projetName) {
        return projets.stream().anyMatch(p -> p.getName().equals(projetName));
    }
    /**
     * Retourne la liste des noms des projets de l'utilisateur.
     *
     * @return une {@link List} contenant les noms des projets, jamais null
     */
    public List<String> getProjetNamesAsList() {
        if (projets == null || projets.isEmpty()) return List.of();
        return projets.stream()
                .map(Projet::getName)
                .collect(Collectors.toList());
    }

}
