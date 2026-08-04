package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.contexts.masterData.application.mappers.SupplierMapper;
import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.exceptions.SupplierNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.in.SupplierUseCase;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Service
public class SupplierService implements SupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;
    private final SupplierMapper mapper;
    private final SupplierCodeGeneratorService codeGenerator;

    public SupplierService(SupplierRepository supplierRepository,
                           MaterialRepository materialRepository,
                           SupplierMapper mapper,
                           SupplierCodeGeneratorService codeGenerator) {
        this.supplierRepository = supplierRepository;
        this.materialRepository = materialRepository;
        this.mapper = mapper;
        this.codeGenerator = codeGenerator;
    }

    // ============================================================
    // CRUD OPERATIONS (BaseUseCase)
    // ============================================================

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public SupplierOutput create(CreateSupplierInput request) {
        Supplier supplier = mapper.toEntity(request);
        supplier.setCode(codeGenerator.generateCode());

        Supplier saved = supplierRepository.save(supplier);
        return mapper.toResponse(saved);
    }

    @Override
    public SupplierOutput update(UUID id, CreateSupplierInput request) {
        UpdateSupplierInput updateRequest = new UpdateSupplierInput();
        updateRequest.setName(request.getName());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setContactPerson(request.getContactPerson());
        updateRequest.setContactEmail(request.getContactEmail());
        updateRequest.setContactPhone(request.getContactPhone());
        updateRequest.setAddress(request.getAddress());
        updateRequest.setCity(request.getCity());
        updateRequest.setCountry(request.getCountry());
        updateRequest.setPostalCode(request.getPostalCode());
        updateRequest.setPaymentTerms(request.getPaymentTerms());
        updateRequest.setPaymentDelay(request.getPaymentDelay());
        updateRequest.setCurrencyCode(request.getCurrencyCode());
        updateRequest.setUpdatedBy(request.getCreatedBy());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public SupplierOutput update(UUID id, UpdateSupplierInput request) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id.toString()));
        String previousName = existing.getName();
        mapper.updateEntity(existing, request);
        Supplier updated = supplierRepository.save(existing);

        if (!Objects.equals(previousName, updated.getName())) {
            syncMaterialSupplierNames(updated);
        }

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id.toString()));

        if (materialRepository.existsBySupplierId(id.toString())) {
            throw new BusinessException("Cannot delete supplier with linked materials", "SUPPLIER_HAS_MATERIALS");
        }

        supplierRepository.deleteById(id);
    }

    @Override
    public SupplierOutput getById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id.toString()));
        return mapper.toResponse(supplier);
    }

    @Override
    public SupplierOutput getByCode(String code) {
        Supplier supplier = supplierRepository.findByCode(code)
                .orElseThrow(() -> new SupplierNotFoundException(code));
        return mapper.toResponse(supplier);
    }

    @Override
    public List<SupplierOutput> getAll() {
        return mapper.toResponseList(supplierRepository.findAll());
    }

    // ============================================================
    // CUSTOM OPERATIONS
    // ============================================================

    @Override
    public List<SupplierOutput> searchSuppliers(String keyword) {
        return mapper.toResponseList(supplierRepository.search(keyword));
    }

    private void syncMaterialSupplierNames(Supplier supplier) {
        List<com.materia.backend.contexts.masterData.domain.entities.Material> materials =
                materialRepository.findBySupplierId(supplier.getId().toString());

        if (materials.isEmpty()) {
            return;
        }

        for (com.materia.backend.contexts.masterData.domain.entities.Material material : materials) {
            material.setSupplierName(supplier.getName());
        }
        materialRepository.saveAll(materials);
    }
}
