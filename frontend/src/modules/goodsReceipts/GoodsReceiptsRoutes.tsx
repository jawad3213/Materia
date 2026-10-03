import { Routes, Route } from "react-router-dom";
import GoodsReceiptsPage from "./pages/GoodsReceiptsPage";
import CreateGoodsReceiptPage from "./pages/CreateGoodsReceiptPage";
import GoodsReceiptDetailPage from "./pages/GoodsReceiptDetailPage";
import RoleGuard from "../../app/routes/RoleGuard";

export default function GoodsReceiptsRoutes() {
  return (
    <Routes>
      <Route path="" element={<GoodsReceiptsPage />} />
      <Route
        path="create"
        element={
          <RoleGuard allowedRoles={["ADMIN", "RECEIVER"]}>
            <CreateGoodsReceiptPage />
          </RoleGuard>
        }
      />
      <Route path=":id" element={<GoodsReceiptDetailPage />} />
    </Routes>
  );
}
