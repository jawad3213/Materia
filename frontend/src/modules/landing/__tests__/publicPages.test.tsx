import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, within, fireEvent } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { HelmetProvider } from "react-helmet-async";
import * as useAuthModule from "../../auth/hooks/useAuth";
import PublicLayout from "../layout/PublicLayout";
import HomePage from "../pages/HomePage";
import FeaturesPage from "../pages/FeaturesPage";
import SecurityPage from "../pages/SecurityPage";
import { CYCLE, MODULES, ROLES } from "../content/landingContent";

// jsdom has no matchMedia, which GSAP reads as soon as it loads; no query matches, so animations stay off.
vi.hoisted(() => {
  window.matchMedia = ((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  })) as unknown as typeof window.matchMedia;
});

// Charts need a real SVG engine; the pages only need them to mount.
vi.mock("react-apexcharts", () => ({ default: () => <div data-testid="chart" /> }));

function visitAs(authenticated: boolean) {
  vi.spyOn(useAuthModule, "default").mockReturnValue({
    user: authenticated ? { id: "u1", name: "Ada", email: "ada@materia.test", role: "ADMIN", permissions: [] } : null,
    isAuthenticated: authenticated,
    isLoading: false,
  } as unknown as ReturnType<typeof useAuthModule.default>);
}

function renderAt(path: string) {
  return render(
    <HelmetProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route element={<PublicLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/features" element={<FeaturesPage />} />
            <Route path="/security" element={<SecurityPage />} />
          </Route>
        </Routes>
      </MemoryRouter>
    </HelmetProvider>
  );
}

describe("public pages", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    window.scrollTo = vi.fn();
  });

  it("rule: the home page presents the product, every stage of the cycle and a sign-in call to action", () => {
    visitAs(false);
    renderAt("/");

    expect(screen.getByRole("heading", { level: 1 })).toHaveTextContent(/first request to the last payment/i);
    CYCLE.forEach((step) => expect(screen.getByRole("heading", { name: step.title })).toBeInTheDocument());
    expect(screen.getAllByRole("link", { name: /sign in/i }).length).toBeGreaterThan(0);
    expect(screen.getByText("Illustrative data")).toBeInTheDocument();
  });

  it("rule: a signed-in visitor is offered the dashboard instead of sign-in", () => {
    visitAs(true);
    renderAt("/");

    expect(screen.getAllByRole("link", { name: /dashboard/i })[0]).toHaveAttribute("href", "/dashboard");
    expect(screen.queryByRole("link", { name: /^sign in$/i })).not.toBeInTheDocument();
  });

  it("rule: the role tabs switch the abilities shown", () => {
    visitAs(false);
    renderAt("/");

    const receiver = ROLES.find((r) => r.id === "RECEIVER")!;
    fireEvent.click(screen.getByRole("tab", { name: receiver.title }));
    expect(screen.getByRole("tab", { name: receiver.title })).toHaveAttribute("aria-selected", "true");
    receiver.can.forEach((ability) => expect(screen.getByText(ability)).toBeInTheDocument());
  });

  it("rule: the features page has a section and an index entry for every module", () => {
    visitAs(false);
    renderAt("/features");

    const index = screen.getByRole("navigation", { name: "Modules" });
    MODULES.forEach((m) => {
      expect(within(index).getByRole("link", { name: m.eyebrow })).toBeInTheDocument();
      expect(screen.getByRole("heading", { name: m.title })).toBeInTheDocument();
    });
  });

  it("rule: the security page shows the permission matrix, with payments reserved to administrators", () => {
    visitAs(false);
    renderAt("/security");

    const row = screen.getByText("Execute payments").closest("tr")!;
    const marks = within(row).getAllByLabelText(/allowed/i);
    expect(marks.map((m) => m.getAttribute("aria-label"))).toEqual(["Allowed", "Not allowed", "Not allowed"]);
  });
});
