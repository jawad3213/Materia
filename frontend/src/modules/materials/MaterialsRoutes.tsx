import { Routes, Route } from 'react-router-dom';
import CreateMaterialPage from './pages/CreateMaterialPage';

export default function MaterialsRoutes() {
  return (
    <Routes>
      <Route path="create-material" element={<CreateMaterialPage />} />
      {/* Add other material routes here later, e.g. path="" for list */}
    </Routes>
  );
}
