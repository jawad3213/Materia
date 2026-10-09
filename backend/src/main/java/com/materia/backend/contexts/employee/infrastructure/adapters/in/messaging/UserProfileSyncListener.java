package com.materia.backend.contexts.employee.infrastructure.adapters.in.messaging;

import com.materia.backend.contexts.auth.domain.events.UserProfileUpdatedEvent;
import com.materia.backend.contexts.employee.domain.ports.in.EmployeeUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Keeps the employee record's name and phone in step with the account the employee edits on their profile.
 * Runs after the profile change committed, in its own transaction: a failure here never undoes the profile edit.
 */
@Component
public class UserProfileSyncListener {

    private static final Logger log = LoggerFactory.getLogger(UserProfileSyncListener.class);

    private final EmployeeUseCase employeeUseCase;

    public UserProfileSyncListener(EmployeeUseCase employeeUseCase) {
        this.employeeUseCase = employeeUseCase;
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onProfileUpdated(UserProfileUpdatedEvent event) {
        try {
            employeeUseCase.syncContactDetailsFromAccount(
                    event.getUserId(), event.getFirstName(), event.getLastName(), event.getPhone());
        } catch (RuntimeException e) {
            log.warn("[EMPLOYEE] Could not sync the employee of user {} from their profile: {}",
                    event.getUserId(), e.getMessage());
        }
    }
}
