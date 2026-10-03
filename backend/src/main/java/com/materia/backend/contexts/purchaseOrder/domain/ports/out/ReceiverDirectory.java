package com.materia.backend.contexts.purchaseOrder.domain.ports.out;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port listing the people a purchase order can be assigned to for receipt.
 */
public interface ReceiverDirectory {

    List<Receiver> findAssignableReceivers();

    Optional<Receiver> findAssignableReceiver(String userId);

    record Receiver(String id, String name, String email) {
    }
}
