import React from 'react';
import Badge from '../../../shared/components/ui/badge/Badge';
import { EmploymentStatus, type EmploymentStatusValue } from '../enums/EmploymentStatus';

interface UserStatusBadgeProps {
  status: EmploymentStatusValue;
}

export const UserStatusBadge: React.FC<UserStatusBadgeProps> = ({ status }) => {
  switch (status) {
    case EmploymentStatus.ACTIVE:
      return (
        <Badge variant="light" color="success" size="sm">
          Active
        </Badge>
      );
    case EmploymentStatus.PROBATION:
      return (
        <Badge variant="light" color="warning" size="sm">
          Probation
        </Badge>
      );
    case EmploymentStatus.ON_LEAVE:
      return (
        <Badge variant="light" color="info" size="sm">
          On Leave
        </Badge>
      );
    case EmploymentStatus.SUSPENDED:
      return (
        <Badge variant="light" color="error" size="sm">
          Suspended
        </Badge>
      );
    case EmploymentStatus.TERMINATED:
      return (
        <Badge variant="light" color="dark" size="sm">
          Terminated
        </Badge>
      );
    default:
      return (
        <Badge variant="light" color="light" size="sm">
          {status}
        </Badge>
      );
  }
};

export default UserStatusBadge;
