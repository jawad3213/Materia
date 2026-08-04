package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.mappers;

import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import com.materia.backend.contexts.goodsReceipt.application.dtos.CreateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineInput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptLineOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.GoodsReceiptOutput;
import com.materia.backend.contexts.goodsReceipt.application.dtos.UpdateGoodsReceiptInput;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.CreateGoodsReceiptWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.GoodsReceiptLineWebRequest;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.GoodsReceiptLineWebResponse;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.GoodsReceiptWebResponse;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos.UpdateGoodsReceiptWebRequest;
import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Maps goods receipt web DTOs to the application layer. */
@Component
public class GoodsReceiptWebMapper implements BaseWebMapper<
        CreateGoodsReceiptWebRequest,
        UpdateGoodsReceiptWebRequest,
        CreateGoodsReceiptInput,
        UpdateGoodsReceiptInput,
        GoodsReceiptWebResponse,
        GoodsReceiptOutput> {

    @Override
    public CreateGoodsReceiptInput toAppCreateRequest(CreateGoodsReceiptWebRequest webRequest) {
        if (webRequest == null) return null;
        CreateGoodsReceiptInput input = new CreateGoodsReceiptInput();
        copyReceiptFields(webRequest.getReceiptCode(), webRequest.getPurchaseOrderId(), webRequest.getPurchaseOrderCode(),
                webRequest.getReceiptDate(), webRequest.getExpectedDeliveryDate(), webRequest.getReceivedBy(),
                webRequest.getReceivedByName(), webRequest.getNotes(), webRequest.getSupplierId(),
                webRequest.getSupplierName(), webRequest.getDiscrepancyNotes(), webRequest.getLines(), input);
        input.setUserId(webRequest.getUserId());
        return input;
    }

    @Override
    public UpdateGoodsReceiptInput toAppUpdateRequest(UpdateGoodsReceiptWebRequest webRequest) {
        if (webRequest == null) return null;
        UpdateGoodsReceiptInput input = new UpdateGoodsReceiptInput();
        copyReceiptFields(webRequest.getReceiptCode(), webRequest.getPurchaseOrderId(), webRequest.getPurchaseOrderCode(),
                webRequest.getReceiptDate(), webRequest.getExpectedDeliveryDate(), webRequest.getReceivedBy(),
                webRequest.getReceivedByName(), webRequest.getNotes(), webRequest.getSupplierId(),
                webRequest.getSupplierName(), webRequest.getDiscrepancyNotes(), webRequest.getLines(), input);
        input.setUserId(webRequest.getUserId());
        return input;
    }

    @Override
    public GoodsReceiptWebResponse toWebResponse(GoodsReceiptOutput appResponse) {
        if (appResponse == null) return null;
        GoodsReceiptWebResponse response = new GoodsReceiptWebResponse();
        response.setSuccess(appResponse.isSuccess());
        response.setMessage(appResponse.getMessage());
        response.setErrorCode(appResponse.getErrorCode());
        response.setId(appResponse.getId());
        response.setReceiptCode(appResponse.getReceiptCode());
        response.setPurchaseOrderId(appResponse.getPurchaseOrderId());
        response.setPurchaseOrderCode(appResponse.getPurchaseOrderCode());
        response.setStatus(appResponse.getStatus());
        response.setReceiptDate(appResponse.getReceiptDate());
        response.setExpectedDeliveryDate(appResponse.getExpectedDeliveryDate());
        response.setReceivedBy(appResponse.getReceivedBy());
        response.setReceivedByName(appResponse.getReceivedByName());
        response.setNotes(appResponse.getNotes());
        response.setSupplierId(appResponse.getSupplierId());
        response.setSupplierName(appResponse.getSupplierName());
        response.setTotalQuantityOrdered(appResponse.getTotalQuantityOrdered());
        response.setTotalQuantityReceived(appResponse.getTotalQuantityReceived());
        response.setTotalQuantityRejected(appResponse.getTotalQuantityRejected());
        response.setTotalQuantityAccepted(appResponse.getTotalQuantityAccepted());
        response.setHasDiscrepancy(appResponse.isHasDiscrepancy());
        response.setDiscrepancyNotes(appResponse.getDiscrepancyNotes());
        response.setCreatedBy(appResponse.getCreatedBy());
        response.setCreatedAt(appResponse.getCreatedAt());
        response.setUpdatedBy(appResponse.getUpdatedBy());
        response.setUpdatedAt(appResponse.getUpdatedAt());
        response.setLines(toWebLines(appResponse.getLines()));
        return response;
    }

    public GoodsReceiptLineInput toAppLine(GoodsReceiptLineWebRequest webLine) {
        if (webLine == null) return null;
        GoodsReceiptLineInput input = new GoodsReceiptLineInput();
        input.setId(webLine.getId());
        input.setLineNumber(webLine.getLineNumber());
        input.setPurchaseOrderLineId(webLine.getPurchaseOrderLineId());
        input.setMaterialCode(webLine.getMaterialCode());
        input.setMaterialId(webLine.getMaterialId());
        input.setMaterialName(webLine.getMaterialName());
        input.setUnitOfMeasure(webLine.getUnitOfMeasure());
        input.setQuantityOrdered(webLine.getQuantityOrdered());
        input.setQuantityReceived(webLine.getQuantityReceived());
        input.setQuantityRejected(webLine.getQuantityRejected());
        input.setQualityStatus(webLine.getQualityStatus());
        input.setQualityNotes(webLine.getQualityNotes());
        input.setRejectionReason(webLine.getRejectionReason());
        input.setStockBefore(webLine.getStockBefore());
        input.setStockAfter(webLine.getStockAfter());
        input.setUnitPrice(toMoney(webLine.getUnitPrice(), webLine.getCurrencyCode()));
        input.setSupplierId(webLine.getSupplierId());
        input.setSupplierName(webLine.getSupplierName());
        input.setBatchNumber(webLine.getBatchNumber());
        input.setExpiryDate(webLine.getExpiryDate());
        input.setStorageLocation(webLine.getStorageLocation());
        input.setNotes(webLine.getNotes());
        return input;
    }

    private void copyReceiptFields(String receiptCode, String purchaseOrderId, String purchaseOrderCode,
                                   java.time.LocalDate receiptDate, java.time.LocalDate expectedDeliveryDate,
                                   String receivedBy, String receivedByName, String notes, String supplierId,
                                   String supplierName, String discrepancyNotes,
                                   List<GoodsReceiptLineWebRequest> lines, CreateGoodsReceiptInput input) {
        input.setReceiptCode(receiptCode);
        input.setPurchaseOrderId(purchaseOrderId);
        input.setPurchaseOrderCode(purchaseOrderCode);
        input.setReceiptDate(receiptDate);
        input.setExpectedDeliveryDate(expectedDeliveryDate);
        input.setReceivedBy(receivedBy);
        input.setReceivedByName(receivedByName);
        input.setNotes(notes);
        input.setSupplierId(supplierId);
        input.setSupplierName(supplierName);
        input.setDiscrepancyNotes(discrepancyNotes);
        input.setLines(toAppLines(lines));
    }

    private List<GoodsReceiptLineInput> toAppLines(List<GoodsReceiptLineWebRequest> webLines) {
        if (webLines == null) return new ArrayList<>();
        return webLines.stream().map(this::toAppLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<GoodsReceiptLineWebResponse> toWebLines(List<GoodsReceiptLineOutput> appLines) {
        if (appLines == null) return new ArrayList<>();
        return appLines.stream().map(this::toWebLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private GoodsReceiptLineWebResponse toWebLine(GoodsReceiptLineOutput line) {
        if (line == null) return null;
        GoodsReceiptLineWebResponse response = new GoodsReceiptLineWebResponse();
        response.setId(line.getId()); response.setLineNumber(line.getLineNumber());
        response.setPurchaseOrderLineId(line.getPurchaseOrderLineId()); response.setMaterialCode(line.getMaterialCode());
        response.setMaterialId(line.getMaterialId()); response.setMaterialName(line.getMaterialName());
        response.setUnitOfMeasure(line.getUnitOfMeasure()); response.setQuantityOrdered(line.getQuantityOrdered());
        response.setQuantityReceived(line.getQuantityReceived()); response.setQuantityRejected(line.getQuantityRejected());
        response.setQuantityAccepted(line.getQuantityAccepted()); response.setQuantityPending(line.getQuantityPending());
        response.setQualityStatus(line.getQualityStatus()); response.setQualityNotes(line.getQualityNotes());
        response.setRejectionReason(line.getRejectionReason()); response.setStockBefore(line.getStockBefore());
        response.setStockAfter(line.getStockAfter()); response.setUnitPrice(formatMoney(line.getUnitPrice()));
        response.setLineTotal(formatMoney(line.getLineTotal())); response.setSupplierId(line.getSupplierId());
        response.setSupplierName(line.getSupplierName()); response.setBatchNumber(line.getBatchNumber());
        response.setExpiryDate(line.getExpiryDate()); response.setStorageLocation(line.getStorageLocation());
        response.setNotes(line.getNotes());
        return response;
    }

    private Money toMoney(BigDecimal amount, String currencyCode) {
        if (amount == null) return null;
        CurrencyCode currency = currencyCode == null || currencyCode.isBlank()
                ? CurrencyCode.MAD : CurrencyCode.fromCode(currencyCode);
        return Money.of(amount, currency);
    }

    private String formatMoney(Money money) {
        return money != null ? money.format() : null;
    }
}
