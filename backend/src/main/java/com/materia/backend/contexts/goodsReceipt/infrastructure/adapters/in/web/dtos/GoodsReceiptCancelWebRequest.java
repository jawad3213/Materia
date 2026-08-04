package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos;

import com.materia.backend.common.application.BaseInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Validated request for cancelling a goods receipt. */
public class GoodsReceiptCancelWebRequest extends BaseInput {
    @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters")
    @NotBlank(message = "Cancellation reason is mandatory")
    private String reason;
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
