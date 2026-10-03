package com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.mappers.GoodsReceiptWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import com.materia.backend.support.ActionMatrix;
import org.junit.jupiter.api.DisplayName;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/** [T041] Every goods receipt endpoint, as every role and with no session (US4, FR-010, FR-011). */
@WebMvcTest(GoodsReceiptController.class)
@Import(GoodsReceiptWebMapper.class)
class GoodsReceiptControllerAuthorizationTest extends AbstractWebMvcTest {

    private static final String BASE = "/api/v1/goods-receipts";
    private static final UUID ID = UUID.randomUUID();
    private static final Map<String, Object> LINE =
            Map.of("materialCode", "MAT-1", "quantityReceived", 1, "quantityRejected", 0);

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private GoodsReceiptUseCase useCase;

    @Autowired
    private ObjectMapper objectMapper;

    record Endpoint(String name, HttpMethod method, String path, Object body, String permission) {
        @Override
        public String toString() {
            return name;
        }
    }

    static List<Endpoint> endpoints() {
        return List.of(
                new Endpoint("21 create", HttpMethod.POST, BASE,
                        Map.of("purchaseOrderId", UUID.randomUUID().toString(), "lines", List.of(LINE)), "receipt:write"),
                new Endpoint("22 get by id", HttpMethod.GET, BASE + "/" + ID, null, "receipt:read"),
                new Endpoint("23 get by code", HttpMethod.GET, BASE + "/code/GR-2026-0001", null, "receipt:read"),
                new Endpoint("24 list", HttpMethod.GET, BASE, null, "receipt:read"),
                new Endpoint("25 update", HttpMethod.PUT, BASE + "/" + ID, Map.of("notes", "x"), "receipt:write"),
                new Endpoint("26 delete", HttpMethod.DELETE, BASE + "/" + ID, null, "receipt:write"),
                new Endpoint("27 by status", HttpMethod.GET, BASE + "/status/DRAFT", null, "receipt:read"),
                new Endpoint("28 by order", HttpMethod.GET, BASE + "/purchase-order/" + UUID.randomUUID(), null, "receipt:read"),
                new Endpoint("29 by receiver", HttpMethod.GET, BASE + "/receiver/receiver-1", null, "receipt:read"),
                new Endpoint("30 search", HttpMethod.GET, BASE + "/search/keyword?keyword=acme", null, "receipt:read"),
                new Endpoint("31 add line", HttpMethod.PATCH, BASE + "/" + ID + "/lines", Map.of("line", LINE), "receipt:write"),
                new Endpoint("32 remove line", HttpMethod.DELETE, BASE + "/" + ID + "/lines/0", Map.of(), "receipt:write"),
                new Endpoint("33 complete", HttpMethod.PATCH, BASE + "/" + ID + "/complete", Map.of(), "receipt:write"),
                new Endpoint("34 cancel", HttpMethod.PATCH, BASE + "/" + ID + "/cancel", Map.of("reason", "x"), "receipt:write")
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
    @DisplayName("access: reads need receipt:read and writes need receipt:write; others get 403 (FR-010)")
    void eachRole_isAllowedOrForbidden(Endpoint endpoint, String role) throws Exception {
        boolean allowed = ActionMatrix.permissions(role).contains(endpoint.permission());

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
}
