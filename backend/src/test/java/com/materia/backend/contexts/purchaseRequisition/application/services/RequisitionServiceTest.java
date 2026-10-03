package com.materia.backend.contexts.purchaseRequisition.application.services;

import com.materia.backend.common.domain.services.ExchangeRateService;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.mappers.RequisitionMapper;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionInvalidStatusTransitionException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotDeletableException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotFoundException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionNotModifiableException;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus.*;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Service orchestration for the requisition workflow (T056-T058). */
@ExtendWith(MockitoExtension.class)
class RequisitionServiceTest {

    @Mock private RequisitionRepository requisitions;
    @Mock private MaterialRepository materials;
    @Mock private RequisitionCodeGeneratorService codeGenerator;
    @Mock private ExchangeRateService exchangeRates;

    private RequisitionService service;

    @BeforeEach
    void setUp() {
        service = new RequisitionService(requisitions, materials, new RequisitionMapper(), codeGenerator, exchangeRates);
        lenient().when(requisitions.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Requisition stored(RequisitionStatus status) {
        Requisition r = aRequisition().inStatus(status).build();
        when(requisitions.findById(r.getId())).thenReturn(Optional.of(r));
        return r;
    }

    // ---- Transitions persist, refusals do not ----

    @Test
    @DisplayName("submit: a permitted transition is persisted")
    void submit_persists() {
        Requisition r = stored(DRAFT);

        assertEquals(SUBMITTED.getCode(), codeOf(service.submit(r.getId(), "user-1").getStatus()));
        verify(requisitions).save(r);
    }

    @Test
    @DisplayName("approve: approving a draft is refused and nothing is saved")
    void approve_fromDraft_isRefusedAndNotSaved() {
        Requisition r = stored(DRAFT);

        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> service.approve(r.getId(), "mgr", "Morgan", null));
        verify(requisitions, never()).save(any());
    }

    @Test
    @DisplayName("convert: converting an unapproved requisition is refused and nothing is saved")
    void convert_fromSubmitted_isRefusedAndNotSaved() {
        Requisition r = stored(SUBMITTED);

        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> service.convert(r.getId(), "po-1", "PO-2026-0001", "buyer"));
        verify(requisitions, never()).save(any());
    }

    @Test
    @DisplayName("release: reverting a conversion linked to the order persists the requisition as approved (US2-6)")
    void revertConversion_persists() {
        UUID orderId = UUID.randomUUID();
        Requisition r = aRequisition().convertedTo(orderId, "PO-2026-0001").build();
        when(requisitions.findById(r.getId())).thenReturn(Optional.of(r));

        assertEquals(APPROVED.getCode(), codeOf(service.revertConversion(r.getId(), orderId.toString(), "buyer").getStatus()));
        verify(requisitions).save(r);
    }

    @Test
    @DisplayName("release: reverting for a different order is refused and nothing is saved (US2-7)")
    void revertConversion_otherOrder_isRefusedAndNotSaved() {
        Requisition r = aRequisition().convertedTo(UUID.randomUUID(), "PO-2026-0001").build();
        when(requisitions.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RequisitionInvalidStatusTransitionException.class,
                () -> service.revertConversion(r.getId(), UUID.randomUUID().toString(), "buyer"));
        verify(requisitions, never()).save(any());
    }

    @Test
    @DisplayName("release: reverting an unknown requisition is a not-found")
    void revertConversion_unknown_isNotFound() {
        UUID unknown = UUID.randomUUID();
        when(requisitions.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(RequisitionNotFoundException.class, () -> service.revertConversion(unknown, "po", "buyer"));
    }

    @Test
    @DisplayName("transitions: an unknown requisition is a not-found, for every action")
    void transitions_unknownId_isNotFound() {
        UUID unknown = UUID.randomUUID();
        when(requisitions.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(RequisitionNotFoundException.class, () -> service.submit(unknown, "u"));
        assertThrows(RequisitionNotFoundException.class, () -> service.approve(unknown, "a", "A", null));
        assertThrows(RequisitionNotFoundException.class, () -> service.reject(unknown, "a", "A", "r"));
        assertThrows(RequisitionNotFoundException.class, () -> service.cancel(unknown, "u", "r"));
        assertThrows(RequisitionNotFoundException.class, () -> service.convert(unknown, "p", "PO-2026-0001", "u"));
        assertThrows(RequisitionNotFoundException.class, () -> service.getById(unknown));
    }

    // ---- Edit and delete guards (US2 scenarios 6-7) ----

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = RequisitionStatus.class, names = {"APPROVED", "REJECTED", "CANCELLED", "CONVERTED"})
    @DisplayName("update: editing a requisition past submission is refused, and its contents are left alone")
    void update_pastSubmission_isRefused(RequisitionStatus status) {
        Requisition r = stored(status);
        String originalTitle = r.getTitle();
        UpdateRequisitionInput change = new UpdateRequisitionInput();
        change.setTitle("tampered");

        assertThrows(RequisitionNotModifiableException.class, () -> service.update(r.getId(), change));
        assertEquals(originalTitle, r.getTitle(), "the guard must run before any change is applied");
        verify(requisitions, never()).save(any());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = RequisitionStatus.class, names = {"SUBMITTED", "APPROVED", "CONVERTED"})
    @DisplayName("delete: a requisition in progress or converted cannot be deleted")
    void delete_inProgress_isRefused(RequisitionStatus status) {
        Requisition r = stored(status);

        assertThrows(RequisitionNotDeletableException.class, () -> service.delete(r.getId()));
        verify(requisitions, never()).deleteById(any());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = RequisitionStatus.class, names = {"DRAFT", "REJECTED", "CANCELLED"})
    @DisplayName("delete: draft, rejected and cancelled requisitions may be deleted")
    void delete_permittedStates_succeed(RequisitionStatus status) {
        Requisition r = stored(status);

        service.delete(r.getId());

        verify(requisitions).deleteById(r.getId());
    }

    // ---- Queries (US2 scenario 8) ----

    @Test
    @DisplayName("query by status: passes the parsed status through and returns only what the repository matched")
    void getByStatus_filters() {
        Requisition submitted = aRequisition().inStatus(SUBMITTED).build();
        when(requisitions.findByStatus(SUBMITTED)).thenReturn(List.of(submitted));

        var result = service.getByStatus("SUBMITTED");

        assertEquals(1, result.size());
        verify(requisitions).findByStatus(SUBMITTED);
    }

    @Test
    @DisplayName("query by status: an unknown status is refused rather than matching nothing")
    void getByStatus_unknownStatus_isRefused() {
        assertThrows(RuntimeException.class, () -> service.getByStatus("NOT_A_STATUS"));
        verify(requisitions, never()).findByStatus(any());
    }

    @Test
    @DisplayName("query: a search matching nothing returns an empty list, never an error")
    void search_noMatch_isEmpty() {
        when(requisitions.search("nothing-matches")).thenReturn(List.of());

        assertTrue(service.searchByKeyword("nothing-matches").isEmpty());
    }

    @Test
    @DisplayName("query by requester: returns that requester's requisitions")
    void getByRequester_filters() {
        when(requisitions.findByRequesterId("alice")).thenReturn(List.of(aRequisition().requestedBy("alice").build()));

        assertEquals(1, service.getByRequesterId("alice").size());
    }

    private static String codeOf(Object status) {
        return status == null ? null : status.toString();
    }
}
