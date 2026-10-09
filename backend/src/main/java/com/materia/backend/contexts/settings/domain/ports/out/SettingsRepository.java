package com.materia.backend.contexts.settings.domain.ports.out;

import java.util.Map;
import java.util.Optional;

/**
 * 🔹 SETTINGS REPOSITORY (OUTPUT PORT)
 *
 * Stored setting values by key; a key without a row uses its default.
 */
public interface SettingsRepository {

    Map<String, String> findAll();

    Optional<String> find(String key);

    /** Record id of each stored setting, which its history entries refer to. */
    Map<String, java.util.UUID> idsByKey();

    /** Creates or replaces the value of a key. */
    void save(String key, String value, String actorId);
}
