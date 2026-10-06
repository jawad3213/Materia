package com.materia.backend.contexts.payment.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class PaymentCompleteWebRequest {

    
    private String bankReference;
    private String transactionId;
    
    @NotBlank(message = "The payment method is required")
    private String paymentMethod;


    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
