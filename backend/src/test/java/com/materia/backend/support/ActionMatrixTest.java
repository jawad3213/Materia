package com.materia.backend.support;

import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.goodsReceipt.domain.enums.ReceiptStatus;
import com.materia.backend.contexts.purchaseOrder.domain.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** [T015] The shared action matrix is well-formed and matches the Java enums it describes. */
class ActionMatrixTest {

    @Test
    @DisplayName("action matrix: lists exactly the roles defined by Role")
    void roles_matchRoleEnum() {
        Set<String> expected = Arrays.stream(Role.values()).map(Enum::name).collect(Collectors.toSet());

        assertEquals(expected, Set.copyOf(ActionMatrix.roles()));
    }

    @Test
    @DisplayName("action matrix: each role's order and receipt permissions match Role.java")
    void rolePermissions_matchRoleEnum() {
        for (Role role : Role.values()) {
            Set<String> fromEnum = role.getPermissions().stream()
                    .filter(p -> p.startsWith("order:") || p.startsWith("receipt:"))
                    .collect(Collectors.toSet());

            assertEquals(fromEnum, ActionMatrix.permissions(role.name()), "permissions for " + role);
        }
    }

    @Test
    @DisplayName("action matrix: lists exactly the purchase order and goods receipt statuses")
    void statuses_matchEnums() {
        Set<String> orders = Arrays.stream(OrderStatus.values()).map(Enum::name).collect(Collectors.toSet());
        Set<String> receipts = Arrays.stream(ReceiptStatus.values()).map(Enum::name).collect(Collectors.toSet());

        assertEquals(orders, Set.copyOf(ActionMatrix.statuses(ActionMatrix.PURCHASE_ORDER)));
        assertEquals(receipts, Set.copyOf(ActionMatrix.statuses(ActionMatrix.GOODS_RECEIPT)));
    }

    @Test
    @DisplayName("action matrix: every action names a permission and only known statuses")
    void actions_areWellFormed() {
        for (String aggregate : new String[]{ActionMatrix.PURCHASE_ORDER, ActionMatrix.GOODS_RECEIPT}) {
            Set<String> statuses = Set.copyOf(ActionMatrix.statuses(aggregate));
            for (String action : ActionMatrix.actions(aggregate)) {
                var rule = ActionMatrix.action(aggregate, action);
                assertFalse(rule.path("permission").asText().isBlank(), aggregate + "." + action + " has no permission");
                rule.path("statuses").forEach(s ->
                        assertTrue(statuses.contains(s.asText()), aggregate + "." + action + " names unknown status " + s));
            }
        }
    }

    @Test
    @DisplayName("action matrix: ownership rules gate receiving to the assigned receiver")
    void ownership_isRequiredForReceiving() {
        assertTrue(ActionMatrix.isAllowed(ActionMatrix.PURCHASE_ORDER, "RECEIVER", "READY_FOR_RECEIPT", "receive", true));
        assertFalse(ActionMatrix.isAllowed(ActionMatrix.PURCHASE_ORDER, "RECEIVER", "READY_FOR_RECEIPT", "receive", false));
        assertFalse(ActionMatrix.isAllowed(ActionMatrix.PURCHASE_ORDER, "PURCHASER", "READY_FOR_RECEIPT", "receive", true));
    }
}
