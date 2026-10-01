package com.materia.backend.support;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Assertions for the error contract in {@code contracts/error-contract.md}.
 *
 * <p>Refusals must be distinguishable (FR-007), so tests assert both the HTTP status and the
 * machine-readable {@code errorCode}. Neither the human-readable {@code message} nor the
 * {@code timestamp} value is asserted: wording may change freely, and timestamps are
 * non-deterministic (FR-011). Only their presence is checked.
 */
public final class ErrorResponseAssertions {

    private ErrorResponseAssertions() {
    }

    /** Asserts an error response with the given status and a well-formed body. */
    public static ResultActions assertError(ResultActions result, HttpStatus expected) throws Exception {
        return result
                .andExpect(status().is(expected.value()))
                .andExpect(jsonPath("$.status").value(expected.value()))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.message").value(notNullValue()));
    }

    /** Asserts an error response with the given status and a specific {@code errorCode}. */
    public static ResultActions assertError(ResultActions result, HttpStatus expected, String errorCode) throws Exception {
        return assertError(result, expected)
                .andExpect(jsonPath("$.errorCode").value(errorCode));
    }

    /** Asserts a 400 that names the offending field in {@code validationErrors}. */
    public static ResultActions assertValidationError(ResultActions result, String field) throws Exception {
        return assertError(result, HttpStatus.BAD_REQUEST)
                .andExpect(jsonPath("$.validationErrors." + field).value(notNullValue()));
    }
}
