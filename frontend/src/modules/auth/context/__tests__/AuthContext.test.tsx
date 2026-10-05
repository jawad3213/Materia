import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import React from 'react';
import { AuthProvider } from '../AuthContext';
import { AuthContext } from '../authContextValue';
import authService from '../../services/authService';

describe('AuthContext Role & Permission Engine', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <AuthProvider>{children}</AuthProvider>
  );

  it('correctly evaluates permissions for ADMIN role', async () => {
    vi.spyOn(authService, 'getStoredUser').mockReturnValue({
      id: 'admin-1',
      name: 'Admin User',
      email: 'admin@materia.com',
      role: 'ADMIN',
    });

    const { result } = renderHook(() => React.useContext(AuthContext)!, { wrapper });

    expect(result.current.hasRole('ADMIN')).toBe(true);
    expect(result.current.hasRole(['PURCHASER', 'RECEIVER'])).toBe(false);
    expect(result.current.hasPermission('user:write')).toBe(true);
    expect(result.current.hasPermission('material:delete')).toBe(true);
    expect(result.current.hasPermission('supplier:delete')).toBe(true);
    expect(result.current.hasPermission('requisition:validate')).toBe(true);
  });

  it('correctly restricts permissions for PURCHASER role per permission-matrix', async () => {
    vi.spyOn(authService, 'getStoredUser').mockReturnValue({
      id: 'purchaser-1',
      name: 'Purchaser User',
      email: 'purchaser@materia.com',
      role: 'PURCHASER',
    });

    const { result } = renderHook(() => React.useContext(AuthContext)!, { wrapper });

    expect(result.current.hasRole('PURCHASER')).toBe(true);
    expect(result.current.hasPermission('requisition:write')).toBe(true);
    expect(result.current.hasPermission('requisition:validate')).toBe(true);
    expect(result.current.hasPermission('requisition:convert')).toBe(true);
    expect(result.current.hasPermission('supplier:write')).toBe(true);

    // Forbidden for PURCHASER per matrix:
    expect(result.current.hasPermission('user:write')).toBe(false);
    expect(result.current.hasPermission('supplier:delete')).toBe(false);
    expect(result.current.hasPermission('category:delete')).toBe(false);
  });

  it('correctly restricts permissions for RECEIVER role per permission-matrix', async () => {
    vi.spyOn(authService, 'getStoredUser').mockReturnValue({
      id: 'receiver-1',
      name: 'Receiver User',
      email: 'receiver@materia.com',
      role: 'RECEIVER',
    });

    const { result } = renderHook(() => React.useContext(AuthContext)!, { wrapper });

    expect(result.current.hasRole('RECEIVER')).toBe(true);
    expect(result.current.hasPermission('material:stock:read')).toBe(true);
    expect(result.current.hasPermission('material:stock:write')).toBe(true);

    // Forbidden for RECEIVER per matrix:
    expect(result.current.hasPermission('requisition:write')).toBe(false);
    expect(result.current.hasPermission('requisition:validate')).toBe(false);
    expect(result.current.hasPermission('supplier:write')).toBe(false);
    expect(result.current.hasPermission('user:write')).toBe(false);
  });
});
