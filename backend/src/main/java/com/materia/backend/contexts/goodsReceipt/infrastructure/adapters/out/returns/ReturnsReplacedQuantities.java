package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.returns;

import com.materia.backend.contexts.goodsReceipt.domain.ports.out.ReplacedReturnQuantities;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReplacementQuantitiesUseCase;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Reads the replacements suppliers owe through the public use case of the return-to-vendor context. */
@Component
public class ReturnsReplacedQuantities implements ReplacedReturnQuantities {

    private final ReplacementQuantitiesUseCase replacements;

    public ReturnsReplacedQuantities(ReplacementQuantitiesUseCase replacements) {
        this.replacements = replacements;
    }

    @Override
    public Map<String, Integer> byPurchaseOrderLine(String purchaseOrderId) {
        return replacements.replacedQuantitiesByPurchaseOrderLine(purchaseOrderId);
    }
}
