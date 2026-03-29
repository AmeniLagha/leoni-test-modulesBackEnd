package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByEmailAndCodeAndUsedFalse(String email, String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode v WHERE v.email = :email")
    void deleteByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE VerificationCode v SET v.used = true WHERE v.email = :email AND v.code = :code")
    void markAsUsed(String email, String code);
}