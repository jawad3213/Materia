package com.materia.backend.gateway.exception;

import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.application.exceptions.NotFoundException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.contexts.auth.domain.exceptions.*;
import com.materia.backend.contexts.employee.domain.exceptions.*;
import com.materia.backend.contexts.masterData.domain.exceptions.*;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * [T119] Exception Mapping Guard Test.
 *
 * <p>Asserts that every in-scope business/domain exception resolves to a specific
 * 4xx handler method in {@link GlobalExceptionHandler} and NEVER falls through to the
 * generic 500 {@code handleGlobalException(Exception, WebRequest)} handler.
 *
 * <p>Prevents recurrence of FINDING-005 (DuplicateMaterialCodeException -> 500),
 * FINDING-006 (AccountLockedException -> 500), and FINDING-007 (InvalidCredentialsException -> 500).
 */
class ExceptionMappingGuardTest {

    private ExceptionHandlerMethodResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class);
    }

    private static final List<Class<? extends Throwable>> IN_SCOPE_EXCEPTIONS = List.of(
            // Common
            BusinessException.class,
            NotFoundException.class,
            ValidationException.class,

            // Auth (US1)
            AccountLockedException.class,
            InvalidCredentialsException.class,
            AuthenticationFailedException.class,
            EmailAlreadyExistsException.class,
            UserAlreadyExistsException.class,
            UserNotFoundException.class,
            InvalidTokenException.class,
            TokenExpiredException.class,

            // Employees (US4)
            EmployeeNotFoundException.class,
            EmployeeCodeAlreadyExistsException.class,
            EmailAlreadyInUseException.class,

            // Catalogue & Materials (US3, US5)
            DuplicateMaterialCodeException.class,
            MaterialNotFoundException.class,
            InsufficientStockException.class,

            // Requisitions (US2)
            RequisitionBusinessException.class,
            RequisitionNotFoundException.class,
            RequisitionInvalidStatusTransitionException.class,
            RequisitionNotModifiableException.class,
            RequisitionNotDeletableException.class,

            // Database constraints (a referenced record, a duplicate unique value)
            org.springframework.dao.DataIntegrityViolationException.class
    );

    @Test
    @DisplayName("exception guard: no in-scope domain exception resolves to the generic 500 handler")
    void noInScopeException_resolvesTo500Handler() {
        Method fallbackMethod = resolver.resolveMethodByExceptionType(Exception.class);
        assertThat(fallbackMethod).isNotNull();
        assertThat(fallbackMethod.getName()).isEqualTo("handleGlobalException");

        for (Class<? extends Throwable> exClass : IN_SCOPE_EXCEPTIONS) {
            Method resolved = resolver.resolveMethodByExceptionType(exClass);
            assertThat(resolved)
                    .as("Exception %s must have a dedicated handler and not fall through to 500", exClass.getSimpleName())
                    .isNotNull()
                    .isNotEqualTo(fallbackMethod);
        }
    }

    @Test
    @DisplayName("exception guard: FINDING-005 DuplicateMaterialCodeException resolves to conflict handler")
    void duplicateMaterialCodeException_doesNotMapTo500() {
        Method resolved = resolver.resolveMethodByExceptionType(DuplicateMaterialCodeException.class);
        assertThat(resolved).isNotNull();
        assertThat(resolved.getName()).isEqualTo("handleDuplicateMaterialCodeException");
    }

    @Test
    @DisplayName("exception guard: FINDING-006 AccountLockedException resolves to unauthorized handler")
    void accountLockedException_doesNotMapTo500() {
        Method resolved = resolver.resolveMethodByExceptionType(AccountLockedException.class);
        assertThat(resolved).isNotNull();
        assertThat(resolved.getName()).isEqualTo("handleAccountLockedException");
    }

    @Test
    @DisplayName("exception guard: FINDING-007 InvalidCredentialsException resolves to unauthorized handler")
    void invalidCredentialsException_doesNotMapTo500() {
        Method resolved = resolver.resolveMethodByExceptionType(InvalidCredentialsException.class);
        assertThat(resolved).isNotNull();
        assertThat(resolved.getName()).isEqualTo("handleInvalidCredentialsException");
    }

    @Test
    @DisplayName("exception guard: a constraint violation is a 409 that says whether the record is referenced or a duplicate")
    void dataIntegrityViolation_is409WithAReadableReason() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        org.springframework.web.context.request.WebRequest request =
                new org.springframework.web.context.request.ServletWebRequest(
                        new org.springframework.mock.web.MockHttpServletRequest("DELETE", "/api/v1/masterdata/suppliers/1"));

        var referenced = handler.handleDataIntegrityViolation(new org.springframework.dao.DataIntegrityViolationException(
                "x", new RuntimeException("violates foreign key constraint \"fk_orders_supplier\"")), request);
        assertThat(referenced.getStatusCode().value()).isEqualTo(409);
        assertThat(referenced.getBody().getErrorCode()).isEqualTo("REFERENCED_RECORD");

        var duplicate = handler.handleDataIntegrityViolation(new org.springframework.dao.DataIntegrityViolationException(
                "x", new RuntimeException("duplicate key value violates unique constraint \"uk_code\"")), request);
        assertThat(duplicate.getStatusCode().value()).isEqualTo(409);
        assertThat(duplicate.getBody().getErrorCode()).isEqualTo("DATA_INTEGRITY_VIOLATION");
    }
}
