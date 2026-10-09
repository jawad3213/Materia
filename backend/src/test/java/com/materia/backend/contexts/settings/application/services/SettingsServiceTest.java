package com.materia.backend.contexts.settings.application.services;

import com.materia.backend.contexts.settings.application.dtos.SettingOutput;
import com.materia.backend.contexts.settings.domain.SettingKey;
import com.materia.backend.contexts.settings.domain.ports.out.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Settings: defaults, validation, and only real changes written (so the history stays meaningful). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SettingsServiceTest {

    @Mock private SettingsRepository repository;
    private SettingsService service;

    @BeforeEach
    void setUp() {
        service = new SettingsService(repository, new BigDecimal("2.00"));
        when(repository.findAll()).thenReturn(Map.of("company.name", "Atlas Industries"));
    }

    private static SettingOutput byKey(List<SettingOutput> settings, String key) {
        return settings.stream().filter(s -> s.key().equals(key)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("rule: every setting is listed with its stored value or its default; the tolerance defaults to the configured 2%")
    void getAll_valuesAndDefaults() {
        List<SettingOutput> settings = service.getAll();

        assertThat(settings).hasSize(SettingKey.values().length);
        assertThat(byKey(settings, "company.name").value()).isEqualTo("Atlas Industries");
        assertThat(byKey(settings, "company.name").customized()).isTrue();
        assertThat(byKey(settings, "invoice.priceTolerancePercent").value()).isEqualTo("2");
        assertThat(byKey(settings, "invoice.priceTolerancePercent").customized()).isFalse();
    }

    @Test
    @DisplayName("rule: valid values are normalised and saved; a value equal to the one in effect is not written")
    void update_savesRealChanges() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("company.name", " Atlas Industries ");
        values.put("company.taxId", "001525874000045");
        values.put("invoice.priceTolerancePercent", "3,50");

        service.update(values, "admin-1");

        verify(repository, never()).save(eq("company.name"), anyString(), any());
        verify(repository).save("company.taxId", "001525874000045", "admin-1");
        verify(repository).save("invoice.priceTolerancePercent", "3.5", "admin-1");
    }

    @Test
    @DisplayName("rule: one invalid value or an unknown key saves nothing")
    void update_validatesEverythingFirst() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("company.address", "12 Rue Ibnou Sina, Casablanca");
        values.put("invoice.priceTolerancePercent", "75");

        assertThatThrownBy(() -> service.update(values, "admin-1")).hasMessageContaining("between 0 and 50");
        assertThatThrownBy(() -> service.update(Map.of("theme", "dark"), "admin-1")).hasMessageContaining("Unknown setting");
        assertThatThrownBy(() -> service.update(Map.of(), "admin-1")).hasMessageContaining("No setting");
        verify(repository, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("rule: each kind of setting checks its format")
    void keys_validateFormats() {
        assertThatThrownBy(() -> SettingKey.COMPANY_EMAIL.normalize("not-an-email")).hasMessageContaining("e-mail");
        assertThatThrownBy(() -> SettingKey.COMPANY_PHONE.normalize("call us")).hasMessageContaining("digits");
        assertThatThrownBy(() -> SettingKey.COMPANY_TAX_ID.normalize("1234")).hasMessageContaining("15 digits");
        assertThatThrownBy(() -> SettingKey.INVOICE_PRICE_TOLERANCE.normalize("abc")).hasMessageContaining("number");
        assertThatThrownBy(() -> SettingKey.INVOICE_PRICE_TOLERANCE.normalize("")).hasMessageContaining("required");
        assertThatThrownBy(() -> SettingKey.COMPANY_NAME.normalize("x".repeat(201))).hasMessageContaining("200");
        assertThat(SettingKey.COMPANY_EMAIL.normalize(" achats@atlas.ma ")).isEqualTo("achats@atlas.ma");
        assertThat(SettingKey.COMPANY_ADDRESS.normalize("   ")).isEmpty();
    }
}
