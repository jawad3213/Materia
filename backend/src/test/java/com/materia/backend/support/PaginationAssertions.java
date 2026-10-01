package com.materia.backend.support;

import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Assertions for paged responses.
 *
 * <p>The page arithmetic itself ({@code totalPages}, {@code last}) is computed by Spring Data,
 * which Spring already tests, so it is not re-verified here. What this project owns is the
 * field-by-field copy from Spring's {@code Page} into {@code PageResponse}, done by hand in
 * each persistence adapter, where fields can be dropped or swapped. These assertions check
 * that copy, using the field names the frontend reads.
 */
public final class PaginationAssertions {

    private PaginationAssertions() {
    }

    /**
     * Asserts every page field. {@code last} is the serialised name of {@code PageResponse.isLast()},
     * which Jackson derives by dropping the "is" prefix.
     */
    public static ResultActions assertPage(ResultActions result,
                                           int pageNumber,
                                           int pageSize,
                                           long totalElements,
                                           int totalPages,
                                           boolean last,
                                           int contentSize) throws Exception {
        return result
                .andExpect(jsonPath("$.pageNumber").value(pageNumber))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.totalElements").value(totalElements))
                .andExpect(jsonPath("$.totalPages").value(totalPages))
                .andExpect(jsonPath("$.last").value(last))
                .andExpect(jsonPath("$.content", hasSize(contentSize)));
    }

    /** Asserts an empty result: a valid page containing nothing, rather than an error. */
    public static ResultActions assertEmptyPage(ResultActions result) throws Exception {
        return result
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
