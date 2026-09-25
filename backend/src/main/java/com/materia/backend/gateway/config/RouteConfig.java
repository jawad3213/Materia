package com.materia.backend.gateway.config;

import com.materia.backend.contexts.auth.domain.enums.Role;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🔹 ROUTE CONFIGURATION
 * 
 * Définit la cartographie des routes API et leur association
 * vers les différents modules métiers (Bounded Contexts) de Materia.
 */
@Configuration
public class RouteConfig {

    /**
     * Route metadata. {@code allowedRoles} documents the intended audience of a route and is
     * descriptive only — enforcement happens through method security on the controllers.
     * An empty list means every authenticated user may call the route.
     */
    public record RouteDefinition(
            String moduleName,
            String pathPattern,
            String description,
            boolean requiresAuth,
            List<String> allowedRoles
    ) {}

    /**
     * Builds the authority names for the given roles, in the exact "ROLE_&lt;CODE&gt;" form that
     * AuthService puts into the JWT. Derived from the Role enum so the two can never drift apart.
     */
    private static List<String> authorities(Role... roles) {
        return Arrays.stream(roles)
                .map(role -> "ROLE_" + role.getCode())
                .collect(Collectors.toUnmodifiableList());
    }

    private static final Map<String, RouteDefinition> ROUTES;

    static {
        Map<String, RouteDefinition> map = new LinkedHashMap<>();

        // 1. Auth Module
        map.put("auth", new RouteDefinition(
                "auth-service",
                "/api/v1/auth/**",
                "Authentication, registration and token management",
                false,
                Collections.emptyList()
        ));

        // 2. Master Data Module
        map.put("masterdata", new RouteDefinition(
                "masterData",
                "/api/v1/masterdata/**",
                "Master data, suppliers, currencies, and reference entities",
                true,
                authorities(Role.ADMIN, Role.PURCHASER, Role.RECEIVER)
        ));

        // 3. Purchase Requisition Module
        map.put("purchaseRequisition", new RouteDefinition(
                "purchaseRequisition",
                "/api/v1/purchase-requisitions/**",
                "Internal purchase requisitions and approvals",
                true,
                authorities(Role.ADMIN, Role.PURCHASER, Role.RECEIVER)
        ));

        // 4. Purchase Order Module
        map.put("purchaseOrder", new RouteDefinition(
                "purchaseOrder",
                "/api/v1/purchase-orders/**",
                "Purchase orders management and lifecycle",
                true,
                authorities(Role.ADMIN, Role.PURCHASER, Role.RECEIVER)
        ));

        // 5. Goods Receipt Module
        map.put("goodsReceipt", new RouteDefinition(
                "goodsReceipt",
                "/api/v1/goods-receipts/**",
                "Warehouse receipts and inventory inspection",
                true,
                authorities(Role.ADMIN, Role.PURCHASER, Role.RECEIVER)
        ));

        // 6. Invoice Module
        map.put("invoice", new RouteDefinition(
                "invoice",
                "/api/v1/invoices/**",
                "Supplier invoices, matching, and validations",
                true,
                authorities(Role.ADMIN, Role.PURCHASER)
        ));

        // 7. Payment Module
        map.put("payment", new RouteDefinition(
                "payment",
                "/api/v1/payments/**",
                "Payment processing and reconciliation",
                true,
                authorities(Role.ADMIN, Role.PURCHASER)
        ));

        // 8. Return To Vendor Module
        map.put("returnToVendor", new RouteDefinition(
                "returnToVendor",
                "/api/v1/return-to-vendors/**",
                "Merchandise returns and vendor claims",
                true,
                authorities(Role.ADMIN, Role.PURCHASER, Role.RECEIVER)
        ));

        // 9. Analytics Service
        map.put("analytics", new RouteDefinition(
                "analytics-service",
                "/api/v1/analytics/**",
                "Business metrics, KPI dashboards, and reporting",
                true,
                authorities(Role.ADMIN, Role.PURCHASER, Role.RECEIVER)
        ));

        // 10. Notification Service
        map.put("notification", new RouteDefinition(
                "notification-service",
                "/api/v1/notifications/**",
                "User alerts, system notifications, and messaging",
                true,
                Collections.emptyList()
        ));

        // 11. Employee Service
        map.put("employee", new RouteDefinition(
                "employee-service",
                "/api/v1/employees/**",
                "Employee records, organizational structure, onboarding, and offboarding",
                true,
                authorities(Role.ADMIN)
        ));

        ROUTES = Collections.unmodifiableMap(map);
    }

    public Map<String, RouteDefinition> getRoutes() {
        return ROUTES;
    }

    public boolean isPublicRoute(String uri) {
        if (uri == null) return false;
        return uri.startsWith("/api/v1/auth/")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/actuator")
                || uri.equals("/error");
    }
}
