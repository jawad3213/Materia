/** One application setting (backend SettingOutput). */
export interface AppSetting {
  /** Record id of the stored value (what its history refers to); null while the default applies. */
  id?: string | null;
  key: string;
  group: string;
  label: string;
  description: string;
  kind: "TEXT" | "EMAIL" | "PHONE" | "TAX_ID" | "PERCENT";
  maxLength: number;
  value: string;
  defaultValue?: string | null;
  customized: boolean;
}
