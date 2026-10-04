package com.materia.backend.contexts.purchaseOrder.domain.ports.out;

import java.util.UUID;

/**
 * The quantity of a material ordered from suppliers but not yet received. Orders add to it when created,
 * receipts reduce it, and withdrawn orders release what was never received.
 */
public interface OnOrderStock {

    void add(UUID materialId, String materialCode, int quantity);

    /** Releases up to {@code quantity}; never takes the material's on-order stock below zero. */
    void release(UUID materialId, String materialCode, int quantity);

    /** For callers that do not track stock (unit tests of unrelated rules). */
    OnOrderStock NONE = new OnOrderStock() {
        @Override
        public void add(UUID materialId, String materialCode, int quantity) {
        }

        @Override
        public void release(UUID materialId, String materialCode, int quantity) {
        }
    };
}
