-- Application settings managed from the Settings page (setting:read / setting:write): one row per setting key.
-- A key with no row uses its default (SettingKey). Changes are recorded in the audit log like any other record.
create table if not exists public.app_settings (
    id            uuid          not null primary key,
    setting_key   varchar(100)  not null,
    setting_value varchar(2000),
    created_at    timestamp(6)  not null,
    updated_at    timestamp(6),
    created_by    varchar(100),
    updated_by    varchar(100),
    version       bigint,
    constraint uk_app_settings_key unique (setting_key)
);
