package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.mappers;

import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import com.materia.backend.contexts.returnToVendor.application.dtos.*;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReturnToVendorWebMapper implements BaseWebMapper<
        CreateReturnToVendorWebRequest, 
        UpdateReturnToVendorWebRequest, 
        CreateReturnToVendorInput, 
        UpdateReturnToVendorInput, 
        ReturnToVendorWebResponse, 
        ReturnToVendorOutput> {

    @Override
    public CreateReturnToVendorInput toAppCreateRequest(CreateReturnToVendorWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        CreateReturnToVendorInput input = new CreateReturnToVendorInput();
        input.setReturnCode(webRequest.getReturnCode());
        input.setGoodsReceiptId(webRequest.getGoodsReceiptId());
        input.setGoodsReceiptCode(webRequest.getGoodsReceiptCode());
        input.setPurchaseOrderId(webRequest.getPurchaseOrderId());
        input.setPurchaseOrderCode(webRequest.getPurchaseOrderCode());
        input.setSupplierId(webRequest.getSupplierId());
        input.setSupplierName(webRequest.getSupplierName());
        input.setSupplierCode(webRequest.getSupplierCode());
        input.setReturnDate(webRequest.getReturnDate());
        input.setReturnReason(webRequest.getReturnReason());
        input.setRejectionSummary(webRequest.getRejectionSummary());
        input.setNotes(webRequest.getNotes());
        input.setInternalNotes(webRequest.getInternalNotes());
        input.setLines(toLineInputList(webRequest.getLines()));
        return input;
    }

    @Override
    public UpdateReturnToVendorInput toAppUpdateRequest(UpdateReturnToVendorWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        UpdateReturnToVendorInput input = new UpdateReturnToVendorInput();
        input.setReturnDate(webRequest.getReturnDate());
        input.setReturnReason(webRequest.getReturnReason());
        input.setSupplierResponse(webRequest.getSupplierResponse());
        input.setRejectionSummary(webRequest.getRejectionSummary());
        input.setCreditNoteReference(webRequest.getCreditNoteReference());
        input.setCreditNoteAmount(webRequest.getCreditNoteAmount());
        input.setReplacementPurchaseOrderReference(webRequest.getReplacementPurchaseOrderReference());
        input.setReplacementPurchaseOrderCode(webRequest.getReplacementPurchaseOrderCode());
        input.setNotes(webRequest.getNotes());
        input.setInternalNotes(webRequest.getInternalNotes());
        input.setLines(toLineInputList(webRequest.getLines()));
        return input;
    }

    @Override
    public ReturnToVendorWebResponse toWebResponse(ReturnToVendorOutput appResponse) {
        if (appResponse == null) {
            return null;
        }

        ReturnToVendorWebResponse response = new ReturnToVendorWebResponse();
        response.setId(appResponse.getId());
        response.setReturnCode(appResponse.getReturnCode());
        response.setGoodsReceiptId(appResponse.getGoodsReceiptId());
        response.setGoodsReceiptCode(appResponse.getGoodsReceiptCode());
        response.setPurchaseOrderId(appResponse.getPurchaseOrderId());
        response.setPurchaseOrderCode(appResponse.getPurchaseOrderCode());
        response.setSupplierId(appResponse.getSupplierId());
        response.setSupplierName(appResponse.getSupplierName());
        response.setSupplierCode(appResponse.getSupplierCode());
        response.setStatus(appResponse.getStatus());
        response.setResolutionType(appResponse.getResolutionType());
        response.setReturnDate(appResponse.getReturnDate());
        response.setResolutionDate(appResponse.getResolutionDate());
        response.setReturnReason(appResponse.getReturnReason());
        response.setSupplierResponse(appResponse.getSupplierResponse());
        response.setRejectionSummary(appResponse.getRejectionSummary());
        response.setCreditNoteReference(appResponse.getCreditNoteReference());
        response.setCreditNoteAmount(appResponse.getCreditNoteAmount());
        response.setReplacementPurchaseOrderReference(appResponse.getReplacementPurchaseOrderReference());
        response.setReplacementPurchaseOrderCode(appResponse.getReplacementPurchaseOrderCode());
        response.setNotes(appResponse.getNotes());
        response.setInternalNotes(appResponse.getInternalNotes());
        response.setCreatedBy(appResponse.getCreatedBy());
        response.setCreatedAt(appResponse.getCreatedAt());
        response.setUpdatedBy(appResponse.getUpdatedBy());
        response.setUpdatedAt(appResponse.getUpdatedAt());
        response.setLines(toLineResponseList(appResponse.getLines()));
        return response;
    }

    public ReturnToVendorLineInput toLineInput(ReturnToVendorLineWebRequest request) {
        if (request == null) {
            return null;
        }

        ReturnToVendorLineInput input = new ReturnToVendorLineInput();
        input.setId(request.getId());
        input.setLineNumber(request.getLineNumber());
        input.setGoodsReceiptLineId(request.getGoodsReceiptLineId());
        input.setMaterialCode(request.getMaterialCode());
        input.setMaterialName(request.getMaterialName());
        input.setUnitOfMeasure(request.getUnitOfMeasure());
        input.setRejectedQuantity(request.getRejectedQuantity());
        input.setQuantityToReturn(request.getQuantityToReturn());
        input.setQuantityAlreadyReturned(request.getQuantityAlreadyReturned());
        input.setRejectionReason(request.getRejectionReason());
        input.setQualityNotes(request.getQualityNotes());
        input.setDefectDescription(request.getDefectDescription());
        input.setReplaced(request.isReplaced());
        input.setCreditNote(request.isCreditNote());
        input.setNotes(request.getNotes());
        return input;
    }

    private ReturnToVendorLineWebResponse toLineResponse(ReturnToVendorLineOutput output) {
        if (output == null) {
            return null;
        }

        ReturnToVendorLineWebResponse response = new ReturnToVendorLineWebResponse();
        response.setId(output.getId());
        response.setLineNumber(output.getLineNumber());
        response.setGoodsReceiptLineId(output.getGoodsReceiptLineId());
        response.setMaterialCode(output.getMaterialCode());
        response.setMaterialName(output.getMaterialName());
        response.setUnitOfMeasure(output.getUnitOfMeasure());
        response.setRejectedQuantity(output.getRejectedQuantity());
        response.setQuantityToReturn(output.getQuantityToReturn());
        response.setQuantityAlreadyReturned(output.getQuantityAlreadyReturned());
        response.setRemainingQuantity(output.getRemainingQuantity());
        response.setRejectionReason(output.getRejectionReason());
        response.setQualityNotes(output.getQualityNotes());
        response.setDefectDescription(output.getDefectDescription());
        response.setReplaced(output.isReplaced());
        response.setCreditNote(output.isCreditNote());
        response.setNotes(output.getNotes());
        return response;
    }

    private List<ReturnToVendorLineInput> toLineInputList(List<ReturnToVendorLineWebRequest> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream().map(this::toLineInput).collect(Collectors.toList());
    }

    private List<ReturnToVendorLineWebResponse> toLineResponseList(List<ReturnToVendorLineOutput> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream().map(this::toLineResponse).collect(Collectors.toList());
    }
}
