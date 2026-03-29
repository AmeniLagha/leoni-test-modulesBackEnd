package com.example.security.token;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
public interface TokenRepository  extends JpaRepository<Token, Integer> {
    @Query(value = """    
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByUser(Integer id);

    Optional<Token> findByToken(String token);
    // Supprimer tous les tokens d'un utilisateur
    @Transactional
    void deleteByUserId(Integer userId);
}
