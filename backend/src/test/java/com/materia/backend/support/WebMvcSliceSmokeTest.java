package com.materia.backend.support;

import com.materia.backend.contexts.masterData.domain.exceptions.CategoryNotFoundException;
import com.materia.backend.contexts.masterData.domain.ports.in.CategoryUseCase;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.controllers.CategoryController;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers.CategoryWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static com.materia.backend.support.ErrorResponseAssertions.assertError;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the web-slice infrastructure itself works before any story builds on it:
 * the test profile resolves, the real security rules load, and requests reach
 * the controller. Needs no database, so it runs without Docker.
 */
@WebMvcTest(CategoryController.class)
@Import(CategoryWebMapper.class)
class WebMvcSliceSmokeTest extends AbstractWebMvcTest {

    @MockBean
    private CategoryUseCase categoryUseCase;

    @Test
    @DisplayName("slice: a protected endpoint refuses an unauthenticated request with 401")
    void protectedEndpoint_unauthenticated_is401() throws Exception {
        mockMvc.perform(get("/api/v1/masterdata/categories/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "category:read")
    @DisplayName("slice: an authenticated request passes security and reaches the controller")
    void protectedEndpoint_authenticated_reachesController() throws Exception {
        // The mocked use case returns null, so the mapper yields an empty body. What matters
        // here is that the request got past security to the controller, not the payload.
        mockMvc.perform(get("/api/v1/masterdata/categories/{id}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "category:read")
    @DisplayName("slice: a domain not-found becomes a 404 with a specific errorCode, per the error contract")
    void missingRecord_mapsTo404WithErrorCode() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryUseCase.getById(id)).thenThrow(new CategoryNotFoundException(id.toString()));

        assertError(mockMvc.perform(get("/api/v1/masterdata/categories/{id}", id)),
                HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND");
    }

    @Test
    @DisplayName("slice: a public endpoint is not blocked by authentication")
    void publicEndpoint_unauthenticated_isNotBlockedBySecurity() throws Exception {
        // No auth controller is loaded in this slice, so the request finds no handler.
        // 404 proves security let it through; a 401 would mean the public list is not applied.
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isNotFound());
    }
}
