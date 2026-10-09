package com.materia.backend.contexts.invoice.infrastructure.adapters.out.settings;

import com.materia.backend.contexts.settings.domain.SettingKey;
import com.materia.backend.contexts.settings.domain.ports.in.SettingsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingsPriceToleranceTest {

    @Test
    @DisplayName("tolerance: reads the key the Settings page stores it under")
    void key_matchesTheSettingsPage() {
        assertEquals(SettingKey.INVOICE_PRICE_TOLERANCE.key(), SettingsPriceTolerance.KEY);
    }

    @Test
    @DisplayName("tolerance: a stored number is used; none stored, or not a number, falls back to the configured one")
    void current_parsesOrFallsBack() {
        SettingsUseCase settings = mock(SettingsUseCase.class);
        SettingsPriceTolerance tolerance = new SettingsPriceTolerance(settings);

        when(settings.storedValue(SettingsPriceTolerance.KEY)).thenReturn(Optional.of(" 3.5 "));
        assertEquals(0, new BigDecimal("3.5").compareTo(tolerance.current().orElseThrow()));

        when(settings.storedValue(SettingsPriceTolerance.KEY)).thenReturn(Optional.empty());
        assertTrue(tolerance.current().isEmpty());

        when(settings.storedValue(SettingsPriceTolerance.KEY)).thenReturn(Optional.of("abc"));
        assertTrue(tolerance.current().isEmpty());
    }
}
