import { Routes, Route } from 'react-router-dom';
import CreateMaterialPage from './pages/CreateMaterialPage';
import MaterialListPage from './pages/MaterialListPage';
import UpdateMaterialPage from './pages/UpdateMaterialPage';
import MaterialCardViewPage from './pages/MaterialCardViewPage';

export default function MaterialsRoutes() {
  return (
    <Routes>
      <Route path="" element={<MaterialListPage />} />
      <Route path="create-material" element={<CreateMaterialPage />} />
      <Route path="edit/:id" element={<UpdateMaterialPage />} />
      <Route path="cards" element={<MaterialCardViewPage />} />
    </Routes>
  );
}
