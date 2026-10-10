package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities.SupplierJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between Supplier domain entity and SupplierJpaEntity
 */
@Component
public class SupplierPersistenceMapper {

    /**
     * Domain Entity → JPA Entity
     */
    public SupplierJpaEntity toJpaEntity(Supplier domain) {
        if (domain == null) return null;

        SupplierJpaEntity jpa = new SupplierJpaEntity();
        jpa.setId(domain.getId());
        jpa.setCode(domain.getCode());
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());

        jpa.setContactPerson(domain.getContactPerson());
        jpa.setContactEmail(domain.getContactEmail());
        jpa.setContactPhone(domain.getContactPhone());

        jpa.setAddress(domain.getAddress());
        jpa.setCity(domain.getCity());
        jpa.setCountry(domain.getCountry());
        jpa.setPostalCode(domain.getPostalCode());

        jpa.setPaymentTerms(domain.getPaymentTerms());
        jpa.setPaymentDelay(domain.getPaymentDelay());
        jpa.setCurrencyCode(domain.getCurrencyCode());
        jpa.setStatus(domain.getStatus());

        // Audit fields
        jpa.setCreatedAt(domain.getCreatedAt());
        jpa.setUpdatedAt(domain.getUpdatedAt());
        jpa.setVersion(domain.getVersion());
        jpa.setCreatedBy(domain.getCreatedBy());
        jpa.setUpdatedBy(domain.getUpdatedBy());

        return jpa;
    }

    /**
     * JPA Entity → Domain Entity
     */
    public Supplier toDomainEntity(SupplierJpaEntity jpa) {
        if (jpa == null) return null;

        Supplier domain = Supplier.builder()
                .id(jpa.getId())
                .code(jpa.getCode())
                .name(jpa.getName())
                .description(jpa.getDescription())
                .contactPerson(jpa.getContactPerson())
                .contactEmail(jpa.getContactEmail())
                .contactPhone(jpa.getContactPhone())
                .address(jpa.getAddress())
                .city(jpa.getCity())
                .country(jpa.getCountry())
                .postalCode(jpa.getPostalCode())
                .paymentTerms(jpa.getPaymentTerms())
                .paymentDelay(jpa.getPaymentDelay())
                .currencyCode(jpa.getCurrencyCode())
                .status(jpa.getStatus())
                .createdBy(jpa.getCreatedBy())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();

        domain.setVersion(jpa.getVersion());
        domain.setUpdatedBy(jpa.getUpdatedBy());

        return domain;
    }
}
