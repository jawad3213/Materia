package com.materia.backend.contexts.masterdata.application.services;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.SupplierOutput;
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
    public SupplierOutput create(CreateSupplierInput request) {
        Supplier supplier = mapper.toEntity(request);
        Supplier saved = supplierRepository.save(supplier);
        return mapper.toResponse(saved);
    }

    @Override
    public SupplierOutput update(UUID id, CreateSupplierInput request) {
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
}
