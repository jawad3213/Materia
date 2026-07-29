package com.materia.backend.contexts.masterdata.application.mappers;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.requests.CreateSupplierRequest;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.requests.UpdateSupplierRequest;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.responses.SupplierResponseDto;
import com.materia.backend.contexts.masterdata.domain.entities.Supplier;
import com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import com.materia.backend.common.application.BaseMapper;

/**
 * Mapper for Supplier: Request DTO -> Entity and Entity -> Response DTO
 */
@Component
public class SupplierMapper implements BaseMapper<Supplier, CreateSupplierRequest, UpdateSupplierRequest, SupplierResponseDto> {

    // ============================================================
    // REQUEST DTO -> ENTITY
    // ============================================================

    public Supplier toEntity(CreateSupplierRequest request) {
        if (request == null) return null;

        return Supplier.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .contactPerson(request.getContactPerson())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .paymentTerms(request.getPaymentTerms())
                .paymentDelay(request.getPaymentDelay())
                .currencyCode(request.getCurrencyCode() != null
                        ? CurrencyCode.fromCode(request.getCurrencyCode())
                        : CurrencyCode.MAD)
                .createdBy(request.getCreatedBy())
                .build();
    }

    public void updateEntity(Supplier entity, UpdateSupplierRequest request) {
        if (entity == null || request == null) return;

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getContactPerson() != null) entity.setContactPerson(request.getContactPerson());
        if (request.getContactEmail() != null) entity.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) entity.setContactPhone(request.getContactPhone());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
        if (request.getCity() != null) entity.setCity(request.getCity());
        if (request.getCountry() != null) entity.setCountry(request.getCountry());
        if (request.getPostalCode() != null) entity.setPostalCode(request.getPostalCode());
        if (request.getPaymentTerms() != null) entity.setPaymentTerms(request.getPaymentTerms());
        if (request.getPaymentDelay() != null) entity.setPaymentDelay(request.getPaymentDelay());
        if (request.getCurrencyCode() != null) entity.setCurrencyCode(CurrencyCode.fromCode(request.getCurrencyCode()));
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
    }

    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    public SupplierResponseDto toResponse(Supplier entity) {
        if (entity == null) return null;

        SupplierResponseDto response = new SupplierResponseDto();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setContactPerson(entity.getContactPerson());
        response.setContactEmail(entity.getContactEmail());
        response.setContactPhone(entity.getContactPhone());
        response.setAddress(entity.getAddress());
        response.setCity(entity.getCity());
        response.setCountry(entity.getCountry());
        response.setPostalCode(entity.getPostalCode());
        response.setFullAddress(entity.getFullAddress());
        response.setPaymentTerms(entity.getPaymentTerms());
        response.setPaymentDelay(entity.getPaymentDelay());
        response.setCurrencyCode(entity.getCurrencyCode() != null ? entity.getCurrencyCode().getCode() : null);
        response.setStatus(entity.getStatus());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public List<SupplierResponseDto> toResponseList(List<Supplier> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
