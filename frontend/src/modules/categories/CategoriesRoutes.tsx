import { Routes, Route } from 'react-router-dom';
import CreateCategoryPage from './pages/CreateCategoryPage';

export default function CategoriesRoutes() {
  return (
    <Routes>
      <Route path="create-category" element={<CreateCategoryPage />} />
      {/* Other category routes will go here (e.g. view-category, category-list) */}
    </Routes>
  );
}
