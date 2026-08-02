package com.materia.backend.contexts.masterdata.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "code_sequences")
public class CodeSequenceJpaEntity {

    @Id
    @Column(name = "prefix", length = 10, nullable = false)
    private String prefix;

    @Column(name = "next_val", nullable = false)
    private Integer nextVal;

    public CodeSequenceJpaEntity() {
    }

    public CodeSequenceJpaEntity(String prefix, Integer nextVal) {
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
