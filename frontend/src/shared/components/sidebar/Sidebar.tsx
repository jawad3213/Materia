import React, { useMemo } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useSidebar } from '../../context/SidebarContext';
import useAuth from '../../../modules/auth/hooks/useAuth';
import type { UserRole } from '../../../modules/auth/types/auth.types';
import {
  GridIcon,
  BoxIcon,
  FolderIcon,
  GroupIcon,
  DocsIcon,
  FileIcon,
  BoxIconLine,
  DollarLineIcon,
  CheckCircleIcon,
  ArrowRightIcon,
  ChevronDownIcon,
  CloseIcon,
  UserCircleIcon,
} from '../../icons';

interface SubmenuItem {
  title: string;
  path: string;
  isNew?: boolean;
  roles?: UserRole[];
}

interface MenuItem {
  title: string;
  icon: React.ComponentType<{ className?: string }>;
  path?: string;
  isNew?: boolean;
  submenu?: SubmenuItem[];
  menuKey?: string;
  roles?: UserRole[];
}

interface MenuSection {
  sectionTitle?: string;
  roles?: UserRole[];
  items: MenuItem[];
}

const menuSections: MenuSection[] = [
  {
    sectionTitle: 'MAIN MENU',
    items: [
      {
        title: 'Dashboard',
        icon: GridIcon,
        path: '/dashboard',
      },
      {
        title: 'Materials',
        menuKey: 'materials',
        icon: BoxIcon,
        path: '/materials',
        submenu: [
          { title: 'Materials List', path: '/materials' },
          { title: 'Material Cards', path: '/materials/cards' },
          { title: 'Add Material', path: '/materials/create-material' },
        ],
      },
      {
        title: 'Categories',
        menuKey: 'categories',
        icon: FolderIcon,
        path: '/categories',
        roles: ['ADMIN', 'PURCHASER'],
        submenu: [
          { title: 'Category Tree', path: '/categories' },
          { title: 'Add Category', path: '/categories/create-category' },
        ],
      },
      {
        title: 'Suppliers',
        menuKey: 'suppliers',
        icon: GroupIcon,
        path: '/suppliers',
        roles: ['ADMIN', 'PURCHASER'],
        submenu: [
          { title: 'Supplier Directory', path: '/suppliers' },
          { title: 'Add Supplier', path: '/suppliers/create-supplier' },
        ],
      },
    ],
  },
  {
    sectionTitle: 'PROCUREMENT & INVENTORY',
    items: [
      {
        title: 'Requisitions',
        menuKey: 'requisitions',
        icon: DocsIcon,
        path: '/requisitions',
        roles: ['ADMIN', 'PURCHASER'],
        submenu: [
          { title: 'Requisitions List', path: '/requisitions' },
          { title: 'Create Requisition', path: '/requisitions/create' },
          { title: 'Approvals Portal', path: '/requisitions/approvals' },
        ],
      },
      {
        title: 'Purchase Orders',
        menuKey: 'purchaseOrders',
        icon: FileIcon,
        path: '/purchase-orders',
        roles: ['ADMIN', 'PURCHASER', 'RECEIVER'],
        submenu: [
          { title: 'Orders List', path: '/purchase-orders' },
          { title: 'Create Order', path: '/purchase-orders/create', roles: ['ADMIN', 'PURCHASER'] },
        ],
      },
      {
        title: 'Goods Receipts',
        icon: BoxIconLine,
        path: '/goods-receipts',
        roles: ['ADMIN', 'PURCHASER', 'RECEIVER'],
      },
    ],
  },
  {
    sectionTitle: 'FINANCE & BILLING',
    roles: ['ADMIN', 'PURCHASER'],
    items: [
      {
        title: 'Invoices',
        icon: DollarLineIcon,
        path: '/invoices',
        submenu: [
          { title: 'Invoices List', path: '/invoices' },
          { title: 'Record Invoice', path: '/invoices/create' },
        ],
      },
      {
        title: 'Payments',
        icon: CheckCircleIcon,
        path: '/payments',
      },
      {
        title: 'Vendor Returns',
        icon: ArrowRightIcon,
        path: '/returns',
      },
    ],
  },
  {
    sectionTitle: 'ADMINISTRATION',
    roles: ['ADMIN'],
    items: [
      {
        title: 'Users & Staff',
        menuKey: 'users',
        icon: UserCircleIcon,
        path: '/users',
        roles: ['ADMIN'],
        submenu: [
          { title: 'Staff Directory', path: '/users' },
          { title: 'Onboard Employee', path: '/users/create' },
        ],
      },
    ],
  },
];

