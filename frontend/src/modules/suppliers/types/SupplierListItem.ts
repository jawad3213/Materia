export interface SupplierListItem {
  id: string;
  code?: string;
  name: string;
  contactPerson: string;
  contactEmail: string;
  contactPhone: string;
  city: string;
  country: string;
  currencyCode: string;
  status?: string;
}
