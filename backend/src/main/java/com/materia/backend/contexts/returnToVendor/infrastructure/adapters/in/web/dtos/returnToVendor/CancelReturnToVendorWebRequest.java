package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelReturnToVendorWebRequest(
        @NotBlank(message = "A cancellation reason is required") @Size(max = 500) String reason) {
}
