package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * JPA entity storing purchase requisition code sequences.
 */
@Entity
@Table(name = "purchase_requisition_code_sequences", indexes = {
        @Index(name = "idx_pr_code_sequence_prefix", columnList = "prefix", unique = true)
})
public class RequisitionCodeSequenceJpaEntity extends BaseJpaEntity {

    @Column(name = "prefix", length = 10, nullable = false, unique = true)
    private String prefix;

    @Column(name = "next_val", nullable = false)
    private Integer nextVal;

    public RequisitionCodeSequenceJpaEntity() {
        super();
    }

    public RequisitionCodeSequenceJpaEntity(String prefix, Integer nextVal) {
        super();
        this.prefix = prefix;
        this.nextVal = nextVal;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getNextVal() {
        return nextVal;
    }

    public void setNextVal(Integer nextVal) {
        this.nextVal = nextVal;
    }
}
