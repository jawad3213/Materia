import type { CategoryTypeValue } from '../enums/CategoryType';
import type { CategoryStatusValue } from '../enums/CategoryStatus';

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  shortDescription?: string;
  parentId?: string;
  categoryType: CategoryTypeValue;
  status?: CategoryStatusValue;
  createdBy: string;
}
