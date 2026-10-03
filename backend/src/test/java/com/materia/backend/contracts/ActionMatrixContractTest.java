package com.materia.backend.contracts;

import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceipt;
import com.materia.backend.contexts.goodsReceipt.domain.entities.GoodsReceiptLine;
import com.materia.backend.contexts.goodsReceipt.domain.enums.QualityStatus;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.goodsReceipt.domain.ports.in.GoodsReceiptUseCase;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.controllers.GoodsReceiptController;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.in.web.mappers.GoodsReceiptWebMapper;
import com.materia.backend.contexts.purchaseOrder.domain.entities.PurchaseOrder;
import com.materia.backend.contexts.purchaseOrder.domain.enums.DeliveryStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import com.materia.backend.contexts.purchaseOrder.domain.ports.in.PurchaseOrderUseCase;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.controllers.PurchaseOrderController;
import com.materia.backend.contexts.purchaseOrder.infrastructure.adapters.in.web.mappers.PurchaseOrderWebMapper;
import com.materia.backend.support.AbstractWebMvcTest;
import com.materia.backend.support.ActionMatrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.materia.backend.support.fixtures.GoodsReceiptFixtures.aReceipt;
import static com.materia.backend.support.fixtures.PurchaseOrderFixtures.anOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * [T055] The backend agrees with the shared action matrix in every cell (SC-002a).
 *
 * <p>Two halves, each checked against the matrix file the frontend tests also read:
 * <ul>
 *   <li><b>Who</b>: each role reaches the action's endpoint exactly when the matrix grants the permission.</li>
 *   <li><b>When</b>: the entity accepts the action in exactly the statuses the matrix lists.</li>
 * </ul>
 * Ownership (assigned receiver, own receipt) is enforced in the services and verified by the
 * goods receipt service tests; here it is the permission and status halves that must agree.
 */
