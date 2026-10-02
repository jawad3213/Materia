package com.materia.backend.contexts.purchaseRequisition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.materia.backend.common.application.PageResponse;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionSearchFilter;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The requisition workflow through the whole stack: HTTP, service, persistence and a real
 * PostgreSQL schema (T063, T064). Also carries the edge cases that only exist end to end.
 */
@AutoConfigureMockMvc
@WithMockUser(username = "alice", authorities = {"requisition:read", "requisition:write", "requisition:validate", "requisition:convert", "material:read", "material:write", "material:stock:read", "material:stock:write"})
class RequisitionWorkflowIT extends AbstractIntegrationTest {

    private static final String BASE = "/api/v1/purchase-requisitions";
    private static final String FINDING_021 =
            "FINDING-021: create honours a client-supplied status, so a requisition can be born APPROVED";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RequisitionRepository requisitions;
    @Autowired private MaterialRepository materials;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private Material orderableMaterial() {
        Material m = materials.save(aMaterial().price("12.50").build());
        flushAndClear();
        return m;
    }

    private JsonNode createViaHttp(Map<String, Object> body) throws Exception {
        String json = mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json);
    }

    // ---- Persistence round trip (T063) ----

    @Test
    @DisplayName("round trip: a requisition and its lines read back intact from the database")
    void roundTrip_withLines() {
        Requisition saved = requisitions.save(aRequisition().withLine(3, "10.00").withLine(2, "4.50").build());
        flushAndClear();

        Requisition read = requisitions.findById(saved.getId()).orElseThrow();
        assertEquals(2, read.getLines().size());
        assertEquals(new java.math.BigDecimal("39.00"), read.getTotalAmount().getAmount());
        assertEquals(RequisitionStatus.DRAFT, read.getStatus());
    }

    @Test
    @DisplayName("queries: by status and by requester match only the right rows")
    void queries_filterCorrectly() {
        String requester = "req-" + UUID.randomUUID();
        requisitions.save(aRequisition().inStatus(RequisitionStatus.SUBMITTED).requestedBy(requester).build());
        requisitions.save(aRequisition().inStatus(RequisitionStatus.DRAFT).requestedBy(requester).build());
        flushAndClear();

        List<Requisition> mine = requisitions.findByRequesterId(requester);
        assertEquals(2, mine.size());
        assertTrue(requisitions.findByStatus(RequisitionStatus.SUBMITTED).stream()
                .allMatch(r -> r.getStatus() == RequisitionStatus.SUBMITTED));
    }

    // ---- Pagination (T064) ----

    @Test
    @DisplayName("paging: walking every page visits each matching requisition exactly once")
    void paging_neitherRepeatsNorSkips() {
        String requester = "pager-" + UUID.randomUUID();
        for (int i = 0; i < 7; i++) {
            requisitions.save(aRequisition().requestedBy(requester).titled("Page item " + i).build());
        }
        flushAndClear();

        RequisitionSearchFilter filter = RequisitionSearchFilter.builder().requesterId(requester).build();
        Set<UUID> seen = new HashSet<>();
        int visited = 0;
        PageResponse<Requisition> page;
        int index = 0;
        do {
            page = requisitions.searchAdvanced(filter, index++, 3);
            for (Requisition r : page.getContent()) {
                assertTrue(seen.add(r.getId()), "requisition " + r.getId() + " appeared on two pages");
                visited++;
            }
        } while (!page.isLast());

        assertEquals(7, visited);
        assertEquals(7L, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    @DisplayName("paging: a negative page or a zero page size is 400, never a server error (spec edge case)")
    void paging_invalidValues_are400() throws Exception {
        mockMvc.perform(post(BASE + "/search").param("page", "-1").param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE + "/search").param("page", "0").param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("paging: a page beyond the last is an empty page, not an error (spec edge case)")
    void paging_beyondEnd_isEmpty() throws Exception {
        mockMvc.perform(post(BASE + "/search").param("page", "9999").param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.content").isEmpty());
    }

    // ---- End to end through HTTP ----

    @Test
    @DisplayName("create: a requisition created over HTTP starts as a draft, priced from the material")
    void create_overHttp_startsAsDraftPricedFromMaterial() throws Exception {
        Material material = orderableMaterial();

        JsonNode created = createViaHttp(Map.of("title", "Printer paper", "requesterName", "Alice",
                "lines", List.of(Map.of("materialId", material.getId().toString(), "quantity", 4))));

        assertEquals("DRAFT", created.get("status").asText());
        Requisition stored = requisitions.findById(UUID.fromString(created.get("id").asText())).orElseThrow();
        assertEquals(new java.math.BigDecimal("50.00"), stored.getTotalAmount().getAmount(), "4 x 12.50");
    }

    @Test
    @DisplayName("create: a line naming a material that does not exist is 404, and nothing is saved")
    void create_unknownMaterial_is404() throws Exception {
        long before = requisitions.findAll().size();

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(
                        Map.of("title", "T", "requesterName", "Alice",
                                "lines", List.of(Map.of("materialId", UUID.randomUUID().toString(), "quantity", 1))))))
                .andExpect(status().isNotFound());

        assertEquals(before, requisitions.findAll().size());
    }

    @Test
    @DisplayName("create: a requisition always starts as a draft, whatever status the caller asks for")
    void create_ignoresClientSuppliedStatus() throws Exception {
        Material material = orderableMaterial();

        JsonNode created = createViaHttp(Map.of("title", "Pre-approved", "requesterName", "Alice",
                "status", "APPROVED",
                "lines", List.of(Map.of("materialId", material.getId().toString(), "quantity", 1))));

        Requisition stored = requisitions.findById(UUID.fromString(created.get("id").asText())).orElseThrow();
        assertEquals(RequisitionStatus.DRAFT, stored.getStatus(),
                "a caller must not be able to skip approval by declaring the result");
    }
}
