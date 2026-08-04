package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.dtos;

import com.materia.backend.common.application.BaseInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Validated request for adding a line to a goods receipt. */
public class AddGoodsReceiptLineWebRequest extends BaseInput {
    @NotNull(message = "Goods receipt line is mandatory") @Valid
    private GoodsReceiptLineWebRequest line;
    public GoodsReceiptLineWebRequest getLine() { return line; }
    public void setLine(GoodsReceiptLineWebRequest line) { this.line = line; }
}
