package com.materia.backend.contexts.settings.application.dtos;

/**
 * 🔹 SETTING OUTPUT
 *
 * @param id           record id of the stored value (what its history refers to), null while the default applies
 * @param value        current value (the default when nothing is stored)
 * @param customized   whether a value is stored rather than the default
 */
public record SettingOutput(java.util.UUID id, String key, String group, String label, String description, String kind, int maxLength,
                            String value, String defaultValue, boolean customized) {
}
