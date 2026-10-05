/**
 * What the public pages say about Materia. Everything here describes what the product does; there are no invented
 * customers, testimonials or usage figures.
 */

export type IconName =
  | "requisition"
  | "approval"
  | "order"
  | "receipt"
  | "return"
  | "invoice"
  | "payment"
  | "dashboard"
  | "stock"
  | "supplier"
  | "shield"
  | "audit"
  | "currency"
  | "bell";

export interface CycleStep {
  id: string;
  icon: IconName;
  title: string;
  summary: string;
  detail: string[];
}

/** The procurement cycle, in the order Materia runs it. */
export const CYCLE: CycleStep[] = [
  {
    id: "request",
    icon: "requisition",
    title: "Request",
    summary: "Anyone with a need files a requisition: materials, quantities, the date they are needed.",
    detail: ["Lines from the material catalogue", "Estimated cost in the requisition currency", "Drafts until submitted"],
  },
  {
    id: "approve",
    icon: "approval",
    title: "Approve",
    summary: "An approver reviews it. Nobody can approve a requisition they requested themselves.",
    detail: ["Approve with notes or reject with a reason", "Separation of duties enforced by the server", "Approvals portal, oldest first"],
  },
  {
    id: "order",
    icon: "order",
    title: "Order",
    summary: "Approved requisitions become purchase orders, line for line, never above the quantity requested.",
    detail: ["Supplier, terms, incoterm and delivery date", "Stock on order updated as soon as it is sent", "Supplier confirmation or rejection recorded"],
  },
  {
    id: "receive",
    icon: "receipt",
    title: "Receive",
    summary: "The assigned receiver records what arrived and inspects it. Accepted goods enter stock.",
    detail: ["Partial deliveries and discrepancies tracked", "Quality check with rejection reasons", "Stock updated at the moment of receipt"],
  },
  {
    id: "return",
    icon: "return",
    title: "Return",
    summary: "Rejected goods go back to the supplier, then are replaced or credited.",
    detail: ["Only what was rejected can be returned", "A replacement reopens the order to receive it", "A credit note is valued at order prices"],
  },
  {
    id: "invoice",
    icon: "invoice",
    title: "Invoice",
    summary: "Supplier invoices are matched against what was ordered and what was actually accepted.",
    detail: ["Three-way match, line by line", "Discrepancies flagged before verification", "Credit notes supported"],
  },
  {
    id: "pay",
    icon: "payment",
    title: "Pay",
    summary: "Verified invoices are paid in full or in part; open payments reserve their amounts.",
    detail: ["Several invoices of one supplier in one payment", "Bank reference required for transfers and cheques", "Invoices settle automatically when fully paid"],
  },
];

export interface Feature {
  icon: IconName;
  title: string;
  text: string;
}

/** The capabilities shown on the home page. */
export const FEATURES: Feature[] = [
  { icon: "approval", title: "Approvals with real controls", text: "Separation of duties is enforced by the server, not by a checkbox. Requesters never approve their own requests." },
  { icon: "receipt", title: "Receiving with quality control", text: "Receivers record quantities, accept or reject with a reason, and stock follows the goods that were accepted." },
  { icon: "invoice", title: "Three-way invoice matching", text: "Every invoice line is checked against the order and the receipts, so you only pay for what arrived in good condition." },
  { icon: "payment", title: "Partial and grouped payments", text: "Pay several invoices at once or settle one in instalments. Amounts already reserved by open payments are never paid twice." },
  { icon: "return", title: "Vendor returns that close the loop", text: "Send rejected goods back; a replacement reopens the order for receipt, a credit note is valued automatically." },
  { icon: "dashboard", title: "A dashboard that tells the truth", text: "Spend, payables aging, delivery quality and stock alerts, computed from live documents and never mixing currencies." },
  { icon: "stock", title: "Stock that stays honest", text: "Current stock, stock on order and reorder points move with every order and receipt, with reorder recommendations." },
  { icon: "shield", title: "Role-based access", text: "Administrators, purchasers and receivers each see and do exactly what their role allows, on every screen and endpoint." },
];

export interface Role {
  id: "ADMIN" | "PURCHASER" | "RECEIVER";
  title: string;
  tagline: string;
  can: string[];
}

/** What each role does in Materia (Role.java). */
export const ROLES: Role[] = [
  {
    id: "PURCHASER",
    title: "Purchaser",
    tagline: "Turns needs into orders and keeps suppliers in line.",
    can: ["Create and approve requisitions (not their own)", "Create, send and track purchase orders", "Record and submit supplier invoices", "Prepare vendor returns"],
  },
  {
    id: "RECEIVER",
    title: "Receiver",
    tagline: "Owns the dock: what arrives, what is accepted, what goes back.",
    can: ["Receive the orders assigned to them", "Inspect quality and reject with a reason", "Update stock as goods are accepted", "Ship rejected goods back to suppliers"],
  },
  {
    id: "ADMIN",
    title: "Administrator",
    tagline: "Verifies, pays and keeps the whole chain under control.",
    can: ["Verify invoices and execute payments", "Manage staff, roles and master data", "Close orders short and cancel when needed", "See every figure on the dashboard"],
  },
];

