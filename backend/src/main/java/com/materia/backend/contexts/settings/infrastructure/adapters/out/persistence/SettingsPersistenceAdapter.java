package com.materia.backend.contexts.settings.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.settings.domain.ports.out.SettingsRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Settings in the app_settings table. */
@Component
public class SettingsPersistenceAdapter implements SettingsRepository {

    private final SpringDataAppSettingRepository repository;

    public SettingsPersistenceAdapter(SpringDataAppSettingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<String, String> findAll() {
        Map<String, String> values = new LinkedHashMap<>();
        repository.findAll().forEach(s -> values.put(s.getSettingKey(), s.getSettingValue() == null ? "" : s.getSettingValue()));
        return values;
    }

    @Override
    public Optional<String> find(String key) {
        return repository.findBySettingKey(key).map(s -> s.getSettingValue() == null ? "" : s.getSettingValue());
    }

    @Override
    public Map<String, java.util.UUID> idsByKey() {
        Map<String, java.util.UUID> ids = new LinkedHashMap<>();
        repository.findAll().forEach(s -> ids.put(s.getSettingKey(), s.getId()));
        return ids;
    }

    @Override
    public void save(String key, String value, String actorId) {
        AppSettingJpaEntity setting = repository.findBySettingKey(key).orElseGet(() -> {
            AppSettingJpaEntity created = new AppSettingJpaEntity(key, value);
            created.setCreatedBy(actorId);
            return created;
        });
        setting.setSettingValue(value);
        setting.setUpdatedBy(actorId);
        repository.save(setting);
    }
}
