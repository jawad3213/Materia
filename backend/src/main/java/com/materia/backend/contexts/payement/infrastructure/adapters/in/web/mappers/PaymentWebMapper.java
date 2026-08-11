package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.payement.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payement.application.dtos.CreatePaymentLineInput;
import com.materia.backend.contexts.payement.application.dtos.PaymentLineOutput;
import com.materia.backend.contexts.payement.application.dtos.PaymentOutput;
import com.materia.backend.contexts.payement.application.dtos.UpdatePaymentInput;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.CreatePaymentLineWebRequest;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.CreatePaymentWebRequest;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.PaymentLineWebResponse;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.PaymentWebResponse;
import com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos.UpdatePaymentWebRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentWebMapper {

    // ============================================================
    // TO APP REQUESTS
    // ============================================================

    public CreatePaymentInput toAppCreateRequest(CreatePaymentWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        CreatePaymentInput appRequest = new CreatePaymentInput();
        appRequest.setSupplierId(webRequest.getSupplierId());
        appRequest.setSupplierName(webRequest.getSupplierName());
        appRequest.setSupplierCode(webRequest.getSupplierCode());
        appRequest.setTotalAmount(webRequest.getTotalAmount());
        appRequest.setCurrencyCode(webRequest.getCurrencyCode());
        appRequest.setNotes(webRequest.getNotes());
        appRequest.setInternalNotes(webRequest.getInternalNotes());
        appRequest.setUserId(webRequest.getUserId());
        
        if (webRequest.getLines() != null) {
            appRequest.setLines(webRequest.getLines().stream()
                    .map(this::toAppCreateLineRequest)
                    .collect(Collectors.toList()));
        }

        return appRequest;
    }

    private CreatePaymentLineInput toAppCreateLineRequest(CreatePaymentLineWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        CreatePaymentLineInput appRequest = new CreatePaymentLineInput();
        appRequest.setInvoiceId(webRequest.getInvoiceId());
        appRequest.setInvoiceCode(webRequest.getInvoiceCode());
        appRequest.setSupplierId(webRequest.getSupplierId());
        appRequest.setSupplierName(webRequest.getSupplierName());
        appRequest.setAmount(webRequest.getAmount());
        appRequest.setNotes(webRequest.getNotes());

        return appRequest;
    }

    public UpdatePaymentInput toAppUpdateRequest(UpdatePaymentWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        UpdatePaymentInput appRequest = new UpdatePaymentInput();
        appRequest.setNotes(webRequest.getNotes());
        appRequest.setInternalNotes(webRequest.getInternalNotes());
        appRequest.setUserId(webRequest.getUserId());

        return appRequest;
    }

    // ============================================================
    // TO WEB RESPONSES
    // ============================================================

    public PaymentWebResponse toWebResponse(PaymentOutput appResponse) {
        if (appResponse == null) {
            return null;
        }

        PaymentWebResponse webResponse = new PaymentWebResponse();
        webResponse.setId(appResponse.getId());
        webResponse.setPaymentCode(appResponse.getPaymentCode());
        webResponse.setSupplierId(appResponse.getSupplierId());
        webResponse.setSupplierName(appResponse.getSupplierName());
        webResponse.setSupplierCode(appResponse.getSupplierCode());
        webResponse.setStatus(appResponse.getStatus() != null ? appResponse.getStatus().name() : null);
        
        webResponse.setTotalAmount(appResponse.getTotalAmount());
        webResponse.setPaidAmount(appResponse.getPaidAmount());
        webResponse.setCurrencyCode(appResponse.getCurrencyCode());
        
        webResponse.setPaymentDate(appResponse.getPaymentDate());
        webResponse.setConfirmedDate(appResponse.getConfirmedDate());
        
        webResponse.setBankReference(appResponse.getBankReference());
        webResponse.setTransactionId(appResponse.getTransactionId());
        webResponse.setPaymentMethod(appResponse.getPaymentMethod());
        webResponse.setPaymentReceipt(appResponse.getPaymentReceipt());
        
        webResponse.setNotes(appResponse.getNotes());
        webResponse.setInternalNotes(appResponse.getInternalNotes());
        
        webResponse.setCreatedAt(appResponse.getCreatedAt());
        webResponse.setCreatedBy(appResponse.getCreatedBy());
        webResponse.setUpdatedAt(appResponse.getUpdatedAt());
        webResponse.setUpdatedBy(appResponse.getUpdatedBy());
        
        if (appResponse.getLines() != null) {
            webResponse.setLines(appResponse.getLines().stream()
                    .map(this::toWebLineResponse)
                    .collect(Collectors.toList()));
        }

        return webResponse;
    }

    private PaymentLineWebResponse toWebLineResponse(PaymentLineOutput appResponse) {
        if (appResponse == null) {
            return null;
        }

        PaymentLineWebResponse webResponse = new PaymentLineWebResponse();
        webResponse.setId(appResponse.getId());
        webResponse.setLineNumber(appResponse.getLineNumber());
        webResponse.setInvoiceId(appResponse.getInvoiceId());
        webResponse.setInvoiceCode(appResponse.getInvoiceCode());
        webResponse.setSupplierId(appResponse.getSupplierId());
        webResponse.setSupplierName(appResponse.getSupplierName());
        
        webResponse.setAmount(appResponse.getAmount());
        webResponse.setPaidAmount(appResponse.getPaidAmount());
        webResponse.setCurrencyCode(appResponse.getCurrencyCode());
        
        webResponse.setPaid(appResponse.isPaid());
        webResponse.setNotes(appResponse.getNotes());

        return webResponse;
    }

    public List<PaymentWebResponse> toWebResponseList(List<PaymentOutput> appResponses) {
        if (appResponses == null) {
            return new ArrayList<>();
        }
        return appResponses.stream()
                .map(this::toWebResponse)
                .collect(Collectors.toList());
    }
}
