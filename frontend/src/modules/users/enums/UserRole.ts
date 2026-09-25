export const UserRole = {
  ADMIN: 'ADMIN',
  PURCHASER: 'PURCHASER',
  RECEIVER: 'RECEIVER',
} as const;

export type UserRoleValue = (typeof UserRole)[keyof typeof UserRole];
