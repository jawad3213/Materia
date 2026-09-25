import { Routes, Route, Navigate } from 'react-router-dom';
import MaterialsRoutes from '../../modules/materials/MaterialsRoutes';
import CategoriesRoutes from '../../modules/categories/CategoriesRoutes';
import SuppliersRoutes from '../../modules/suppliers/SuppliersRoutes';
import RequisitionsRoutes from '../../modules/requisitions/RequisitionsRoutes';
import DashboardPage from '../../modules/dashboard/pages/DashboardPage';
import ProfilePage from '../../modules/userProfile/pages/ProfilePage';
import AuthRoutes from '../../modules/auth/AuthRoutes';
import LoginPage from '../../modules/auth/pages/LoginPage';
import ForgotPasswordPage from '../../modules/auth/pages/ForgotPasswordPage';
import ResetPasswordPage from '../../modules/auth/pages/ResetPasswordPage';
import NotFoundPage from '../../shared/pages/NotFoundPage';
import AppLayout from '../../shared/layout/AppLayout';
import PrivateRoute from './PrivateRoute';
import PublicRoute from './PublicRoute';

export default function AppRoutes() {
  return (
    <Routes>
      {/* Root redirect: Authenticated users will go to /materials, unauthenticated will be caught by PrivateRoute and redirected to /login */}
      <Route
        path="/"
        element={
          <PrivateRoute>
            <Navigate to="/materials" replace />
          </PrivateRoute>
        }
      />

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
        <Route path="/materials/*" element={<MaterialsRoutes />} />
        <Route path="/categories/*" element={<CategoriesRoutes />} />
        <Route path="/suppliers/*" element={<SuppliersRoutes />} />
        <Route path="/requisitions/*" element={<RequisitionsRoutes />} />
        <Route path="/purchase-requisitions/*" element={<RequisitionsRoutes />} />
        <Route path="/approvals" element={<Navigate to="/requisitions/approvals" replace />} />
        <Route path="/approvals/*" element={<Navigate to="/requisitions/approvals" replace />} />
      </Route>

      {/* 404 Error Page */}
      <Route path="/404" element={<NotFoundPage />} />

      {/* Catch-all fallback */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
