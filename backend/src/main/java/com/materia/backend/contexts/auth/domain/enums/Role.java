package com.materia.backend.contexts.auth.domain.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Role Enum - Version MVP (3 rôles)
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public enum Role {
    
    ADMIN("ADMIN", "Administrateur", "Accès complet au système", 
          new HashSet<>(Arrays.asList(
              // ---- UTILISATEURS ----
              "user:read", "user:write", "user:delete",
              // ---- MATÉRIAUX ----
              "material:read", "material:write", "material:delete",
              "material:stock:read", "material:stock:write",
              // ---- FOURNISSEURS ----
              "supplier:read", "supplier:write", "supplier:delete",
              // ---- CATÉGORIES ----
              "category:read", "category:write", "category:delete",
              // ---- DEMANDES ----
              "requisition:read", "requisition:write", 
              "requisition:validate", "requisition:convert",
              // ---- COMMANDES ----
              "order:read", "order:write", "order:validate", "order:cancel",
              // ---- RÉCEPTIONS ----
              "receipt:read", "receipt:write", "receipt:quality",
              // ---- RETOURS ----
              "return:read", "return:write",
              // ---- FACTURES ----
              "invoice:read", "invoice:write", "invoice:validate",
              // ---- PAIEMENTS ----
              "payment:read", "payment:write",
              // ---- DASHBOARD ----
              "dashboard:read", "dashboard:export",
              // ---- CONFIGURATION ----
              "setting:read", "setting:write",
              // ---- LOGS ----
              "log:read", "log:export"
          ))),
    
    PURCHASER("PURCHASER", "Acheteur", "Gestion des achats", 
              new HashSet<>(Arrays.asList(
                  // ---- MATÉRIAUX ----
                  "material:read", "material:write-price",
                  // ---- FOURNISSEURS ----
                  "supplier:read", "supplier:write",
                  // ---- CATÉGORIES ----
                  "category:read", "category:write",
                  // ---- DEMANDES ----
                  "requisition:read", "requisition:write",
                  "requisition:validate", "requisition:convert",
                  // ---- COMMANDES ----
                  "order:read", "order:write", "order:cancel",
                  // ---- RÉCEPTIONS ----
                  "receipt:read",
                  // ---- RETOURS ----
                  "return:read", "return:write",
                  // ---- FACTURES ----
                  "invoice:read", "invoice:write",
                  // ---- PAIEMENTS ----
                  "payment:read",
                  // ---- DASHBOARD ----
                  "dashboard:read"
              ))),
    
    RECEIVER("RECEIVER", "Réceptionnaire", "Gestion des stocks et réceptions", 
             new HashSet<>(Arrays.asList(
                 // ---- MATÉRIAUX ----
                 "material:read", "material:stock:read", "material:stock:write",
                 // ---- FOURNISSEURS ----
                 "supplier:read",
                 // ---- CATÉGORIES ----
                 "category:read",
                 // ---- DEMANDES ----
                 "requisition:read",
                 // ---- COMMANDES ----
                 "order:read",
                 // ---- RÉCEPTIONS ----
                 "receipt:read", "receipt:write", "receipt:quality",
                 // ---- RETOURS ----
                 "return:read", "return:write",
                 // ---- DASHBOARD ----
                 "dashboard:read"
             )));
    
    private final String code;
    private final String label;
    private final String description;
    private final Set<String> permissions;
    
    Role(String code, String label, String description, Set<String> permissions) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.permissions = permissions;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
    public Set<String> getPermissions() { return permissions; }
    
    public boolean hasPermission(String permission) {
        return this.permissions.contains(permission);
    }
    
    public static Role fromCode(String code) {
        for (Role role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle inconnu : " + code);
    }
    
    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(Role::getCode)
                .collect(Collectors.toList());
    }
}
