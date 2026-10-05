import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import DeleteConfirmModal from '../../../shared/components/ui/modal/DeleteConfirmModal';
import UserStatusBadge from './UserStatusBadge';
import UserRoleBadge from './UserRoleBadge';
import OffboardUserModal from './OffboardUserModal';
import { useUsers } from '../hooks/useUsers';
import { EmploymentStatus, type EmploymentStatusValue } from '../enums/EmploymentStatus';
import { UserRole, type UserRoleValue } from '../enums/UserRole';
import type { UserListItem, OffboardUserRequest } from '../types';
import ListCard, { type FilterPill } from '../../../shared/components/page/ListCard';
import FilterPanel, { FilterSelect } from '../../../shared/components/page/FilterPanel';
import StatFilterCards, { type StatCard } from '../../../shared/components/page/StatFilterCards';
import { InitialsAvatar, ListFooter, RowIconButton, StackedCell, TableStateRow, ViewAction } from '../../../shared/components/page/ListParts';
import { FloatingToast } from '../../../shared/components/page/DetailParts';
import { StatIcons } from '../../../shared/components/page/pageIcons';
import { BODY_CELL, HEAD_CELL } from '../../../shared/components/page/pageStyles';
import { useClientPagination } from '../../../shared/components/page/useClientPagination';
import { Table, TableBody, TableCell, TableHeader, TableRow } from '../../../shared/components/ui/table';

type QuickFilter = 'ALL' | 'ACTIVE' | 'PROBATION' | 'TERMINATED';
interface UserFilterValues {
  status: EmploymentStatusValue | '';
  role: UserRoleValue | 'NONE' | '';
}

const QUICK_FILTERS: Record<QuickFilter, (u: UserListItem) => boolean> = {
  ALL: () => true,
  ACTIVE: (u) => u.status === EmploymentStatus.ACTIVE,
  PROBATION: (u) => u.status === EmploymentStatus.PROBATION,
  TERMINATED: (u) => u.status === EmploymentStatus.TERMINATED,
};

const STATUS_LABELS: Record<EmploymentStatusValue, string> = {
  ACTIVE: 'Active',
  PROBATION: 'Probation',
  ON_LEAVE: 'On Leave',
  SUSPENDED: 'Suspended',
  TERMINATED: 'Offboarded',
};

const ROLE_LABELS: Record<UserRoleValue | 'NONE', string> = {
  ADMIN: 'Admin',
  PURCHASER: 'Purchaser',
  RECEIVER: 'Receiver',
  NONE: 'No system login',
};

const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  ...(Object.keys(STATUS_LABELS) as EmploymentStatusValue[]).map((s) => ({ value: s, label: STATUS_LABELS[s] })),
];
const ROLE_OPTIONS = [
  { value: '', label: 'All Roles' },
  ...[...Object.values(UserRole), 'NONE' as const].map((r) => ({ value: r, label: ROLE_LABELS[r] })),
];

const EMPTY_FILTERS: UserFilterValues = { status: '', role: '' };
const COLUMNS = 6;

const formatDate = (value?: string) => (value ? new Date(value).toLocaleDateString() : '—');

const icons = {
  edit: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
    </svg>
  ),
  offboard: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
    </svg>
  ),
  delete: (
    <svg className="size-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
      />
    </svg>
  ),
};

