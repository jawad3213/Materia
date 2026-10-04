package com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.invoice.application.dtos.CreateInvoiceInput;
import com.materia.backend.contexts.invoice.application.dtos.InvoiceOutput;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceStatus;
import com.materia.backend.contexts.invoice.domain.enums.InvoiceType;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotFoundException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceRuleViolationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceValidationException;
import com.materia.backend.contexts.invoice.domain.ports.in.InvoiceUseCase;
import com.materia.backend.contexts.invoice.infrastructure.adapters.in.web.mappers.InvoiceWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import org.junit.jupiter.api.BeforeEach;
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

/** Invoice endpoints: permissions per role (Role.java), the acting user, the frontend's payload, and error codes. */
@WebMvcTest(InvoiceController.class)
@Import(InvoiceWebMapper.class)
class InvoiceControllerTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/invoices";
    private static final UUID ID = UUID.randomUUID();

    /** Invoice-related permissions per role, as in Role.java. */
    private static final Map<String, List<String>> PERMISSIONS = Map.of(
            "ADMIN", List.of("invoice:read", "invoice:write", "invoice:validate", "payment:read", "payment:write"),
            "PURCHASER", List.of("invoice:read", "invoice:write", "payment:read"),
            "RECEIVER", List.of());

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private InvoiceUseCase useCase;

    /** The create payload exactly as the invoice screen sends it (no createdBy: the server takes the session user). */
    private static Map<String, Object> frontendCreatePayload() {
        return Map.of(
                "purchaseOrderId", UUID.randomUUID().toString(),
                "purchaseOrderCode", "PO-2026-0001",
                "supplierId", UUID.randomUUID().toString(),
                "supplierName", "Acme Supplies",
                "invoiceType", "STANDARD",
                "externalReference", "FA-2026-00123",
                "invoiceDate", "2026-10-04",
                "dueDate", "2026-11-03",
                "currencyCode", "MAD",
                "lines", List.of(Map.of(
                        "lineNumber", 1,
                        "purchaseOrderLineId", UUID.randomUUID().toString(),
                        "materialCode", "MAT-1",
                        "materialName", "Bolts",
                        "unitOfMeasure", "PCE",
                        "quantityInvoiced", 6,
                        "unitPrice", 5.0,
                        "taxAmount", 2.0)));
    }

    record Endpoint(String name, HttpMethod method, String path, Object body, String permission) {
        @Override
        public String toString() {
            return name;
        }
    }

    static List<Endpoint> endpoints() {
        return List.of(
                new Endpoint("create", HttpMethod.POST, BASE, frontendCreatePayload(), "invoice:write"),
                new Endpoint("get by id", HttpMethod.GET, BASE + "/" + ID, null, "invoice:read"),
                new Endpoint("get by code", HttpMethod.GET, BASE + "/code/INV-2026-0001", null, "invoice:read"),
                new Endpoint("list", HttpMethod.GET, BASE, null, "invoice:read"),
                new Endpoint("update", HttpMethod.PUT, BASE + "/" + ID, Map.of("notes", "x"), "invoice:write"),
                new Endpoint("delete", HttpMethod.DELETE, BASE + "/" + ID, null, "invoice:write"),
                new Endpoint("by status", HttpMethod.GET, BASE + "/status/DRAFT", null, "invoice:read"),
                new Endpoint("by supplier", HttpMethod.GET, BASE + "/supplier/s-1", null, "invoice:read"),
                new Endpoint("by order", HttpMethod.GET, BASE + "/purchase-order/" + UUID.randomUUID(), null, "invoice:read"),
                new Endpoint("search", HttpMethod.GET, BASE + "/search/keyword?keyword=acme", null, "invoice:read"),
                new Endpoint("submit", HttpMethod.PATCH, BASE + "/" + ID + "/submit", Map.of(), "invoice:write"),
                new Endpoint("verify", HttpMethod.PATCH, BASE + "/" + ID + "/verify", Map.of("userName", "Ada"), "invoice:validate"),
                new Endpoint("pay", HttpMethod.PATCH, BASE + "/" + ID + "/pay", Map.of("amount", 32.0, "userName", "Ada"), "payment:write"),
                new Endpoint("cancel", HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of("reason", "Duplicate"), "invoice:write")
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

    private void storedInvoiceIs(InvoiceStatus status) {
        InvoiceOutput stored = new InvoiceOutput();
        stored.setId(ID);
        stored.setStatus(status);
        when(useCase.getById(ID)).thenReturn(stored);
    }

    @BeforeEach
    void draftByDefault() {
        storedInvoiceIs(InvoiceStatus.DRAFT);
    }

    @ParameterizedTest(name = "{0} as {1}")
    @MethodSource("endpointsByRole")
    @DisplayName("access: each endpoint needs its Role.java permission; others get 403")
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
    @DisplayName("cancel: a verified invoice can be cancelled by a verifier, not by a purchaser")
    void cancelVerified_needsValidate() throws Exception {
        storedInvoiceIs(InvoiceStatus.VERIFIED);

        mockMvc.perform(as("PURCHASER", call(HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of("reason", "x"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of("reason", "x"))))
                .andExpect(status().isOk());
        verify(useCase, times(1)).cancel(eq(ID), eq("user-ADMIN"), eq("x"));
    }

    @Test
    @DisplayName("identity: the acting user is the session user; ids sent in the body are ignored")
    void actingUser_isThePrincipal() throws Exception {
        mockMvc.perform(as("PURCHASER", call(HttpMethod.POST, BASE,
                new java.util.HashMap<>(frontendCreatePayload()) {{ put("createdBy", "someone-else"); }})))
                .andExpect(status().isCreated());
        ArgumentCaptor<CreateInvoiceInput> created = ArgumentCaptor.forClass(CreateInvoiceInput.class);
        verify(useCase).create(created.capture());
        assertEquals("user-PURCHASER", created.getValue().getUserId());

        // Ids and names in the body are ignored: the service names the session user from their account.
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/verify",
                Map.of("userId", "someone-else", "userName", "Forged Name")))).andExpect(status().isOk());
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/verify", null))).andExpect(status().isOk());
        verify(useCase, times(2)).verify(ID, "user-ADMIN");

        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/pay",
                Map.of("amount", 32.0, "userId", "someone-else", "userName", "Forged Name")))).andExpect(status().isOk());
        verify(useCase).pay(ID, "user-ADMIN", 32.0);

        mockMvc.perform(as("PURCHASER", call(HttpMethod.PATCH, BASE + "/" + ID + "/cancel",
                Map.of("reason", "Duplicate", "userId", "someone-else")))).andExpect(status().isOk());
        verify(useCase).cancel(ID, "user-PURCHASER", "Duplicate");
    }

    @Test
    @DisplayName("payload: the frontend's create request maps type, dates, whole quantities and prices in the invoice currency")
    void frontendPayload_isMapped() throws Exception {
        mockMvc.perform(as("PURCHASER", call(HttpMethod.POST, BASE, frontendCreatePayload()))).andExpect(status().isCreated());

        ArgumentCaptor<CreateInvoiceInput> created = ArgumentCaptor.forClass(CreateInvoiceInput.class);
        verify(useCase).create(created.capture());
        CreateInvoiceInput input = created.getValue();
        assertEquals(InvoiceType.STANDARD, input.getInvoiceType());
        assertEquals(java.time.LocalDate.of(2026, 11, 3), input.getDueDate());
        assertEquals(6, input.getLines().get(0).getQuantityInvoiced());
        assertEquals("MAD", input.getLines().get(0).getUnitPrice().getCurrencyCode());
        assertEquals(0, new java.math.BigDecimal("5").compareTo(input.getLines().get(0).getUnitPrice().getAmount()));
    }

    @Test
    @DisplayName("payload: a fractional invoiced quantity is refused with 400, never truncated")
    void fractionalQuantity_isRefused() throws Exception {
        Map<String, Object> payload = new java.util.HashMap<>(frontendCreatePayload());
        payload.put("lines", List.of(Map.of("purchaseOrderLineId", "x", "materialCode", "MAT-1", "materialName", "Bolts",
                "quantityInvoiced", 2.5, "unitPrice", 5.0)));

        mockMvc.perform(as("PURCHASER", call(HttpMethod.POST, BASE, payload))).andExpect(status().isBadRequest());
        verify(useCase, never()).create(any());
    }

    @Test
    @DisplayName("errors: not found is 404, invalid input 400, and a business-rule refusal 409")
    void errorCodes() throws Exception {
        when(useCase.submit(eq(ID), anyString())).thenThrow(new InvoiceRuleViolationException("Only drafts"));
        mockMvc.perform(as("PURCHASER", call(HttpMethod.PATCH, BASE + "/" + ID + "/submit", Map.of())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only drafts"));

        UUID missing = UUID.randomUUID();
        when(useCase.getById(missing)).thenThrow(new InvoiceNotFoundException(missing.toString()));
        mockMvc.perform(as("PURCHASER", call(HttpMethod.GET, BASE + "/" + missing, null))).andExpect(status().isNotFound());

        when(useCase.pay(eq(ID), anyString(), anyDouble()))
                .thenThrow(new InvoiceValidationException("amount", "Le montant payé doit être positif"));
        mockMvc.perform(as("ADMIN", call(HttpMethod.PATCH, BASE + "/" + ID + "/pay", Map.of("amount", 1.0))))
                .andExpect(status().isBadRequest());
    }
}
