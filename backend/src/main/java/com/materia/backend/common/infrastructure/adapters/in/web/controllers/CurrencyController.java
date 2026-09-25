package com.materia.backend.common.infrastructure.adapters.in.web.controllers;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.services.ExchangeRateService;
import com.materia.backend.common.domain.valueObjects.ExchangeRate;
import com.materia.backend.common.domain.valueObjects.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/currencies")
@Tag(name = "Currencies & Exchange Rates", description = "Currency information, live exchange rates and conversions")
public class CurrencyController {

    private final ExchangeRateService exchangeRateService;

    public CurrencyController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    @Operation(summary = "Get list of supported currencies")
    public ResponseEntity<List<CurrencyInfoResponse>> getSupportedCurrencies() {
        List<CurrencyInfoResponse> currencies = Arrays.stream(CurrencyCode.values())
                .map(c -> new CurrencyInfoResponse(
                        c.name(),
                        c.getLabel(),
                        c.getSymbol(),
                        c.getDecimalPlaces(),
                        c == CurrencyCode.getDefault()
                ))
                .toList();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/rates")
    @Operation(summary = "Get latest exchange rates for a base currency")
    public ResponseEntity<Map<CurrencyCode, BigDecimal>> getLatestRates(
            @RequestParam(defaultValue = "EUR") CurrencyCode base) {
        return ResponseEntity.ok(exchangeRateService.getLatestRates(base));
    }

    @GetMapping("/pair")
    @Operation(summary = "Get exchange rate between two currencies using ExchangeRate-API")
    public ResponseEntity<ExchangeRate> getExchangeRate(
            @RequestParam CurrencyCode from,
            @RequestParam CurrencyCode to) {
        return ResponseEntity.ok(exchangeRateService.getExchangeRate(from, to));
    }

    @GetMapping("/convert")
    @Operation(summary = "Convert an amount from one currency to another")
    public ResponseEntity<MoneyConversionResponse> convert(
            @RequestParam BigDecimal amount,
            @RequestParam CurrencyCode from,
            @RequestParam CurrencyCode to) {
        Money original = Money.of(amount, from);
        Money converted = exchangeRateService.convert(original, to);
        ExchangeRate rate = exchangeRateService.getExchangeRate(from, to);

        return ResponseEntity.ok(new MoneyConversionResponse(
                original.getAmount(),
                original.getCurrency(),
                converted.getAmount(),
                converted.getCurrency(),
                rate.getRate(),
                rate.getRateDate().toString()
        ));
    }

    public record CurrencyInfoResponse(
            String code,
            String displayName,
            String symbol,
            int decimalPlaces,
            boolean isDefault
    ) {}

    public record MoneyConversionResponse(
            BigDecimal originalAmount,
            CurrencyCode originalCurrency,
            BigDecimal convertedAmount,
            CurrencyCode targetCurrency,
            BigDecimal rate,
            String rateDate
    ) {}
}
