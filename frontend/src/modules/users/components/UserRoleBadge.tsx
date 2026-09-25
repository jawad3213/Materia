import React from 'react';
import Badge from '../../../shared/components/ui/badge/Badge';
import { UserRole, type UserRoleValue } from '../enums/UserRole';

interface UserRoleBadgeProps {
  role?: UserRoleValue | string;
  hasAccount?: boolean;
}

export const UserRoleBadge: React.FC<UserRoleBadgeProps> = ({ role, hasAccount }) => {
  if (!role) {
    if (hasAccount) {
      return (
        <Badge variant="light" color="info" size="sm">
          Active Account
        </Badge>
      );
    }
    return (
      <span className="text-xs text-gray-400 italic">No System Login</span>
    );
  }

  switch (role) {
    case UserRole.ADMIN:
      return (
        <Badge variant="solid" color="primary" size="sm">
          Admin
        </Badge>
      );
    case UserRole.PURCHASER:
      return (
        <Badge variant="solid" color="info" size="sm">
          Purchaser
        </Badge>
      );
    case UserRole.RECEIVER:
      return (
        <Badge variant="solid" color="success" size="sm">
          Receiver
        </Badge>
      );
    default:
      return (
        <Badge variant="light" color="light" size="sm">
          {role}
        </Badge>
      );
  }
};

export default UserRoleBadge;
