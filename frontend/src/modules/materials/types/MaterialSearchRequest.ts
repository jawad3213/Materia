export interface MaterialSearchRequest {
  // ---- Search fields ----
  code?: string;
  name?: string;
  description?: string;
  shortDescription?: string;
  searchKeywords?: string;
  alternativeName?: string;

  // ---- Filter fields ----
  categoryId?: string;
  materialType?: string;
  status?: string;
}
