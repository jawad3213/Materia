package com.materia.backend.contexts.returnToVendor.application.services;

import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/** What goods receiving expects again because suppliers replace returned goods. */
@ExtendWith(MockitoExtension.class)
class ReplacementQuantitiesServiceTest {

    @Mock private ReturnToVendorRepository returns;

    private static ReturnToVendor aReturn(ReturnStatus status, ResolutionType resolution, ReturnToVendorLine... lines) {
        return ReturnToVendor.builder().goodsReceiptId("gr-1").supplierId("sup-1").supplierName("Acme")
                .returnReason("Damaged").status(status).resolutionType(resolution).lines(List.of(lines)).build();
    }

    private static ReturnToVendorLine line(String orderLineId, Integer quantity) {
        return ReturnToVendorLine.builder().materialCode("MAT-1").materialName("Bolt").rejectionReason("Scratched")
                .purchaseOrderLineId(orderLineId).quantityToReturn(quantity).build();
    }

    @Test
    @DisplayName("replacements: only resolved replacement returns count, summed per purchase-order line")
    void onlyResolvedReplacementsCount() {
        when(returns.findByPurchaseOrderId("po-1")).thenReturn(List.of(
                aReturn(ReturnStatus.RESOLVED, ResolutionType.REPLACEMENT, line("l-1", 2), line("l-2", 1)),
                aReturn(ReturnStatus.RESOLVED, ResolutionType.REPLACEMENT, line("l-1", 3), line(null, 9)),
                aReturn(ReturnStatus.RESOLVED, ResolutionType.CREDIT_NOTE, line("l-1", 5)),
                aReturn(ReturnStatus.PENDING, ResolutionType.REPLACEMENT, line("l-1", 7))));

        Map<String, Integer> replaced = new ReplacementQuantitiesService(returns).replacedQuantitiesByPurchaseOrderLine("po-1");

        assertEquals(Map.of("l-1", 5, "l-2", 1), replaced);
    }
}
