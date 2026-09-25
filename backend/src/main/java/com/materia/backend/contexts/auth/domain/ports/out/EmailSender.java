package com.materia.backend.contexts.auth.domain.ports.out;

/**
 * 🔹 EMAIL SENDER PORT (OUTPUT PORT)
 * 
 * Defines the contract for dispatching transactional authentication emails.
 * Pure domain interface — decoupled from Spring Mail or SMTP implementations.
 */
public interface EmailSender {

    /**
     * Sends a password reset email with the reset link containing token & email.
     *
     * @param toEmail   Recipient email address
     * @param resetToken Generated secure reset token
     */
    void sendPasswordResetEmail(String toEmail, String resetToken);
}
