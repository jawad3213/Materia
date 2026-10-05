package com.materia.backend.contexts.dashboard.infrastructure.adapters.in.web.controllers;

import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;
import com.materia.backend.contexts.dashboard.domain.ports.in.DashboardUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/** The procurement dashboard; each section follows the caller's own permissions (Role.java). */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardUseCase useCase;

    public DashboardController(DashboardUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<DashboardOutput> getOverview(Authentication authentication) {
        Set<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(useCase.getOverview(permissions));
    }
}
