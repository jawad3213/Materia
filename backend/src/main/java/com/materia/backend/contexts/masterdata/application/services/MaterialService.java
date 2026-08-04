package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.contexts.masterData.application.mappers.MaterialMapper;
import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.entities.Supplier;
import com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException;
import com.materia.backend.contexts.masterData.domain.exceptions.InsufficientStockException;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.ports.in.MaterialUseCase;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.ports.out.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Service
public class MaterialService implements MaterialUseCase {

    private final MaterialRepository materialRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialMapper mapper;
    private final MaterialCodeGeneratorService codeGenerator;

    public MaterialService(MaterialRepository materialRepository,
                           CategoryRepository categoryRepository,
                           SupplierRepository supplierRepository,
                           MaterialMapper mapper,
                           MaterialCodeGeneratorService codeGenerator) {
        this.materialRepository = materialRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
        this.codeGenerator = codeGenerator;
    }

    // ============================================================
    // CRUD OPERATIONS (BaseUseCase)
    // ============================================================

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public MaterialOutput create(CreateMaterialInput request) {
        MaterialType materialType = MaterialType.fromValue(request.getMaterialType());
        Category category = getCategoryByStringId(request.getCategoryId(), "categoryId");
        Supplier supplier = getSupplierByStringId(request.getSupplierId(), "supplierId");
        Material material = mapper.toEntity(request);
        material.setCode(codeGenerator.generateCode(materialType).getValue());
        material.setCategoryId(category.getId().toString());
        material.setCategoryName(category.getName());
        material.setSupplierId(supplier.getId().toString());
        material.setSupplierName(supplier.getName());

        Material saved = materialRepository.save(material);
        return mapper.toResponse(saved);
    }

    @Override
    public MaterialOutput update(UUID id, CreateMaterialInput request) {
        UpdateMaterialInput updateRequest = new UpdateMaterialInput();
        updateRequest.setName(request.getName());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setShortDescription(request.getShortDescription());
        updateRequest.setSearchKeywords(request.getSearchKeywords());
        updateRequest.setAlternativeName(request.getAlternativeName());
        updateRequest.setCategoryId(request.getCategoryId());
        updateRequest.setSupplierId(request.getSupplierId());
        updateRequest.setMaterialType(request.getMaterialType());
        updateRequest.setStatus(request.getStatus());
        updateRequest.setUnitOfMeasure(request.getUnitOfMeasure());
        updateRequest.setCurrentStock(request.getCurrentStock());
        updateRequest.setMinimumStock(request.getMinimumStock());
        updateRequest.setMaximumStock(request.getMaximumStock());
        updateRequest.setReorderPoint(request.getReorderPoint());
        updateRequest.setSafetyStock(request.getSafetyStock());
        updateRequest.setEconomicOrderQuantity(request.getEconomicOrderQuantity());
        updateRequest.setStandardPrice(request.getStandardPrice());
        updateRequest.setStandardPriceCurrency(request.getStandardPriceCurrency());
        updateRequest.setCostPrice(request.getCostPrice());
        updateRequest.setCostPriceCurrency(request.getCostPriceCurrency());
        updateRequest.setUpdatedBy(request.getCreatedBy());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public MaterialOutput update(UUID id, UpdateMaterialInput request) {
        Material existing = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = getCategoryByStringId(request.getCategoryId(), "categoryId");
        }

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = getSupplierByStringId(request.getSupplierId(), "supplierId");
        }

        mapper.updateEntity(existing, request);

        if (category != null) {
            existing.setCategoryId(category.getId().toString());
            existing.setCategoryName(category.getName());
        }
        if (supplier != null) {
            existing.setSupplierId(supplier.getId().toString());
            existing.setSupplierName(supplier.getName());
        }

        return mapper.toResponse(materialRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        materialRepository.findById(id)
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
    @Transactional
    public MaterialOutput increaseStock(UUID id, int quantity) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));
        validatePositiveQuantity(quantity);
        if (material.isObsolete()) {
            throw new BusinessException("Cannot increase stock of an obsolete material", "MATERIAL_OBSOLETE");
        }
        material.increaseStock(quantity);
        return mapper.toResponse(materialRepository.save(material));
    }

    @Override
    @Transactional
    public MaterialOutput decreaseStock(UUID id, int quantity) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id.toString()));
        validatePositiveQuantity(quantity);
        if (material.isObsolete()) {
            throw new BusinessException("Cannot decrease stock of an obsolete material", "MATERIAL_OBSOLETE");
        }
        if (material.getCurrentStock() == null || material.getCurrentStock() < quantity) {
            throw new InsufficientStockException(
                material.getCode() != null ? material.getCode().getValue() : "UNKNOWN",
                quantity,
                material.getCurrentStock() != null ? material.getCurrentStock() : 0
            );
        }
        material.decreaseStock(quantity);
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

    @Override
    public List<MaterialOutput> getMaterialsByStatus(String status) {
        MaterialStatus materialStatus = MaterialStatus.fromValue(status);
        return mapper.toResponseList(materialRepository.findByStatus(materialStatus));
    }

    @Override
    public List<MaterialOutput> searchByKeyword(String keyword) {
        return mapper.toResponseList(materialRepository.search(keyword));
    }

    @Override
    public com.materia.backend.common.application.PageResponse<MaterialOutput> searchAdvanced(
            com.materia.backend.contexts.masterData.application.dtos.material.MaterialSearchCriteria criteria,
            int page, 
            int size) {
        
        MaterialStatus statusEnum = null;
        if (criteria.getStatus() != null && !criteria.getStatus().trim().isEmpty()) {
            statusEnum = MaterialStatus.fromValue(criteria.getStatus());
        }

        com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter filter =
            com.materia.backend.contexts.masterData.domain.valueObjects.MaterialSearchFilter.builder()
                .keyword(criteria.getKeyword())
                .categoryId(criteria.getCategoryId())
                .supplierId(criteria.getSupplierId())
                .status(statusEnum)
                .minPrice(criteria.getMinPrice())
                .maxPrice(criteria.getMaxPrice())
                .lowStockOnly(criteria.getLowStockOnly())
                .build();
                
        com.materia.backend.common.application.PageResponse<com.materia.backend.contexts.masterData.domain.entities.Material> domainPage =
            materialRepository.searchAdvanced(filter, page, size);
            
        return new com.materia.backend.common.application.PageResponse<>(
                mapper.toResponseList(domainPage.getContent()),
                domainPage.getPageNumber(),
                domainPage.getPageSize(),
                domainPage.getTotalElements(),
                domainPage.getTotalPages(),
                domainPage.isLast()
        );
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive", Map.of("quantity", "Must be greater than zero"));
        }
    }

    private Category getCategoryByStringId(String rawId, String fieldName) {
        UUID uuid = parseUuid(rawId, fieldName);
        return categoryRepository.findById(uuid)
                .orElseThrow(() -> new com.materia.backend.contexts.masterData.domain.exceptions.CategoryNotFoundException(rawId));
    }

    private Supplier getSupplierByStringId(String rawId, String fieldName) {
        UUID uuid = parseUuid(rawId, fieldName);
        return supplierRepository.findById(uuid)
                .orElseThrow(() -> new com.materia.backend.contexts.masterData.domain.exceptions.SupplierNotFoundException(rawId));
    }

    private UUID parseUuid(String rawId, String fieldName) {
        try {
            return UUID.fromString(rawId);
        } catch (RuntimeException ex) {
            throw new ValidationException("Invalid " + fieldName,
                    Map.of(fieldName, "Must be a valid UUID"));
        }
    }

}
