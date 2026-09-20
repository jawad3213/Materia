package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Web request DTO for creating a purchase requisition.
 */
public class CreateRequisitionWebRequest {

    @NotBlank(message = "Title is mandatory")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Justification must not exceed 500 characters")
    private String justification;

    private String requesterId;

    @NotBlank(message = "Requester name is mandatory")
    @Size(max = 255, message = "Requester name must not exceed 255 characters")
    private String requesterName;

    private LocalDate requiredDate;

    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    @NotEmpty(message = "At least one requisition line is required")
    @Valid
    private List<RequisitionLineWebRequest> lines = new ArrayList<>();

    private String createdBy;
    private String status;

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

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
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

    public List<RequisitionLineWebRequest> getLines() {
        return lines;
    }

    public void setLines(List<RequisitionLineWebRequest> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
