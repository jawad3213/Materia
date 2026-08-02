package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.repositories;

import com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities.CodeSequenceJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataCodeSequenceRepository extends JpaRepository<CodeSequenceJpaEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CodeSequenceJpaEntity s WHERE s.prefix = :prefix")
    Optional<CodeSequenceJpaEntity> findByPrefixForUpdate(@Param("prefix") String prefix);
}
