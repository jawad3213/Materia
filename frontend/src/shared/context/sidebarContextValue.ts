import { createContext } from 'react';

export interface SidebarContextType {
  isExpanded: boolean;
  isMobileOpen: boolean;
  isHovered: boolean;
  activeSubmenu: string | null;
  toggleSidebar: () => void;
  toggleMobileSidebar: () => void;
  closeMobileSidebar: () => void;
  setIsHovered: (hovered: boolean) => void;
  toggleSubmenu: (menuKey: string) => void;
  isSubmenuOpen: (menuKey: string) => boolean;
}

export const SidebarContext = createContext<SidebarContextType | undefined>(undefined);