export const UserListTable: React.FC = () => {
  const { users, isLoading, error, offboard, remove } = useUsers();

  const [keyword, setKeyword] = useState('');
  const [quick, setQuick] = useState<QuickFilter>('ALL');
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [draftFilters, setDraftFilters] = useState<UserFilterValues>(EMPTY_FILTERS);
  const [filters, setFilters] = useState<UserFilterValues>(EMPTY_FILTERS);
  const [userToOffboard, setUserToOffboard] = useState<UserListItem | null>(null);
  const [userToDelete, setUserToDelete] = useState<UserListItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [dismissedError, setDismissedError] = useState<string | null>(null);

  const handleOffboard = async (data: OffboardUserRequest) => {
    if (userToOffboard) await offboard(data);
  };

  const handleDelete = async () => {
    if (!userToDelete) return;
    setIsDeleting(true);
    try {
      await remove(userToDelete.id);
      setUserToDelete(null);
    } finally {
      setIsDeleting(false);
    }
  };

  const active = users.filter(QUICK_FILTERS.ACTIVE).length;
  const probation = users.filter(QUICK_FILTERS.PROBATION).length;
  const offboarded = users.filter(QUICK_FILTERS.TERMINATED).length;
  const withLogin = users.filter((u) => !!u.userId).length;
  const cards: StatCard<QuickFilter>[] = [
    { id: 'ALL', title: 'All Staff', value: users.length, unit: 'people', subtitle: `${withLogin} with a system login`, tone: 'brand', icon: StatIcons.all },
    { id: 'ACTIVE', title: 'Active', value: active, unit: 'people', subtitle: 'Currently employed', tone: 'green', icon: StatIcons.done },
    { id: 'PROBATION', title: 'On Probation', value: probation, unit: 'people', subtitle: 'Trial period', tone: 'amber', icon: StatIcons.pending, badge: probation },
    { id: 'TERMINATED', title: 'Offboarded', value: offboarded, unit: 'people', subtitle: 'No longer with the company', tone: 'red', icon: StatIcons.overdue },
  ];

  const visible = useMemo(() => {
    const term = keyword.trim().toLowerCase();
    return users
      .filter(QUICK_FILTERS[quick])
      .filter((u) => !filters.status || u.status === filters.status)
      .filter((u) => !filters.role || (filters.role === 'NONE' ? !u.role : u.role === filters.role))
      .filter((u) => !term || [u.fullName, u.email, u.code].some((v) => String(v ?? '').toLowerCase().includes(term)))
      .sort((a, b) => a.fullName.localeCompare(b.fullName));
  }, [users, quick, filters, keyword]);

  const paging = useClientPagination(visible);
  const pills: FilterPill[] = [
    ...(filters.status ? [{ label: `Status: ${STATUS_LABELS[filters.status]}`, onRemove: () => setFilters({ ...filters, status: '' }) }] : []),
    ...(filters.role ? [{ label: `Role: ${ROLE_LABELS[filters.role]}`, onRemove: () => setFilters({ ...filters, role: '' }) }] : []),
  ];

  return (
    <>
      <FloatingToast
        feedback={error && error !== dismissedError ? { type: 'error', text: error } : null}
        onClose={() => setDismissedError(error)}
      />

      <StatFilterCards cards={cards} active={quick} onSelect={setQuick} />

      <ListCard
        title="Staff Directory"
        search={{ value: keyword, onChange: setKeyword, placeholder: 'Search staff...' }}
        filter={{
          activeCount: pills.length,
          isOpen: isFilterOpen,
          onToggle: () => {
            setDraftFilters(filters);
            setIsFilterOpen(!isFilterOpen);
          },
          panel: (
            <FilterPanel
              title="Filter Staff"
              isOpen={isFilterOpen}
              onClose={() => setIsFilterOpen(false)}
              activeCount={[draftFilters.status, draftFilters.role].filter(Boolean).length}
              onApply={() => {
                setFilters(draftFilters);
                setIsFilterOpen(false);
              }}
              onClear={() => {
                setDraftFilters(EMPTY_FILTERS);
                setFilters(EMPTY_FILTERS);
                setIsFilterOpen(false);
              }}
            >
              <FilterSelect
                label="Status"
                value={draftFilters.status}
                onChange={(status) => setDraftFilters({ ...draftFilters, status: status as EmploymentStatusValue | '' })}
                options={STATUS_OPTIONS}
              />
              <FilterSelect
                label="Role"
                value={draftFilters.role}
                onChange={(role) => setDraftFilters({ ...draftFilters, role: role as UserFilterValues['role'] })}
                options={ROLE_OPTIONS}
              />
            </FilterPanel>
          ),
        }}
        action={{ label: 'Onboard Employee', to: '/users/create' }}
        pills={pills}
        onClearPills={() => setFilters(EMPTY_FILTERS)}
        footer={
          <ListFooter page={paging.page} size={paging.size} total={paging.total} totalPages={paging.totalPages} onPageChange={paging.setPage} />
        }
      >
        <div className="max-w-full overflow-x-auto">
          <Table>
            <TableHeader className="border-b border-gray-100 bg-gray-50/50 dark:border-white/[0.05] dark:bg-gray-900/50">
              <TableRow>
                <TableCell isHeader className={HEAD_CELL}>Employee</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Code</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Status</TableCell>
                <TableCell isHeader className={HEAD_CELL}>System Role</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Joined</TableCell>
                <TableCell isHeader className={HEAD_CELL}>Action</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
              {isLoading ? (
                <TableStateRow colSpan={COLUMNS} loading message="Fetching staff..." />
              ) : paging.pageItems.length === 0 ? (
                <TableStateRow colSpan={COLUMNS} message="No employees found." />
              ) : (
                paging.pageItems.map((u) => (
                  <TableRow key={u.id}>
                    <TableCell className={BODY_CELL}>
                      <div className="flex items-center gap-3">
                        <InitialsAvatar name={u.fullName} />
                        <StackedCell
                          main={
                            <Link to={`/users/${u.id}`} className="hover:text-brand-500">
                              {u.fullName}
                            </Link>
                          }
                          sub={
                            <a href={`mailto:${u.email}`} className="hover:text-brand-500">
                              {u.email}
                            </a>
                          }
                        />
                      </div>
                    </TableCell>
                    <TableCell className={`${BODY_CELL} font-mono`}>{u.code}</TableCell>
                    <TableCell className={BODY_CELL}>
                      <UserStatusBadge status={u.status} />
                    </TableCell>
                    <TableCell className={BODY_CELL}>
                      <UserRoleBadge role={u.role} hasAccount={!!u.userId} />
                    </TableCell>
                    <TableCell className={BODY_CELL}>{formatDate(u.createdAt)}</TableCell>
                    <TableCell className={BODY_CELL}>
                      <div className="flex items-center">
                        <ViewAction to={`/users/${u.id}`} title="View Employee" />
                        <Link
                          to={`/users/edit/${u.id}`}
                          title="Edit"
                          aria-label="Edit"
                          className="flex items-center justify-center rounded-lg p-2 text-gray-400 transition-colors hover:bg-brand-50 hover:text-brand-500 dark:hover:bg-brand-500/10"
                        >
                          {icons.edit}
                        </Link>
                        {u.status !== EmploymentStatus.TERMINATED && (
                          <RowIconButton title="Offboard" tone="warning" onClick={() => setUserToOffboard(u)}>
                            {icons.offboard}
                          </RowIconButton>
                        )}
                        <RowIconButton title="Delete" tone="danger" onClick={() => setUserToDelete(u)}>
                          {icons.delete}
                        </RowIconButton>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </ListCard>

      <OffboardUserModal isOpen={!!userToOffboard} user={userToOffboard} onClose={() => setUserToOffboard(null)} onSuccess={handleOffboard} />

      <DeleteConfirmModal
        isOpen={!!userToDelete}
        onClose={() => setUserToDelete(null)}
        onConfirm={handleDelete}
        isDeleting={isDeleting}
        title="Delete Employee"
        message={`Delete ${userToDelete?.fullName} (${userToDelete?.code}) permanently? This cannot be undone.`}
      />
    </>
  );
};

export default UserListTable;
