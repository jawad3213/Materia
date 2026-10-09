package com.materia.backend.contexts.settings.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataAppSettingRepository extends JpaRepository<AppSettingJpaEntity, UUID> {

    Optional<AppSettingJpaEntity> findBySettingKey(String settingKey);
}
