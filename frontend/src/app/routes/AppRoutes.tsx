import { Routes, Route, Navigate } from 'react-router-dom';
import MaterialsRoutes from '../../modules/materials/MaterialsRoutes';
import CategoriesRoutes from '../../modules/categories/CategoriesRoutes';
import SuppliersRoutes from '../../modules/suppliers/SuppliersRoutes';
import RequisitionsRoutes from '../../modules/requisitions/RequisitionsRoutes';
import PurchaseOrdersRoutes from '../../modules/purchaseOrders/PurchaseOrdersRoutes';
import GoodsReceiptsRoutes from '../../modules/goodsReceipts/GoodsReceiptsRoutes';
import InvoicesRoutes from '../../modules/invoices/InvoicesRoutes';
import PaymentsRoutes from '../../modules/payments/PaymentsRoutes';
import ReturnToVendorRoutes from '../../modules/returnToVendor/ReturnToVendorRoutes';
import UsersRoutes from '../../modules/users/UsersRoutes';
import DashboardPage from '../../modules/dashboard/pages/DashboardPage';
import ProfilePage from '../../modules/userProfile/pages/ProfilePage';
import AuthRoutes from '../../modules/auth/AuthRoutes';
import LoginPage from '../../modules/auth/pages/LoginPage';
import ForgotPasswordPage from '../../modules/auth/pages/ForgotPasswordPage';
import ResetPasswordPage from '../../modules/auth/pages/ResetPasswordPage';
import NotFoundPage from '../../shared/pages/NotFoundPage';
import AppLayout from '../../shared/layout/AppLayout';
import PublicLayout from '../../modules/landing/layout/PublicLayout';
import HomePage from '../../modules/landing/pages/HomePage';
import FeaturesPage from '../../modules/landing/pages/FeaturesPage';
import SecurityPage from '../../modules/landing/pages/SecurityPage';
import PrivateRoute from './PrivateRoute';
import PublicRoute from './PublicRoute';
import RoleGuard from './RoleGuard';

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public marketing pages, open to everyone; the header offers sign-in or the dashboard */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/features" element={<FeaturesPage />} />
        <Route path="/security" element={<SecurityPage />} />
      </Route>

      {/* Public Auth Routes */}
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />
      <Route
        path="/signin"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />
      <Route
        path="/register"
        element={<Navigate to="/login" replace />}
      />
      <Route
        path="/signup"
        element={<Navigate to="/login" replace />}
      />
      <Route
        path="/forgot-password"
        element={
          <PublicRoute>
            <ForgotPasswordPage />
          </PublicRoute>
        }
      />
      <Route
        path="/reset-password"
        element={
          <PublicRoute>
            <ResetPasswordPage />
          </PublicRoute>
        }
      />
      {/* Auth nested submodule routes */}
      <Route
        path="/auth/*"
        element={
          <PublicRoute>
            <AuthRoutes />
          </PublicRoute>
        }
      />

      {/* Protected Application Routes sharing a SINGLE persistent AppLayout */}
      <Route
        element={
          <PrivateRoute>
            <AppLayout />
          </PrivateRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        
        {/* Materials: Available to all authenticated roles */}
        <Route path="/materials/*" element={<MaterialsRoutes />} />
        
        {/* Categories: Restricted to Admin and Purchaser */}
        <Route
          path="/categories/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER']}>
              <CategoriesRoutes />
            </RoleGuard>
          }
        />
        
        {/* Suppliers: Restricted to Admin and Purchaser */}
        <Route
          path="/suppliers/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER']}>
              <SuppliersRoutes />
            </RoleGuard>
          }
        />
        
        {/* Requisitions: Restricted to Admin and Purchaser */}
        <Route
          path="/requisitions/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER']}>
              <RequisitionsRoutes />
            </RoleGuard>
          }
        />
        <Route
          path="/purchase-requisitions/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER']}>
              <RequisitionsRoutes />
            </RoleGuard>
          }
        />
        <Route
          path="/approvals"
          element={
            <RoleGuard allowedRoles={['ADMIN']} fallbackPath="/requisitions">
              <Navigate to="/requisitions/approvals" replace />
            </RoleGuard>
          }
        />
        <Route
          path="/approvals/*"
          element={
            <RoleGuard allowedRoles={['ADMIN']} fallbackPath="/requisitions">
              <Navigate to="/requisitions/approvals" replace />
            </RoleGuard>
          }
        />
        
        {/* Purchase Orders: receivers can view the orders they receive; creation and editing are guarded inside */}
        <Route
          path="/purchase-orders/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER', 'RECEIVER']}>
              <PurchaseOrdersRoutes />
            </RoleGuard>
          }
        />

        {/* Goods Receipts: purchasers can consult, receivers record */}
        <Route
          path="/goods-receipts/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER', 'RECEIVER']}>
              <GoodsReceiptsRoutes />
            </RoleGuard>
          }
        />

        {/* Vendor returns: every procurement role sends rejected goods back (return:read / return:write) */}
        <Route
          path="/returns/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER', 'RECEIVER']}>
              <ReturnToVendorRoutes />
            </RoleGuard>
          }
        />

        {/* Invoices: purchasers record and submit, administrators verify and pay */}
        <Route
          path="/invoices/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER']}>
              <InvoicesRoutes />
            </RoleGuard>
          }
        />

        {/* Payments: administrators pay verified invoices, purchasers consult */}
        <Route
          path="/payments/*"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'PURCHASER']}>
              <PaymentsRoutes />
            </RoleGuard>
          }
        />
        
        {/* Users & Staff Management: STRICTLY ADMIN ONLY */}
        <Route
          path="/users/*"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <UsersRoutes />
            </RoleGuard>
          }
        />
        <Route
          path="/employees/*"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <UsersRoutes />
            </RoleGuard>
          }
        />
        <Route
          path="/admin/users/*"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <UsersRoutes />
            </RoleGuard>
          }
        />
      </Route>

      {/* 404 Error Page */}
      <Route path="/404" element={<NotFoundPage />} />

      {/* Catch-all fallback */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
