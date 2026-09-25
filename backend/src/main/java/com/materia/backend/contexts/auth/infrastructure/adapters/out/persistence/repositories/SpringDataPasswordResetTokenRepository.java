package com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.auth.infrastructure.adapters.out.persistence.entities.PasswordResetTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔹 SPRING DATA JPA PASSWORD RESET TOKEN REPOSITORY
 */
@Repository
public interface SpringDataPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {

    Optional<PasswordResetTokenJpaEntity> findByToken(String token);

    Optional<PasswordResetTokenJpaEntity> findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("UPDATE PasswordResetTokenJpaEntity p SET p.used = true WHERE p.userId = :userId AND p.used = false")
    void invalidateAllForUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenJpaEntity p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenJpaEntity p WHERE p.expiryDate < :now OR p.used = true")
    void deleteExpiredOrUsed(@Param("now") LocalDateTime now);
}
