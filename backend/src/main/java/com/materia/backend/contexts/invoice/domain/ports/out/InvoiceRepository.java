package com.materia.backend.contexts.invoice.domain.ports.out;

import com.materia.backend.common.domain.BaseRepository;
import com.materia.backend.contexts.invoice.domain.entities.Invoice;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Output port for invoice persistence.
 */
public interface InvoiceRepository extends BaseRepository<Invoice> {

    Optional<Invoice> findByInvoiceCode(InvoiceCode code);
    
    Optional<Invoice> findByInvoiceCode(String code);

    boolean existsByInvoiceCode(String code);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findBySupplierId(String supplierId);

    List<Invoice> findByPurchaseOrderId(String purchaseOrderId);
    
    List<Invoice> findByInvoiceType(InvoiceType type);

    List<Invoice> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Invoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<Invoice> search(String keyword);
}