export interface ModuleDetail {
  icon: IconName;
  eyebrow: string;
  title: string;
  text: string;
  points: string[];
  /** The lifecycle a document of this module goes through, shown as a sequence of statuses. */
  flow: string[];
}

/** The modules, one section each on the features page. */
export const MODULES: ModuleDetail[] = [
  {
    icon: "supplier",
    eyebrow: "Master data",
    title: "Materials, suppliers and categories",
    text: "A clean catalogue is where good purchasing starts: every material with its unit, prices, stock levels and preferred supplier.",
    points: ["Category tree with unlimited depth", "Minimum, safety and reorder levels per material", "Supplier payment terms and currency"],
    flow: ["Draft", "Active", "Inactive", "Obsolete"],
  },
  {
    icon: "requisition",
    eyebrow: "Requisitions",
    title: "Requests that are easy to file and hard to abuse",
    text: "Requesters build their request from the catalogue; approvers see the justification, the estimated cost and the need-by date.",
    points: ["Draft, submit, approve, reject or cancel", "No self-approval, enforced server-side", "One click from approval to purchase order"],
    flow: ["Draft", "Submitted", "Approved", "Ordered"],
  },
  {
    icon: "order",
    eyebrow: "Purchase orders",
    title: "Orders that stay faithful to what was approved",
    text: "Orders inherit their lines from the requisition and can never order more than was requested. Stock on order follows every change.",
    points: ["Send, confirm, reject, cancel or close short", "Delivery tracking and late-delivery flags", "A named receiver for every order"],
    flow: ["Draft", "Submitted", "Confirmed", "Ready for receipt", "Completed"],
  },
  {
    icon: "receipt",
    eyebrow: "Goods receipts",
    title: "Receiving that knows what to expect",
    text: "Receipts expect exactly what is outstanding on the order. Partial deliveries, discrepancies and rejections are recorded as they happen.",
    points: ["Quality status per line", "Accepted goods enter stock immediately", "Receive everything outstanding in one action"],
    flow: ["Draft", "In progress", "Completed"],
  },
  {
    icon: "return",
    eyebrow: "Vendor returns",
    title: "Rejected goods, resolved",
    text: "A return can only send back what a receipt rejected. When the supplier replaces it, the order reopens; when they credit it, the amount is computed for you.",
    points: ["Draft, ship, resolve or cancel", "Replacement puts the quantity back on order", "Credit note valued at purchase-order prices"],
    flow: ["Draft", "Shipped", "Resolved"],
  },
  {
    icon: "invoice",
    eyebrow: "Invoices",
    title: "Pay for what you received, nothing more",
    text: "Each invoice line is matched against the ordered and accepted quantities and the order price. Discrepancies are visible before anyone verifies.",
    points: ["Standard invoices and credit notes", "Submit, verify, cancel", "Outstanding and overdue amounts at a glance"],
    flow: ["Draft", "Submitted", "Verified", "Paid"],
  },
  {
    icon: "payment",
    eyebrow: "Payments",
    title: "One payment, many invoices, no double payments",
    text: "Payments cover one or more verified invoices of a supplier. Open payments reserve their amounts so the same money is never committed twice.",
    points: ["Partial payments with the remaining balance shown", "Bank reference required for transfers and cheques", "All-or-nothing recording on every invoice"],
    flow: ["Draft", "Prepared", "Paid"],
  },
  {
    icon: "dashboard",
    eyebrow: "Dashboard",
    title: "The whole chain on one screen",
    text: "Headline figures, the spend trend, supplier performance, payables aging and stock alerts, each limited to what the viewer may read.",
    points: ["Ordered, invoiced and paid over twelve months", "Delivery acceptance and on-time rates", "Never adds amounts across currencies"],
    flow: ["Ordered", "Invoiced", "Paid"],
  },
];

export interface Control {
  icon: IconName;
  title: string;
  text: string;
}

/** The controls described on the security page. */
export const CONTROLS: Control[] = [
  { icon: "shield", title: "Permissions checked on the server", text: "Every endpoint checks the caller's permissions. The interface hides what a role cannot do, but the server is what refuses it." },
  { icon: "approval", title: "Separation of duties", text: "The person who requested a purchase cannot approve it. The receiver of an order is named, and only they can receive it." },
  { icon: "audit", title: "Who did what, and when", text: "Documents record who created, approved, verified, received and paid them, taken from the signed-in session rather than from the request." },
  { icon: "currency", title: "No silent currency mixing", text: "Amounts in different currencies are never added together; totals are kept per currency or explicitly limited to one." },
  { icon: "invoice", title: "Rules that protect the money", text: "Orders cannot exceed approved quantities, receipts cannot exceed orders, invoices are matched to receipts, payments cannot exceed what is owed." },
  { icon: "bell", title: "Sessions handled carefully", text: "Short-lived access tokens with silent refresh, and sessions that expire cleanly when a refresh is no longer possible." },
];
