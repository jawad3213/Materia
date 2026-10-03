package com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.auth.domain.ports.out.UserRepository;
import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.enums.EmploymentStatus;
import com.materia.backend.contexts.employee.domain.ports.out.EmployeeRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import com.materia.backend.support.fixtures.EmployeeFixtures;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.EmployeeFixtures.anEmployee;
import static com.materia.backend.support.fixtures.UserFixtures.aUser;
import static org.junit.jupiter.api.Assertions.*;

/**
 * [T086] Employee persistence against a real PostgreSQL Testcontainer (US4).
 *
 * <p>Verifies save/find/delete operations and all query variants (by code, email,
 * userId, status). Rollback isolation means each test is fully independent (SC-005).
 */
class EmployeePersistenceIT extends AbstractIntegrationTest {

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private UserRepository users;

    @PersistenceContext
    private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    // ---- save / findById ----

    @Test
    @DisplayName("save and findById: a persisted employee can be reloaded by id")
    void save_and_findById() {
        Employee saved = employees.save(anEmployee().build());
        flushAndClear();

        Optional<Employee> found = employees.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getCode().getValue(), found.get().getCode().getValue());
        assertEquals(saved.getEmail(), found.get().getEmail());
    }

    // ---- findByCode ----

    @Test
    @DisplayName("findByCode: returns the employee when the code exists")
    void findByCode_returnsEmployee() {
        Employee saved = employees.save(anEmployee().code("EMP-7001").build());
        flushAndClear();

        Optional<Employee> found = employees.findByCode(saved.getCode());

        assertTrue(found.isPresent());
        assertEquals("EMP-7001", found.get().getCode().getValue());
    }

    @Test
    @DisplayName("findByCode: returns empty for an unknown code")
    void findByCode_returnsEmpty_forUnknownCode() {
        Optional<Employee> found = employees.findByCode(
                com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode.of("EMP-0000"));

        assertTrue(found.isEmpty());
    }

    // ---- findByEmail ----

    @Test
    @DisplayName("findByEmail: returns the employee when the email exists (case-normalised)")
    void findByEmail_returnsEmployee() {
        String email = EmployeeFixtures.uniqueEmail("em7");
        employees.save(anEmployee().code("EMP-7002").email(email).build());
        flushAndClear();

        Optional<Employee> found = employees.findByEmail(email);

        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    // ---- findByUserId ----

    @Test
    @DisplayName("findByUserId: returns the employee when the userId is linked")
    void findByUserId_returnsEmployee() {
        // employees.user_id references users(id), so the linked account must exist (002 research F-010).
        UUID userId = users.save(aUser().build()).getId();
        employees.save(anEmployee().code("EMP-7003").userId(userId)
                .email(EmployeeFixtures.uniqueEmail("em8")).build());
        flushAndClear();

        Optional<Employee> found = employees.findByUserId(userId);

        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getUserId());
    }

    @Test
    @DisplayName("findByUserId: returns empty when no employee is linked to that userId")
    void findByUserId_returnsEmpty_whenNotLinked() {
        assertTrue(employees.findByUserId(UUID.randomUUID()).isEmpty());
    }

    // ---- findAll / findByStatus ----

    @Test
    @DisplayName("findAll: includes all saved employees")
    void findAll_includesSavedEmployees() {
        long before = employees.count();

        employees.save(anEmployee().code("EMP-7004").email(EmployeeFixtures.uniqueEmail("em9")).build());
        employees.save(anEmployee().code("EMP-7005").email(EmployeeFixtures.uniqueEmail("em10")).build());
        flushAndClear();

        List<Employee> all = employees.findAll();
        assertTrue(all.size() >= before + 2);
    }

    @Test
    @DisplayName("findByStatus: returns only employees with the requested status")
    void findByStatus_filtersCorrectly() {
        employees.save(anEmployee().code("EMP-7006").email(EmployeeFixtures.uniqueEmail("em11"))
                .status(EmploymentStatus.ON_LEAVE).build());
        flushAndClear();

        List<Employee> onLeave = employees.findByStatus(EmploymentStatus.ON_LEAVE);

        assertFalse(onLeave.isEmpty());
        onLeave.forEach(e -> assertEquals(EmploymentStatus.ON_LEAVE, e.getStatus()));
    }

    // ---- existsByCode / existsByEmail ----

    @Test
    @DisplayName("existsByCode: true for a saved code, false for an unknown code")
    void existsByCode() {
        Employee saved = employees.save(anEmployee().code("EMP-7007")
                .email(EmployeeFixtures.uniqueEmail("em12")).build());
        flushAndClear();

        assertTrue(employees.existsByCode(saved.getCode()));
        assertFalse(employees.existsByCode(
                com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode.of("EMP-0000")));
    }

    @Test
    @DisplayName("existsByEmail: true for a saved email, false for an unknown one")
    void existsByEmail() {
        String email = EmployeeFixtures.uniqueEmail("em13");
        employees.save(anEmployee().code("EMP-7008").email(email).build());
        flushAndClear();

        assertTrue(employees.existsByEmail(email));
        assertFalse(employees.existsByEmail("nobody@materia.test"));
    }

    // ---- deleteById ----

    @Test
    @DisplayName("deleteById: the employee is no longer findable after deletion")
    void deleteById_removesEmployee() {
        Employee saved = employees.save(anEmployee().code("EMP-7009")
                .email(EmployeeFixtures.uniqueEmail("em14")).build());
        flushAndClear();

        employees.deleteById(saved.getId());
        flushAndClear();

        assertTrue(employees.findById(saved.getId()).isEmpty());
    }

    // ---- Status persisted after terminate ----

    @Test
    @DisplayName("terminate: TERMINATED status and termination date survive a round-trip")
    void terminate_persistedCorrectly() {
        Employee saved = employees.save(anEmployee().code("EMP-7010")
                .email(EmployeeFixtures.uniqueEmail("em15")).build());
        flushAndClear();

        Employee loaded = employees.findById(saved.getId()).orElseThrow();
        loaded.terminate(LocalDate.of(2026, 6, 30), "Contract ended");
        employees.save(loaded);
        flushAndClear();

        Employee reloaded = employees.findById(saved.getId()).orElseThrow();
        assertEquals(EmploymentStatus.TERMINATED, reloaded.getStatus());
        assertEquals(LocalDate.of(2026, 6, 30), reloaded.getTerminationDate());
        assertEquals("Contract ended", reloaded.getTerminationReason());
    }
}
