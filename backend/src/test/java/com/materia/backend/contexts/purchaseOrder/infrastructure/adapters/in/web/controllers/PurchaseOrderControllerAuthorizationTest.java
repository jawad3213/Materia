package com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.ReceiverDirectory;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers.PurchaseOrderWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import com.materia.backend.support.ActionMatrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * [T040, T045] Every purchase order endpoint, as every role and with no session (US4, FR-010, FR-011).
 *
 * <p>Roles carry exactly the permissions the shared action matrix lists for them (which
 * {@code ActionMatrixTest} keeps equal to {@code Role.java}), plus their {@code ROLE_*} authority.
 */
@WebMvcTest(PurchaseOrderController.class)
@Import(PurchaseOrderWebMapper.class)
class PurchaseOrderControllerAuthorizationTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/purchase-orders";
    private static final UUID ID = UUID.randomUUID();

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private PurchaseOrderUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    /** One endpoint: how to call it, and which permission set may. */
    record Endpoint(String name, HttpMethod method, String path, Map<String, Object> body,
                    BiPredicate<Set<String>, String> allowed) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static BiPredicate<Set<String>, String> needs(String permission) {
        return (permissions, role) -> permissions.contains(permission);
    }

    private static Map<String, Object> createBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("supplierId", UUID.randomUUID());
        body.put("supplierName", "Acme");
        body.put("currencyCode", "MAD");
        body.put("orderedBy", "buyer-1");
        body.put("createdBy", "buyer-1");
        body.put("lines", List.of(Map.of("materialCode", "MAT-1", "quantity", 1, "unitPrice", 1)));
        return body;
    }

    static List<Endpoint> endpoints() {
        return List.of(
                new Endpoint("1 create", HttpMethod.POST, BASE, createBody(), needs("order:write")),
                new Endpoint("2 assignable receivers", HttpMethod.GET, BASE + "/assignable-receivers", null, needs("order:write")),
                new Endpoint("3 get by id", HttpMethod.GET, BASE + "/" + ID, null, needs("order:read")),
                new Endpoint("4 get by code", HttpMethod.GET, BASE + "/code/PO-2026-0001", null, needs("order:read")),
                new Endpoint("5 list", HttpMethod.GET, BASE, null, needs("order:read")),
                new Endpoint("6 update", HttpMethod.PUT, BASE + "/" + ID, Map.of("notes", "x"), needs("order:write")),
                new Endpoint("7 delete", HttpMethod.DELETE, BASE + "/" + ID, null,
                        (permissions, role) -> permissions.contains("order:write") && role.equals("ADMIN")),
                new Endpoint("8 by status", HttpMethod.GET, BASE + "/status/DRAFT", null, needs("order:read")),
                new Endpoint("9 by delivery status", HttpMethod.GET, BASE + "/delivery-status/SHIPPED", null, needs("order:read")),
                new Endpoint("10 by supplier", HttpMethod.GET, BASE + "/supplier/" + UUID.randomUUID(), null, needs("order:read")),
                new Endpoint("11 by requisition", HttpMethod.GET, BASE + "/requisition/" + UUID.randomUUID(), null, needs("order:read")),
                new Endpoint("12 search", HttpMethod.GET, BASE + "/search/keyword?keyword=acme", null, needs("order:read")),
                new Endpoint("13 submit", HttpMethod.PATCH, BASE + "/" + ID + "/submit", null, needs("order:write")),
                new Endpoint("14 confirm", HttpMethod.PATCH, BASE + "/" + ID + "/confirm", null, needs("order:validate")),
                new Endpoint("15 reject", HttpMethod.PATCH, BASE + "/" + ID + "/reject", Map.of("reason", "No stock"), needs("order:validate")),
                new Endpoint("16 assign receiver", HttpMethod.PATCH, BASE + "/" + ID + "/assign-receiver",
                        Map.of("assignedUserId", UUID.randomUUID().toString()), needs("order:write")),
                new Endpoint("17 confirm receipt", HttpMethod.PATCH, BASE + "/" + ID + "/confirm-receipt", null,
                        (permissions, role) -> permissions.contains("order:write") || permissions.contains("receipt:write")),
                new Endpoint("18 cancel", HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of("reason", "x"), needs("order:cancel")),
                new Endpoint("19 complete", HttpMethod.PATCH, BASE + "/" + ID + "/complete", null, needs("order:write")),
                new Endpoint("20 delivery status", HttpMethod.PATCH, BASE + "/" + ID + "/delivery-status",
                        Map.of("deliveryStatus", "SHIPPED"), needs("order:write"))
        );
    }

    static Stream<Arguments> endpointsByRole() {
        return endpoints().stream().flatMap(e -> ActionMatrix.roles().stream().map(role -> Arguments.of(e, role)));
    }

    private MockHttpServletRequestBuilder call(Endpoint e) throws Exception {
        MockHttpServletRequestBuilder builder = request(e.method(), e.path());
        if (e.body() != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(e.body()));
        }
        return builder;
    }

    private static List<SimpleGrantedAuthority> authorities(String role) {
        List<SimpleGrantedAuthority> result = new ArrayList<>();
        ActionMatrix.permissions(role).forEach(p -> result.add(new SimpleGrantedAuthority(p)));
        result.add(new SimpleGrantedAuthority("ROLE_" + role));
        return result;
    }

    @ParameterizedTest(name = "{0} as {1}")
    @MethodSource("endpointsByRole")
    @DisplayName("access: each role reaches exactly the endpoints its permissions allow; others get 403 (FR-010)")
    void eachRole_isAllowedOrForbidden(Endpoint endpoint, String role) throws Exception {
        boolean allowed = endpoint.allowed().test(ActionMatrix.permissions(role), role);

        int status = mockMvc.perform(call(endpoint).with(user("user-" + role).authorities(authorities(role))))
                .andReturn().getResponse().getStatus();

        if (allowed) {
            assertTrue(status < 400, () -> role + " should reach " + endpoint + " but got " + status);
        } else {
            assertEquals(403, status, () -> role + " should be refused " + endpoint);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    @DisplayName("access: every endpoint refuses a request with no session with 401 (FR-011)")
    void noSession_isUnauthenticated(Endpoint endpoint) throws Exception {
        int status = mockMvc.perform(call(endpoint).with(anonymous())).andReturn().getResponse().getStatus();

        assertEquals(401, status, () -> endpoint + " without a session");
    }

    @Test
    @DisplayName("separation of duties: a purchaser cannot record the supplier's confirmation or rejection (US4-4)")
    void purchaser_cannotRecordSupplierDecision() throws Exception {
        for (String action : List.of("confirm", "reject")) {
            int status = mockMvc.perform(request(HttpMethod.PATCH, BASE + "/" + ID + "/" + action)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"x\"}")
                            .with(user("buyer").authorities(authorities("PURCHASER"))))
                    .andReturn().getResponse().getStatus();
            assertEquals(403, status, action);
        }
    }

    @Test
    @DisplayName("separation of duties: receivers can neither assign orders nor change their tracking (F-001, F-002)")
    void receiver_cannotAssignOrTrack() throws Exception {
        int assign = mockMvc.perform(request(HttpMethod.PATCH, BASE + "/" + ID + "/assign-receiver")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"assignedUserId\":\"self\"}")
                        .with(user("receiver").authorities(authorities("RECEIVER"))))
                .andReturn().getResponse().getStatus();
        int track = mockMvc.perform(request(HttpMethod.PATCH, BASE + "/" + ID + "/delivery-status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"deliveryStatus\":\"SHIPPED\"}")
                        .with(user("receiver").authorities(authorities("RECEIVER"))))
                .andReturn().getResponse().getStatus();

        assertEquals(403, assign);
        assertEquals(403, track);
    }

    @Test
    @DisplayName("receivers list: each entry exposes exactly id, name and email and nothing else (US4-9)")
    void assignableReceivers_exposeOnlyIdNameEmail() throws Exception {
        when(useCase.getAssignableReceivers()).thenReturn(List.of(
                new ReceiverDirectory.Receiver("r-1", "Rita Receiver", "rita@materia.test")));

        String json = mockMvc.perform(request(HttpMethod.GET, BASE + "/assignable-receivers")
                        .with(user("buyer").authorities(authorities("PURCHASER"))))
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> entries = objectMapper.readValue(json, new TypeReference<>() { });

        assertEquals(1, entries.size());
        assertEquals(Set.of("id", "name", "email"), entries.get(0).keySet());
    }
}
