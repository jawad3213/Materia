package com.materia.backend.contexts.masterdata.application.services;

import com.materia.backend.contexts.masterdata.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterdata.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.contexts.masterdata.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterdata.application.mappers.MaterialMapper;
import com.materia.backend.contexts.masterdata.domain.entities.Material;
import com.materia.backend.contexts.masterdata.domain.exceptions.MaterialNotFoundException;
import com.materia.backend.contexts.masterdata.domain.exceptions.InsufficientStockException;
import com.materia.backend.contexts.masterdata.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterdata.domain.ports.out.MaterialRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MaterialService implements MaterialUseCase {

    private final MaterialRepository materialRepository;
    private final MaterialMapper mapper;

    public MaterialService(MaterialRepository materialRepository, MaterialMapper mapper) {
        this.materialRepository = materialRepository;
        this.mapper = mapper;
    }

    // ============================================================
    // CRUD OPERATIONS (BaseUseCase)
    // ============================================================

    @Override
    public MaterialOutput create(CreateMaterialInput request) {
        Material material = mapper.toEntity(request);
        Material saved = materialRepository.save(material);
        return mapper.toResponse(saved);
    }

    @Override
    public MaterialOutput update(UUID id, CreateMaterialInput request) {
        Material existing = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));

        // Use UpdateMaterialInput-style partial update
        if (request.getName() != null) existing.setName(request.getName());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getShortDescription() != null) existing.setShortDescription(request.getShortDescription());

        Material updated = materialRepository.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public void delete(UUID id) {
        Material existing = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));
        materialRepository.deleteById(id);
    }

    @Override
    public MaterialOutput getById(UUID id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));
        return mapper.toResponse(material);
    }

    @Override
    public MaterialOutput getByCode(String code) {
        Material material = materialRepository.findByCode(code)
                .orElseThrow(() -> new MaterialNotFoundException(code));
        return mapper.toResponse(material);
    }

    @Override
    public List<MaterialOutput> getAll() {
        return mapper.toResponseList(materialRepository.findAll());
    }

    // ============================================================
    // CUSTOM OPERATIONS
    // ============================================================

    @Override
    public List<MaterialOutput> getMaterialsByCategory(UUID categoryId) {
        return mapper.toResponseList(materialRepository.findByCategoryId(categoryId.toString()));
    }

    @Override
    public List<MaterialOutput> getMaterialsBySupplier(UUID supplierId) {
        return mapper.toResponseList(materialRepository.findBySupplierId(supplierId.toString()));
    }

    @Override
    public MaterialOutput increaseStock(UUID id, int quantity) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));
        material.increaseStock(quantity);
        return mapper.toResponse(materialRepository.save(material));
    }

    @Override
    public MaterialOutput decreaseStock(UUID id, int quantity) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));
        try {
            material.decreaseStock(quantity);
        } catch (IllegalStateException e) {
            throw new InsufficientStockException(
                material.getCode() != null ? material.getCode().getValue() : "UNKNOWN",
                quantity,
                material.getCurrentStock() != null ? material.getCurrentStock() : 0
            );
        }
        return mapper.toResponse(materialRepository.save(material));
    }

    @Override
    public List<MaterialOutput> getMaterialsBelowMinimumStock() {
        return mapper.toResponseList(materialRepository.findBelowMinimumStock());
    }

    @Override
    public List<MaterialOutput> getAvailableStockMaterials() {
        return mapper.toResponseList(materialRepository.findAvailableStock());
    }

    @Override
    public List<MaterialOutput> getOutOfStockMaterials() {
        return mapper.toResponseList(materialRepository.findOutOfStock());
    }
}
