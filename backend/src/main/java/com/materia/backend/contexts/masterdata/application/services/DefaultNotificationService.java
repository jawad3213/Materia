package com.materia.backend.contexts.masterData.application.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultNotificationService implements NotificationService {

    @Override
    public void sendAlert(String recipient, String subject, String message) {
        log.info("📧 [ALERT SENT to {}] Subject: {} | Message: {}", recipient, subject, message);
    }

    @Override
    public void sendReport(String recipient, String subject, String report) {
        log.info("📊 [REPORT SENT to {}] Subject: {} | Content:\n{}", recipient, subject, report);
    }
}
