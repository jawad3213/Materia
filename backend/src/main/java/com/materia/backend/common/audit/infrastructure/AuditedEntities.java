package com.materia.backend.common.audit.infrastructure;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Which persistence entities have a change history, under which name, and which document a line belongs to.
 * Entities are named by class, so this shared service does not depend on the business modules.
 */
public final class AuditedEntities {

    /**
     * @param type           name of the entity in the history ("PurchaseOrder")
     * @param codeProperty   property holding the business code shown in summaries, or null
     * @param parentProperty property referencing the parent document (a line's order), or null
     * @param parentType     name of that parent
     */
    public record Spec(String type, String codeProperty, String parentProperty, String parentType) {
        boolean isLine() {
            return parentProperty != null;
        }
    }

    /** Fields never recorded: bookkeeping, and secrets. */
    public static final Set<String> IGNORED_PROPERTIES = Set.of(
            "version", "createdAt", "updatedAt", "createdBy", "updatedBy", "passwordHash", "storageKey");

    /** Attachments name their document by type and id; these are the history names of those types. */
    public static final Map<String, String> ATTACHMENT_OWNERS = Map.of(
            "PURCHASE_ORDER", "PurchaseOrder",
            "GOODS_RECEIPT", "GoodsReceipt",
            "INVOICE", "Invoice",
            "RETURN_TO_VENDOR", "ReturnToVendor");

    private static final Map<String, Spec> BY_CLASS = Map.ofEntries(
            Map.entry("PurchaseOrderJpaEntity", new Spec("PurchaseOrder", "orderCode", null, null)),
            Map.entry("PurchaseOrderLineJpaEntity", new Spec("PurchaseOrderLine", "materialCode", "purchaseOrder", "PurchaseOrder")),
            Map.entry("RequisitionJpaEntity", new Spec("Requisition", "requisitionCode", null, null)),
            Map.entry("RequisitionLineJpaEntity", new Spec("RequisitionLine", "materialCode", "requisition", "Requisition")),
            Map.entry("GoodsReceiptJpaEntity", new Spec("GoodsReceipt", "receiptCode", null, null)),
            Map.entry("GoodsReceiptLineJpaEntity", new Spec("GoodsReceiptLine", "materialCode", "goodsReceipt", "GoodsReceipt")),
            Map.entry("InvoiceJpaEntity", new Spec("Invoice", "invoiceCode", null, null)),
            Map.entry("InvoiceLineJpaEntity", new Spec("InvoiceLine", "materialCode", "invoice", "Invoice")),
            Map.entry("PaymentJpaEntity", new Spec("Payment", "paymentCode", null, null)),
            Map.entry("PaymentLineJpaEntity", new Spec("PaymentLine", "invoiceCode", "payment", "Payment")),
            Map.entry("ReturnToVendorJpaEntity", new Spec("ReturnToVendor", "returnCode", null, null)),
            Map.entry("ReturnToVendorLineJpaEntity", new Spec("ReturnToVendorLine", "materialCode", "returnToVendor", "ReturnToVendor")),
            Map.entry("SupplierJpaEntity", new Spec("Supplier", "code", null, null)),
            Map.entry("MaterialJpaEntity", new Spec("Material", "code", null, null)),
            Map.entry("CategoryJpaEntity", new Spec("Category", "code", null, null)),
            Map.entry("UserJpaEntity", new Spec("User", "email", null, null)),
            Map.entry("EmployeeJpaEntity", new Spec("Employee", "code", null, null)),
            Map.entry("AttachmentJpaEntity", new Spec("Attachment", "fileName", null, null)),
            Map.entry("AppSettingJpaEntity", new Spec("Setting", "settingKey", null, null)));

    private AuditedEntities() {
    }

    /** The history settings of a persistence entity class, by its simple name. */
    public static Optional<Spec> of(String entityClassName) {
        return Optional.ofNullable(BY_CLASS.get(entityClassName));
    }
}
