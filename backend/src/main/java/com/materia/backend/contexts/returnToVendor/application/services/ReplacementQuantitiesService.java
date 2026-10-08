package com.materia.backend.contexts.returnToVendor.application.services;

import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendor;
import com.materia.backend.contexts.returnToVendor.domain.entities.ReturnToVendorLine;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReplacementQuantitiesUseCase;
import com.materia.backend.contexts.returnToVendor.domain.ports.out.ReturnToVendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReplacementQuantitiesService implements ReplacementQuantitiesUseCase {

    private final ReturnToVendorRepository returnRepository;

    public ReplacementQuantitiesService(ReturnToVendorRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Override
    public Map<String, Integer> replacedQuantitiesByPurchaseOrderLine(String purchaseOrderId) {
        return returnRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .filter(returnToVendor -> returnToVendor.getStatus() == ReturnStatus.RESOLVED)
                .filter(ReturnToVendor::isReplacement)
                .flatMap(returnToVendor -> returnToVendor.getLines().stream())
                .filter(line -> line.getPurchaseOrderLineId() != null && line.getQuantityToReturn() != null)
                .collect(Collectors.groupingBy(ReturnToVendorLine::getPurchaseOrderLineId,
                        Collectors.summingInt(ReturnToVendorLine::getQuantityToReturn)));
    }
}
