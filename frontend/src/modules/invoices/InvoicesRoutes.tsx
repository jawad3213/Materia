import { Routes, Route } from "react-router-dom";
import InvoicesPage from "./pages/InvoicesPage";
import CreateInvoicePage from "./pages/CreateInvoicePage";
import InvoiceDetailPage from "./pages/InvoiceDetailPage";
import RoleGuard from "../../app/routes/RoleGuard";

export default function InvoicesRoutes() {
  return (
    <Routes>
      <Route path="" element={<InvoicesPage />} />
      <Route
        path="create"
        element={
          <RoleGuard allowedRoles={["ADMIN", "PURCHASER"]}>
            <CreateInvoicePage />
          </RoleGuard>
        }
      />
      <Route path=":id" element={<InvoiceDetailPage />} />
    </Routes>
  );
}
