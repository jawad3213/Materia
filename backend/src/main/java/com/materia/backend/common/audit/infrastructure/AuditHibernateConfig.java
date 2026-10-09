package com.materia.backend.common.audit.infrastructure;

import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registers {@link AuditIntegrator} with Hibernate explicitly. It used to rely on a
 * {@code META-INF/services} file, but that folder is git-ignored, so no build outside one developer's machine
 * ever recorded history.
 */
@Configuration
public class AuditHibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer auditIntegratorRegistration() {
        return properties -> properties.put("hibernate.integrator_provider",
                (IntegratorProvider) () -> List.of(new AuditIntegrator()));
    }
}
