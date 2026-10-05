/**
 * Dashboard types matching backend DashboardOutput.java. Money figures are in `currency`; documents in other
 * currencies are counted in `otherCurrencyDocuments` and left out of totals. Sections the user may not read are
 * null or empty.
 */
export type KpiTone = "brand" | "amber" | "orange" | "red" | "green" | "blue";

export interface DashboardKpi {
  key: string;
  label: string;
  value: number;
  /** A currency code for amounts, otherwise a noun ("orders", "materials"). */
  unit: string;
  hint?: string | null;
  link?: string | null;
  tone: KpiTone;
}

export interface MonthlyTrend {
  months: string[];
  ordered: number[];
  invoiced: number[];
  paid: number[];
}

export interface DashboardSlice {
  key: string;
  label: string;
  count: number;
  amount?: number | null;
}

export interface SupplierStats {
  supplierId: string;
  supplierName: string;
  spend: number;
  orders: number;
  acceptanceRate?: number | null;
  onTimeRate?: number | null;
}

export interface ReceiptQuality {
  receipts: number;
  unitsReceived: number;
  unitsAccepted: number;
  unitsRejected: number;
  acceptanceRate?: number | null;
  onTimeReceipts: number;
  onTimeRate?: number | null;
}

export interface AgingBucket {
  key: string;
  label: string;
  invoices: number;
  amount: number;
}

export interface StockAlert {
  materialId: string;
  code: string;
  name: string;
  status: "OUT_OF_STOCK" | "CRITICAL" | "REORDER_NEEDED";
  currentStock: number;
  reorderPoint?: number | null;
  safetyStock?: number | null;
  stockOnOrder: number;
  unit?: string | null;
}

export type ActivityType = "REQUISITION" | "PURCHASE_ORDER" | "GOODS_RECEIPT" | "INVOICE" | "PAYMENT" | "RETURN";

export interface DashboardActivity {
  type: ActivityType;
  code: string;
  title?: string | null;
  status?: string | null;
  date: string;
  link: string;
}

export interface DashboardData {
  generatedAt: string;
  currency: string;
  otherCurrencyDocuments: number;
  kpis: DashboardKpi[];
  monthlyTrend?: MonthlyTrend | null;
  orderStatus: DashboardSlice[];
  spendByCategory: DashboardSlice[];
  topSuppliers: SupplierStats[];
  receiptQuality?: ReceiptQuality | null;
  invoiceAging: AgingBucket[];
  stockAlerts: StockAlert[];
  recentActivity: DashboardActivity[];
}
