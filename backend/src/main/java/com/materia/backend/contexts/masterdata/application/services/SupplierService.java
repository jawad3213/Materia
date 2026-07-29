package com.materia.backend.contexts.masterdata.application.services;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.requests.CreateSupplierRequest;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.responses.SupplierResponseDto;
import com.materia.backend.contexts.masterdata.application.mappers.SupplierMapper;
import com.materia.backend.contexts.masterdata.domain.entities.Supplier;
import com.materia.backend.contexts.masterdata.domain.exceptions.SupplierNotFoundException;
import com.materia.backend.contexts.masterdata.domain.ports.in.SupplierUseCase;
import com.materia.backend.contexts.masterdata.domain.ports.out.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SupplierService implements SupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper mapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper mapper) {
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // CRUD OPERATIONS (BaseUseCase)
    // ============================================================

    @Override
    public SupplierResponseDto create(CreateSupplierRequest request) {
        Supplier supplier = mapper.toEntity(request);
        Supplier saved = supplierRepository.save(supplier);
        return mapper.toResponse(saved);
    }

    @Override
    public SupplierResponseDto update(UUID id, CreateSupplierRequest request) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id.toString()));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());

        Supplier updated = supplierRepository.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id.toString()));
        supplierRepository.deleteById(id);
    }

    @Override
    public SupplierResponseDto getById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id.toString()));
        return mapper.toResponse(supplier);
    }

    @Override
    public SupplierResponseDto getByCode(String code) {
        Supplier supplier = supplierRepository.findByCode(code)
                .orElseThrow(() -> new SupplierNotFoundException(code));
        return mapper.toResponse(supplier);
    }

    @Override
    public List<SupplierResponseDto> getAll() {
        return mapper.toResponseList(supplierRepository.findAll());
    }

    // ============================================================
    // CUSTOM OPERATIONS
    // ============================================================

    @Override
    public List<SupplierResponseDto> searchSuppliers(String keyword) {
        return mapper.toResponseList(supplierRepository.search(keyword));
    }
}
