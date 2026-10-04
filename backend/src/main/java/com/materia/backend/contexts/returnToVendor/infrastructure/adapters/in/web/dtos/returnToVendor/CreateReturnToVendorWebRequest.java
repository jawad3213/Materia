package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** A return prepared from a goods receipt; everything else is taken from the receipt by the server. */
public record CreateReturnToVendorWebRequest(
        @NotBlank(message = "The goods receipt is required") String goodsReceiptId,
        LocalDate returnDate,
        @NotBlank(message = "The return reason is required") @Size(max = 500) String returnReason,
        @Size(max = 500) String rejectionSummary,
        @Size(max = 1000) String notes,
        @Size(max = 1000) String internalNotes,
        @NotEmpty(message = "A return must contain at least one line") List<@Valid ReturnToVendorLineWebRequest> lines) {
}
