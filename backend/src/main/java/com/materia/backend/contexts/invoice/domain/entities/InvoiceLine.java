package com.materia.backend.contexts.invoice.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.common.domain.valueObjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;


public class InvoiceLine extends BaseEntity {
    
    // ============================================================
    // ATTRIBUTS
    // ============================================================
    
    private Integer lineNumber;
    
    // ---- LIEN AVEC LA COMMANDE ET LA RÉCEPTION ----
    private String purchaseOrderLineId;
    private String goodsReceiptLineId;
    
    // ---- MATÉRIAU ----
    private String materialCode;
    private String materialName;
    private String unitOfMeasure;
    
    // ---- QUANTITÉS ----
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private Integer quantityInvoiced;
    private Integer quantityDiscrepancy;
    
    // ---- PRIX ----
    private Money unitPrice;
    private Money lineTotal;
    private Money taxAmount;
    private Money lineTotalWithTax;
    private String currencyCode;
    
    // ---- ÉCARTS ----
    private boolean hasQuantityDiscrepancy;
    private String discrepancyNotes;

    // ---- PRICE MATCH ----
    /** Unit price of the purchase-order line the invoice line was matched against. */
    private Money orderUnitPrice;
    /** How far the invoiced unit price is from the order price, in percent (positive when billed higher). */
    private BigDecimal priceVariancePercent;
    /** The invoiced unit price is outside the tolerance around the order price. */
    private boolean hasPriceDiscrepancy;

    // ---- VAT ----
    /** VAT rate in percent; when given, the line's tax is computed from it. */
    private BigDecimal taxRate;
    /** VAT rate of the purchase-order line it was matched against. */
    private BigDecimal orderTaxRate;
    /** The line's VAT is not what the order line's rate gives. */
    private boolean hasTaxDiscrepancy;
    
    // ---- DIVERS ----
    private String notes;
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    public InvoiceLine() {
        super();
    }
    
    public InvoiceLine(Builder builder) {
        super();
        this.id = builder.id;
        this.lineNumber = builder.lineNumber;
        this.purchaseOrderLineId = builder.purchaseOrderLineId;
        this.goodsReceiptLineId = builder.goodsReceiptLineId;
        this.materialCode = builder.materialCode;
        this.materialName = builder.materialName;
        this.unitOfMeasure = builder.unitOfMeasure;
        this.quantityOrdered = builder.quantityOrdered;
        this.quantityReceived = builder.quantityReceived;
        this.quantityInvoiced = builder.quantityInvoiced;
        this.quantityDiscrepancy = builder.quantityDiscrepancy;
        this.unitPrice = builder.unitPrice;
        this.lineTotal = builder.lineTotal;
        this.taxAmount = builder.taxAmount;
        this.lineTotalWithTax = builder.lineTotalWithTax;
        this.currencyCode = builder.currencyCode;
        this.hasQuantityDiscrepancy = builder.hasQuantityDiscrepancy;
        this.discrepancyNotes = builder.discrepancyNotes;
        this.orderUnitPrice = builder.orderUnitPrice;
        this.priceVariancePercent = builder.priceVariancePercent;
        this.hasPriceDiscrepancy = builder.hasPriceDiscrepancy;
        this.taxRate = builder.taxRate;
        this.notes = builder.notes;
        
        if (builder.createdAt != null) this.setCreatedAt(builder.createdAt);
        if (builder.updatedAt != null) this.setUpdatedAt(builder.updatedAt);
        if (builder.createdBy != null) {
            this.setCreatedBy(builder.createdBy);
            this.setUpdatedBy(builder.createdBy);
        }
    }
    
