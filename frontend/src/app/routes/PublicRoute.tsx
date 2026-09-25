import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../../modules/auth/hooks/useAuth';

interface PublicRouteProps {
  children?: React.ReactNode;
}

export const PublicRoute: React.FC<PublicRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/materials" replace />;
  }

  return <>{children ? children : <Outlet />}</>;
};

export default PublicRoute;
