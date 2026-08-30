import type { CategoryTypeValue } from '../enums/CategoryType';
import type { CategoryStatusValue } from '../enums/CategoryStatus';

export interface Category {
  id: string;
  code: string;
  name: string;
  description: string;
  shortDescription: string;
  parentId: string;
  parentCode: string;
  level: number;
  path: string;
  childrenIds: string[];
  categoryType: CategoryTypeValue;
  status: CategoryStatusValue;
  materialCount: number;
  subCategoryCount: number;
  totalItems: number;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
}
