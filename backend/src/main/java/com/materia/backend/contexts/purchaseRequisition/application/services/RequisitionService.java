package com.materia.backend.contexts.purchaseRequisition.application.services;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.application.exceptions.NotFoundException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.services.ExchangeRateService;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionSearchCriteria;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.mappers.RequisitionMapper;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionMaterialNotFoundException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionMaterialNotOrderableException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotDeletableException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotModifiableException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotFoundException;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionSearchFilter;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for purchase requisitions.
 */
@Service
public class RequisitionService implements RequisitionUseCase {

    private final RequisitionRepository requisitionRepository;
    private final MaterialRepository materialRepository;
    private final RequisitionMapper mapper;
    private final RequisitionCodeGeneratorService codeGenerator;
    private final ExchangeRateService exchangeRateService;

    public RequisitionService(RequisitionRepository requisitionRepository,
                              MaterialRepository materialRepository,
                              RequisitionMapper mapper,
                              RequisitionCodeGeneratorService codeGenerator,
                              ExchangeRateService exchangeRateService) {
        this.requisitionRepository = requisitionRepository;
        this.materialRepository = materialRepository;
        this.mapper = mapper;
        this.codeGenerator = codeGenerator;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    @Retryable(retryFor = DataIntegrityViolationException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    @Transactional
    public RequisitionOutput create(CreateRequisitionInput request) {
        CurrencyCode targetCurrency = CurrencyCode.fromCode(
                request != null && request.getCurrencyCode() != null && !request.getCurrencyCode().isBlank()
                        ? request.getCurrencyCode()
                        : "MAD"
        );

        if (request != null && request.getLines() != null) {
            hydrateAndValidateLines(request.getLines(), targetCurrency);
        }

        // Auto-resolve requesterId and createdBy:
        // requesterId is the same as createdBy, and we look up an existing requester ID from DB by requesterName
        String requesterName = request != null ? request.getRequesterName() : null;
        String resolvedRequesterId = request != null ? request.getRequesterId() : null;

        if ((resolvedRequesterId == null || resolvedRequesterId.trim().isEmpty()) && requesterName != null && !requesterName.trim().isEmpty()) {
            resolvedRequesterId = requisitionRepository.findFirstByRequesterName(requesterName.trim())
                    .map(Requisition::getRequesterId)
                    .orElse(null);
        }

        if (resolvedRequesterId == null || resolvedRequesterId.trim().isEmpty()) {
            if (request != null && request.getUserId() != null && !request.getUserId().trim().isEmpty()) {
                resolvedRequesterId = request.getUserId().trim();
            } else if (requesterName != null && !requesterName.trim().isEmpty()) {
                resolvedRequesterId = requesterName.trim();
            } else {
                resolvedRequesterId = "EMP-DEFAULT";
            }
        }

        if (request != null) {
            request.setRequesterId(resolvedRequesterId);
            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                request.setUserId(resolvedRequesterId);
            }
        }

        Requisition requisition = mapper.toEntity(request);
        if (requisition.getRequesterId() == null || requisition.getRequesterId().trim().isEmpty()) {
            requisition.setRequesterId(resolvedRequesterId);
        }
        if (requisition.getCreatedBy() == null || requisition.getCreatedBy().trim().isEmpty()) {
            requisition.setCreatedBy(resolvedRequesterId);
        }

        requisition.recalculateTotal();
        requisition.setRequisitionCode(generateNextCode());
        return mapper.toResponse(requisitionRepository.save(requisition));
    }

    @Override
    public RequisitionOutput update(UUID id, CreateRequisitionInput request) {
        UpdateRequisitionInput updateRequest = new UpdateRequisitionInput();
        updateRequest.setTitle(request.getTitle());
        updateRequest.setDescription(request.getDescription());
        updateRequest.setJustification(request.getJustification());
        updateRequest.setRequiredDate(request.getRequiredDate());
        updateRequest.setCurrencyCode(request.getCurrencyCode());
        updateRequest.setLines(request.getLines());
        updateRequest.setUserId(request.getUserId());
        return update(id, updateRequest);
    }

    @Override
    @Transactional
    public RequisitionOutput update(UUID id, UpdateRequisitionInput request) {
        Requisition existing = getEntityById(id);
        if (!existing.isModifiable()) {
            throw new RequisitionNotModifiableException();
        }
        mapper.updateEntity(existing, request);
        CurrencyCode targetCurrency = CurrencyCode.fromCode(
                existing.getCurrencyCode() != null && !existing.getCurrencyCode().isBlank()
                        ? existing.getCurrencyCode()
                        : "MAD"
        );
        hydrateAndValidateLines(existing.getLines(), targetCurrency);
        existing.setLines(existing.getLines());
        existing.recalculateTotal();
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(request.getUserId());
        return mapper.toResponse(requisitionRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Requisition requisition = getEntityById(id);
        if (!requisition.isDeletable()) {
            throw new RequisitionNotDeletableException();
        }
        requisitionRepository.deleteById(id);
    }

    @Override
    public RequisitionOutput getById(UUID id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Override
    public RequisitionOutput getByCode(String code) {
        return mapper.toResponse(
                requisitionRepository.findByCode(code)
                        .orElseThrow(() -> new NotFoundException("Purchase requisition not found: " + code))
        );
    }

    @Override
    public List<RequisitionOutput> getAll() {
        return mapper.toResponseList(requisitionRepository.findAll());
    }

    @Override
    public List<RequisitionOutput> getByStatus(String status) {
        RequisitionStatus requisitionStatus = mapper.toStatus(status);
        return mapper.toResponseList(requisitionRepository.findByStatus(requisitionStatus));
    }

    @Override
    public List<RequisitionOutput> getByRequesterId(String requesterId) {
        return mapper.toResponseList(requisitionRepository.findByRequesterId(requesterId));
    }

    @Override
    public List<RequisitionOutput> searchByKeyword(String keyword) {
        return mapper.toResponseList(requisitionRepository.search(keyword));
    }

    @Override
    public PageResponse<RequisitionOutput> searchAdvanced(RequisitionSearchCriteria criteria, int page, int size) {
        RequisitionStatus status = null;
        if (criteria != null && criteria.getStatus() != null && !criteria.getStatus().trim().isEmpty()) {
            status = mapper.toStatus(criteria.getStatus());
        }

        RequisitionSearchFilter filter = RequisitionSearchFilter.builder()
                .keyword(criteria != null ? criteria.getKeyword() : null)
                .requesterId(criteria != null ? criteria.getRequesterId() : null)
                .approverId(criteria != null ? criteria.getApproverId() : null)
                .status(status)
                .currencyCode(criteria != null ? criteria.getCurrencyCode() : null)
                .requiredDateFrom(criteria != null ? criteria.getRequiredDateFrom() : null)
                .requiredDateTo(criteria != null ? criteria.getRequiredDateTo() : null)
                .submittedDateFrom(criteria != null ? criteria.getSubmittedDateFrom() : null)
                .submittedDateTo(criteria != null ? criteria.getSubmittedDateTo() : null)
                .build();

        PageResponse<Requisition> domainPage = requisitionRepository.searchAdvanced(filter, page, size);
        return new PageResponse<>(
                mapper.toResponseList(domainPage.getContent()),
                domainPage.getPageNumber(),
                domainPage.getPageSize(),
                domainPage.getTotalElements(),
                domainPage.getTotalPages(),
                domainPage.isLast()
        );
    }

    @Override
    @Transactional
    public RequisitionOutput submit(UUID id, String userId) {
        Requisition requisition = getEntityById(id);
        requisition.submit(userId);
        return mapper.toResponse(requisitionRepository.save(requisition));
    }

    @Override
    @Transactional
    public RequisitionOutput approve(UUID id, String approverId, String approverName, String notes) {
        Requisition requisition = getEntityById(id);
        requisition.approve(approverId, approverName, notes);
        return mapper.toResponse(requisitionRepository.save(requisition));
    }

    @Override
    @Transactional
    public RequisitionOutput reject(UUID id, String approverId, String approverName, String reason) {
        Requisition requisition = getEntityById(id);
        requisition.reject(approverId, approverName, reason);
        return mapper.toResponse(requisitionRepository.save(requisition));
    }

    @Override
    @Transactional
    public RequisitionOutput convert(UUID id, String purchaseOrderId, String purchaseOrderCode, String userId) {
        Requisition requisition = getEntityById(id);
        requisition.convert(purchaseOrderId, purchaseOrderCode, userId);
        return mapper.toResponse(requisitionRepository.save(requisition));
    }

    @Override
    @Transactional
    public RequisitionOutput cancel(UUID id, String userId, String reason) {
        Requisition requisition = getEntityById(id);
        requisition.cancel(userId, reason);
        return mapper.toResponse(requisitionRepository.save(requisition));
    }

    private Requisition getEntityById(UUID id) {
        return requisitionRepository.findById(id)
                .orElseThrow(() -> new RequisitionNotFoundException(id));
    }

    private void hydrateAndValidateLines(List<RequisitionLine> lines, CurrencyCode targetCurrency) {
        if (lines == null || lines.isEmpty()) {
            throw new ValidationException("At least one requisition line is required",
                    Map.of("lines", "At least one line is required"));
        }

        for (int i = 0; i < lines.size(); i++) {
            RequisitionLine line = lines.get(i);
            if (line == null) {
                throw new ValidationException("Invalid requisition lines",
                        Map.of("lines[" + i + "]", "Line cannot be null"));
            }

            Material material = resolveMaterial(line, i);
            UUID existingId = line.getId();
            line.populateFromMaterial(material);

            if (targetCurrency != null && material.getStandardPrice() != null) {
                Money price = material.getStandardPrice();
                if (!price.getCurrency().equals(targetCurrency)) {
                    Money convertedPrice = exchangeRateService.convert(price, targetCurrency);
                    line.setUnitPrice(convertedPrice);
                } else {
                    line.setUnitPrice(price);
                }
                line.setCurrencyCode(targetCurrency.getCode());
                line.setCurrencyCodeLine(targetCurrency.getCode());
            }

            if (existingId != null) {
                line.setId(existingId);
            } else {
                line.setId(UUID.randomUUID());
            }

            line.setLineNumber(i + 1);

            if (line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new ValidationException("Invalid requisition line quantity",
                        Map.of("lines[" + i + "].quantity", "Must be greater than zero"));
            }

            line.calculateLineTotal();
        }
    }

    private Material resolveMaterial(RequisitionLine line, int index) {
        Material material;
        if (line.getMaterialId() != null) {
            material = materialRepository.findById(line.getMaterialId())
                    .orElseThrow(() -> new RequisitionMaterialNotFoundException("Material not found for line " + (index + 1)));
        } else if (line.getMaterialCode() != null && !line.getMaterialCode().trim().isEmpty()) {
            material = materialRepository.findByCode(line.getMaterialCode().trim())
                    .orElseThrow(() -> new RequisitionMaterialNotFoundException("Material not found for code: " + line.getMaterialCode()));
        } else {
            throw new ValidationException("Material is required for each requisition line",
                    Map.of("lines[" + index + "].material", "Material id or code is required"));
        }

        if (!material.isOrderable()) {
            throw new RequisitionMaterialNotOrderableException(index);
        }

        return material;
    }

    private com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode generateNextCode() {
        return codeGenerator.generateCode();
    }

    @Transactional
    public String createRequisitionFromReorder(Material material, int quantity, String reason, boolean isUrgent) {
        Requisition requisition = new Requisition();
        requisition.setTitle((isUrgent ? "[URGENT] " : "") + "Reorder for " + material.getName());
        requisition.setDescription("Auto-generated reorder requisition: " + reason);
        requisition.setJustification(reason != null && !reason.isBlank() ? reason : "Automated stock reorder");
        requisition.setRequiredDate(java.time.LocalDate.now().plusDays(isUrgent ? 2 : 7));
        requisition.setCurrencyCode(material.getStandardPrice() != null && material.getStandardPrice().getCurrency() != null 
                ? material.getStandardPrice().getCurrency().getCode() : "MAD");
        requisition.setRequisitionCode(generateNextCode());
        requisition.setStatus(com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus.DRAFT);
        
        RequisitionLine line = new RequisitionLine(material, quantity, requisition.getRequiredDate());
        requisition.addLine(line);
        requisition.recalculateTotal();
        
        Requisition saved = requisitionRepository.save(requisition);
        return saved.getRequisitionCode() != null ? saved.getRequisitionCode().getValue() : saved.getId().toString();
    }

    @Transactional
    public String createGroupedRequisition(String supplierId, List<RequisitionLine> lines) {
        Requisition requisition = new Requisition();
        requisition.setTitle("Grouped replenishment for supplier " + supplierId);
        requisition.setDescription("Auto-generated grouped replenishment requisition for supplier: " + supplierId);
        requisition.setJustification("Nightly automated stock replenishment");
        requisition.setRequiredDate(java.time.LocalDate.now().plusDays(7));
        requisition.setCurrencyCode("MAD");
        requisition.setRequisitionCode(generateNextCode());
        requisition.setStatus(com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus.DRAFT);
        
        hydrateAndValidateLines(lines, CurrencyCode.MAD);
        for (RequisitionLine line : lines) {
            requisition.addLine(line);
        }
        requisition.recalculateTotal();
        
        Requisition saved = requisitionRepository.save(requisition);
        return saved.getRequisitionCode() != null ? saved.getRequisitionCode().getValue() : saved.getId().toString();
    }
}
