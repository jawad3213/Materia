import React from "react";
import { Routes, Route } from "react-router-dom";
import PurchaseOrdersPage from "./pages/PurchaseOrdersPage";
import CreatePurchaseOrderPage from "./pages/CreatePurchaseOrderPage";
import PurchaseOrderDetailPage from "./pages/PurchaseOrderDetailPage";
import EditPurchaseOrderPage from "./pages/EditPurchaseOrderPage";
import RoleGuard from "../../app/routes/RoleGuard";

export default function PurchaseOrdersRoutes() {
  return (
    <Routes>
      <Route path="" element={<PurchaseOrdersPage />} />
      <Route path="list" element={<PurchaseOrdersPage />} />
      <Route
        path="create"
        element={
          <RoleGuard allowedRoles={["ADMIN", "PURCHASER"]}>
            <CreatePurchaseOrderPage />
          </RoleGuard>
        }
      />
      <Route
        path="create-order"
        element={
          <RoleGuard allowedRoles={["ADMIN", "PURCHASER"]}>
            <CreatePurchaseOrderPage />
          </RoleGuard>
        }
      />
      <Route path="view/:id" element={<PurchaseOrderDetailPage />} />
      <Route
        path=":id/edit"
        element={
          <RoleGuard allowedRoles={["ADMIN", "PURCHASER"]}>
            <EditPurchaseOrderPage />
          </RoleGuard>
        }
      />
      <Route path=":id" element={<PurchaseOrderDetailPage />} />
    </Routes>
  );
}
