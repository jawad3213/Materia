import { Routes, Route } from "react-router-dom";
import PaymentsPage from "./pages/PaymentsPage";
import CreatePaymentPage from "./pages/CreatePaymentPage";
import PaymentDetailPage from "./pages/PaymentDetailPage";
import RoleGuard from "../../app/routes/RoleGuard";

export default function PaymentsRoutes() {
  return (
    <Routes>
      <Route path="" element={<PaymentsPage />} />
      <Route
        path="create"
        element={
          <RoleGuard allowedRoles={["ADMIN"]}>
            <CreatePaymentPage />
          </RoleGuard>
        }
      />
      <Route path=":id" element={<PaymentDetailPage />} />
    </Routes>
  );
}
