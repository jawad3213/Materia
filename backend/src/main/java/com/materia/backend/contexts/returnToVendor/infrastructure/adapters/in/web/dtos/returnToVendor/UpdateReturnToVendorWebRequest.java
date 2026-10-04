package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** Changes to a draft return; omitted fields are kept. */
public record UpdateReturnToVendorWebRequest(
        LocalDate returnDate,
        @Size(max = 500) String returnReason,
        @Size(max = 500) String rejectionSummary,
        @Size(max = 1000) String notes,
        @Size(max = 1000) String internalNotes,
        List<@Valid ReturnToVendorLineWebRequest> lines) {
}