    // ============================================================
    // BUILDER
    // ============================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private Integer lineNumber;
        private String purchaseOrderLineId;
        private String goodsReceiptLineId;
        private String materialCode;
        private String materialName;
        private String unitOfMeasure;
        private Integer quantityOrdered;
        private Integer quantityReceived;
        private Integer quantityInvoiced;
        private Integer quantityDiscrepancy;
        private Money unitPrice;
        private Money lineTotal;
        private Money taxAmount;
        private Money lineTotalWithTax;
        private String currencyCode;
        private boolean hasQuantityDiscrepancy;
        private String discrepancyNotes;
        private Money orderUnitPrice;
        private BigDecimal priceVariancePercent;
        private boolean hasPriceDiscrepancy;
        private BigDecimal taxRate;
        private String notes;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder purchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; return this; }
        public Builder goodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; return this; }
        public Builder materialCode(String materialCode) { this.materialCode = materialCode; return this; }
        public Builder materialName(String materialName) { this.materialName = materialName; return this; }
        public Builder unitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; return this; }
        public Builder quantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; return this; }
        public Builder quantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; return this; }
        public Builder quantityInvoiced(Integer quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; return this; }
        public Builder quantityDiscrepancy(Integer quantityDiscrepancy) { this.quantityDiscrepancy = quantityDiscrepancy; return this; }
        public Builder unitPrice(Money unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder lineTotal(Money lineTotal) { this.lineTotal = lineTotal; return this; }
        public Builder taxAmount(Money taxAmount) { this.taxAmount = taxAmount; return this; }
        public Builder lineTotalWithTax(Money lineTotalWithTax) { this.lineTotalWithTax = lineTotalWithTax; return this; }
        public Builder currencyCode(String currencyCode) { this.currencyCode = currencyCode; return this; }
        public Builder hasQuantityDiscrepancy(boolean hasQuantityDiscrepancy) { this.hasQuantityDiscrepancy = hasQuantityDiscrepancy; return this; }
        public Builder discrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; return this; }
        public Builder orderUnitPrice(Money orderUnitPrice) { this.orderUnitPrice = orderUnitPrice; return this; }
        public Builder priceVariancePercent(BigDecimal priceVariancePercent) { this.priceVariancePercent = priceVariancePercent; return this; }
        public Builder hasPriceDiscrepancy(boolean hasPriceDiscrepancy) { this.hasPriceDiscrepancy = hasPriceDiscrepancy; return this; }
        public Builder taxRate(BigDecimal taxRate) { this.taxRate = taxRate; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public InvoiceLine build() {
            validateRequiredFields();
            calculateDiscrepancies();
            calculateTotals();
            
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
            
            return new InvoiceLine(this);
        }
        
        private void validateRequiredFields() {
            if (this.materialCode == null || this.materialCode.trim().isEmpty()) {
                throw new IllegalArgumentException("The material code is required");
            }
            if (this.quantityInvoiced == null || this.quantityInvoiced <= 0) {
                throw new IllegalArgumentException("The invoiced quantity must be positive");
            }
            if (this.unitPrice == null) {
                throw new IllegalArgumentException("The unit price is required");
            }
        }
        
        private void calculateDiscrepancies() {
            if (quantityInvoiced != null && quantityReceived != null) {
                this.hasQuantityDiscrepancy = !quantityInvoiced.equals(quantityReceived);
                this.quantityDiscrepancy = quantityInvoiced - quantityReceived;
            }
        }
        
        private void calculateTotals() {
            if (unitPrice != null && quantityInvoiced != null) {
                this.lineTotal = unitPrice.multiply(quantityInvoiced);
                if (taxRate != null) {
                    this.taxAmount = taxOn(lineTotal, taxRate);
                }
                if (taxAmount != null) {
                    this.lineTotalWithTax = lineTotal.add(taxAmount);
                } else {
                    this.lineTotalWithTax = lineTotal;
                }
            }
        }
    }
    
    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================
    
    public boolean hasDiscrepancy() {
        return hasQuantityDiscrepancy || hasPriceDiscrepancy || hasTaxDiscrepancy;
    }

    /** Line total = unit price × invoiced quantity; total with tax adds the tax amount when present. */
    public void recalculateTotals() {
        if (unitPrice != null && quantityInvoiced != null) {
            this.lineTotal = unitPrice.multiply(quantityInvoiced);
            if (taxRate != null) {
                this.taxAmount = taxOn(lineTotal, taxRate);
            }
            this.lineTotalWithTax = taxAmount != null ? lineTotal.add(taxAmount) : lineTotal;
        }
    }

    /**
     * Records the three-way match against the purchase order and its receipts.
     *
     * @param ordered        quantity on the purchase-order line
     * @param received       quantity accepted on validated receipts
     * @param billable       what this invoice may still bill (received minus already invoiced, or for a
     *                       credit note, what was invoiced)
     * @param orderUnitPrice the purchase-order unit price, or null when unknown
     */
    public void recordMatch(int ordered, int received, int billable, Money orderUnitPrice) {
        recordMatch(ordered, received, billable, orderUnitPrice, DEFAULT_PRICE_TOLERANCE_PERCENT);
    }

    /**
     * Same as {@link #recordMatch(int, int, int, Money)}, with the price tolerance: an invoiced unit price more than
     * {@code tolerancePercent} percent away from the order price (either way) is a price discrepancy.
     */
    public void recordMatch(int ordered, int received, int billable, Money orderUnitPrice, BigDecimal tolerancePercent) {
        recordMatch(ordered, received, billable, orderUnitPrice, tolerancePercent, null);
    }

    /**
     * Same as {@link #recordMatch(int, int, int, Money, BigDecimal)}, with the VAT rate of the order line: the line's
     * tax must be what that rate gives on the line total (to the cent), or it is a tax discrepancy.
     */
    public void recordMatch(int ordered, int received, int billable, Money orderUnitPrice, BigDecimal tolerancePercent,
                            BigDecimal orderTaxRate) {
        this.quantityOrdered = ordered;
        this.quantityReceived = received;
        int invoiced = quantityInvoiced != null ? quantityInvoiced : 0;
        int excess = invoiced - Math.max(0, billable);
        this.hasQuantityDiscrepancy = excess > 0;
        this.quantityDiscrepancy = Math.max(0, excess);

        StringBuilder notes = new StringBuilder();
        if (excess > 0) {
            notes.append("Invoiced quantity (").append(invoiced).append(") exceeds the billable quantity (")
                    .append(Math.max(0, billable)).append(").");
        }

        this.orderUnitPrice = orderUnitPrice;
        this.priceVariancePercent = null;
        this.hasPriceDiscrepancy = false;
        if (orderUnitPrice != null && unitPrice != null && orderUnitPrice.getAmount().signum() > 0) {
            BigDecimal variance = unitPrice.getAmount().subtract(orderUnitPrice.getAmount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(orderUnitPrice.getAmount(), 2, RoundingMode.HALF_UP);
            this.priceVariancePercent = variance;
            BigDecimal tolerance = tolerancePercent != null ? tolerancePercent.abs() : DEFAULT_PRICE_TOLERANCE_PERCENT;
            this.hasPriceDiscrepancy = invoiced > 0 && variance.abs().compareTo(tolerance) > 0;
            if (hasPriceDiscrepancy) {
                if (notes.length() > 0) notes.append(" ");
                notes.append("Unit price (").append(unitPrice.getAmount().stripTrailingZeros().toPlainString())
                        .append(") differs from the order price (")
                        .append(orderUnitPrice.getAmount().stripTrailingZeros().toPlainString()).append(") by ")
                        .append(variance.signum() > 0 ? "+" : "").append(variance.stripTrailingZeros().toPlainString())
                        .append("%, beyond the ").append(tolerance.stripTrailingZeros().toPlainString()).append("% tolerance.");
            }
        }

        this.orderTaxRate = orderTaxRate;
        this.hasTaxDiscrepancy = false;
        if (orderTaxRate != null && lineTotal != null && invoiced > 0) {
            Money expected = taxOn(lineTotal, orderTaxRate);
            BigDecimal actual = taxAmount != null ? taxAmount.getAmount() : BigDecimal.ZERO;
            if (actual.subtract(expected.getAmount()).abs().compareTo(TAX_TOLERANCE) > 0) {
                this.hasTaxDiscrepancy = true;
                if (notes.length() > 0) notes.append(" ");
                notes.append("VAT (").append(actual.stripTrailingZeros().toPlainString())
                        .append(") differs from the order VAT at ").append(orderTaxRate.stripTrailingZeros().toPlainString())
                        .append("% (").append(expected.getAmount().stripTrailingZeros().toPlainString()).append(").");
            }
        }
        this.discrepancyNotes = notes.length() > 0 ? notes.toString() : null;
    }

    /** Rounding difference accepted between an invoice line's VAT and the VAT its order line's rate gives. */
    private static final BigDecimal TAX_TOLERANCE = new BigDecimal("0.01");

    /** The VAT on a line total at a rate in percent, rounded to the cent. */
    public static Money taxOn(Money lineTotal, BigDecimal ratePercent) {
        BigDecimal tax = lineTotal.getAmount().multiply(ratePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return Money.of(tax, lineTotal.getCurrency());
    }

    /** Price difference accepted between an invoice line and its order line when none is configured. */
    public static final BigDecimal DEFAULT_PRICE_TOLERANCE_PERCENT = new BigDecimal("2");

    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceLine that = (InvoiceLine) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "InvoiceLine{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", materialCode='" + materialCode + '\'' +
                ", quantityInvoiced=" + quantityInvoiced +
                ", lineTotal=" + lineTotal +
                '}';
    }
    
    // ============================================================
    // GETTERS & SETTERS
    // ============================================================
    
    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
    
    public String getPurchaseOrderLineId() { return purchaseOrderLineId; }
    public void setPurchaseOrderLineId(String purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; }
    
    public String getGoodsReceiptLineId() { return goodsReceiptLineId; }
    public void setGoodsReceiptLineId(String goodsReceiptLineId) { this.goodsReceiptLineId = goodsReceiptLineId; }
    
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    
    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    
    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
    
    public Integer getQuantityInvoiced() { return quantityInvoiced; }
    public void setQuantityInvoiced(Integer quantityInvoiced) { this.quantityInvoiced = quantityInvoiced; }
    
    public Integer getQuantityDiscrepancy() { return quantityDiscrepancy; }
    public void setQuantityDiscrepancy(Integer quantityDiscrepancy) { this.quantityDiscrepancy = quantityDiscrepancy; }
    
    public Money getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Money unitPrice) { this.unitPrice = unitPrice; }
    
    public Money getLineTotal() { return lineTotal; }
    public void setLineTotal(Money lineTotal) { this.lineTotal = lineTotal; }
    
    public Money getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Money taxAmount) { this.taxAmount = taxAmount; }
    
    public Money getLineTotalWithTax() { return lineTotalWithTax; }
    public void setLineTotalWithTax(Money lineTotalWithTax) { this.lineTotalWithTax = lineTotalWithTax; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public boolean isHasQuantityDiscrepancy() { return hasQuantityDiscrepancy; }
    public void setHasQuantityDiscrepancy(boolean hasQuantityDiscrepancy) { this.hasQuantityDiscrepancy = hasQuantityDiscrepancy; }
    
    public String getDiscrepancyNotes() { return discrepancyNotes; }
    public void setDiscrepancyNotes(String discrepancyNotes) { this.discrepancyNotes = discrepancyNotes; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Money getOrderUnitPrice() { return orderUnitPrice; }
    public void setOrderUnitPrice(Money orderUnitPrice) { this.orderUnitPrice = orderUnitPrice; }

    public BigDecimal getPriceVariancePercent() { return priceVariancePercent; }
    public void setPriceVariancePercent(BigDecimal priceVariancePercent) { this.priceVariancePercent = priceVariancePercent; }

    public boolean isHasPriceDiscrepancy() { return hasPriceDiscrepancy; }
    public void setHasPriceDiscrepancy(boolean hasPriceDiscrepancy) { this.hasPriceDiscrepancy = hasPriceDiscrepancy; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getOrderTaxRate() { return orderTaxRate; }
    public void setOrderTaxRate(BigDecimal orderTaxRate) { this.orderTaxRate = orderTaxRate; }

    public boolean isHasTaxDiscrepancy() { return hasTaxDiscrepancy; }
    public void setHasTaxDiscrepancy(boolean hasTaxDiscrepancy) { this.hasTaxDiscrepancy = hasTaxDiscrepancy; }
}
