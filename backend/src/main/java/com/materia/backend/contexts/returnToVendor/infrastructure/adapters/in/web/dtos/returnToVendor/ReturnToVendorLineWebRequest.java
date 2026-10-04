package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReturnToVendorLineWebRequest(
        @NotBlank(message = "The goods receipt line is required") String goodsReceiptLineId,
        @NotNull(message = "The quantity to return is required") @Positive(message = "The quantity to return must be positive") Integer quantityToReturn,
        @Size(max = 500) String rejectionReason,
        @Size(max = 500) String defectDescription,
        @Size(max = 500) String qualityNotes,
        @Size(max = 500) String notes) {
}
