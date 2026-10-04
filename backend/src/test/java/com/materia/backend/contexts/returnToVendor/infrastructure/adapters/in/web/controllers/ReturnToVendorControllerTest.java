package com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorInvalidQuantityException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotFoundException;
import com.materia.backend.contexts.returnToVendor.domain.ports.in.ReturnToVendorUseCase;
import com.materia.backend.contexts.returnToVendor.infrastructure.adapters.in.web.mappers.ReturnToVendorWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Return endpoints: permissions (Role.java), the acting user taken from the session, payload checks and error codes. */
@WebMvcTest(ReturnToVendorController.class)
@Import(ReturnToVendorWebMapper.class)
class ReturnToVendorControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/return-to-vendors";
    private static final UUID ID = UUID.randomUUID();

    /** Every role can read and prepare returns (Role.java); a user without the permissions is refused. */
    private static final Map<String, List<String>> PERMISSIONS = Map.of(
            "ADMIN", List.of("return:read", "return:write"),
            "PURCHASER", List.of("return:read", "return:write"),
            "RECEIVER", List.of("return:read", "return:write"),
            "NONE", List.of());

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private ReturnToVendorUseCase useCase;

    private static Map<String, Object> createPayload() {
        return Map.of(
                "goodsReceiptId", UUID.randomUUID().toString(),
                "returnReason", "Scratched surfaces",
                "lines", List.of(Map.of("goodsReceiptLineId", UUID.randomUUID().toString(), "quantityToReturn", 3)));
    }

    record Endpoint(String name, HttpMethod method, String path, Object body, String permission) {
        @Override
        public String toString() {
            return name;
        }
    }

    static List<Endpoint> endpoints() {
        return List.of(
                new Endpoint("create", HttpMethod.POST, BASE, createPayload(), "return:write"),
                new Endpoint("get by id", HttpMethod.GET, BASE + "/" + ID, null, "return:read"),
                new Endpoint("get by code", HttpMethod.GET, BASE + "/code/RTN-2026-0001", null, "return:read"),
                new Endpoint("list", HttpMethod.GET, BASE, null, "return:read"),
                new Endpoint("update", HttpMethod.PUT, BASE + "/" + ID, Map.of("notes", "x"), "return:write"),
                new Endpoint("delete", HttpMethod.DELETE, BASE + "/" + ID, null, "return:write"),
                new Endpoint("by receipt", HttpMethod.GET, BASE + "/goods-receipt/gr-1", null, "return:read"),
                new Endpoint("by order", HttpMethod.GET, BASE + "/purchase-order/po-1", null, "return:read"),
                new Endpoint("by supplier", HttpMethod.GET, BASE + "/supplier/s-1", null, "return:read"),
                new Endpoint("by status", HttpMethod.GET, BASE + "/status/PENDING", null, "return:read"),
                new Endpoint("search", HttpMethod.GET, BASE + "/search?keyword=acme", null, "return:read"),
                new Endpoint("submit", HttpMethod.PATCH, BASE + "/" + ID + "/submit", null, "return:write"),
                new Endpoint("resolve", HttpMethod.PATCH, BASE + "/" + ID + "/resolve",
                        Map.of("resolutionType", "CREDIT_NOTE", "reference", "AV-1"), "return:write"),
                new Endpoint("cancel", HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of("reason", "Duplicate"), "return:write"));
    }

    static Stream<Arguments> endpointsByRole() {
        return endpoints().stream().flatMap(e -> PERMISSIONS.keySet().stream().map(role -> Arguments.of(e, role)));
    }

    private static List<SimpleGrantedAuthority> authorities(String role) {
        List<SimpleGrantedAuthority> result = new ArrayList<>();
        PERMISSIONS.get(role).forEach(p -> result.add(new SimpleGrantedAuthority(p)));
        result.add(new SimpleGrantedAuthority("ROLE_" + role));
        return result;
    }

    private MockHttpServletRequestBuilder call(HttpMethod method, String path, Object body) throws Exception {
        MockHttpServletRequestBuilder builder = request(method, path);
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }
        return builder;
    }

    private MockHttpServletRequestBuilder as(String role, MockHttpServletRequestBuilder builder) {
        return builder.with(user("user-" + role).authorities(authorities(role)));
    }

    @ParameterizedTest(name = "{0} as {1}")
    @MethodSource("endpointsByRole")
    @DisplayName("access: every role with the return permissions reaches the endpoint; a user without them gets 403")
    void eachRole_isAllowedOrForbidden(Endpoint endpoint, String role) throws Exception {
        boolean allowed = PERMISSIONS.get(role).contains(endpoint.permission());

        int status = mockMvc.perform(as(role, call(endpoint.method(), endpoint.path(), endpoint.body())))
                .andReturn().getResponse().getStatus();

        if (allowed) {
            assertTrue(status < 400, () -> role + " should reach " + endpoint + " but got " + status);
        } else {
            assertEquals(403, status, () -> role + " should be refused " + endpoint);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    @DisplayName("access: every endpoint refuses a request with no session with 401")
    void noSession_isUnauthenticated(Endpoint endpoint) throws Exception {
        int status = mockMvc.perform(call(endpoint.method(), endpoint.path(), endpoint.body()).with(anonymous()))
                .andReturn().getResponse().getStatus();

        assertEquals(401, status, () -> endpoint + " without a session");
    }

    @Test
    @DisplayName("payload: only the receipt, quantities and reasons are mapped, and the acting user is the session user")
    void payload_andActingUser() throws Exception {
        Map<String, Object> forged = new HashMap<>(createPayload());
        forged.put("userId", "someone-else");
        forged.put("supplierName", "Forged Supplier");
        mockMvc.perform(as("RECEIVER", call(HttpMethod.POST, BASE, forged))).andExpect(status().isCreated());

        ArgumentCaptor<CreateReturnToVendorInput> created = ArgumentCaptor.forClass(CreateReturnToVendorInput.class);
        verify(useCase).create(created.capture());
        assertEquals("user-RECEIVER", created.getValue().getUserId());
        assertEquals(3, created.getValue().getLines().get(0).getQuantityToReturn());

        mockMvc.perform(as("PURCHASER", call(HttpMethod.PATCH, BASE + "/" + ID + "/resolve",
                Map.of("resolutionType", "REPLACEMENT", "reference", "RMA-1", "supplierResponse", "New lot")))).andExpect(status().isOk());
        verify(useCase).resolve(ID, "user-PURCHASER", ResolutionType.REPLACEMENT, "RMA-1", "New lot");

        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/submit", null))).andExpect(status().isOk());
        verify(useCase).submit(ID, "user-ADMIN");

        mockMvc.perform(as("ADMIN", call(HttpMethod.DELETE, BASE + "/" + ID + "?userId=forged", null))).andExpect(status().isNoContent());
        verify(useCase).delete(ID, "user-ADMIN");
    }

    @Test
    @DisplayName("payload: a return without lines, a non-positive quantity, a resolution without reference or a cancel without reason is 400")
    void invalidPayloads() throws Exception {
        for (Object lines : List.of(List.of(), List.of(Map.of("goodsReceiptLineId", "l-1", "quantityToReturn", 0)),
                List.of(Map.of("quantityToReturn", 2)))) {
            Map<String, Object> payload = new HashMap<>(createPayload());
            payload.put("lines", lines);
            mockMvc.perform(as("ADMIN", call(HttpMethod.POST, BASE, payload))).andExpect(status().isBadRequest());
        }
        Map<String, Object> noReason = new HashMap<>(createPayload());
        noReason.remove("returnReason");
        mockMvc.perform(as("ADMIN", call(HttpMethod.POST, BASE, noReason))).andExpect(status().isBadRequest());
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/resolve", Map.of("resolutionType", "CREDIT_NOTE"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of())))
                .andExpect(status().isBadRequest());
        verify(useCase, never()).create(any());
        verify(useCase, never()).cancel(any(), any(), any());
    }

    @Test
    @DisplayName("errors: not found is 404 and a quantity beyond the rejection is a 409 rule violation")
    void errorCodes() throws Exception {
        UUID missing = UUID.randomUUID();
        when(useCase.getById(missing)).thenThrow(new ReturnToVendorNotFoundException(missing));
        mockMvc.perform(as("PURCHASER", call(HttpMethod.GET, BASE + "/" + missing, null))).andExpect(status().isNotFound());

        when(useCase.create(any())).thenThrow(new ReturnToVendorInvalidQuantityException("Only 1 PCS can still be returned"));
        mockMvc.perform(as("ADMIN", call(HttpMethod.POST, BASE, createPayload())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only 1 PCS can still be returned"));
        verify(useCase, never()).resolve(any(), anyString(), any(), anyString(), any());
    }
}
