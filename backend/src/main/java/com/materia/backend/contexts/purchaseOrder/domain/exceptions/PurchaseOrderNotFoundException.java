package com.materia.backend.contexts.purchaseOrder.domain.exceptions;

import com.materia.backend.common.application.exceptions.NotFoundException;

import java.util.UUID;

public class PurchaseOrderNotFoundException extends NotFoundException {

    public PurchaseOrderNotFoundException(UUID id) {
        super("Purchase order not found: " + id, "PURCHASE_ORDER_NOT_FOUND");
    }

    public PurchaseOrderNotFoundException(String code) {
        super("Purchase order not found: " + code, "PURCHASE_ORDER_NOT_FOUND");
    }
}
