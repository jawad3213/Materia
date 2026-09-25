package com.materia.backend.contexts.auth.infrastructure.adapters.out.mail;

import com.materia.backend.contexts.auth.domain.ports.out.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 🔹 SMTP EMAIL SENDER (OUTBOUND ADAPTER)
 * 
 * Implements the EmailSender domain port using Spring's JavaMailSender.
 * Dispatches responsive HTML emails for transactional auth events.
 */
@Component
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@materia.com}")
    private String fromEmail;

    @Value("${app.mail.reset-password-base-url:http://localhost:5173/reset-password}")
    private String resetPasswordBaseUrl;

    public SmtpEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String encodedEmail = URLEncoder.encode(toEmail, StandardCharsets.UTF_8);
        String resetLink = String.format("%s?token=%s&email=%s", resetPasswordBaseUrl, resetToken, encodedEmail);

        // The raw token is an account-takeover credential, so it never reaches the logs.
        log.info("[EMAIL-SENDER] Preparing password reset email for {}", toEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Materia - Password Reset Request");

            String htmlContent = buildResetPasswordHtml(toEmail, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[EMAIL-SENDER] Successfully sent password reset email to: {}", toEmail);

        } catch (Exception ex) {
            log.error("[EMAIL-SENDER] Failed to send password reset email to {}: {}",
                    toEmail, ex.getMessage(), ex);
        }
    }

    private String buildResetPasswordHtml(String email, String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Reset Your Password</title>
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; color: #1e293b; margin: 0; padding: 32px 16px; }
                .card { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 40px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); border: 1px solid #e2e8f0; }
                .logo { font-size: 22px; font-weight: 700; color: #465fff; margin-bottom: 24px; text-decoration: none; display: inline-block; }
                h1 { font-size: 20px; font-weight: 600; color: #0f172a; margin-top: 0; margin-bottom: 12px; }
                p { font-size: 14px; line-height: 1.6; color: #475569; margin: 12px 0; }
                .button-container { margin: 28px 0; text-align: center; }
                .btn { display: inline-block; background-color: #465fff; color: #ffffff !important; padding: 12px 28px; border-radius: 8px; font-size: 14px; font-weight: 600; text-decoration: none; }
                .notice { font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; padding-top: 20px; margin-top: 28px; }
                .link-backup { word-break: break-all; font-size: 12px; color: #64748b; background: #f8fafc; padding: 10px; border-radius: 6px; }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="logo">Materia</div>
                <h1>Reset Your Password</h1>
                <p>Hello,</p>
                <p>We received a request to reset the password for your account associated with <strong>%s</strong>.</p>
                <p>Click the button below to set a new password. This link is valid for <strong>30 minutes</strong>.</p>
                <div class="button-container">
                  <a href="%s" class="btn" target="_blank">Reset Password</a>
                </div>
                <p>If you did not make this request, you can safely ignore this email. Your password will remain unchanged.</p>
                <div class="notice">
                  <p>Button not working? Copy and paste this link into your browser:</p>
                  <p class="link-backup">%s</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(email, resetLink, resetLink);
    }
}
