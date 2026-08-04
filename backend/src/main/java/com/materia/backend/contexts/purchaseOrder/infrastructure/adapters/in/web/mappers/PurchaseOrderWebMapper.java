package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers;

import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;
import com.materia.backend.contexts.masterData.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderLineOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.CreatePurchaseOrderWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderLineWebRequest;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderLineWebResponse;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.PurchaseOrderWebResponse;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.dtos.purchaseOrder.UpdatePurchaseOrderWebRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web mapper for purchase orders.
 */
@Component
public class PurchaseOrderWebMapper implements BaseWebMapper<
        CreatePurchaseOrderWebRequest,
        UpdatePurchaseOrderWebRequest,
        CreatePurchaseOrderInput,
        UpdatePurchaseOrderInput,
        PurchaseOrderWebResponse,
        PurchaseOrderOutput> {

    @Override
    public CreatePurchaseOrderInput toAppCreateRequest(CreatePurchaseOrderWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        CreatePurchaseOrderInput request = new CreatePurchaseOrderInput();
        BeanUtils.copyProperties(webRequest, request, "taxAmount", "shippingCost", "lines", "createdBy");
        request.setTaxAmount(toMoney(webRequest.getTaxAmount(), webRequest.getCurrencyCode(), webRequest.getCurrencyCode()));
        request.setShippingCost(toMoney(webRequest.getShippingCost(), webRequest.getCurrencyCode(), webRequest.getCurrencyCode()));
        request.setLines(toAppLines(webRequest.getLines(), webRequest.getCurrencyCode()));
        request.setUserId(webRequest.getCreatedBy());
        return request;
    }

    @Override
    public UpdatePurchaseOrderInput toAppUpdateRequest(UpdatePurchaseOrderWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        UpdatePurchaseOrderInput request = new UpdatePurchaseOrderInput();
        BeanUtils.copyProperties(webRequest, request, "taxAmount", "shippingCost", "lines", "updatedBy");
        request.setTaxAmount(toMoney(webRequest.getTaxAmount(), webRequest.getCurrencyCode(), null));
        request.setShippingCost(toMoney(webRequest.getShippingCost(), webRequest.getCurrencyCode(), null));
        request.setLines(toAppLines(webRequest.getLines(), webRequest.getCurrencyCode()));
        request.setUserId(webRequest.getUpdatedBy());
        return request;
    }

    @Override
    public PurchaseOrderWebResponse toWebResponse(PurchaseOrderOutput appResponse) {
        if (appResponse == null) {
            return null;
        }

        PurchaseOrderWebResponse response = new PurchaseOrderWebResponse();
        BeanUtils.copyProperties(
                appResponse,
                response,
                "totalAmount",
                "taxAmount",
                "shippingCost",
                "grandTotal",
                "lines"
        );
        response.setTotalAmount(formatMoney(appResponse.getTotalAmount()));
        response.setTaxAmount(formatMoney(appResponse.getTaxAmount()));
        response.setShippingCost(formatMoney(appResponse.getShippingCost()));
        response.setGrandTotal(formatMoney(appResponse.getGrandTotal()));
        response.setLines(toWebLines(appResponse.getLines()));
        return response;
    }

    private List<PurchaseOrderLineInput> toAppLines(List<PurchaseOrderLineWebRequest> webLines, String defaultCurrencyCode) {
        if (webLines == null) {
            return new ArrayList<>();
        }

        return webLines.stream()
                .map(line -> toAppLine(line, defaultCurrencyCode))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PurchaseOrderLineInput toAppLine(PurchaseOrderLineWebRequest webLine, String defaultCurrencyCode) {
        if (webLine == null) {
            return null;
        }

        PurchaseOrderLineInput line = new PurchaseOrderLineInput();
        BeanUtils.copyProperties(webLine, line, "unitPrice", "currencyCode");
        String resolvedCurrencyCode = resolveCurrencyCode(webLine.getCurrencyCode(), defaultCurrencyCode);
        line.setUnitPrice(toMoney(webLine.getUnitPrice(), resolvedCurrencyCode, defaultCurrencyCode));
        line.setCurrencyCode(resolvedCurrencyCode);
        return line;
    }

    private List<PurchaseOrderLineWebResponse> toWebLines(List<PurchaseOrderLineOutput> appLines) {
        if (appLines == null) {
            return new ArrayList<>();
        }

        return appLines.stream()
                .map(this::toWebLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PurchaseOrderLineWebResponse toWebLine(PurchaseOrderLineOutput appLine) {
        if (appLine == null) {
            return null;
        }

        PurchaseOrderLineWebResponse response = new PurchaseOrderLineWebResponse();
        BeanUtils.copyProperties(appLine, response, "unitPrice", "lineTotal");
        response.setUnitPrice(formatMoney(appLine.getUnitPrice()));
        response.setLineTotal(formatMoney(appLine.getLineTotal()));
        return response;
    }

    private Money toMoney(BigDecimal amount, String currencyCode, String fallbackCurrencyCode) {
        if (amount == null) {
            return null;
        }

        String resolvedCurrencyCode = resolveCurrencyCode(currencyCode, fallbackCurrencyCode);
        return Money.of(amount, CurrencyCode.valueOf(resolvedCurrencyCode));
    }

    private String resolveCurrencyCode(String currencyCode, String fallbackCurrencyCode) {
        if (StringUtils.hasText(currencyCode)) {
            return currencyCode.trim().toUpperCase();
        }
        if (StringUtils.hasText(fallbackCurrencyCode)) {
            return fallbackCurrencyCode.trim().toUpperCase();
        }
        return CurrencyCode.MAD.name();
    }

    private String formatMoney(Money money) {
        return money != null ? money.format() : null;
    }
}
