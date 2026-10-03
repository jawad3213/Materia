package com.materia.backend.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The shared action matrix: who may do what, in which status (spec 002, research R8).
 *
 * <p>Loaded from {@code contracts/purchase-order-actions.json}. The frontend screen-rule tests read
 * the same file, so a rule changed on one side only fails the other (SC-002a).
 */
public final class ActionMatrix {

    public static final String RESOURCE = "/contracts/purchase-order-actions.json";
    public static final String PURCHASE_ORDER = "purchaseOrder";
    public static final String GOODS_RECEIPT = "goodsReceipt";

    private static final JsonNode ROOT = load();

    private ActionMatrix() {
    }

    private static JsonNode load() {
        try (InputStream in = ActionMatrix.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Action matrix not found on the test classpath: " + RESOURCE);
            }
            return new ObjectMapper().readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<String> roles() {
        List<String> roles = new ArrayList<>();
        ROOT.path("roles").fieldNames().forEachRemaining(roles::add);
        return roles;
    }

    public static Set<String> permissions(String role) {
        Set<String> result = new TreeSet<>();
        ROOT.path("roles").path(role).path("permissions").forEach(p -> result.add(p.asText()));
        return result;
    }

    public static List<String> statuses(String aggregate) {
        List<String> result = new ArrayList<>();
        ROOT.path(aggregate).path("statuses").forEach(s -> result.add(s.asText()));
        return result;
    }

    public static List<String> actions(String aggregate) {
        List<String> result = new ArrayList<>();
        ROOT.path(aggregate).path("actions").fieldNames().forEachRemaining(result::add);
        return result;
    }

    public static JsonNode action(String aggregate, String action) {
        JsonNode node = ROOT.path(aggregate).path("actions").path(action);
        if (node.isMissingNode()) {
            throw new IllegalArgumentException("Unknown action " + aggregate + "." + action);
        }
        return node;
    }

    /** Whether the role holds the action's permission (and role requirement), ignoring status. */
    public static boolean roleMayAttempt(String aggregate, String role, String action) {
        JsonNode rule = action(aggregate, action);
        JsonNode roleNode = ROOT.path("roles").path(role);
        boolean admin = roleNode.path("passesEveryPermissionCheck").asBoolean(false);
        boolean hasPermission = admin || permissions(role).contains(rule.path("permission").asText());
        boolean hasRole = !rule.has("requiresRole") || rule.path("requiresRole").asText().equals(role);
        return hasPermission && hasRole;
    }

    /** Whether the action is permitted for an aggregate in this status, ignoring who asks. */
    public static boolean statusAllows(String aggregate, String status, String action) {
        JsonNode statuses = action(aggregate, action).path("statuses");
        if (statuses.isEmpty()) {
            return true;
        }
        for (JsonNode s : statuses) {
            if (s.asText().equals(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The full rule. {@code ownsRecord} means "is the order's assigned receiver" for
     * {@code requiresAssignedReceiver}, and "is the receipt's receiver" for {@code requiresOwnReceipt}.
     */
    public static boolean isAllowed(String aggregate, String role, String status, String action, boolean ownsRecord) {
        JsonNode rule = action(aggregate, action);
        boolean needsOwnership = rule.path("requiresAssignedReceiver").asBoolean(false)
                || rule.path("requiresOwnReceipt").asBoolean(false);
        return roleMayAttempt(aggregate, role, action)
                && statusAllows(aggregate, status, action)
                && (!needsOwnership || ownsRecord);
    }
}
