import { Routes, Route } from 'react-router-dom';
import CreateCategoryPage from './pages/CreateCategoryPage';
import CategoriesPage from './pages/CategoriesPage';
import CategoryDetailPage from './pages/CategoryDetailPage';
import EditCategoryPage from './pages/EditCategoryPage';

export default function CategoriesRoutes() {
  return (
    <Routes>
      <Route index element={<CategoriesPage />} />
      <Route path="create-category" element={<CreateCategoryPage />} />
      <Route path="view/:id" element={<CategoryDetailPage />} />
      <Route path="edit/:id" element={<EditCategoryPage />} />
    </Routes>
  );
}
