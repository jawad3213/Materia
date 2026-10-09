import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { HelmetProvider } from "react-helmet-async";
import { MemoryRouter } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as useAuthModule from "../../auth/hooks/useAuth";
import axiosClient from "../../../shared/api/axiosClient";
import auditService, { CHANGE_REASON_HEADER } from "../../audit/services/auditService";
import SettingsPage from "../pages/SettingsPage";
import type { AppSetting } from "../types";

const SETTINGS: AppSetting[] = [
  { id: "r1", key: "company.name", group: "Company", label: "Company name", description: "", kind: "TEXT", maxLength: 200, value: "Atlas", defaultValue: "Materia", customized: true },
  { id: null, key: "invoice.priceTolerancePercent", group: "Invoice matching", label: "Price tolerance (%)", description: "", kind: "PERCENT", maxLength: 10, value: "2", defaultValue: "2", customized: false },
];

function signIn(permissions: string[]) {
  vi.spyOn(useAuthModule, "default").mockReturnValue({
    hasPermission: (p: string) => permissions.includes(p),
  } as unknown as ReturnType<typeof useAuthModule.default>);
}

function renderPage() {
  return render(
    <HelmetProvider>
      <MemoryRouter>
        <SettingsPage />
      </MemoryRouter>
    </HelmetProvider>
  );
}

describe("settings page", () => {
  beforeEach(() => {
    vi.spyOn(axiosClient, "get").mockResolvedValue({ data: SETTINGS } as never);
    vi.spyOn(auditService, "recent").mockResolvedValue({
      data: [{ id: "h1", occurredAt: "2026-10-06T09:00:00", actorId: "u1", actorName: "Nadia Tazi", entityType: "Setting",
        entityId: "r1", action: "UPDATE", summary: "Changed settingValue", reason: "Rebranding",
        changes: { settingValue: { from: "Materia", to: "Atlas" } } }],
    } as never);
  });
  afterEach(() => vi.restoreAllMocks());

  it("rule: only changed values are saved, with the reason sent as X-Change-Reason", async () => {
    signIn(["setting:read", "setting:write"]);
    const put = vi.spyOn(axiosClient, "put").mockResolvedValue({ data: SETTINGS } as never);
    const user = userEvent.setup();
    renderPage();

    const tolerance = await screen.findByLabelText("Price tolerance (%)");
    expect(screen.getByRole("button", { name: /save settings/i })).toBeDisabled();
    await user.clear(tolerance);
    await user.type(tolerance, "3.5");
    await user.type(screen.getByLabelText(/reason for this change/i), "Supplier price drift");
    await user.click(screen.getByRole("button", { name: /save settings/i }));

    await waitFor(() =>
      expect(put).toHaveBeenCalledWith("/settings", { "invoice.priceTolerancePercent": "3.5" }, {
        headers: { [CHANGE_REASON_HEADER]: encodeURIComponent("Supplier price drift") },
      })
    );
    expect(await screen.findByText("Settings saved")).toBeInTheDocument();
  });

  it("rule: recent changes name the setting and show before, after and reason", async () => {
    signIn(["setting:read", "log:read"]);
    renderPage();

    expect(await screen.findByText("Rebranding", { exact: false })).toBeInTheDocument();
    expect(screen.getAllByText("Company name").length).toBeGreaterThan(1);
  });

  it("rule: without setting:write the values are read-only and there is no save button", async () => {
    signIn(["setting:read"]);
    renderPage();

    expect(await screen.findByLabelText("Company name")).toBeDisabled();
    expect(screen.queryByRole("button", { name: /save settings/i })).not.toBeInTheDocument();
    expect(auditService.recent).not.toHaveBeenCalled();
  });
});
