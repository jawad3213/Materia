package com.materia.backend.contexts.settings.infrastructure.adapters.out.persistence;

import com.materia.backend.common.audit.application.Audited;
import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** One stored setting value. */
@Entity
@Table(name = "app_settings", uniqueConstraints = @UniqueConstraint(name = "uk_app_settings_key", columnNames = "setting_key"))
@Audited(type = "Setting", code = "settingKey", readPermission = "setting:read")
public class AppSettingJpaEntity extends BaseJpaEntity {

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_value", length = 2000)
    private String settingValue;

    protected AppSettingJpaEntity() {
    }

    public AppSettingJpaEntity(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public String getSettingKey() { return settingKey; }
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
}
