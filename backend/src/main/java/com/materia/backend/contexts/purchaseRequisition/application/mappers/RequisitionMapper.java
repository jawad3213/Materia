package com.materia.backend.contexts.purchaseRequisition.application.mappers;

import com.materia.backend.common.application.BaseMapper;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for purchase requisitions.
 */
@Component
public class RequisitionMapper implements BaseMapper<Requisition, CreateRequisitionInput, UpdateRequisitionInput, RequisitionOutput> {

    @Override
    public Requisition toEntity(CreateRequisitionInput request) {
        if (request == null) {
            return null;
        }

        return Requisition.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .justification(request.getJustification())
                .requesterId(request.getRequesterId())
                .requesterName(request.getRequesterName())
                .requiredDate(request.getRequiredDate())
                .currencyCode(request.getCurrencyCode())
                .lines(copyLines(request.getLines()))
                .createdBy(request.getUserId())
                .build();
    }

    @Override
    public void updateEntity(Requisition entity, UpdateRequisitionInput request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getJustification() != null) {
            entity.setJustification(request.getJustification());
        }
        if (request.getRequiredDate() != null) {
            entity.setRequiredDate(request.getRequiredDate());
        }
        if (request.getCurrencyCode() != null) {
            entity.setCurrencyCode(request.getCurrencyCode());
        }
        if (request.getLines() != null && !request.getLines().isEmpty()) {
            entity.setLines(copyLines(request.getLines()));
        }
    }

    @Override
    public RequisitionOutput toResponse(Requisition entity) {
        if (entity == null) {
            return null;
        }

        RequisitionOutput response = new RequisitionOutput();
        response.setId(entity.getId());
        response.setRequisitionCode(entity.getRequisitionCode() != null ? entity.getRequisitionCode().getValue() : null);
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setJustification(entity.getJustification());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setRequesterId(entity.getRequesterId());
        response.setRequesterName(entity.getRequesterName());
        response.setRequiredDate(entity.getRequiredDate());
        response.setSubmittedDate(entity.getSubmittedDate());
        response.setApprovedDate(entity.getApprovedDate());
        response.setConvertedDate(entity.getConvertedDate());
        response.setTotalAmount(entity.getTotalAmount());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setApproverId(entity.getApproverId());
        response.setApproverName(entity.getApproverName());
        response.setRejectionReason(entity.getRejectionReason());
        response.setApprovalNotes(entity.getApprovalNotes());
        response.setPurchaseOrderId(entity.getPurchaseOrderId());
        response.setPurchaseOrderCode(entity.getPurchaseOrderCode());
        response.setLines(copyLines(entity.getLines()));
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    public RequisitionStatus toStatus(String status) {
        return status != null ? RequisitionStatus.fromCode(status) : null;
    }

    private List<RequisitionLine> copyLines(List<RequisitionLine> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream()
                .map(line -> line != null ? line.copy() : null)
                .filter(line -> line != null)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
