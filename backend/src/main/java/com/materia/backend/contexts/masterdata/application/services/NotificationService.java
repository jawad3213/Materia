package com.materia.backend.contexts.masterData.application.services;

public interface NotificationService {
    void sendAlert(String recipient, String subject, String message);
    void sendReport(String recipient, String subject, String report);
}
