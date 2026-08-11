package com.materia.backend.contexts.payement.infrastructure.adapters.in.web.dtos;

import jakarta.validation.constraints.NotBlank;

public class PaymentCompleteWebRequest {

    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String userId;
    
    private String bankReference;
    private String transactionId;
    
    @NotBlank(message = "La méthode de paiement est obligatoire")
    private String paymentMethod;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
