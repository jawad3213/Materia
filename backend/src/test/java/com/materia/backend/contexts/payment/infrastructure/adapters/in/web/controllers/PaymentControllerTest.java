package com.materia.backend.contexts.payment.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.payment.application.dtos.CreatePaymentInput;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentAmountMismatchException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotFoundException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.payment.domain.ports.in.PaymentUseCase;
import com.materia.backend.contexts.payment.infrastructure.adapters.in.web.mappers.PaymentWebMapper;
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

import java.math.BigDecimal;
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

/** Payment endpoints: permissions per role (Role.java), the acting user, the frontend's payload, and error codes. */
@WebMvcTest(PaymentController.class)
@Import(PaymentWebMapper.class)
class PaymentControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/payments";
    private static final UUID ID = UUID.randomUUID();

    /** Payment permissions per role, as in Role.java. */
    private static final Map<String, List<String>> PERMISSIONS = Map.of(
            "ADMIN", List.of("payment:read", "payment:write"),
            "PURCHASER", List.of("payment:read"),
            "RECEIVER", List.of());

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private PaymentUseCase useCase;

    /** The create payload exactly as the payment screen sends it. */
    private static Map<String, Object> frontendCreatePayload() {
        return Map.of(
                "supplierId", UUID.randomUUID().toString(),
                "currencyCode", "MAD",
                "notes", "Octobre",
                "lines", List.of(
                        Map.of("invoiceId", UUID.randomUUID().toString(), "amount", 32.0),
                        Map.of("invoiceId", UUID.randomUUID().toString(), "amount", 12.5)));
    }

    record Endpoint(String name, HttpMethod method, String path, Object body, String permission) {
        @Override
        public String toString() {
            return name;
        }
    }

    static List<Endpoint> endpoints() {
        return List.of(
                new Endpoint("create", HttpMethod.POST, BASE, frontendCreatePayload(), "payment:write"),
                new Endpoint("get by id", HttpMethod.GET, BASE + "/" + ID, null, "payment:read"),
                new Endpoint("get by code", HttpMethod.GET, BASE + "/code/PAY-2026-0001", null, "payment:read"),
                new Endpoint("list", HttpMethod.GET, BASE, null, "payment:read"),
                new Endpoint("update", HttpMethod.PUT, BASE + "/" + ID, Map.of("notes", "x"), "payment:write"),
                new Endpoint("delete", HttpMethod.DELETE, BASE + "/" + ID, null, "payment:write"),
                new Endpoint("by status", HttpMethod.GET, BASE + "/status/PENDING", null, "payment:read"),
                new Endpoint("by supplier", HttpMethod.GET, BASE + "/supplier/s-1", null, "payment:read"),
                new Endpoint("search", HttpMethod.GET, BASE + "/search/keyword?keyword=acme", null, "payment:read"),
                new Endpoint("prepare", HttpMethod.PATCH, BASE + "/" + ID + "/prepare", Map.of(), "payment:write"),
                new Endpoint("complete", HttpMethod.PATCH, BASE + "/" + ID + "/complete",
                        Map.of("paymentMethod", "BANK_TRANSFER", "bankReference", "VIR-1"), "payment:write"),
                new Endpoint("cancel", HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of("reason", "Duplicate"), "payment:write")
        );
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
    @DisplayName("access: administrators manage payments, purchasers only read them, receivers get 403")
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
    @DisplayName("payload: the frontend's request is mapped without a client total, and the acting user is the session user")
    void frontendPayload_andActingUser() throws Exception {
        Map<String, Object> forged = new HashMap<>(frontendCreatePayload());
        forged.put("userId", "someone-else");
        mockMvc.perform(as("ADMIN", call(HttpMethod.POST, BASE, forged))).andExpect(status().isCreated());

        ArgumentCaptor<CreatePaymentInput> created = ArgumentCaptor.forClass(CreatePaymentInput.class);
        verify(useCase).create(created.capture());
        CreatePaymentInput input = created.getValue();
        assertEquals("user-ADMIN", input.getUserId());
        assertNull(input.getTotalAmount(), "the total is computed by the server");
        assertEquals(2, input.getLines().size());
        assertEquals(0, new BigDecimal("12.5").compareTo(input.getLines().get(1).getAmount()));

        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/complete",
                Map.of("paymentMethod", "CHECK", "bankReference", "CHQ-1", "userId", "someone-else")))).andExpect(status().isOk());
        verify(useCase).complete(ID, "user-ADMIN", "CHQ-1", null, "CHECK");

        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/prepare", null))).andExpect(status().isOk());
        verify(useCase).prepare(ID, "user-ADMIN");
    }

    @Test
    @DisplayName("payload: a line without amount, a non-positive amount, or a payment without lines is refused with 400")
    void invalidPayloads() throws Exception {
        String invoiceId = UUID.randomUUID().toString();
        for (Object lines : List.of(List.of(Map.of("invoiceId", invoiceId)), List.of(Map.of("invoiceId", invoiceId, "amount", 0)), List.of())) {
            Map<String, Object> payload = new HashMap<>(frontendCreatePayload());
            payload.put("lines", lines);
            mockMvc.perform(as("ADMIN", call(HttpMethod.POST, BASE, payload))).andExpect(status().isBadRequest());
        }
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/complete", Map.of())))
                .andExpect(status().isBadRequest());
        verify(useCase, never()).create(any());
    }

    @Test
    @DisplayName("errors: not found is 404, invalid input 400, and a business-rule refusal 409")
    void errorCodes() throws Exception {
        when(useCase.prepare(eq(ID), anyString())).thenThrow(new PaymentAmountMismatchException("dépasse ce qui reste à payer"));
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/prepare", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("dépasse ce qui reste à payer"));

        UUID missing = UUID.randomUUID();
        when(useCase.getById(missing)).thenThrow(new PaymentNotFoundException(missing.toString()));
        mockMvc.perform(as("PURCHASER", call(HttpMethod.GET, BASE + "/" + missing, null))).andExpect(status().isNotFound());

        when(useCase.complete(eq(ID), anyString(), any(), any(), anyString()))
                .thenThrow(new PaymentValidationException("Unknown payment method"));
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/complete", Map.of("paymentMethod", "BARTER"))))
                .andExpect(status().isBadRequest());
    }
}
