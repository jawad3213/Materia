import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { HelmetProvider } from "react-helmet-async";
import { MemoryRouter } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import * as useAuthModule from "../../auth/hooks/useAuth";
import AuditLogPage from "../pages/AuditLogPage";
import auditService, { CHANGE_REASON_HEADER, withChangeReason } from "../services/auditService";
import type { AuditEntry } from "../types";
import { recordLink } from "../utils/auditLinks";
import * as download from "../../../shared/utils/download";

const ENTRIES: AuditEntry[] = [
  {
    id: "a1",
    occurredAt: "2026-10-06T09:30:00",
    actorId: "u1",
    actorName: "Nadia Tazi",
    entityType: "Supplier",
    entityId: "s1",
    action: "UPDATE",
    summary: "Changed phone",
    reason: "New number from the supplier's letter",
    changes: { phone: { from: "0522 00 00 00", to: "0522 11 11 11" } },
  },
  {
    id: "a2",
    occurredAt: "2026-10-06T08:00:00",
    actorId: "u2",
    actorName: "Omar Idrissi",
    entityType: "PurchaseOrderLine",
    entityId: "l1",
    parentType: "PurchaseOrder",
    parentId: "po1",
    action: "CREATE",
    summary: "Created MAT-0001",
    changes: {},
  },
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
        <AuditLogPage />
      </MemoryRouter>
    </HelmetProvider>
  );
}

describe("audit log page", () => {
  beforeEach(() => {
    vi.spyOn(auditService, "recent").mockResolvedValue({ data: ENTRIES } as never);
  });
  afterEach(() => vi.restoreAllMocks());

  it("rule: shows who changed what and why, each change linked to its record (lines to their document)", async () => {
    signIn(["log:read"]);
    renderPage();

    expect(await screen.findByText("Changed phone")).toHaveAttribute("href", "/suppliers/view/s1");
    expect(screen.getByText("Created MAT-0001")).toHaveAttribute("href", "/purchase-orders/view/po1");
    expect(screen.getByText(/New number from the supplier's letter/)).toBeInTheDocument();
    expect(screen.getAllByText("Nadia Tazi").length).toBeGreaterThan(1); // the change and the user filter
    expect(screen.queryByRole("button", { name: /export csv/i })).not.toBeInTheDocument();
  });

  it("rule: the record type is filtered by the server, the user and the search text on the page", async () => {
    signIn(["log:read"]);
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Changed phone");

    await user.selectOptions(screen.getByLabelText("Record type"), "Supplier");
    await waitFor(() => expect(auditService.recent).toHaveBeenLastCalledWith({ entityType: "Supplier", limit: 100 }));

    await user.selectOptions(screen.getByLabelText("User"), "Omar Idrissi");
    expect(screen.queryByText("Changed phone")).not.toBeInTheDocument();
    expect(screen.getByText("Created MAT-0001")).toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText("User"), "");
    await user.type(screen.getByLabelText("Search"), "letter");
    expect(screen.getByText("Changed phone")).toBeInTheDocument();
    expect(screen.queryByText("Created MAT-0001")).not.toBeInTheDocument();
  });

  it("rule: log:export downloads the feed as CSV", async () => {
    signIn(["log:read", "log:export"]);
    const exportCsv = vi.spyOn(auditService, "exportCsv").mockResolvedValue({
      data: new Blob(["x"]),
      headers: { "content-disposition": 'attachment; filename="audit-log-2026-10-06.csv"' },
    } as never);
    const save = vi.spyOn(download, "saveBlob").mockImplementation(() => {});
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Changed phone");

    await user.click(screen.getByRole("button", { name: /export csv/i }));

    await waitFor(() => expect(save).toHaveBeenCalledWith(expect.any(Blob), "audit-log-2026-10-06.csv"));
    expect(exportCsv).toHaveBeenCalledWith({ entityType: undefined, limit: 5000 });
  });

  it("rule: without log:read the feed is not requested", () => {
    signIn([]);
    renderPage();
    expect(screen.getByText(/needs the log:read permission/)).toBeInTheDocument();
    expect(auditService.recent).not.toHaveBeenCalled();
  });

  it("rule: field changes open on request", async () => {
    signIn(["log:read"]);
    const user = userEvent.setup();
    renderPage();
    const row = (await screen.findByText("Changed phone")).closest("li") as HTMLElement;

    await user.click(within(row).getByRole("button", { name: /show 1 field/i }));
    expect(within(row).getByText("0522 11 11 11")).toBeInTheDocument();
  });
});

describe("audit helpers", () => {
  it("rule: the reason travels URL-encoded in X-Change-Reason, and an empty reason sends no header", () => {
    expect(withChangeReason("  Price à jour  ")).toEqual({ headers: { [CHANGE_REASON_HEADER]: encodeURIComponent("Price à jour") } });
    expect(withChangeReason("   ")).toEqual({});
    expect(withChangeReason(undefined)).toEqual({});
  });

  it("rule: user accounts have no page of their own; settings open the Settings page", () => {
    expect(recordLink({ entityType: "User", entityId: "u1" })).toBeNull();
    expect(recordLink({ entityType: "Setting", entityId: "x" })).toBe("/settings");
    expect(recordLink({ entityType: "Employee", entityId: "e1" })).toBe("/users/view/e1");
  });
});
