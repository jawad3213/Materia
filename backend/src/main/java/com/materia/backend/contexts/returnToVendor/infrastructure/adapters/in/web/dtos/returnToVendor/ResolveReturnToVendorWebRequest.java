package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.dtos.returnToVendor;

import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** How the supplier settled the return: the replacement reference or the credit note number. */
public record ResolveReturnToVendorWebRequest(
        @NotNull(message = "The resolution type is required") ResolutionType resolutionType,
        @NotBlank(message = "The resolution reference is required") @Size(max = 100) String reference,
        @Size(max = 500) String supplierResponse) {
}
