import React from 'react';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import UserMetaCard from '../../../shared/components/UserProfile/UserMetaCard';
import UserInfoCard from '../../../shared/components/UserProfile/UserInfoCard';
import UserAddressCard from '../../../shared/components/UserProfile/UserAddressCard';

export default function ProfilePage() {
  return (
    <>
      <PageMeta
        title="Profile | TailAdmin - React.js Tailwind Admin Dashboard Template"
        description="This is React.js Profile Dashboard page for TailAdmin"
      />
      <PageBreadcrumb pageTitle="Profile" />
      <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6">
        <h3 className="mb-5 text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-7">
          Profile
        </h3>
        <div className="space-y-6">
          <UserMetaCard />
          <UserInfoCard />
          <UserAddressCard />
        </div>
      </div>
    </>
  );
}
