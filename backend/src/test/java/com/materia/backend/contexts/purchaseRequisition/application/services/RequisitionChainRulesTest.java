package com.materia.backend.contexts.purchaseRequisition.application.services;

import com.materia.backend.common.domain.services.ExchangeRateService;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseRequisition.application.mappers.RequisitionMapper;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionBusinessException;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequesterDirectory;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Requisition rules used by the procurement chain: requesters, open requisitions, receipts and approval names. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequisitionChainRulesTest {

    @Mock private RequisitionRepository requisitions;
    @Mock private MaterialRepository materials;
    @Mock private RequisitionCodeGeneratorService codes;
    @Mock private ExchangeRateService rates;
    @Mock private RequesterDirectory directory;

    private RequisitionService service;

    @BeforeEach
    void setUp() {
        service = new RequisitionService(requisitions, materials, new RequisitionMapper(), codes, rates, directory);
        when(requisitions.save(any(Requisition.class))).thenAnswer(inv -> inv.getArgument(0));
        when(codes.generateCode()).thenReturn(RequisitionCode.createDefault());
        when(directory.displayName("user-1")).thenReturn("Uma User");
        when(requisitions.findByStatus(any())).thenReturn(List.of());
    }

    private Requisition saved() {
        ArgumentCaptor<Requisition> captor = ArgumentCaptor.forClass(Requisition.class);
        verify(requisitions, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("reorder: the system requests automatic reorders; a user who clicked is the requester, named from their account")
    void reorderRequester() {
        Material m = aMaterial().stock(1).price("2.00").build();

        service.createRequisitionFromReorder(m, 5, "auto", true);
        assertEquals(RequisitionService.SYSTEM_REQUESTER_ID, saved().getRequesterId());
        assertEquals(RequisitionService.SYSTEM_REQUESTER_NAME, saved().getRequesterName());

        service.createRequisitionFromReorder(m, 5, "manual", false, "user-1");
        assertEquals("user-1", saved().getRequesterId());
        assertEquals("Uma User", saved().getRequesterName());
        assertEquals("user-1", saved().getCreatedBy());

        service.createRequisitionFromReorder(m, 5, "blank user", false, " ");
        assertEquals(RequisitionService.SYSTEM_REQUESTER_ID, saved().getRequesterId());
    }

    @Test
    @DisplayName("reorder: without a directory the requester's id stands in for their name")
    void reorderRequester_withoutDirectory() {
        RequisitionService bare = new RequisitionService(requisitions, materials, new RequisitionMapper(), codes, rates);
        bare.createRequisitionFromReorder(aMaterial().stock(1).build(), 5, "manual", false, "user-2");

        assertEquals("user-2", saved().getRequesterName());
    }

    @Test
    @DisplayName("open requisitions: a draft, submitted or approved requisition for the material counts, by id or by code")
    void openRequisitions() {
        UUID materialId = UUID.randomUUID();
        Requisition byId = aRequisition().build();
        byId.getLines().get(0).setMaterialId(materialId);
        Requisition byCode = aRequisition().build();
        byCode.getLines().get(0).setMaterialCode("MAT-OPEN");
        Requisition noLines = aRequisition().build();
        noLines.setLines(null);
        when(requisitions.findByStatus(RequisitionStatus.DRAFT)).thenReturn(List.of(noLines, byId));
        when(requisitions.findByStatus(RequisitionStatus.APPROVED)).thenReturn(List.of(byCode));

        assertTrue(service.hasOpenRequisitionFor(materialId, null));
        assertTrue(service.hasOpenRequisitionFor(null, "MAT-OPEN"));
        assertFalse(service.hasOpenRequisitionFor(UUID.randomUUID(), "MAT-OTHER"));
        assertFalse(service.hasOpenRequisitionFor(null, null));
    }

    @Test
    @DisplayName("receipts: accepted and rejected quantities accumulate, capped at the requested quantity; unknown lines change nothing")
    void recordReceipt() {
        RequisitionLine line = new RequisitionLine("MAT-1", 10);
        line.setId(UUID.randomUUID());
        Requisition requisition = aRequisition().build();
        requisition.setLines(new ArrayList<>(List.of(line)));
        when(requisitions.findById(requisition.getId())).thenReturn(Optional.of(requisition));

        service.recordReceipt(requisition.getId(), line.getId(), 6, 1);
        assertEquals(6, line.getQuantityReceived());
        assertEquals(1, line.getQuantityRejected());

        service.recordReceipt(requisition.getId(), line.getId(), 8, 5);
        assertEquals(10, line.getQuantityReceived(), "capped at the requested 10");
        assertEquals(0, line.getQuantityRejected(), "nothing left for rejections once everything is received");

        clearInvocations(requisitions);
        service.recordReceipt(requisition.getId(), UUID.randomUUID(), 3, 0);
        service.recordReceipt(requisition.getId(), null, 3, 0);
        verify(requisitions, never()).save(any());

        RequisitionLine noQuantity = new RequisitionLine("MAT-2", null);
        noQuantity.setId(UUID.randomUUID());
        requisition.setLines(new ArrayList<>(List.of(noQuantity)));
        service.recordReceipt(requisition.getId(), noQuantity.getId(), 3, -2);
        verify(requisitions, never()).save(any());
    }

    @Test
    @DisplayName("approval: the approver is named from their account; neither the requester nor the creator may approve")
    void approvalNamesAndSelfApproval() {
        Requisition requisition = aRequisition().requestedBy("employee-1").inStatus(RequisitionStatus.SUBMITTED).build();
        requisition.setCreatedBy("user-1");
        when(requisitions.findById(requisition.getId())).thenReturn(Optional.of(requisition));

        assertThrows(RequisitionBusinessException.class, () -> service.approve(requisition.getId(), "user-1", "user-1", null));
        assertThrows(RequisitionBusinessException.class, () -> service.approve(requisition.getId(), "employee-1", "E", null));

        when(directory.displayName("manager-1")).thenReturn("Max Manager");
        assertEquals("Max Manager", service.approve(requisition.getId(), "manager-1", "manager-1", null).getApproverName());

        Requisition other = aRequisition().inStatus(RequisitionStatus.SUBMITTED).build();
        when(requisitions.findById(other.getId())).thenReturn(Optional.of(other));
        assertEquals("Given Name", service.reject(other.getId(), "manager-2", "Given Name", "no").getApproverName(),
                "a name given explicitly is kept");
    }
}
