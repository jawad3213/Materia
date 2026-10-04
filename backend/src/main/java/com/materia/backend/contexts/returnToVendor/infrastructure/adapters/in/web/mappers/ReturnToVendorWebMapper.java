package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.CreateReturnToVendorWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.ReturnToVendorLineWebRequest;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.ReturnToVendorLineWebResponse;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.ReturnToVendorWebResponse;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.UpdateReturnToVendorWebRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReturnToVendorWebMapper {

    public CreateReturnToVendorInput toCreateInput(CreateReturnToVendorWebRequest request, String userId) {
        CreateReturnToVendorInput input = new CreateReturnToVendorInput();
        input.setGoodsReceiptId(request.goodsReceiptId());
        input.setReturnDate(request.returnDate());
        input.setReturnReason(request.returnReason());
        input.setRejectionSummary(request.rejectionSummary());
        input.setNotes(request.notes());
        input.setInternalNotes(request.internalNotes());
        input.setLines(toLineInputs(request.lines()));
        input.setUserId(userId);
        return input;
    }

    public UpdateReturnToVendorInput toUpdateInput(UpdateReturnToVendorWebRequest request, String userId) {
        UpdateReturnToVendorInput input = new UpdateReturnToVendorInput();
        input.setReturnDate(request.returnDate());
        input.setReturnReason(request.returnReason());
        input.setRejectionSummary(request.rejectionSummary());
        input.setNotes(request.notes());
        input.setInternalNotes(request.internalNotes());
        input.setLines(request.lines() != null ? toLineInputs(request.lines()) : null);
        input.setUserId(userId);
        return input;
    }

    public ReturnToVendorWebResponse toWebResponse(ReturnToVendorOutput output) {
        return new ReturnToVendorWebResponse(
                output.getId(),
                output.getReturnCode(),
                output.getGoodsReceiptId(),
                output.getGoodsReceiptCode(),
                output.getPurchaseOrderId(),
                output.getPurchaseOrderCode(),
                output.getSupplierId(),
                output.getSupplierName(),
                output.getSupplierCode(),
                output.getCurrencyCode(),
                output.getTotalValue(),
                output.getTotalQuantity(),
                output.getStatus(),
                output.getResolutionType(),
                output.getReturnDate(),
                output.getResolutionDate(),
                output.getReturnReason(),
                output.getSupplierResponse(),
                output.getRejectionSummary(),
                output.getCreditNoteReference(),
                output.getCreditNoteAmount(),
                output.getReplacementPurchaseOrderReference(),
                output.getNotes(),
                output.getInternalNotes(),
                output.getCreatedBy(),
                output.getCreatedAt(),
                output.getUpdatedBy(),
                output.getUpdatedAt(),
                output.getLines() != null ? output.getLines().stream().map(this::toLineResponse).toList() : List.of());
    }

    public List<ReturnToVendorWebResponse> toWebResponseList(List<ReturnToVendorOutput> outputs) {
        return outputs.stream().map(this::toWebResponse).toList();
    }

    private ReturnToVendorLineWebResponse toLineResponse(ReturnToVendorLineOutput line) {
        return new ReturnToVendorLineWebResponse(
                line.getId(),
                line.getLineNumber(),
                line.getGoodsReceiptLineId(),
                line.getPurchaseOrderLineId(),
                line.getMaterialId(),
                line.getMaterialCode(),
                line.getMaterialName(),
                line.getUnitOfMeasure(),
                line.getRejectedQuantity(),
                line.getQuantityToReturn(),
                line.getQuantityAlreadyReturned(),
                line.getRemainingQuantity(),
                line.getUnitPrice(),
                line.getLineValue(),
                line.getRejectionReason(),
                line.getQualityNotes(),
                line.getDefectDescription(),
                line.isReplaced(),
                line.isCreditNote(),
                line.getNotes());
    }

    private List<ReturnToVendorLineInput> toLineInputs(List<ReturnToVendorLineWebRequest> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(line -> {
            ReturnToVendorLineInput input = new ReturnToVendorLineInput(
                    line.goodsReceiptLineId(), line.quantityToReturn(), line.rejectionReason());
            input.setDefectDescription(line.defectDescription());
            input.setQualityNotes(line.qualityNotes());
            input.setNotes(line.notes());
            return input;
        }).toList();
    }
}
