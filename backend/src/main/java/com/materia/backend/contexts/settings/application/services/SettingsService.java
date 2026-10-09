package com.materia.backend.contexts.settings.application.services;

import com.materia.backend.contexts.settings.application.dtos.SettingOutput;
import com.materia.backend.contexts.settings.domain.SettingKey;
import com.materia.backend.contexts.settings.domain.ports.in.SettingsUseCase;
import com.materia.backend.contexts.settings.domain.ports.out.SettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 🔹 SETTINGS SERVICE
 *
 * Reads every setting with its stored value or default, and stores validated changes. A value equal to what is
 * already in effect is not written, so the history only shows real changes.
 */
@Service
@Transactional(readOnly = true)
public class SettingsService implements SettingsUseCase {

    private final SettingsRepository repository;
    private final String defaultPriceTolerance;

    public SettingsService(SettingsRepository repository,
                           @Value("${app.invoice.price-tolerance-percent:2}") BigDecimal defaultPriceTolerance) {
        this.repository = repository;
        this.defaultPriceTolerance = defaultPriceTolerance.stripTrailingZeros().toPlainString();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Optional<String> storedValue(String key) {
        return repository.find(key).filter(value -> !value.isBlank());
    }

    @Override
    public List<SettingOutput> getAll() {
        Map<String, String> stored = repository.findAll();
        Map<String, java.util.UUID> ids = repository.idsByKey();
        return Arrays.stream(SettingKey.values()).map(key -> output(key, stored, ids.get(key.key()))).toList();
    }

    @Override
    @Transactional
    public List<SettingOutput> update(Map<String, String> values, String actorId) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("No setting to save");
        }
        // Validate everything first, so one invalid value saves nothing.
        Map<SettingKey, String> accepted = new LinkedHashMap<>();
        values.forEach((key, raw) -> {
            SettingKey setting = SettingKey.of(key).orElseThrow(() -> new IllegalArgumentException("Unknown setting: " + key));
            accepted.put(setting, setting.normalize(raw));
        });
        Map<String, String> stored = repository.findAll();
        accepted.forEach((setting, value) -> {
            if (!Objects.equals(value, effective(setting, stored))) {
                repository.save(setting.key(), value, actorId);
            }
        });
        return getAll();
    }

    private SettingOutput output(SettingKey key, Map<String, String> stored, java.util.UUID id) {
        return new SettingOutput(id, key.key(), key.group(), key.label(), key.description(), key.kind().name(),
                key.maxLength(), effective(key, stored), defaultOf(key), stored.containsKey(key.key()));
    }

    private String effective(SettingKey key, Map<String, String> stored) {
        return stored.containsKey(key.key()) ? stored.get(key.key()) : defaultOf(key);
    }

    private String defaultOf(SettingKey key) {
        return key == SettingKey.INVOICE_PRICE_TOLERANCE ? defaultPriceTolerance : key.defaultValue();
    }
}
