import type { IconName } from "./landingContent";

/** One stage of the procure-to-pay loop in the hero, with the (illustrative) document it is handling. */
export interface P2PStage {
  key: string;
  label: string;
  icon: IconName;
  /** Short live line for the ticker above the headline. */
  ticker: string;
  code: string;
  document: string;
  title: string;
  detail: string;
  status: string;
  tone: "brand" | "amber" | "sky" | "green";
  amount?: string;
}

/** A single purchase of 50 bolts followed around the loop; the figures are an example, not customer data. */
export const P2P_STAGES: P2PStage[] = [
  {
    key: "request",
    label: "Request",
    icon: "requisition",
    ticker: "REQ-2026-0142 submitted for approval",
    code: "REQ-2026-0142",
    document: "Requisition",
    title: "Steel bolts M8 for line 3",
    detail: "50 pcs · needed by Oct 24",
    status: "Submitted",
    tone: "sky",
    amount: "≈ 250.00 MAD",
  },
  {
    key: "approve",
    label: "Approve",
    icon: "approval",
    ticker: "REQ-2026-0142 approved by another manager",
    code: "REQ-2026-0142",
    document: "Requisition",
    title: "Approved, separation of duties kept",
    detail: "Requester cannot approve their own request",
    status: "Approved",
    tone: "green",
  },
  {
    key: "order",
    label: "Order",
    icon: "order",
    ticker: "PO-2026-0214 confirmed by the supplier",
    code: "PO-2026-0214",
    document: "Purchase order",
    title: "Sent to the supplier, line for line",
    detail: "50 × 5.00 · delivery Oct 22",
    status: "Confirmed",
    tone: "brand",
    amount: "250.00 MAD",
  },
  {
    key: "receive",
    label: "Receive",
    icon: "receipt",
    ticker: "GR-2026-0087: 48 accepted, 2 rejected",
    code: "GR-2026-0087",
    document: "Goods receipt",
    title: "Inspected at the dock",
    detail: "48 accepted into stock · 2 rejected",
    status: "Completed",
    tone: "amber",
  },
  {
    key: "match",
    label: "Match",
    icon: "invoice",
    ticker: "INV-2026-0311 matched to order and receipt",
    code: "INV-2026-0311",
    document: "Supplier invoice",
    title: "Three-way match passed",
    detail: "48 ordered · 48 accepted · 48 invoiced",
    status: "Verified",
    tone: "green",
    amount: "240.00 MAD",
  },
  {
    key: "pay",
    label: "Pay",
    icon: "payment",
    ticker: "PAY-2026-0098 executed by bank transfer",
    code: "PAY-2026-0098",
    document: "Payment",
    title: "Paid for what arrived, nothing more",
    detail: "Bank transfer · reference VIR-55102",
    status: "Paid",
    tone: "green",
    amount: "240.00 MAD",
  },
];
