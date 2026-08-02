package com.materia.backend.contexts.purchaseRequisition.application.dtos;

import com.materia.backend.common.application.BaseInput;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for updating a purchase requisition.
 */
public class UpdateRequisitionInput extends BaseInput {

    private String title;
    private String description;
    private String justification;
    private LocalDate requiredDate;
    private String currencyCode;
    private List<RequisitionLine> lines = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public List<RequisitionLine> getLines() {
        return lines;
    }

    public void setLines(List<RequisitionLine> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
}
