package com.materia.backend.contexts.purchaseOrder.application.services;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.mappers.PurchaseOrderMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrderLine;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderLineRequiredException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
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

import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * [T084] Line normalisation on save. The request mapper already rejects most bad lines, so these rules are
 * reached through lines already stored on a draft (for example, rows written before the validation existed).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PurchaseOrderServiceLineNormalizationTest {

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
                goodsReceiptUseCase, requisitionUseCase, eventPublisher, receiverDirectory);
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PurchaseOrder storedDraft() {
        PurchaseOrder order = anOrder().withLine(2, "5.00").build();
        when(repository.findById(order.getId())).thenReturn(Optional.of(order));
        return order;
    }

    private PurchaseOrderLine onlyLine(PurchaseOrder order) {
        return order.getLines().get(0);
    }

    @Test
    @DisplayName("normalise: a priced line without a currency takes its unit price's currency")
    void pricedLine_withoutCurrency_takesPriceCurrency() {
        PurchaseOrder order = storedDraft();
        onlyLine(order).setCurrencyCode(null);

        service.update(order.getId(), new UpdatePurchaseOrderInput());

        assertEquals("MAD", onlyLine(order).getCurrencyCode());
    }

    @Test
    @DisplayName("normalise: a line currency matching its price in another case is accepted")
    void pricedLine_withMatchingCurrency_isAccepted() {
        PurchaseOrder order = storedDraft();
        onlyLine(order).setCurrencyCode("mad");

        assertDoesNotThrow(() -> service.update(order.getId(), new UpdatePurchaseOrderInput()));
    }

    @Test
    @DisplayName("normalise: a stored line whose currency differs from its price is refused")
    void pricedLine_withOtherCurrency_isRefused() {
        PurchaseOrder order = storedDraft();
        onlyLine(order).setUnitPrice(Money.of("5.00", CurrencyCode.EUR));
        onlyLine(order).setCurrencyCode("MAD");

        assertThrows(PurchaseOrderValidationException.class,
                () -> service.update(order.getId(), new UpdatePurchaseOrderInput()));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("normalise: an unpriced line with a currency is refused")
    void unpricedLine_withCurrency_isRefused() {
        PurchaseOrder order = storedDraft();
        onlyLine(order).setUnitPrice(null);
        onlyLine(order).setCurrencyCode("MAD");

        assertThrows(PurchaseOrderValidationException.class,
                () -> service.update(order.getId(), new UpdatePurchaseOrderInput()));
    }

    @Test
    @DisplayName("normalise: an unpriced line without a currency takes the order's currency, or none when the order has none")
    void unpricedLine_withoutCurrency_takesOrderCurrency() {
        PurchaseOrder order = storedDraft();
        onlyLine(order).setUnitPrice(null);
        onlyLine(order).setCurrencyCode(null);
        order.setCurrencyCode("MAD");
        service.update(order.getId(), new UpdatePurchaseOrderInput());
        assertEquals("MAD", onlyLine(order).getCurrencyCode());

        PurchaseOrder noCurrency = storedDraft();
        onlyLine(noCurrency).setUnitPrice(null);
        onlyLine(noCurrency).setCurrencyCode(null);
        noCurrency.setCurrencyCode(" ");
        service.update(noCurrency.getId(), new UpdatePurchaseOrderInput());
        assertTrue(onlyLine(noCurrency).getCurrencyCode() == null || onlyLine(noCurrency).getCurrencyCode().isBlank());
    }

    @Test
    @DisplayName("normalise: a stored line without an id is given one; an empty or null-holding line list is refused")
    void ids_andEmptyLists() {
        PurchaseOrder order = storedDraft();
        onlyLine(order).setId(null);
        service.update(order.getId(), new UpdatePurchaseOrderInput());
        assertNotNull(onlyLine(order).getId());

        PurchaseOrder empty = storedDraft();
        empty.getLines().clear();
        assertThrows(PurchaseOrderLineRequiredException.class,
                () -> service.update(empty.getId(), new UpdatePurchaseOrderInput()));

        PurchaseOrder withNull = storedDraft();
        withNull.getLines().add(null);
        assertThrows(PurchaseOrderInvalidLineException.class,
                () -> service.update(withNull.getId(), new UpdatePurchaseOrderInput()));
    }

    @Test
    @DisplayName("update: changing the originating requisition is refused")
    void update_changingRequisition_isRefused() {
        PurchaseOrder order = storedDraft();
        UpdatePurchaseOrderInput input = new UpdatePurchaseOrderInput();
        input.setRequisitionId(UUID.randomUUID());

        assertThrows(PurchaseOrderValidationException.class, () -> service.update(order.getId(), input));
    }
}
