import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import CurrencyNote from "../components/CurrencyNote";
import type { CurrencyConversion } from "../types/dashboard.types";

const base: CurrencyConversion = {
  reportingCurrency: "MAD",
  fallbackRates: false,
  note: "All amounts in MAD. EUR converted to MAD at the rate of each document's date; documents not yet committed use the latest rate.",
  currencies: [
    { currency: "EUR", documents: 3, valuedAtDocumentDate: 2, latestRate: 10.8, latestRateDate: "2026-10-06", latestRateSource: "LIVE" },
  ],
};

describe("currency note", () => {
  it("rule: explains the conversion and shows the latest rate of each currency", () => {
    render(<CurrencyNote conversion={base} />);

    expect(screen.getByRole("note")).toHaveTextContent("EUR converted to MAD at the rate of each document's date");
    expect(screen.getByText(/1 EUR = 10\.80 MAD/)).toBeInTheDocument();
    expect(screen.getByRole("note").className).not.toMatch(/warning/);
  });

  it("rule: fallback rates and currencies without a rate are flagged as a warning", () => {
    render(
      <CurrencyNote
        conversion={{
          ...base,
          fallbackRates: true,
          note: "All amounts in MAD. The exchange-rate service was unavailable: built-in fallback rates are in use. 2 document(s) in USD left out: no exchange rate available.",
          currencies: [
            { ...base.currencies[0], latestRateSource: "FALLBACK" },
            { currency: "USD", documents: 2, valuedAtDocumentDate: 0, latestRate: null },
          ],
        }}
      />
    );

    expect(screen.getByRole("note").className).toMatch(/warning/);
    expect(screen.getByText(/fallback$/)).toBeInTheDocument();
    expect(screen.getByText(/no rate/)).toBeInTheDocument();
    expect(screen.getByRole("note")).toHaveTextContent("left out");
  });
});
