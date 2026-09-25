package com.materia.backend.contexts.purchaseOrder.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidLineException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderInvalidStatusTransitionException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderLineRequiredException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotModifiableException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PurchaseOrder extends BaseEntity {

    public static final int MAX_NOTES_LENGTH = 1000;
    public static final int MAX_TERMS_LENGTH = 500;

    private OrderCode orderCode;
    private UUID requisitionId;
    private String requisitionCode;

    private OrderStatus status;
    private DeliveryStatus deliveryStatus;

    private UUID supplierId;
    private String supplierName;
    private String supplierCode;

    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate confirmedDeliveryDate;
    private LocalDate receivedDate;

    private String paymentTerms;
    private Integer paymentDelayDays;
    private String deliveryTerms;
    private String incoterm;
    private String currencyCode;

    private Money totalAmount;
    private Money taxAmount;
    private Money shippingCost;
    private Money grandTotal;

    private String orderedBy;
    private String orderedByName;
    private String approvedBy;
    private String approvedByName;
    private String assignedTo;
    private String assignedToName;
    private LocalDateTime assignedAt;
    private String assignedBy;
    private String assignedByName;

    private String notes;
    private String internalNotes;

    private LocalDateTime obsoletedAt;
    private String obsoletedBy;
    private String obsoletedReason;

    private List<PurchaseOrderLine> lines = new ArrayList<>();

    public PurchaseOrder() {
        super();
    }

    public PurchaseOrder(Builder builder) {
        super();
        this.id = builder.id;
        this.orderCode = builder.orderCode;
        this.requisitionId = builder.requisitionId;
        this.requisitionCode = builder.requisitionCode;

        this.status = builder.status != null ? builder.status : OrderStatus.DRAFT;
        this.deliveryStatus = builder.deliveryStatus != null ? builder.deliveryStatus : DeliveryStatus.NOT_SHIPPED;

        this.supplierId = builder.supplierId;
        this.supplierName = builder.supplierName;
        this.supplierCode = builder.supplierCode;

        this.orderDate = builder.orderDate;
        this.expectedDeliveryDate = builder.expectedDeliveryDate;
        this.confirmedDeliveryDate = builder.confirmedDeliveryDate;
        this.receivedDate = builder.receivedDate;

        this.paymentTerms = builder.paymentTerms;
        this.paymentDelayDays = builder.paymentDelayDays;
        this.deliveryTerms = builder.deliveryTerms;
        this.incoterm = builder.incoterm != null ? builder.incoterm : "EXW";
        this.currencyCode = builder.currencyCode != null ? builder.currencyCode : "MAD";

        this.totalAmount = builder.totalAmount;
        this.taxAmount = builder.taxAmount;
        this.shippingCost = builder.shippingCost;
        this.grandTotal = builder.grandTotal;

        this.orderedBy = builder.orderedBy;
        this.orderedByName = builder.orderedByName;
        this.approvedBy = builder.approvedBy;
        this.approvedByName = builder.approvedByName;
        this.assignedTo = builder.assignedTo;
        this.assignedToName = builder.assignedToName;
        this.assignedAt = builder.assignedAt;
        this.assignedBy = builder.assignedBy;
        this.assignedByName = builder.assignedByName;

        this.notes = builder.notes;
        this.internalNotes = builder.internalNotes;

        this.obsoletedAt = builder.obsoletedAt;
        this.obsoletedBy = builder.obsoletedBy;
        this.obsoletedReason = builder.obsoletedReason;

        this.lines = builder.lines != null ? new ArrayList<>(builder.lines) : new ArrayList<>();

        if (builder.createdAt != null) this.setCreatedAt(builder.createdAt);
        if (builder.updatedAt != null) this.setUpdatedAt(builder.updatedAt);
        if (builder.createdBy != null) {
            this.setCreatedBy(builder.createdBy);
            this.setUpdatedBy(builder.createdBy);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private OrderCode orderCode;
        private UUID requisitionId;
        private String requisitionCode;

        private OrderStatus status;
        private DeliveryStatus deliveryStatus;

        private UUID supplierId;
        private String supplierName;
        private String supplierCode;

        private LocalDate orderDate;
        private LocalDate expectedDeliveryDate;
        private LocalDate confirmedDeliveryDate;
        private LocalDate receivedDate;

        private String paymentTerms;
        private Integer paymentDelayDays;
        private String deliveryTerms;
        private String incoterm;
        private String currencyCode;

        private Money totalAmount;
        private Money taxAmount;
        private Money shippingCost;
        private Money grandTotal;

        private String orderedBy;
        private String orderedByName;
        private String approvedBy;
        private String approvedByName;
        private String assignedTo;
        private String assignedToName;
        private LocalDateTime assignedAt;
        private String assignedBy;
        private String assignedByName;

        private String notes;
        private String internalNotes;

        private LocalDateTime obsoletedAt;
        private String obsoletedBy;
        private String obsoletedReason;

        private List<PurchaseOrderLine> lines = new ArrayList<>();

        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder orderCode(OrderCode orderCode) {
            this.orderCode = orderCode;
            return this;
        }

        public Builder orderCode(String orderCode) {
            this.orderCode = OrderCode.of(orderCode);
            return this;
        }

        public Builder requisitionId(UUID requisitionId) {
            this.requisitionId = requisitionId;
            return this;
        }

        public Builder requisitionCode(String requisitionCode) {
            this.requisitionCode = requisitionCode;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder deliveryStatus(DeliveryStatus deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }

        public Builder supplierId(UUID supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public Builder supplierName(String supplierName) {
            this.supplierName = supplierName;
            return this;
        }

        public Builder supplierCode(String supplierCode) {
            this.supplierCode = supplierCode;
            return this;
        }

        public Builder orderDate(LocalDate orderDate) {
            this.orderDate = orderDate != null ? orderDate : LocalDate.now();
            return this;
        }

        public Builder expectedDeliveryDate(LocalDate expectedDeliveryDate) {
            this.expectedDeliveryDate = expectedDeliveryDate;
            return this;
        }

        public Builder confirmedDeliveryDate(LocalDate confirmedDeliveryDate) {
            this.confirmedDeliveryDate = confirmedDeliveryDate;
            return this;
        }

        public Builder receivedDate(LocalDate receivedDate) {
            this.receivedDate = receivedDate;
            return this;
        }

        public Builder paymentTerms(String paymentTerms) {
            this.paymentTerms = paymentTerms;
            return this;
        }

        public Builder paymentDelayDays(Integer paymentDelayDays) {
            this.paymentDelayDays = paymentDelayDays;
            return this;
        }

        public Builder deliveryTerms(String deliveryTerms) {
            this.deliveryTerms = deliveryTerms;
            return this;
        }

        public Builder incoterm(String incoterm) {
            this.incoterm = incoterm;
            return this;
        }

        public Builder currencyCode(String currencyCode) {
            this.currencyCode = normalizeCurrencyCode(currencyCode);
            return this;
        }

        public Builder totalAmount(Money totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder taxAmount(Money taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }

        public Builder shippingCost(Money shippingCost) {
            this.shippingCost = shippingCost;
            return this;
        }

        public Builder grandTotal(Money grandTotal) {
            this.grandTotal = grandTotal;
            return this;
        }

        public Builder orderedBy(String orderedBy) {
            this.orderedBy = orderedBy;
            return this;
        }

        public Builder orderedByName(String orderedByName) {
            this.orderedByName = orderedByName;
            return this;
        }

        public Builder approvedBy(String approvedBy) {
            this.approvedBy = approvedBy;
            return this;
        }

        public Builder approvedByName(String approvedByName) {
            this.approvedByName = approvedByName;
            return this;
        }

        public Builder assignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }

        public Builder assignedToName(String assignedToName) {
            this.assignedToName = assignedToName;
            return this;
        }

        public Builder assignedAt(LocalDateTime assignedAt) {
            this.assignedAt = assignedAt;
            return this;
        }

        public Builder assignedBy(String assignedBy) {
            this.assignedBy = assignedBy;
            return this;
        }

        public Builder assignedByName(String assignedByName) {
            this.assignedByName = assignedByName;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder internalNotes(String internalNotes) {
            this.internalNotes = internalNotes;
            return this;
        }

        public Builder obsoletedAt(LocalDateTime obsoletedAt) {
            this.obsoletedAt = obsoletedAt;
            return this;
        }

        public Builder obsoletedBy(String obsoletedBy) {
            this.obsoletedBy = obsoletedBy;
            return this;
        }

        public Builder obsoletedReason(String obsoletedReason) {
            this.obsoletedReason = obsoletedReason;
            return this;
        }

        public Builder addLine(PurchaseOrderLine line) {
            if (line == null) {
                throw new PurchaseOrderInvalidLineException("Purchase order line cannot be null");
            }
            if (this.lines == null) {
                this.lines = new ArrayList<>();
            }
            this.lines.add(line);
            return this;
        }

        public Builder lines(List<PurchaseOrderLine> lines) {
            if (lines == null) {
                throw new PurchaseOrderInvalidLineException("Purchase order lines cannot be null");
            }
            this.lines = new ArrayList<>(lines);
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PurchaseOrder build() {
            validateRequiredFields();
            validateLines();

            if (this.id == null) this.id = UUID.randomUUID();
            if (this.orderCode == null) this.orderCode = OrderCode.createDefault();
            if (this.orderDate == null) this.orderDate = LocalDate.now();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            if (this.status == null) this.status = OrderStatus.DRAFT;
            if (this.deliveryStatus == null) this.deliveryStatus = DeliveryStatus.NOT_SHIPPED;
            if (this.currencyCode == null) this.currencyCode = resolveCurrencyCodeFromLines();
            if (this.incoterm == null) this.incoterm = "EXW";

            calculateTotals();

            return new PurchaseOrder(this);
        }

        private void validateRequiredFields() {
            if (this.supplierId == null) {
                throw new PurchaseOrderValidationException("Supplier is required");
            }
            if (this.supplierName == null || this.supplierName.trim().isEmpty()) {
                throw new PurchaseOrderValidationException("Supplier name is required");
            }
            if (this.orderedBy == null || this.orderedBy.trim().isEmpty()) {
                throw new PurchaseOrderValidationException("Ordered by is required");
            }
        }

        private void validateLines() {
            if (this.lines == null || this.lines.isEmpty()) {
                throw new PurchaseOrderLineRequiredException();
            }
            for (int i = 0; i < this.lines.size(); i++) {
                PurchaseOrderLine line = this.lines.get(i);
                if (line.getMaterialCode() == null || line.getMaterialCode().trim().isEmpty()) {
                    throw new PurchaseOrderInvalidLineException("Material is required for line " + (i + 1));
                }
                if (line.getQuantity() == null || line.getQuantity() <= 0) {
                    throw new PurchaseOrderInvalidLineException("Quantity must be positive for line " + (i + 1));
                }
            }
        }

        private void calculateTotals() {
            CurrencyCode orderCurrency = resolveOrderCurrency();

            if (this.lines == null || this.lines.isEmpty()) {
                this.totalAmount = Money.zero(orderCurrency);
                this.grandTotal = Money.zero(orderCurrency);
                this.currencyCode = orderCurrency.getCode();
                return;
            }

            Money total = Money.zero(orderCurrency);
            for (PurchaseOrderLine line : this.lines) {
                if (line.getLineTotal() != null) {
                    if (!orderCurrency.getCode().equalsIgnoreCase(line.getLineTotal().getCurrencyCode())) {
                        throw new PurchaseOrderValidationException("All purchase order lines must use the same currency as the order");
                    }
                    total = total.add(line.getLineTotal());
                }
            }

            this.totalAmount = total;
            this.currencyCode = orderCurrency.getCode();

            Money tax = resolveAdditionalAmount(this.taxAmount, orderCurrency, "Tax amount");
            Money shipping = resolveAdditionalAmount(this.shippingCost, orderCurrency, "Shipping cost");

            this.grandTotal = total.add(tax).add(shipping);
        }

        private CurrencyCode resolveOrderCurrency() {
            if (currencyCode != null) {
                return CurrencyCode.fromCode(currencyCode);
            }
            return CurrencyCode.fromCode(resolveCurrencyCodeFromLines());
        }

        private String resolveCurrencyCodeFromLines() {
            if (this.lines != null) {
                for (PurchaseOrderLine line : this.lines) {
                    if (line == null) {
                        continue;
                    }

                    if (line.getUnitPrice() != null) {
                        return line.getUnitPrice().getCurrencyCode();
                    }
                    if (line.getLineTotal() != null) {
                        return line.getLineTotal().getCurrencyCode();
                    }
                    if (line.getCurrencyCode() != null && CurrencyCode.isValidCode(line.getCurrencyCode())) {
                        return line.getCurrencyCode().toUpperCase();
                    }
                }
            }
            return CurrencyCode.MAD.getCode();
        }
    }

    public void submit(String userId) {
        if (status != OrderStatus.DRAFT) {
            throw new PurchaseOrderInvalidStatusTransitionException("Only a draft purchase order can be submitted");
        }
        this.status = OrderStatus.SUBMITTED;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    public void confirm(String userId) {
        if (status != OrderStatus.SUBMITTED) {
            throw new PurchaseOrderInvalidStatusTransitionException("Only a submitted purchase order can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedDeliveryDate = LocalDate.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    public void assignReceiver(String userId, String userName, String assignedUserId, String assignedUserName) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("La commande doit etre confirmee avant l'assignation");
        }

        this.assignedTo = assignedUserId;
        this.assignedToName = assignedUserName;
        this.assignedAt = LocalDateTime.now();
        this.assignedBy = userId;
        this.assignedByName = userName;
        this.status = OrderStatus.READY_FOR_RECEIPT;

        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    public void confirmReceipt(String receiverId, String receiverName) {
        if (status != OrderStatus.READY_FOR_RECEIPT) {
            throw new IllegalStateException("La commande n'est pas prete pour la reception");
        }

        if (this.assignedTo == null || !this.assignedTo.equals(receiverId)) {
            throw new SecurityException("Vous n'etes pas assigne a cette reception");
        }

        this.status = OrderStatus.RECEIVED;
        this.receivedDate = LocalDate.now();
        if (receiverName != null && !receiverName.isBlank()) {
            this.assignedToName = receiverName;
        }
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(receiverId);
    }

    public void cancel(String userId, String reason) {
        if (!status.isCancellable()) {
            throw new PurchaseOrderInvalidStatusTransitionException("This purchase order cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        this.notes = (this.notes != null ? this.notes + " " : "") + "Annulee: " + reason;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    public void updateDeliveryStatus(DeliveryStatus newStatus, String userId) {
        this.deliveryStatus = newStatus;

        if (newStatus == DeliveryStatus.DELIVERED) {
            this.receivedDate = LocalDate.now();
            this.status = OrderStatus.COMPLETED;
        } else if (newStatus == DeliveryStatus.PARTIAL) {
            this.status = OrderStatus.PARTIALLY_RECEIVED;
        }

        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    public void complete(String userId) {
        if (status != OrderStatus.PARTIALLY_RECEIVED && status != OrderStatus.CONFIRMED && status != OrderStatus.RECEIVED) {
            throw new PurchaseOrderInvalidStatusTransitionException("This purchase order cannot be completed");
        }
        this.status = OrderStatus.COMPLETED;
        this.receivedDate = LocalDate.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    public void addLine(PurchaseOrderLine line) {
        if (line == null) {
            throw new PurchaseOrderInvalidLineException("Purchase order line cannot be null");
        }
        if (status != OrderStatus.DRAFT) {
            throw new PurchaseOrderNotModifiableException();
        }
        this.lines.add(line);
        recalculateTotals();
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void removeLine(int index) {
        if (index < 0 || index >= this.lines.size()) {
            throw new PurchaseOrderInvalidLineException("Invalid purchase order line index: " + index);
        }
        if (status != OrderStatus.DRAFT) {
            throw new PurchaseOrderNotModifiableException();
        }
        this.lines.remove(index);
        recalculateTotals();
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void recalculateTotals() {
        CurrencyCode orderCurrency = resolveOrderCurrency();

        if (this.lines == null || this.lines.isEmpty()) {
            this.totalAmount = Money.zero(orderCurrency);
            this.grandTotal = Money.zero(orderCurrency);
            this.currencyCode = orderCurrency.getCode();
            return;
        }

        Money total = Money.zero(orderCurrency);
        for (PurchaseOrderLine line : this.lines) {
            if (line.getLineTotal() != null) {
                if (!orderCurrency.getCode().equalsIgnoreCase(line.getLineTotal().getCurrencyCode())) {
                    throw new PurchaseOrderValidationException("All purchase order lines must use the same currency as the order");
                }
                total = total.add(line.getLineTotal());
            }
        }

        this.totalAmount = total;
        this.currencyCode = orderCurrency.getCode();

        Money tax = resolveAdditionalAmount(this.taxAmount, orderCurrency, "Tax amount");
        Money shipping = resolveAdditionalAmount(this.shippingCost, orderCurrency, "Shipping cost");

        this.grandTotal = total.add(tax).add(shipping);
    }

    private CurrencyCode resolveOrderCurrency() {
        if (currencyCode != null && CurrencyCode.isValidCode(currencyCode)) {
            return CurrencyCode.fromCode(currencyCode);
        }

        if (lines != null) {
            for (PurchaseOrderLine line : lines) {
                if (line == null) {
                    continue;
                }

                if (line.getUnitPrice() != null) {
                    return line.getUnitPrice().getCurrency();
                }
                if (line.getLineTotal() != null) {
                    return line.getLineTotal().getCurrency();
                }
                if (line.getCurrencyCode() != null && CurrencyCode.isValidCode(line.getCurrencyCode())) {
                    return CurrencyCode.fromCode(line.getCurrencyCode());
                }
            }
        }

        return CurrencyCode.MAD;
    }

    private static Money resolveAdditionalAmount(Money amount, CurrencyCode expectedCurrency, String fieldName) {
        if (amount == null) {
            return Money.zero(expectedCurrency);
        }

        if (!expectedCurrency.equals(amount.getCurrency())) {
            throw new PurchaseOrderValidationException(fieldName + " must use the same currency as the order");
        }

        return amount;
    }

    private static String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return CurrencyCode.fromCode(currencyCode.trim()).getCode();
    }

    public boolean isModifiable() {
        return status != null && status.isModifiable();
    }

    public boolean isActive() {
        return status != null && status.isActive();
    }

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED || status == OrderStatus.RECEIVED;
    }

    public int getTotalQuantity() {
        if (this.lines == null) {
            return 0;
        }
        return this.lines.stream()
                .mapToInt(line -> line.getQuantity() != null ? line.getQuantity() : 0)
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseOrder that = (PurchaseOrder) o;
        return Objects.equals(getId(), that.getId()) || Objects.equals(orderCode, that.orderCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), orderCode);
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "id=" + getId() +
                ", orderCode=" + orderCode +
                ", supplierName='" + supplierName + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", orderDate=" + orderDate +
                '}';
    }

    public OrderCode getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(OrderCode orderCode) {
        this.orderCode = orderCode;
    }

    public UUID getRequisitionId() {
        return requisitionId;
    }

    public void setRequisitionId(UUID requisitionId) {
        this.requisitionId = requisitionId;
    }

    public String getRequisitionCode() {
        return requisitionCode;
    }

    public void setRequisitionCode(String requisitionCode) {
        this.requisitionCode = requisitionCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public LocalDate getConfirmedDeliveryDate() {
        return confirmedDeliveryDate;
    }

    public void setConfirmedDeliveryDate(LocalDate confirmedDeliveryDate) {
        this.confirmedDeliveryDate = confirmedDeliveryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public Integer getPaymentDelayDays() {
        return paymentDelayDays;
    }

    public void setPaymentDelayDays(Integer paymentDelayDays) {
        this.paymentDelayDays = paymentDelayDays;
    }

    public String getDeliveryTerms() {
        return deliveryTerms;
    }

    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = normalizeCurrencyCode(currencyCode);
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Money getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Money getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Money shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Money getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Money grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public String getOrderedByName() {
        return orderedByName;
    }

    public void setOrderedByName(String orderedByName) {
        this.orderedByName = orderedByName;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public LocalDateTime getObsoletedAt() {
        return obsoletedAt;
    }

    public void setObsoletedAt(LocalDateTime obsoletedAt) {
        this.obsoletedAt = obsoletedAt;
    }

    public String getObsoletedBy() {
        return obsoletedBy;
    }

    public void setObsoletedBy(String obsoletedBy) {
        this.obsoletedBy = obsoletedBy;
    }

    public String getObsoletedReason() {
        return obsoletedReason;
    }

    public void setObsoletedReason(String obsoletedReason) {
        this.obsoletedReason = obsoletedReason;
    }

    public List<PurchaseOrderLine> getLines() {
        return lines;
    }

    public void setLines(List<PurchaseOrderLine> lines) {
        this.lines = lines;
    }
}
