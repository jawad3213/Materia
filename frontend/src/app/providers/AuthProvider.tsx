import React from 'react';
import { AuthProvider as ModuleAuthProvider } from '../../modules/auth/context/AuthContext';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <ModuleAuthProvider>{children}</ModuleAuthProvider>;
};

export default AuthProvider;
