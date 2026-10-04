package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.out.goodsReceipt;

import com.materia.backend.contexts.goodsReceipt.domain.ports.out.ReplacedReturnQuantities;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/** Tells goods receipts which returned quantities the supplier is replacing, per purchase-order line. */
@Component
public class ReturnReplacementQuantitiesAdapter implements ReplacedReturnQuantities {

    private final ReturnToVendorRepository returnRepository;

    public ReturnReplacementQuantitiesAdapter(ReturnToVendorRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Override
    public Map<String, Integer> byPurchaseOrderLine(String purchaseOrderId) {
        return returnRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .filter(returnToVendor -> returnToVendor.getStatus() == ReturnStatus.RESOLVED)
                .filter(ReturnToVendor::isReplacement)
                .flatMap(returnToVendor -> returnToVendor.getLines().stream())
                .filter(line -> line.getPurchaseOrderLineId() != null && line.getQuantityToReturn() != null)
                .collect(Collectors.groupingBy(ReturnToVendorLine::getPurchaseOrderLineId,
                        Collectors.summingInt(ReturnToVendorLine::getQuantityToReturn)));
    }
}
