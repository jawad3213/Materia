package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.common.domain.valueObjects.Money;

import java.util.Objects;

/**
 * Reorder Quantity Value Object
 * Quantité de réapprovisionnement recommandée
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public class ReorderQuantity {
    
    private final int quantity;
    private final Money estimatedCost;
    private final String reason;
    private final boolean isUrgent;
    
    public ReorderQuantity(int quantity, Money estimatedCost, String reason, boolean isUrgent) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La quantité de réapprovisionnement doit être positive");
        }
        this.quantity = quantity;
        this.estimatedCost = estimatedCost;
        this.reason = reason;
        this.isUrgent = isUrgent;
    }
    
    public int getQuantity() { return quantity; }
    public Money getEstimatedCost() { return estimatedCost; }
    public String getReason() { return reason; }
    public boolean isUrgent() { return isUrgent; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReorderQuantity that = (ReorderQuantity) o;
        return quantity == that.quantity &&
               isUrgent == that.isUrgent &&
               Objects.equals(estimatedCost, that.estimatedCost) &&
               Objects.equals(reason, that.reason);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(quantity, estimatedCost, reason, isUrgent);
    }
    
    @Override
    public String toString() {
        return "ReorderQuantity{" +
               "quantity=" + quantity +
               ", estimatedCost=" + estimatedCost +
               ", reason='" + reason + '\'' +
               ", isUrgent=" + isUrgent +
               '}';
    }
}
