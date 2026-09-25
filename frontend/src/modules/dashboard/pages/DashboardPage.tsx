import React from 'react';
import PageMeta from '../../../shared/components/common/PageMeta';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import { BoxIcon, GroupIcon, FolderIcon, DollarLineIcon, ArrowUpIcon, ArrowDownIcon } from '../../../shared/icons';
import { Link } from 'react-router-dom';

export default function DashboardPage() {
  const stats = [
    {
      title: 'Total Materials',
      value: '1,420',
      change: '+12.5%',
      isPositive: true,
      icon: BoxIcon,
      color: 'bg-blue-500',
    },
    {
      title: 'Active Suppliers',
      value: '86',
      change: '+4.2%',
      isPositive: true,
      icon: GroupIcon,
      color: 'bg-emerald-500',
    },
    {
      title: 'Categories',
      value: '34',
      change: '+2',
      isPositive: true,
      icon: FolderIcon,
      color: 'bg-amber-500',
    },
    {
      title: 'Monthly Spend',
      value: '$48,250',
      change: '-2.4%',
      isPositive: false,
      icon: DollarLineIcon,
      color: 'bg-indigo-500',
    },
  ];

  const recentMaterials = [
    { name: 'Reinforced Steel Bar 12mm', sku: 'MAT-STL-001', category: 'Construction Metals', stock: 450, status: 'In Stock' },
    { name: 'Portland Cement Type I', sku: 'MAT-CEM-042', category: 'Aggregates & Cement', stock: 24, status: 'Low Stock' },
    { name: 'Copper Wiring Gauge 10', sku: 'MAT-ELE-108', category: 'Electrical', stock: 120, status: 'In Stock' },
    { name: 'Industrial PVC Pipes 4"', sku: 'MAT-PLM-019', category: 'Plumbing', stock: 80, status: 'In Stock' },
    { name: 'Hardwood Timber 2x4x8', sku: 'MAT-WOD-055', category: 'Lumber & Wood', stock: 12, status: 'Low Stock' },
  ];

  return (
    <>
      <PageMeta
        title="Dashboard | Materia Admin"
        description="Materia supply chain & inventory management dashboard"
      />
      <PageBreadcrumb pageTitle="Dashboard" />

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4 md:gap-6 mb-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.title}
              className="p-5 bg-white border border-gray-200 rounded-2xl dark:border-gray-800 dark:bg-gray-900 shadow-sm"
            >
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-gray-500 dark:text-gray-400">{stat.title}</span>
                <div className={`flex items-center justify-center w-10 h-10 rounded-xl text-white ${stat.color} shadow-sm`}>
                  <Icon className="w-5 h-5 text-white" />
                </div>
              </div>
              <div className="flex items-baseline justify-between mt-4">
                <h3 className="text-2xl font-bold text-gray-900 dark:text-white">{stat.value}</h3>
                <span
                  className={`inline-flex items-center gap-1 text-xs font-semibold px-2 py-0.5 rounded-full ${
                    stat.isPositive
                      ? 'text-emerald-700 bg-emerald-50 dark:bg-emerald-500/10 dark:text-emerald-400'
                      : 'text-rose-700 bg-rose-50 dark:bg-rose-500/10 dark:text-rose-400'
                  }`}
                >
                  {stat.isPositive ? <ArrowUpIcon className="w-3 h-3" /> : <ArrowDownIcon className="w-3 h-3" />}
                  {stat.change}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Main Grid: Recent Activity & Quick Navigation */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3 mb-6">
        {/* Recent Materials Table */}
        <div className="lg:col-span-2 p-5 bg-white border border-gray-200 rounded-2xl dark:border-gray-800 dark:bg-gray-900 shadow-sm">
          <div className="flex items-center justify-between mb-5">
            <h4 className="text-base font-semibold text-gray-900 dark:text-white">Recent Inventory Items</h4>
            <Link
              to="/materials"
              className="text-xs font-medium text-blue-600 dark:text-blue-400 hover:underline"
            >
              View All
            </Link>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm text-gray-600 dark:text-gray-400">
              <thead className="text-xs uppercase bg-gray-50 dark:bg-gray-800/50 text-gray-700 dark:text-gray-300">
                <tr>
                  <th className="px-4 py-3 rounded-l-lg">Material</th>
                  <th className="px-4 py-3">SKU</th>
                  <th className="px-4 py-3">Category</th>
                  <th className="px-4 py-3">Stock</th>
                  <th className="px-4 py-3 rounded-r-lg">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {recentMaterials.map((m) => (
                  <tr key={m.sku} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/30 transition">
                    <td className="px-4 py-3 font-medium text-gray-900 dark:text-white">{m.name}</td>
                    <td className="px-4 py-3 text-xs text-gray-500 dark:text-gray-400">{m.sku}</td>
                    <td className="px-4 py-3">{m.category}</td>
                    <td className="px-4 py-3 font-semibold text-gray-900 dark:text-white">{m.stock}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                          m.status === 'In Stock'
                            ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-400'
                            : 'bg-amber-50 text-amber-700 dark:bg-amber-500/10 dark:text-amber-400'
                        }`}
                      >
                        {m.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Quick Actions Card */}
        <div className="p-5 bg-white border border-gray-200 rounded-2xl dark:border-gray-800 dark:bg-gray-900 shadow-sm flex flex-col justify-between">
          <div>
            <h4 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Quick Management</h4>
            <div className="space-y-3">
              <Link
                to="/materials/create-material"
                className="flex items-center justify-between p-3 rounded-xl border border-gray-200 dark:border-gray-800 hover:border-blue-500 hover:bg-blue-50/50 dark:hover:bg-blue-500/10 transition group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-blue-50 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400">
                    <BoxIcon className="w-5 h-5" />
                  </div>
                  <div>
                    <span className="block text-sm font-medium text-gray-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400">
                      Add New Material
                    </span>
                    <span className="text-xs text-gray-500 dark:text-gray-400">Register inventory items</span>
                  </div>
                </div>
                <ArrowRightIcon className="w-4 h-4 text-gray-400 group-hover:text-blue-600 transition" />
              </Link>

              <Link
                to="/categories/create-category"
                className="flex items-center justify-between p-3 rounded-xl border border-gray-200 dark:border-gray-800 hover:border-emerald-500 hover:bg-emerald-50/50 dark:hover:bg-emerald-500/10 transition group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
                    <FolderIcon className="w-5 h-5" />
                  </div>
                  <div>
                    <span className="block text-sm font-medium text-gray-900 dark:text-white group-hover:text-emerald-600 dark:group-hover:text-emerald-400">
                      Add Category
                    </span>
                    <span className="text-xs text-gray-500 dark:text-gray-400">Organize supply taxonomy</span>
                  </div>
                </div>
                <ArrowRightIcon className="w-4 h-4 text-gray-400 group-hover:text-emerald-600 transition" />
              </Link>

              <Link
                to="/suppliers/create-supplier"
                className="flex items-center justify-between p-3 rounded-xl border border-gray-200 dark:border-gray-800 hover:border-amber-500 hover:bg-amber-50/50 dark:hover:bg-amber-500/10 transition group"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-amber-50 dark:bg-amber-500/10 text-amber-600 dark:text-amber-400">
                    <GroupIcon className="w-5 h-5" />
                  </div>
                  <div>
                    <span className="block text-sm font-medium text-gray-900 dark:text-white group-hover:text-amber-600 dark:group-hover:text-amber-400">
                      Add Supplier
                    </span>
                    <span className="text-xs text-gray-500 dark:text-gray-400">Onboard vendor partners</span>
                  </div>
                </div>
                <ArrowRightIcon className="w-4 h-4 text-gray-400 group-hover:text-amber-600 transition" />
              </Link>
            </div>
          </div>

          <div className="mt-6 p-4 rounded-xl bg-blue-50/70 dark:bg-blue-500/10 border border-blue-100 dark:border-blue-900/30">
            <span className="text-xs font-semibold text-blue-700 dark:text-blue-300 uppercase tracking-wider block mb-1">
              System Status
            </span>
            <p className="text-xs text-blue-900 dark:text-blue-200">
              Materia ERP Core is online. Inventory synchronization active.
            </p>
          </div>
        </div>
      </div>
    </>
  );
}
