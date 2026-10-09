package com.materia.backend.contexts.invoice.infrastructure.adapters.out.settings;

import com.materia.backend.contexts.invoice.domain.ports.out.PriceToleranceSetting;
import com.materia.backend.contexts.settings.domain.ports.in.SettingsUseCase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/** Gives invoice matching the price tolerance stored on the Settings page, through the settings module's public API. */
@Component
public class SettingsPriceTolerance implements PriceToleranceSetting {

    /** Must match the settings module's SettingKey.INVOICE_PRICE_TOLERANCE. */
    static final String KEY = "invoice.priceTolerancePercent";

    private final SettingsUseCase settings;

    public SettingsPriceTolerance(SettingsUseCase settings) {
        this.settings = settings;
    }

    @Override
    public Optional<BigDecimal> current() {
        return settings.storedValue(KEY).flatMap(value -> {
            try {
                return Optional.of(new BigDecimal(value.trim()));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }
}
