export interface CreateSupplierRequest {
  name: string;
  description?: string;
  contactPerson: string;
  contactEmail: string;
  contactPhone: string;
  address: string;
  city: string;
  country: string;
  postalCode?: string;
  paymentTerms: string[];
  paymentDelay?: number;
  currencyCode: string;
  createdBy: string;
}
