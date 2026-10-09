package com.materia.backend.contexts.invoice.application.services;

import com.materia.backend.contexts.invoice.domain.ports.out.InvoicedOrders;
import com.materia.backend.contexts.invoice.application.mappers.InvoiceMapper;
import com.materia.backend.contexts.invoice.domain.ports.out.InvoiceRepository;
import com.materia.backend.contexts.invoice.domain.ports.out.PriceToleranceSetting;
import com.materia.backend.contexts.invoice.domain.ports.out.UserDirectory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** The price tolerance used by invoice matching: the Settings page value when set, else the configured one. */
class InvoicePriceToleranceTest {

    private InvoiceService service() {
        InvoiceService service = new InvoiceService(mock(InvoiceRepository.class), new InvoiceMapper(),
                mock(InvoiceCodeGeneratorService.class), mock(InvoicedOrders.class), mock(UserDirectory.class));
        service.setPriceTolerancePercent(new BigDecimal("2"));
        return service;
    }

    @Test
    @DisplayName("rule: the tolerance set on the Settings page replaces the configured one")
    void settingWins() {
        InvoiceService service = service();
        PriceToleranceSetting setting = () -> Optional.of(new BigDecimal("5"));
        service.setPriceToleranceSetting(setting);

        assertThat(service.effectivePriceTolerance()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("rule: without a stored setting (or without the settings module) the configured tolerance applies")
    void configuredOtherwise() {
        InvoiceService service = service();
        assertThat(service.effectivePriceTolerance()).isEqualByComparingTo("2");

        service.setPriceToleranceSetting(Optional::empty);
        assertThat(service.effectivePriceTolerance()).isEqualByComparingTo("2");
    }
}
