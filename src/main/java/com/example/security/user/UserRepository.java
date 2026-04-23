package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    boolean existsByEmail(String email);
    boolean existsByMatricule(Integer matricule);

    // Récupérer tous les emails des utilisateurs
    @Query("SELECT u.email FROM User u")
    List<String> findAllUserEmails();

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    default List<String> findAllActiveUserEmails() {
        return findAllUserEmails();
    }

    // ✅ CORRECTION : Utiliser la jointure avec projets
    @Query("""
        SELECT DISTINCT u.email FROM User u
        JOIN u.projets p
        WHERE p.name = :projet OR u.role = 'ADMIN'
    """)
    List<String> findActiveUserEmailsByProjet(@Param("projet") String projet);

    // ✅ CORRECTION : Exclure l'utilisateur courant
    @Query("""
        SELECT DISTINCT u.email FROM User u
        JOIN u.projets p
        WHERE (p.name = :projet OR u.role = 'ADMIN')
        AND u.email <> :currentEmail
    """)
    List<String> findActiveUserEmailsByProjetExcludingCurrent(
            @Param("projet") String projet,
            @Param("currentEmail") String currentEmail);

    // ✅ CORRECTION : Trouver les MC et MP par projet
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.projets p
        WHERE p.name = :projet 
        AND (u.role = 'MC' OR u.role = 'MP')
    """)
    List<User> findMcAndMpByProject(@Param("projet") String projet);

    @Query("""
        SELECT DISTINCT u.email FROM User u
        JOIN u.projets p
        WHERE p.name IN :projets
        AND (u.site.name = :site OR u.role = 'ADMIN')
    """)
    List<String> findEmailsByProjectsAndSite(
            @Param("projets") List<String> projets,
            @Param("site") String site);

    // Dans UserRepository.java - Ajoutez ces méthodes

    // ✅ Trouver les utilisateurs PP par projet et site
    @Query("""
    SELECT DISTINCT u FROM User u
    JOIN u.projets p
    WHERE u.role = 'PP' 
    AND p.name = :projet
    AND u.site.name = :site
""")
    List<User> findPpUsersByProjectAndSite(@Param("projet") String projet, @Param("site") String site);

    // Trouver les emails des utilisateurs par projet ET par site
    @Query("""
    SELECT DISTINCT u.email FROM User u
    JOIN u.projets p
    WHERE p.name = :projet 
    AND u.site.name = :site
""")
    List<String> findEmailsByProjectAndSite(@Param("projet") String projet, @Param("site") String site);


}