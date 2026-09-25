package com.materia.backend.domain;

import com.materia.backend.contexts.goodsReceipt.application.services.GoodsReceiptCodeGeneratorService;
import com.materia.backend.contexts.goodsReceipt.domain.valueObjects.ReceiptCode;
import com.materia.backend.contexts.invoice.application.services.InvoiceCodeGeneratorService;
import com.materia.backend.contexts.invoice.domain.valueObjects.InvoiceCode;
import com.materia.backend.contexts.masterData.application.services.CategoryCodeGeneratorService;
import com.materia.backend.contexts.masterData.application.services.MaterialCodeGeneratorService;
import com.materia.backend.contexts.masterData.application.services.SupplierCodeGeneratorService;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.ports.out.CodeSequenceRepository;
import com.materia.backend.contexts.masterData.domain.valueObjects.MaterialCode;
import com.materia.backend.contexts.payement.application.services.PaymentCodeGeneratorService;
import com.materia.backend.contexts.payement.domain.valueObjects.PaymentCode;
import com.materia.backend.contexts.purchaseOrder.application.services.PurchaseOrderCodeGeneratorService;
import com.materia.backend.contexts.purchaseOrder.domain.valueObjects.OrderCode;
import com.materia.backend.contexts.purchaseRequisition.application.services.RequisitionCodeGeneratorService;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode;
import com.materia.backend.contexts.returnToVendor.application.services.ReturnToVendorCodeGeneratorService;
import com.materia.backend.contexts.returnToVendor.domain.valueObjects.ReturnCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YearBasedCodeGenerationTest {

    private static class InMemoryCodeSequenceRepository implements CodeSequenceRepository {
        private final Map<String, Integer> sequences = new HashMap<>();

        @Override
        public int getNextValueAndIncrement(String prefix) {
            int current = sequences.getOrDefault(prefix, 1);
            sequences.put(prefix, current + 1);
            return current;
        }
    }

    @Test
    @DisplayName("MaterialCode supports year-based formats and backward compatibility")
    void testMaterialCodeValidationAndParsing() {
        int currentYear = Year.now().getValue();

        MaterialCode withYear = MaterialCode.of("MAT-" + currentYear + "-0001");
        assertEquals("MAT", withYear.getPrefix());
        assertEquals("0001", withYear.getNumber());
        assertEquals(1, withYear.getNumberAsInt());
        assertEquals(currentYear, withYear.getYear());

        MaterialCode lowerCase = MaterialCode.of("material-" + currentYear + "-0005");
        assertEquals("material", lowerCase.getPrefix());
        assertEquals(5, lowerCase.getNumberAsInt());

        MaterialCode legacy = MaterialCode.of("MAT-0042");
        assertEquals("MAT", legacy.getPrefix());
        assertEquals(42, legacy.getNumberAsInt());
        assertEquals(-1, legacy.getYear());

        MaterialCode generated = MaterialCode.fromPrefixAndNumber("MAT", 7);
        assertEquals(String.format("MAT-%d-0007", currentYear), generated.getValue());

        MaterialCode next = MaterialCode.generateNext(withYear);
        assertEquals(String.format("MAT-%d-0002", currentYear), next.getValue());
    }

    @Test
    @DisplayName("OrderCode supports year-based formats, increment, and legacy format")
    void testOrderCodeValidationAndParsing() {
        int currentYear = Year.now().getValue();

        OrderCode code = OrderCode.of("PO-" + currentYear + "-0001");
        assertEquals("PO", code.getPrefix());
        assertEquals("0001", code.getNumber());
        assertEquals(currentYear, code.getYear());

        OrderCode next = code.increment();
        assertEquals(String.format("PO-%d-0002", currentYear), next.getValue());

        OrderCode legacy = OrderCode.of("PO-0099");
        assertEquals("PO", legacy.getPrefix());
        assertEquals(99, legacy.getNumberAsInt());
        assertEquals(-1, legacy.getYear());
    }

    @Test
    @DisplayName("RequisitionCode supports REQ/PR with year and legacy")
    void testRequisitionCodeValidation() {
        int currentYear = Year.now().getValue();

        RequisitionCode req = RequisitionCode.of("REQ-" + currentYear + "-0001");
        assertEquals("REQ", req.getPrefix());
        assertEquals(currentYear, req.getYear());

        RequisitionCode pr = RequisitionCode.of("PR-" + currentYear + "-0002");
        assertEquals("PR", pr.getPrefix());
        assertEquals(currentYear, pr.getYear());

        RequisitionCode legacy = RequisitionCode.of("REQ-0001");
        assertEquals(-1, legacy.getYear());
    }

    @Test
    @DisplayName("ReceiptCode, InvoiceCode, PaymentCode, ReturnCode validate year formats")
    void testOtherCodes() {
        int currentYear = Year.now().getValue();

        ReceiptCode gr = ReceiptCode.of("GR-" + currentYear + "-0001");
        assertEquals("GR", gr.getPrefix());
        assertEquals(currentYear, gr.getYear());

        InvoiceCode inv = InvoiceCode.of("INV-" + currentYear + "-0001");
        assertEquals("INV", inv.getPrefix());
        assertEquals(currentYear, inv.getYear());

        PaymentCode pay = PaymentCode.of("PAY-" + currentYear + "-0001");
        assertEquals("PAY", pay.getPrefix());
        assertEquals(currentYear, pay.getYear());

        ReturnCode rtn = ReturnCode.of("RTN-" + currentYear + "-0001");
        assertEquals("RTN", rtn.getPrefix());
        assertEquals(currentYear, rtn.getYear());

        ReturnCode rtv = ReturnCode.of("RTV-" + currentYear + "-0001");
        assertEquals("RTV", rtv.getPrefix());
        assertEquals(currentYear, rtv.getYear());
    }

    @Test
    @DisplayName("Code generators generate codes with current year and sequence")
    void testCodeGeneratorsWithSequence() {
        InMemoryCodeSequenceRepository repo = new InMemoryCodeSequenceRepository();
        int currentYear = Year.now().getValue();

        MaterialCodeGeneratorService matService = new MaterialCodeGeneratorService(repo);
        MaterialCode mat1 = matService.generateCode(MaterialType.RAW_MATERIAL);
        assertEquals(String.format("RMT-%d-0001", currentYear), mat1.getValue());
        MaterialCode mat2 = matService.generateCode(MaterialType.RAW_MATERIAL);
        assertEquals(String.format("RMT-%d-0002", currentYear), mat2.getValue());

        PurchaseOrderCodeGeneratorService poService = new PurchaseOrderCodeGeneratorService(repo);
        OrderCode po1 = poService.generateCode();
        assertEquals(String.format("PO-%d-0001", currentYear), po1.getValue());
        OrderCode po2 = poService.generateCode();
        assertEquals(String.format("PO-%d-0002", currentYear), po2.getValue());

        RequisitionCodeGeneratorService reqService = new RequisitionCodeGeneratorService(repo);
        RequisitionCode req1 = reqService.generateCode();
        assertEquals(String.format("REQ-%d-0001", currentYear), req1.getValue());

        GoodsReceiptCodeGeneratorService grService = new GoodsReceiptCodeGeneratorService(repo);
        ReceiptCode gr1 = grService.generateCode();
        assertEquals(String.format("GR-%d-0001", currentYear), gr1.getValue());

        InvoiceCodeGeneratorService invService = new InvoiceCodeGeneratorService(repo);
        InvoiceCode inv1 = invService.generateCode();
        assertEquals(String.format("INV-%d-0001", currentYear), inv1.getValue());

        PaymentCodeGeneratorService payService = new PaymentCodeGeneratorService(repo);
        PaymentCode pay1 = payService.generateCode();
        assertEquals(String.format("PAY-%d-0001", currentYear), pay1.getValue());

        ReturnToVendorCodeGeneratorService rtvService = new ReturnToVendorCodeGeneratorService(repo);
        ReturnCode rtv1 = rtvService.generateCode();
        assertEquals(String.format("RTN-%d-0001", currentYear), rtv1.getValue());

        SupplierCodeGeneratorService supService = new SupplierCodeGeneratorService(repo);
        String sup1 = supService.generateCode();
        assertEquals(String.format("SUP-%d-0001", currentYear), sup1);

        CategoryCodeGeneratorService catService = new CategoryCodeGeneratorService(repo);
        String cat1 = catService.generateCode();
        assertEquals(String.format("CAT-%d-0001", currentYear), cat1);
    }
}
