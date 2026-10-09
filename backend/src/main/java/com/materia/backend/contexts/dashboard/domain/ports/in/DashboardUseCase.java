package com.materia.backend.contexts.dashboard.domain.ports.in;

import com.materia.backend.contexts.dashboard.application.dtos.DashboardOutput;

import java.util.Set;

/** The procurement overview, limited to what the user's permissions let them read. */
public interface DashboardUseCase {

    DashboardOutput getOverview(Set<String> permissions);
}
