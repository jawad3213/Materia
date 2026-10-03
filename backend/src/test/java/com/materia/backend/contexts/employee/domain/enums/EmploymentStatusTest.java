package com.materia.backend.contexts.employee.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/** [T071] Employment statuses: who counts as active and who can still be terminated (feature 001 coverage). */
class EmploymentStatusTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(EmploymentStatus.class)
    @DisplayName("employment status: ACTIVE and PROBATION are active; only TERMINATED is terminated and cannot be terminated again")
    void statusRules(EmploymentStatus status) {
        assertEquals(EnumSet.of(EmploymentStatus.ACTIVE, EmploymentStatus.PROBATION).contains(status), status.isActive());
        assertEquals(status == EmploymentStatus.TERMINATED, status.isTerminated());
        assertEquals(status != EmploymentStatus.TERMINATED, status.canBeTerminated());
        assertFalse(status.getLabel().isBlank());
    }
}
