import { Routes, Route } from "react-router-dom";
import ReturnsPage from "./pages/ReturnsPage";
import CreateReturnPage from "./pages/CreateReturnPage";
import ReturnDetailPage from "./pages/ReturnDetailPage";

/** Every procurement role can read and prepare returns (Role.java: return:read / return:write). */
export default function ReturnToVendorRoutes() {
  return (
    <Routes>
      <Route path="" element={<ReturnsPage />} />
      <Route path="create" element={<CreateReturnPage />} />
      <Route path=":id" element={<ReturnDetailPage />} />
    </Routes>
  );
}
