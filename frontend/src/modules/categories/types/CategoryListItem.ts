import type { CategoryTypeValue } from '../enums/CategoryType';
import type { CategoryStatusValue } from '../enums/CategoryStatus';

export interface CategoryListItem {
  id: string;              
  code: string;            
  name: string;            
  categoryType: CategoryTypeValue;    
  status: CategoryStatusValue;        
  materialCount: number;   
  subCategoryCount: number;
  level: number;           
  path: string;            
  parentCode?: string;     
}
