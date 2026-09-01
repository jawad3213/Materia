import { Routes, Route } from 'react-router-dom';
import CreateSupplierPage from './pages/CreateSupplierPage';
import SupplierListPage from './pages/SupplierListPage';
import UpdateSupplierPage from './pages/UpdateSupplierPage';

export default function SuppliersRoutes() {
  return (
    <Routes>
      <Route path="/" element={<SupplierListPage />} />
      <Route path="create-supplier" element={<CreateSupplierPage />} />
      <Route path="edit/:id" element={<UpdateSupplierPage />} />
      {/* Add other supplier routes here later */}
    </Routes>
  );
}
