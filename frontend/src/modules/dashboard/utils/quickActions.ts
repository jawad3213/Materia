export interface QuickAction {
  label: string;
  description: string;
  to: string;
  permission: string;
}

/** Shortcuts to start the most common documents, shown only to users who may create them. */
export const QUICK_ACTIONS: QuickAction[] = [
  { label: "New Requisition", description: "Request materials", to: "/requisitions/create", permission: "requisition:write" },
  { label: "New Purchase Order", description: "Order from a supplier", to: "/purchase-orders/create", permission: "order:write" },
  { label: "Record Receipt", description: "Receive a delivery", to: "/goods-receipts/create", permission: "receipt:write" },
  { label: "Record Invoice", description: "Match a supplier invoice", to: "/invoices/create", permission: "invoice:write" },
  { label: "New Payment", description: "Pay verified invoices", to: "/payments/create", permission: "payment:write" },
  { label: "Return Goods", description: "Send rejected goods back", to: "/returns/create", permission: "return:write" },
];