export const Sidebar: React.FC = () => {
  const {
    isExpanded,
    isMobileOpen,
    isHovered,
    setIsHovered,
    closeMobileSidebar,
    toggleSubmenu,
    isSubmenuOpen,
  } = useSidebar();
  const { user } = useAuth();
  const location = useLocation();

  const currentRole: UserRole = user?.role || 'ADMIN';

  // Dynamically filter sections and items according to the logged-in user's role
  const visibleSections = useMemo(() => {
    return menuSections
      .filter((section) => !section.roles || section.roles.includes(currentRole))
      .map((section) => ({
        ...section,
        items: section.items
          .filter((item) => !item.roles || item.roles.includes(currentRole))
          .map((item) => ({
            ...item,
            submenu: item.submenu?.filter(
              (sub) => !sub.roles || sub.roles.includes(currentRole)
            ),
          })),
      }))
      .filter((section) => section.items.length > 0);
  }, [currentRole]);

  const isSubmenuItemActive = (sub: SubmenuItem, allSubmenu: SubmenuItem[]): boolean => {
    if (location.pathname === sub.path) return true;

    // If current route exactly matches another sibling submenu item, this item is not active
    const hasSiblingExactMatch = allSubmenu.some(
      (other) => other.path !== sub.path && location.pathname === other.path
    );
    if (hasSiblingExactMatch) return false;

    // For nested routes (e.g. /materials/edit/1, /categories/view/1)
    // Only match if no other sibling has a more specific prefix match
    if (location.pathname.startsWith(sub.path + '/')) {
      const hasMoreSpecificSibling = allSubmenu.some(
        (other) =>
          other.path !== sub.path &&
          location.pathname.startsWith(other.path + '/') &&
          other.path.length > sub.path.length
      );
      return !hasMoreSpecificSibling;
    }
    return false;
  };

  const isCurrentActive = (item: MenuItem): boolean => {
    if (item.path && (location.pathname === item.path || (item.path === '/materials' && location.pathname === '/'))) {
      return true;
    }
    if (item.submenu) {
      return item.submenu.some((sub) => isSubmenuItemActive(sub, item.submenu!));
    }
    if (item.path && location.pathname.startsWith(item.path + '/')) {
      return true;
    }
    return false;
  };

  const isFullOpen = isExpanded || isHovered;

  return (
    <>
      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-gray-900/50 backdrop-blur-sm lg:hidden transition-opacity duration-300"
          onClick={closeMobileSidebar}
        />
      )}

      {/* Sidebar Container with Smooth Transition */}
      <aside
        onMouseEnter={() => !isExpanded && setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
        className={`fixed top-0 left-0 z-50 flex flex-col h-screen bg-white border-r border-gray-200 dark:bg-gray-900 dark:border-gray-800 transition-all duration-300 ease-in-out lg:static ${
          isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        } ${
          isFullOpen
            ? 'w-72 shadow-2xl lg:shadow-none'
            : 'w-72 lg:w-[90px]'
        }`}
      >
        {/* Brand Logo Header */}
        <div
          className={`flex items-center border-b border-gray-100 dark:border-gray-800/80 transition-all duration-300 ${
            isFullOpen ? 'justify-between px-6 py-5' : 'justify-center px-4 py-5'
          }`}
        >
          <NavLink
            to="/"
            className="flex items-center gap-3 overflow-hidden"
            onClick={closeMobileSidebar}
          >
            <img
              src="/images/Black_White_Minimalist_Professional_Initial_Logo__1_-removebg-preview332.png"
              alt="Materia Logo"
              className="h-12 w-12 shrink-0 object-contain dark:invert transition-transform duration-200 hover:scale-105"
            />
            <img
              src="/images/Black_White_Minimalist_Professional_Initial_Logo__2_-removebg-preview.png"
              alt="Materia"
              className={`h-7 w-auto object-contain dark:invert transition-all duration-300 ${
                isFullOpen ? 'opacity-100 max-w-[180px]' : 'opacity-0 max-w-0 hidden lg:block'
              }`}
            />
          </NavLink>

          {/* Close button for Mobile Drawer */}
          <button
            onClick={closeMobileSidebar}
            className="p-1.5 text-gray-500 rounded-lg lg:hidden hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800"
            aria-label="Close sidebar"
          >
            <CloseIcon className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation Scroll Area */}
        <div className="flex flex-col flex-1 px-3 py-5 overflow-y-auto custom-scrollbar">
          {visibleSections.map((section, sIndex) => (
            <div key={section.sectionTitle || sIndex} className="mb-5 last:mb-2">
              {section.sectionTitle && (
                isFullOpen ? (
                  <span className="block px-3 mb-2.5 text-[11px] font-semibold tracking-wider text-gray-400 uppercase dark:text-gray-500 whitespace-nowrap transition-opacity duration-200">
                    {section.sectionTitle}
                  </span>
                ) : (
                  <div className="h-px bg-gray-100 dark:bg-gray-800 my-2 mx-2" />
                )
              )}

              <nav className="flex flex-col gap-1">
                {section.items.map((item) => {
                  const Icon = item.icon;
                  const hasSubmenu = item.submenu && item.submenu.length > 0;
                  const isOpen = item.menuKey ? isSubmenuOpen(item.menuKey) : false;
                  const active = isCurrentActive(item);

                  if (hasSubmenu) {
                    const targetPath = item.path || (item.submenu && item.submenu.length > 0 ? item.submenu[0].path : '#');

                    return (
                      <div key={item.title} className="flex flex-col">
                        <div
                          className={`flex items-center w-full rounded-xl transition-colors ${
                            active || isOpen
                              ? 'bg-blue-50/80 text-blue-600 dark:bg-blue-500/15 dark:text-blue-400 font-semibold'
                              : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-gray-800/60 dark:hover:text-white'
                          }`}
                        >
                          <NavLink
                            to={targetPath}
                            title={!isFullOpen ? item.title : undefined}
                            onClick={() => {
                              closeMobileSidebar();
                              if (!isOpen && item.menuKey) {
                                toggleSubmenu(item.menuKey);
                              }
                            }}
                            className={`flex items-center flex-1 py-2.5 text-sm font-medium transition-colors ${
                              isFullOpen ? 'pl-3.5 pr-2' : 'justify-center px-2 w-full'
                            }`}
                          >
                            <div className="flex items-center gap-3">
                              <Icon
                                className={`shrink-0 w-5 h-5 ${
                                  active || isOpen
                                    ? 'text-blue-600 dark:text-blue-400'
                                    : 'text-gray-500 dark:text-gray-400'
                                }`}
                              />
                              {isFullOpen && (
                                <span className="whitespace-nowrap transition-opacity duration-200">
                                  {item.title}
                                </span>
                              )}
                            </div>
                          </NavLink>

                          {isFullOpen && (
                            <div className="flex items-center gap-1.5 pr-2.5">
                              {item.isNew && (
                                <span className="px-2 py-0.5 text-[11px] font-semibold text-emerald-600 bg-emerald-50 rounded-full dark:bg-emerald-500/15 dark:text-emerald-400">
                                  NEW
                                </span>
                              )}
                              <button
                                type="button"
                                aria-label={`Toggle ${item.title} submenu`}
                                onClick={(e) => {
                                  e.preventDefault();
                                  e.stopPropagation();
                                  if (item.menuKey) {
                                    toggleSubmenu(item.menuKey);
                                  }
                                }}
                                className="p-1.5 rounded-lg hover:bg-black/5 dark:hover:bg-white/10 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors"
                              >
                                <ChevronDownIcon
                                  className={`w-4 h-4 transition-transform duration-200 ${
                                    isOpen ? 'rotate-180 text-blue-600 dark:text-blue-400' : ''
                                  }`}
                                />
                              </button>
                            </div>
                          )}
                        </div>

                        {/* Submenu Items */}
                        {isOpen && isFullOpen && (
                          <div className="flex flex-col pl-11 pr-2 py-1 gap-0.5 animate-fadeIn">
                            {item.submenu!.map((sub) => {
                              const isSubActive = isSubmenuItemActive(sub, item.submenu!);
                              return (
                                <NavLink
                                  key={sub.title}
                                  to={sub.path}
                                  onClick={closeMobileSidebar}
                                  className={`flex items-center justify-between py-2 px-2.5 rounded-lg text-sm transition-colors ${
                                    isSubActive
                                      ? 'font-medium text-blue-600 dark:text-blue-400'
                                      : 'text-gray-600 hover:text-gray-900 dark:text-gray-400 dark:hover:text-white'
                                  }`}
                                >
                                  <span>{sub.title}</span>
                                  {sub.isNew && (
                                    <span className="px-2 py-0.5 text-[10px] font-semibold text-emerald-600 bg-emerald-50 rounded-full dark:bg-emerald-500/15 dark:text-emerald-400">
                                      NEW
                                    </span>
                                  )}
                                </NavLink>
                              );
                            })}
                          </div>
                        )}
                      </div>
                    );
                  }

                  return (
                    <NavLink
                      key={item.title}
                      to={item.path || '#'}
                      title={!isFullOpen ? item.title : undefined}
                      onClick={closeMobileSidebar}
                      className={`flex items-center w-full py-2.5 rounded-xl text-sm font-medium transition-colors ${
                        isFullOpen ? 'justify-between px-3.5' : 'justify-center px-2'
                      } ${
                        active
                          ? 'bg-blue-50/80 text-blue-600 dark:bg-blue-500/15 dark:text-blue-400 font-semibold'
                          : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-gray-800/60 dark:hover:text-white'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <Icon
                          className={`shrink-0 w-5 h-5 ${
                            active
                              ? 'text-blue-600 dark:text-blue-400'
                              : 'text-gray-500 dark:text-gray-400'
                          }`}
                        />
                        {isFullOpen && (
                          <span className="whitespace-nowrap transition-opacity duration-200">
                            {item.title}
                          </span>
                        )}
                      </div>

                      {isFullOpen && item.isNew && (
                        <span className="px-2 py-0.5 text-[11px] font-semibold text-emerald-600 bg-emerald-50 rounded-full dark:bg-emerald-500/15 dark:text-emerald-400">
                          NEW
                        </span>
                      )}
                    </NavLink>
                  );
                })}
              </nav>
            </div>
          ))}
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
