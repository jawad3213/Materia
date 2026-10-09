package com.materia.backend.contexts.auth.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.util.UUID;

/**
 * A user changed their own name or phone on their profile. Contexts that keep a copy of these details
 * (the employee record) bring theirs up to date.
 */
public class UserProfileUpdatedEvent extends DomainEvent {

    private final String firstName;
    private final String lastName;
    private final String phone;

    public UserProfileUpdatedEvent(UUID userId, String firstName, String lastName, String phone) {
        super(userId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public UUID getUserId() {
        return (UUID) getAggregateId();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }
}
