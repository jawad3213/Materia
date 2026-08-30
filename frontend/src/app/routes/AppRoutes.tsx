import { Routes, Route, Navigate } from 'react-router-dom';
import MaterialsRoutes from '../../modules/materials/MaterialsRoutes';
import CategoriesRoutes from '../../modules/categories/CategoriesRoutes';
import SuppliersRoutes from '../../modules/suppliers/SuppliersRoutes';

export default function AppRoutes() {
  return (
    <Routes>
      {/* Redirect root to the create material page temporarily */}
      <Route path="/" element={<Navigate to="/materials/create-material" replace />} />
      {/* Mount all materials routes under /materials/* */}
      <Route path="/materials/*" element={<MaterialsRoutes />} />
      <Route path="/categories/*" element={<CategoriesRoutes />} />
      <Route path="/suppliers/*" element={<SuppliersRoutes />} />
    </Routes>
  );
}