@WebMvcTest({PurchaseOrderController.class, GoodsReceiptController.class})
@Import({PurchaseOrderWebMapper.class, GoodsReceiptWebMapper.class})
class ActionMatrixContractTest extends AbstractWebMvcTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String PO = "/api/v1/purchase-orders";
    private static final String GR = "/api/v1/goods-receipts";

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private PurchaseOrderUseCase orders;

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private GoodsReceiptUseCase receipts;

    /** How to reach each matrix action over HTTP. */
    private record Call(HttpMethod method, String path, String body) { }

    private static final Map<String, Call> ORDER_CALLS = Map.of(
            "edit", new Call(HttpMethod.PUT, PO + "/" + ID, "{\"notes\":\"x\"}"),
            "delete", new Call(HttpMethod.DELETE, PO + "/" + ID, null),
            "submit", new Call(HttpMethod.PATCH, PO + "/" + ID + "/submit", null),
            "confirm", new Call(HttpMethod.PATCH, PO + "/" + ID + "/confirm", null),
            "reject", new Call(HttpMethod.PATCH, PO + "/" + ID + "/reject", "{\"reason\":\"x\"}"),
            "assignReceiver", new Call(HttpMethod.PATCH, PO + "/" + ID + "/assign-receiver", "{\"assignedUserId\":\"r\"}"),
            "trackDelivery", new Call(HttpMethod.PATCH, PO + "/" + ID + "/delivery-status", "{\"deliveryStatus\":\"SHIPPED\"}"),
            "closeShort", new Call(HttpMethod.PATCH, PO + "/" + ID + "/complete", null),
            "cancel", new Call(HttpMethod.PATCH, PO + "/" + ID + "/cancel", "{\"reason\":\"x\"}")
    );

    private static final Map<String, Call> RECEIPT_CALLS = Map.of(
            "record", new Call(HttpMethod.POST, GR,
                    "{\"purchaseOrderId\":\"po\",\"lines\":[{\"materialCode\":\"M\",\"quantityReceived\":1,\"quantityRejected\":0}]}"),
            "editLines", new Call(HttpMethod.PATCH, GR + "/" + ID + "/lines",
                    "{\"line\":{\"materialCode\":\"M\",\"quantityReceived\":1,\"quantityRejected\":0}}"),
            "complete", new Call(HttpMethod.PATCH, GR + "/" + ID + "/complete", "{}"),
            "cancel", new Call(HttpMethod.PATCH, GR + "/" + ID + "/cancel", "{\"reason\":\"x\"}"),
            "delete", new Call(HttpMethod.DELETE, GR + "/" + ID, null)
    );

    /** How each matrix action is applied to an entity, to test the status half. */
    private static final Map<String, Consumer<PurchaseOrder>> ORDER_RULES = Map.of(
            "edit", o -> requireModifiable(o.isModifiable()),
            "delete", o -> requireModifiable(o.isModifiable()),
            "submit", o -> o.submit("u"),
            "confirm", o -> o.confirm("u"),
            "reject", o -> o.reject("u", "x"),
            "assignReceiver", o -> o.assignReceiver("u", "U", "r", "R"),
            "trackDelivery", o -> o.updateDeliveryStatus(DeliveryStatus.IN_TRANSIT, "u"),
            "receive", o -> o.recordReceipt(false, "u"),
            "closeShort", o -> o.complete("u"),
            "cancel", o -> o.cancel("u", "x")
    );

    private static final Map<String, Consumer<GoodsReceipt>> RECEIPT_RULES = Map.of(
            "editLines", r -> r.addLine(GoodsReceiptLine.builder().materialCode("M").quantityOrdered(1)
                    .quantityReceived(1).quantityRejected(0).qualityStatus(QualityStatus.ACCEPTED).build()),
            "complete", r -> r.complete("u"),
            "cancel", r -> r.cancel("u", "x"),
            "delete", r -> requireModifiable(r.getStatus().isModifiable())
    );

    private static void requireModifiable(boolean modifiable) {
        if (!modifiable) {
            throw new IllegalStateException("not modifiable");
        }
    }

    // ---- Who ----

    static Stream<Arguments> permissionCells() {
        List<Arguments> cells = new ArrayList<>();
        for (String role : ActionMatrix.roles()) {
            ORDER_CALLS.keySet().forEach(a -> cells.add(Arguments.of(ActionMatrix.PURCHASE_ORDER, a, role)));
            RECEIPT_CALLS.keySet().forEach(a -> cells.add(Arguments.of(ActionMatrix.GOODS_RECEIPT, a, role)));
        }
        return cells.stream();
    }

    @ParameterizedTest(name = "{2} may attempt {0}.{1}")
    @MethodSource("permissionCells")
    @DisplayName("who: the API lets a role attempt an action exactly when the matrix grants it (SC-002a)")
    void permission_matchesMatrix(String aggregate, String action, String role) throws Exception {
        Call call = (aggregate.equals(ActionMatrix.PURCHASE_ORDER) ? ORDER_CALLS : RECEIPT_CALLS).get(action);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        ActionMatrix.permissions(role).forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        var builder = request(call.method(), call.path()).with(user("u").authorities(authorities));
        if (call.body() != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(call.body());
        }

        int status = mockMvc.perform(builder).andReturn().getResponse().getStatus();

        assertEquals(ActionMatrix.roleMayAttempt(aggregate, role, action), status != 403,
                () -> role + " " + aggregate + "." + action + " returned " + status);
    }

    // ---- When ----

    static Stream<Arguments> orderStatusCells() {
        return ORDER_RULES.keySet().stream().flatMap(action -> ActionMatrix.statuses(ActionMatrix.PURCHASE_ORDER)
                .stream().map(status -> Arguments.of(action, status)));
    }

    @ParameterizedTest(name = "order.{0} in {1}")
    @MethodSource("orderStatusCells")
    @DisplayName("when: an order accepts an action in exactly the statuses the matrix lists (SC-002a)")
    void orderStatus_matchesMatrix(String action, String status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").inStatus(OrderStatus.valueOf(status)).build();

        assertEquals(ActionMatrix.statusAllows(ActionMatrix.PURCHASE_ORDER, status, action), accepts(() -> ORDER_RULES.get(action).accept(order)),
                () -> "order." + action + " in " + status);
    }

    static Stream<Arguments> receiptStatusCells() {
        return RECEIPT_RULES.keySet().stream().flatMap(action -> ActionMatrix.statuses(ActionMatrix.GOODS_RECEIPT)
                .stream().map(status -> Arguments.of(action, status)));
    }

    @ParameterizedTest(name = "receipt.{0} in {1}")
    @MethodSource("receiptStatusCells")
    @DisplayName("when: a receipt accepts an action in exactly the statuses the matrix lists (SC-002a)")
    void receiptStatus_matchesMatrix(String action, String status) {
        PurchaseOrder order = anOrder().withLine(10, "5.00").withLine(2, "1.00").inStatus(OrderStatus.READY_FOR_RECEIPT).build();
        GoodsReceipt receipt = aReceipt(order).inStatus(ReceiptStatus.valueOf(status)).build();

        assertEquals(ActionMatrix.statusAllows(ActionMatrix.GOODS_RECEIPT, status, action), accepts(() -> RECEIPT_RULES.get(action).accept(receipt)),
                () -> "receipt." + action + " in " + status);
    }

    private static boolean accepts(Runnable action) {
        try {
            action.run();
            return true;
        } catch (RuntimeException refused) {
            return false;
        }
    }
}
