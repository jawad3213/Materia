package com.materia.backend.contexts.invoice.domain.ports.in;

import com.materia.backend.common.domain.BaseUseCase;
import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.application.dtos.UpdateInvoiceInput;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Input port for invoice use cases.
 */
public interface InvoiceUseCase extends BaseUseCase<CreateInvoiceInput, InvoiceOutput, UUID> {

    InvoiceOutput update(UUID id, UpdateInvoiceInput request);

    List<InvoiceOutput> getByStatus(String status);

    List<InvoiceOutput> getBySupplierId(String supplierId);

    List<InvoiceOutput> getByPurchaseOrderId(String purchaseOrderId);

    List<InvoiceOutput> searchByKeyword(String keyword);

    InvoiceOutput submit(UUID id, String userId);

    InvoiceOutput verify(UUID id, String userId, String userName);

    InvoiceOutput pay(UUID id, String userId, String userName, Double amount);

    InvoiceOutput cancel(UUID id, String userId, String reason);
}
