import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

export default function AuthRoutes() {
  return (
    <Routes>
      <Route index element={<LoginPage />} />
      <Route path="login" element={<LoginPage />} />
      <Route path="signin" element={<LoginPage />} />
      <Route path="register" element={<Navigate to="/auth/login" replace />} />
      <Route path="signup" element={<Navigate to="/auth/login" replace />} />
      <Route path="forgot-password" element={<ForgotPasswordPage />} />
      <Route path="reset-password" element={<ResetPasswordPage />} />
      <Route path="*" element={<Navigate to="login" replace />} />
    </Routes>
  );
}
