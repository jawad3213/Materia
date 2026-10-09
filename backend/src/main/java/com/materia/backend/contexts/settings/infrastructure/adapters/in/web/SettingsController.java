package com.materia.backend.contexts.settings.infrastructure.adapters.in.web;

import com.materia.backend.contexts.settings.application.dtos.SettingOutput;
import com.materia.backend.contexts.settings.domain.ports.in.SettingsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 🔹 SETTINGS REST CONTROLLER
 *
 * Reading needs setting:read, saving needs setting:write. A change is recorded in the audit log, with the reason
 * sent in X-Change-Reason.
 */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsUseCase settings;

    public SettingsController(SettingsUseCase settings) {
        this.settings = settings;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('setting:read')")
    public ResponseEntity<List<SettingOutput>> getAll() {
        return ResponseEntity.ok(settings.getAll());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('setting:write')")
    public ResponseEntity<List<SettingOutput>> update(@RequestBody Map<String, String> values, Authentication authentication) {
        return ResponseEntity.ok(settings.update(values, authentication != null ? authentication.getName() : null));
    }
}
