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
    // Récupérer tous les emails (méthode par défaut si vous n'avez pas enabled)
    default List<String> findAllActiveUserEmails() {
        return findAllUserEmails(); // Retourne tous les utilisateurs
    }

    @Query("""
SELECT u.email FROM User u
WHERE u.projet = :projet OR u.role = 'ADMIN'
""")
    List<String> findActiveUserEmailsByProjet(@Param("projet") String projet);

    @Query("""
SELECT u.email FROM User u
WHERE (u.projet = :projet OR u.role = 'ADMIN')
AND u.email <> :currentEmail
""")
    List<String> findActiveUserEmailsByProjetExcludingCurrent(
            @Param("projet") String projet,
            @Param("currentEmail") String currentEmail);




}
