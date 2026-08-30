import { Routes, Route } from 'react-router-dom';
import CreateSupplierPage from './pages/CreateSupplierPage';

export default function SuppliersRoutes() {
  return (
    <Routes>
      <Route path="create-supplier" element={<CreateSupplierPage />} />
      {/* Add other supplier routes here later */}
    </Routes>
  );
}
