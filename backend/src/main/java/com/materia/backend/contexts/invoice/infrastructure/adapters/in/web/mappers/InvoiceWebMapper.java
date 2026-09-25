package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.mappers;

import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceLineOutput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.CreateInvoiceWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceLineWebRequest;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceLineWebResponse;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.InvoiceWebResponse;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.dtos.invoice.UpdateInvoiceWebRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web mapper for invoices.
 */
@Component
public class InvoiceWebMapper implements BaseWebMapper<
        CreateInvoiceWebRequest,
        UpdateInvoiceWebRequest,
        CreateInvoiceInput,
        UpdateInvoiceInput,
        InvoiceWebResponse,
        InvoiceOutput> {

    @Override
    public CreateInvoiceInput toAppCreateRequest(CreateInvoiceWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        CreateInvoiceInput request = new CreateInvoiceInput();
        BeanUtils.copyProperties(webRequest, request, "lines", "createdBy", "invoiceType");
        request.setInvoiceType(
                webRequest.getInvoiceType() != null
                        ? com.materia.backend.contexts.invoice.domain.enums.InvoiceType.valueOf(webRequest.getInvoiceType())
                        : null
        );
        request.setLines(toAppLines(webRequest.getLines(), webRequest.getCurrencyCode()));
        request.setUserId(webRequest.getCreatedBy());
        return request;
    }

    @Override
    public UpdateInvoiceInput toAppUpdateRequest(UpdateInvoiceWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        UpdateInvoiceInput request = new UpdateInvoiceInput();
        BeanUtils.copyProperties(webRequest, request, "lines", "updatedBy", "invoiceType");
        if (webRequest.getInvoiceType() != null) {
            request.setInvoiceType(
                    com.materia.backend.contexts.invoice.domain.enums.InvoiceType.valueOf(webRequest.getInvoiceType())
            );
        }
        request.setLines(toAppLines(webRequest.getLines(), webRequest.getCurrencyCode()));
        request.setUserId(webRequest.getUpdatedBy());
        return request;
    }

    @Override
    public InvoiceWebResponse toWebResponse(InvoiceOutput appResponse) {
        if (appResponse == null) {
            return null;
        }

        InvoiceWebResponse response = new InvoiceWebResponse();
        BeanUtils.copyProperties(
                appResponse,
                response,
                "totalAmount",
                "totalTaxAmount",
                "totalAmountWithTax",
                "paidAmount",
                "invoiceType",
                "status",
                "lines"
        );
        response.setInvoiceType(appResponse.getInvoiceType() != null ? appResponse.getInvoiceType().name() : null);
        response.setStatus(appResponse.getStatus() != null ? appResponse.getStatus().name() : null);
        response.setTotalAmount(formatMoney(appResponse.getTotalAmount()));
        response.setTotalTaxAmount(formatMoney(appResponse.getTotalTaxAmount()));
        response.setTotalAmountWithTax(formatMoney(appResponse.getTotalAmountWithTax()));
        response.setPaidAmount(formatMoney(appResponse.getPaidAmount()));
        response.setLines(toWebLines(appResponse.getLines()));
        return response;
    }

    // ---- Line Mapping ----

    private List<InvoiceLineInput> toAppLines(List<InvoiceLineWebRequest> webLines, String defaultCurrencyCode) {
        if (webLines == null) {
            return new ArrayList<>();
        }

        return webLines.stream()
                .map(line -> toAppLine(line, defaultCurrencyCode))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private InvoiceLineInput toAppLine(InvoiceLineWebRequest webLine, String defaultCurrencyCode) {
        if (webLine == null) {
            return null;
        }

        InvoiceLineInput line = new InvoiceLineInput();
        BeanUtils.copyProperties(webLine, line, "unitPrice", "taxAmount", "quantityInvoiced");
        line.setQuantityInvoiced(webLine.getQuantityInvoiced() != null ? webLine.getQuantityInvoiced().intValue() : null);
        line.setUnitPrice(toMoney(webLine.getUnitPrice(), defaultCurrencyCode, defaultCurrencyCode));
        line.setTaxAmount(toMoney(webLine.getTaxAmount(), defaultCurrencyCode, defaultCurrencyCode));
        return line;
    }

    private List<InvoiceLineWebResponse> toWebLines(List<InvoiceLineOutput> appLines) {
        if (appLines == null) {
            return new ArrayList<>();
        }

        return appLines.stream()
                .map(this::toWebLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private InvoiceLineWebResponse toWebLine(InvoiceLineOutput appLine) {
        if (appLine == null) {
            return null;
        }

        InvoiceLineWebResponse response = new InvoiceLineWebResponse();
        BeanUtils.copyProperties(appLine, response, "unitPrice", "lineTotal", "taxAmount", "lineTotalWithTax");
        response.setUnitPrice(formatMoney(appLine.getUnitPrice()));
        response.setLineTotal(formatMoney(appLine.getLineTotal()));
        response.setTaxAmount(formatMoney(appLine.getTaxAmount()));
        response.setLineTotalWithTax(formatMoney(appLine.getLineTotalWithTax()));
        return response;
    }

    // ---- Helper Methods ----

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

    private BigDecimal formatMoney(Money money) {
        return money != null ? money.getAmount() : null;
    }
}
