import React from "react";
import { Routes, Route } from "react-router-dom";
import RequisitionsPage from "./pages/RequisitionsPage";
import CreateRequisitionPage from "./pages/CreateRequisitionPage";
import RequisitionApprovalsPage from "./pages/RequisitionApprovalsPage";
import RequisitionDetailPage from "./pages/RequisitionDetailPage";
import EditRequisitionPage from "./pages/EditRequisitionPage";

export default function RequisitionsRoutes() {
  return (
    <Routes>
      <Route path="" element={<RequisitionsPage />} />
      <Route path="list" element={<RequisitionsPage />} />
      <Route path="create" element={<CreateRequisitionPage />} />
      <Route path="create-requisition" element={<CreateRequisitionPage />} />
      <Route path="approvals" element={<RequisitionApprovalsPage />} />
      <Route path="view/:id" element={<RequisitionDetailPage />} />
      <Route path="edit/:id" element={<EditRequisitionPage />} />
      <Route path=":id" element={<RequisitionDetailPage />} />
    </Routes>
  );
}
