package com.materia.backend.contexts.auth.domain.ports.out;

import java.util.UUID;

/**
 * 🔹 EMPLOYEE PROFILE SYNC (OUTBOUND PORT)
 *
 * Keeps the employee record linked to a user account in step when users edit their own name or phone,
 * so the user directory shows the same details as the profile.
 */
public interface EmployeeProfileSync {

    void syncContactDetails(UUID userId, String firstName, String lastName, String phone);
}
