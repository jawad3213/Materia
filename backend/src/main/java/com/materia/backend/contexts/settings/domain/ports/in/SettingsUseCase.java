package com.materia.backend.contexts.settings.domain.ports.in;

import com.materia.backend.contexts.settings.application.dtos.SettingOutput;

import java.util.List;
import java.util.Map;

/**
 * 🔹 SETTINGS USE CASE (INPUT PORT)
 */
public interface SettingsUseCase {

    /** Every setting with its current value (or default), in display order. */
    List<SettingOutput> getAll();

    /** Validates and stores the given values (key → value); unknown keys and invalid values are refused. */
    List<SettingOutput> update(Map<String, String> values, String actorId);

    /** The value stored for a setting key; empty when none is stored (the configured default then applies). */
    java.util.Optional<String> storedValue(String key);
}
