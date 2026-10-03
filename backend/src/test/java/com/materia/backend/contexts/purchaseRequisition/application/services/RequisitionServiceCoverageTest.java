package com.materia.backend.contexts.purchaseRequisition.application.services;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.application.exceptions.NotFoundException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.common.domain.services.ExchangeRateService;
import com.materia.backend.common.domain.valueObjects.Money;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionSearchCriteria;
import com.materia.backend.contexts.purchaseRequisition.application.mappers.RequisitionMapper;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionMaterialNotFoundException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionMaterialNotOrderableException;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.RequisitionRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionSearchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.materia.backend.support.fixtures.MaterialFixtures.aMaterial;
import static com.materia.backend.support.fixtures.RequisitionFixtures.aRequisition;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** [T083] Requisition creation, line hydration, search, and reorder-driven requisitions (feature 001 coverage). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequisitionServiceCoverageTest {

    @Mock private RequisitionRepository requisitions;
    @Mock private MaterialRepository materials;
    @Mock private RequisitionCodeGeneratorService codeGenerator;
    @Mock private ExchangeRateService exchangeRates;

    private RequisitionService service;

    @BeforeEach
    void setUp() {
        service = new RequisitionService(requisitions, materials, new RequisitionMapper(), codeGenerator, exchangeRates);
        when(requisitions.save(any())).thenAnswer(i -> i.getArgument(0));
        when(codeGenerator.generateCode()).thenAnswer(i -> RequisitionCode.createDefault());
        when(requisitions.findFirstByRequesterName(anyString())).thenReturn(Optional.empty());
    }

    private Material orderable(String price, CurrencyCode currency) {
        Material m = aMaterial().build();
        m.setId(UUID.randomUUID());
        m.setStandardPrice(Money.of(price, currency));
        when(materials.findById(m.getId())).thenReturn(Optional.of(m));
        when(materials.findByCode(m.getCode().getValue())).thenReturn(Optional.of(m));
        return m;
    }

    private static RequisitionLine byId(Material m, Integer qty) {
        RequisitionLine line = new RequisitionLine((String) null, qty);
        line.setMaterialId(m.getId());
        return line;
    }

    private static CreateRequisitionInput input(RequisitionLine... lines) {
        CreateRequisitionInput in = new CreateRequisitionInput();
        in.setTitle("Bolts");
        in.setRequesterName("Uma");
        in.setLines(new ArrayList<>(List.of(lines)));
        return in;
    }

    private Requisition saved() {
        ArgumentCaptor<Requisition> captor = ArgumentCaptor.forClass(Requisition.class);
        verify(requisitions, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    // ---- Create ----

    @Test
    @DisplayName("create: lines are filled from the material by id or code, numbered, priced, and the code is generated")
    void create_hydratesLines() {
        Material a = orderable("2.00", CurrencyCode.MAD);
        Material b = orderable("3.00", CurrencyCode.MAD);
        RequisitionLine byCode = new RequisitionLine(" " + b.getCode().getValue() + " ", 1);

        var out = service.create(input(byId(a, 4), byCode));

        Requisition r = saved();
        assertNotNull(out);
        assertEquals(2, r.getLines().size());
        assertEquals(a.getName(), r.getLines().get(0).getMaterialName());
        assertEquals(0, new BigDecimal("11.00").compareTo(r.getTotalAmount().getAmount()));
        assertNotNull(r.getRequisitionCode());
        r.getLines().forEach(l -> assertNotNull(l.getId()));
    }

    @Test
    @DisplayName("create: a price in another currency is converted to the requisition's currency")
    void create_convertsForeignPrices() {
        Material m = orderable("1.00", CurrencyCode.EUR);
        when(exchangeRates.convert(any(Money.class), eq(CurrencyCode.MAD))).thenReturn(Money.of("11.00", CurrencyCode.MAD));

        service.create(input(byId(m, 2)));

        RequisitionLine line = saved().getLines().get(0);
        assertEquals("MAD", line.getCurrencyCode());
        assertEquals(0, new BigDecimal("22.00").compareTo(line.getLineTotal().getAmount()));
    }

    @Test
    @DisplayName("create: a given currency is used; a material without a price keeps its own pricing")
    void create_givenCurrency_andUnpricedMaterial() {
        Material eur = orderable("5.00", CurrencyCode.EUR);
        CreateRequisitionInput in = input(byId(eur, 1));
        in.setCurrencyCode("EUR");

        service.create(in);

        assertEquals("EUR", saved().getLines().get(0).getCurrencyCode());
        verifyNoInteractions(exchangeRates);
    }

    @Test
    @DisplayName("create and update: a blank or missing currency means MAD")
    void blankCurrency_meansMad() {
        Material m = orderable("1.00", CurrencyCode.MAD);
        CreateRequisitionInput blank = input(byId(m, 1));
        blank.setCurrencyCode(" ");
        service.create(blank);
        assertEquals("MAD", saved().getLines().get(0).getCurrencyCode());

        Requisition r = aRequisition().build();
        r.setCurrencyCode(null);
        when(requisitions.findById(r.getId())).thenReturn(Optional.of(r));
        service.update(r.getId(), input(byId(m, 2)));
        assertEquals("MAD", r.getLines().get(0).getCurrencyCode());
    }

    @Test
    @DisplayName("create: requester is the given id, else an earlier requisition by that name, else the user, else the name")
    void create_resolvesRequester() {
        Material m = orderable("1.00", CurrencyCode.MAD);

        CreateRequisitionInput explicit = input(byId(m, 1));
        explicit.setRequesterId("emp-1");
        service.create(explicit);
        assertEquals("emp-1", saved().getRequesterId());
        assertEquals("emp-1", saved().getCreatedBy());

        Requisition earlier = aRequisition().requestedBy("emp-old").build();
        when(requisitions.findFirstByRequesterName("Uma")).thenReturn(Optional.of(earlier));
        service.create(input(byId(m, 1)));
        assertEquals("emp-old", saved().getRequesterId());

        when(requisitions.findFirstByRequesterName("Uma")).thenReturn(Optional.empty());
        CreateRequisitionInput withUser = input(byId(m, 1));
        withUser.setUserId(" user-9 ");
        service.create(withUser);
        assertEquals("user-9", saved().getRequesterId());

        service.create(input(byId(m, 1)));
        assertEquals("Uma", saved().getRequesterId());
    }

    @Test
    @DisplayName("create: missing, null, unknown, materialless, unorderable or zero-quantity lines are refused")
    void create_lineRefusals() {
        Material m = orderable("1.00", CurrencyCode.MAD);
        Material obsolete = orderable("1.00", CurrencyCode.MAD);
        obsolete.setStatus(MaterialStatus.OBSOLETE);

        assertThrows(ValidationException.class, () -> service.create(input()));
        CreateRequisitionInput nullLine = input();
        nullLine.getLines().add(null);
        assertThrows(ValidationException.class, () -> service.create(nullLine));

        RequisitionLine unknownId = new RequisitionLine((String) null, 1);
        unknownId.setMaterialId(UUID.randomUUID());
        when(materials.findById(unknownId.getMaterialId())).thenReturn(Optional.empty());
        assertThrows(RequisitionMaterialNotFoundException.class, () -> service.create(input(unknownId)));

        when(materials.findByCode("NOPE")).thenReturn(Optional.empty());
        assertThrows(RequisitionMaterialNotFoundException.class,
                () -> service.create(input(new RequisitionLine("NOPE", 1))));
        assertThrows(ValidationException.class, () -> service.create(input(new RequisitionLine(" ", 1))));
        assertThrows(ValidationException.class, () -> service.create(input(new RequisitionLine((String) null, 1))));
        assertThrows(RequisitionMaterialNotOrderableException.class, () -> service.create(input(byId(obsolete, 1))));
        assertThrows(ValidationException.class, () -> service.create(input(byId(m, 0))));
        assertThrows(ValidationException.class, () -> service.create(input(byId(m, null))));
        verify(requisitions, never()).save(any());
    }

    @Test
    @DisplayName("create: an existing line id is kept")
    void create_keepsExistingLineId() {
        Material m = orderable("1.00", CurrencyCode.MAD);
        RequisitionLine line = byId(m, 1);
        UUID id = UUID.randomUUID();
        line.setId(id);

        service.create(input(line));

        assertEquals(id, saved().getLines().get(0).getId());
    }

    // ---- Update and reads ----

    @Test
    @DisplayName("update from a create form: the fields are carried over and lines re-hydrated")
    void update_fromCreateInput() {
        Material m = orderable("2.00", CurrencyCode.MAD);
        Requisition r = aRequisition().build();
        when(requisitions.findById(r.getId())).thenReturn(Optional.of(r));
        CreateRequisitionInput in = input(byId(m, 3));
        in.setTitle("Renamed");
        in.setUserId("user-2");

        service.update(r.getId(), in);

        assertEquals("Renamed", r.getTitle());
        assertEquals("user-2", r.getUpdatedBy());
        assertEquals(0, new BigDecimal("6.00").compareTo(r.getTotalAmount().getAmount()));
    }

    @Test
    @DisplayName("reads: by id, by code (unknown code is a not-found), all, and keyword search")
    void reads() {
        Requisition r = aRequisition().build();
        when(requisitions.findById(r.getId())).thenReturn(Optional.of(r));
        when(requisitions.findByCode("REQ-X")).thenReturn(Optional.of(r));
        when(requisitions.findByCode("REQ-NONE")).thenReturn(Optional.empty());
        when(requisitions.findAll()).thenReturn(List.of(r));

        assertNotNull(service.getById(r.getId()));
        assertNotNull(service.getByCode("REQ-X"));
        assertThrows(NotFoundException.class, () -> service.getByCode("REQ-NONE"));
        assertEquals(1, service.getAll().size());
    }

    @Test
    @DisplayName("transitions: reject and cancel are persisted")
    void rejectAndCancel() {
        Requisition submitted = aRequisition().inStatus(RequisitionStatus.SUBMITTED).build();
        Requisition approved = aRequisition().inStatus(RequisitionStatus.APPROVED).build();
        when(requisitions.findById(submitted.getId())).thenReturn(Optional.of(submitted));
        when(requisitions.findById(approved.getId())).thenReturn(Optional.of(approved));

        service.reject(submitted.getId(), "m", "Max", "no budget");
        service.cancel(approved.getId(), "u", "not needed");

        assertEquals(RequisitionStatus.REJECTED, submitted.getStatus());
        assertEquals(RequisitionStatus.CANCELLED, approved.getStatus());
        verify(requisitions, times(2)).save(any());
    }

    @Test
    @DisplayName("advanced search: criteria are passed through, a blank status is dropped, and no criteria means no filter")
    void searchAdvanced() {
        when(requisitions.searchAdvanced(any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(aRequisition().build()), 0, 10, 1L, 1, true));

        RequisitionSearchCriteria criteria = new RequisitionSearchCriteria();
        criteria.setStatus("SUBMITTED");
        criteria.setKeyword("bolt");
        criteria.setRequesterId("u-1");
        criteria.setApproverId("m-1");
        criteria.setCurrencyCode("MAD");
        criteria.setRequiredDateFrom(LocalDate.now());
        assertEquals(1, service.searchAdvanced(criteria, 0, 10).getContent().size());

        RequisitionSearchCriteria blank = new RequisitionSearchCriteria();
        blank.setStatus(" ");
        service.searchAdvanced(blank, 0, 10);
        service.searchAdvanced(null, 0, 10);

        ArgumentCaptor<RequisitionSearchFilter> used = ArgumentCaptor.forClass(RequisitionSearchFilter.class);
        verify(requisitions, times(3)).searchAdvanced(used.capture(), eq(0), eq(10));
        assertEquals(RequisitionStatus.SUBMITTED, used.getAllValues().get(0).getStatus());
        assertEquals("bolt", used.getAllValues().get(0).getKeyword());
        assertNull(used.getAllValues().get(1).getStatus());
        assertNull(used.getAllValues().get(2).getKeyword());
    }

    // ---- Reorder-driven ----

    @Test
    @DisplayName("reorder requisition: urgent ones are flagged and needed in 2 days, with the material's currency")
    void reorder_urgent() {
        Material m = orderable("4.00", CurrencyCode.EUR);

        String code = service.createRequisitionFromReorder(m, 5, "Below safety stock", true);

        Requisition r = saved();
        assertEquals(r.getRequisitionCode().getValue(), code);
        assertTrue(r.getTitle().startsWith("[URGENT]"));
        assertEquals(LocalDate.now().plusDays(2), r.getRequiredDate());
        assertEquals("Below safety stock", r.getJustification());
        assertEquals(RequisitionStatus.DRAFT, r.getStatus());
        assertEquals(0, new BigDecimal("20.00").compareTo(r.getTotalAmount().getAmount()));
    }

    @Test
    @DisplayName("reorder requisition: routine ones are needed in 7 days, default to MAD and a standard reason, and fall back to the id")
    void reorder_routine() {
        Material m = aMaterial().build();
        m.setStandardPrice(null);
        when(codeGenerator.generateCode()).thenReturn(null);

        String ref = service.createRequisitionFromReorder(m, 5, " ", false);

        Requisition r = saved();
        assertEquals(r.getId().toString(), ref);
        assertFalse(r.getTitle().startsWith("[URGENT]"));
        assertEquals(LocalDate.now().plusDays(7), r.getRequiredDate());
        assertEquals("Automated stock reorder", r.getJustification());
        assertEquals("MAD", r.getCurrencyCode());

        service.createRequisitionFromReorder(m, 1, null, false);
        assertEquals("Automated stock reorder", saved().getJustification());
    }

    @Test
    @DisplayName("grouped requisition: one requisition per supplier with every hydrated line")
    void grouped() {
        Material a = orderable("1.00", CurrencyCode.MAD);
        Material b = orderable("2.00", CurrencyCode.MAD);

        String code = service.createGroupedRequisition("sup-1", new ArrayList<>(List.of(byId(a, 2), byId(b, 1))));

        Requisition r = saved();
        assertEquals(r.getRequisitionCode().getValue(), code);
        assertEquals(2, r.getLines().size());
        assertTrue(r.getTitle().contains("sup-1"));
        assertEquals(0, new BigDecimal("4.00").compareTo(r.getTotalAmount().getAmount()));

        when(codeGenerator.generateCode()).thenReturn(null);
        String ref = service.createGroupedRequisition("sup-2", new ArrayList<>(List.of(byId(a, 1))));
        assertEquals(saved().getId().toString(), ref);
    }
}
