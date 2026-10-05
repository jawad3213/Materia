import { useContext } from 'react';
import { SidebarContext, type SidebarContextType } from './sidebarContextValue';

/** The sidebar state; outside a provider it returns an expanded, inert sidebar. */
export const useSidebar = (): SidebarContextType => {
  const context = useContext(SidebarContext);
  if (!context) {
    return {
      isExpanded: true,
      isMobileOpen: false,
      isHovered: false,
      activeSubmenu: 'pages',
      toggleSidebar: () => {},
      toggleMobileSidebar: () => {},
      closeMobileSidebar: () => {},
      setIsHovered: () => {},
      toggleSubmenu: () => {},
      isSubmenuOpen: () => false,
    };
  }
  return context;
};
