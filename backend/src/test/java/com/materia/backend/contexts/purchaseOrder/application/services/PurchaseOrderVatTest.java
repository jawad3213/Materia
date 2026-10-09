package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.requisition.RequisitionSourceAdapter;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.out.goodsReceipt.GoodsReceiptRecordsAdapter;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.in.RequisitionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.aLine;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** VAT per order line: each line's VAT from its rate, the order's VAT as their sum, and the default rate. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderVatTest {

    @Mock private PurchaseOrderRepository repository;
    @Mock private PurchaseOrderCodeGeneratorService codeGenerator;
    @Mock private GoodsReceiptUseCase goodsReceiptUseCase;
    @Mock private RequisitionUseCase requisitionUseCase;
    @Mock private PurchaseOrderEventPublisher eventPublisher;
    @Mock private ReceiverDirectory receiverDirectory;

    private PurchaseOrderService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderService(repository, new PurchaseOrderMapper(), codeGenerator,
                new GoodsReceiptRecordsAdapter(goodsReceiptUseCase), new RequisitionSourceAdapter(requisitionUseCase),
                eventPublisher, receiverDirectory);
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PurchaseOrder stored(PurchaseOrder order) {
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        return order;
    }

    private static BigDecimal amount(Money money) {
        return money.getAmount();
    }

    @Test
    @DisplayName("line: the VAT of a line is its total at its rate, to the cent")
    void lineVat_fromRate() {
        PurchaseOrderLine line = aLine(3, "33.33", CurrencyCode.MAD);
        line.setTaxRate(new BigDecimal("20"));
        line.calculateLineTotal();

        assertEquals(0, new BigDecimal("99.99").compareTo(amount(line.getLineTotal())));
        assertEquals(0, new BigDecimal("20.00").compareTo(amount(line.getTaxAmount())), "99.99 x 20% = 19.998, rounded");
    }

    @Test
    @DisplayName("order: with rates on its lines, the order's VAT is the sum of the lines' VAT, in the grand total")
    void orderVat_isTheSumOfItsLines() {
        PurchaseOrderLine standard = aLine(10, "10.00", CurrencyCode.MAD);
        standard.setTaxRate(new BigDecimal("20"));
        standard.calculateLineTotal();
        PurchaseOrderLine reduced = aLine(10, "5.00", CurrencyCode.MAD);
        reduced.setTaxRate(new BigDecimal("10"));
        reduced.calculateLineTotal();
        PurchaseOrder order = anOrder().withLine(standard).withLine(reduced).build();

        order.recalculateTotals();

        assertEquals(0, new BigDecimal("150.00").compareTo(amount(order.getTotalAmount())));
        assertEquals(0, new BigDecimal("25.00").compareTo(amount(order.getTaxAmount())), "100 x 20% + 50 x 10%");
        assertEquals(0, new BigDecimal("175.00").compareTo(amount(order.getGrandTotal())));
    }

    @Test
    @DisplayName("service: a line saved without a rate gets the default VAT rate")
    void save_appliesTheDefaultRate() {
        PurchaseOrder order = stored(anOrder().withLine(2, "5.00").build());
        order.setTaxAmount(null);

        service.update(order.getId(), new UpdatePurchaseOrderInput());

        PurchaseOrderLine line = order.getLines().get(0);
        assertEquals(0, new BigDecimal("20").compareTo(line.getTaxRate()));
        assertEquals(0, new BigDecimal("2.00").compareTo(amount(line.getTaxAmount())));
        assertEquals(0, new BigDecimal("2.00").compareTo(amount(order.getTaxAmount())));
    }

    @Test
    @DisplayName("service: the default rate is configurable")
    void save_configurableDefaultRate() {
        service.setDefaultTaxRate(new BigDecimal("14"));
        PurchaseOrder order = stored(anOrder().withLine(10, "10.00").build());
        order.setTaxAmount(null);

        service.update(order.getId(), new UpdatePurchaseOrderInput());

        assertEquals(0, new BigDecimal("14.00").compareTo(amount(order.getTaxAmount())));
    }

    @Test
    @DisplayName("service: an order that only carries one tax amount, with no rate on any line, keeps that amount")
    void save_legacyOrderTaxIsKept() {
        PurchaseOrder order = stored(anOrder().withLine(2, "5.00").build());
        order.setTaxAmount(Money.of("7.00", CurrencyCode.MAD));

        service.update(order.getId(), new UpdatePurchaseOrderInput());

        assertNull(order.getLines().get(0).getTaxRate());
        assertEquals(0, new BigDecimal("7.00").compareTo(amount(order.getTaxAmount())));
    }

    @Test
    @DisplayName("line: a VAT rate below 0 or above 100 percent is refused")
    void invalidRate_isRefused() {
        PurchaseOrderLine line = aLine(1, "5.00", CurrencyCode.MAD);

        assertThrows(PurchaseOrderInvalidLineException.class, () -> line.setTaxRate(new BigDecimal("-1")));
        assertThrows(PurchaseOrderInvalidLineException.class, () -> line.setTaxRate(new BigDecimal("120")));
    }
}
