import React, { useState, useEffect } from 'react';
import { SidebarContext } from './sidebarContextValue';

export const SidebarProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isExpanded, setIsExpanded] = useState<boolean>(true);
  const [isMobileOpen, setIsMobileOpen] = useState<boolean>(false);
  const [isHovered, setIsHovered] = useState<boolean>(false);
  const [activeSubmenu, setActiveSubmenu] = useState<string | null>('pages');

  // Close mobile sidebar on window resize to desktop
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 1024) {
        setIsMobileOpen(false);
      }
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const toggleSidebar = () => {
    setIsExpanded((prev) => !prev);
  };

  const toggleMobileSidebar = () => {
    setIsMobileOpen((prev) => !prev);
  };

  const closeMobileSidebar = () => {
    setIsMobileOpen(false);
  };

  const toggleSubmenu = (menuKey: string) => {
    setActiveSubmenu((prev) => (prev === menuKey ? null : menuKey));
  };

  const isSubmenuOpen = (menuKey: string) => {
    return activeSubmenu === menuKey;
  };

  return (
    <SidebarContext.Provider
      value={{
        isExpanded,
        isMobileOpen,
        isHovered,
        activeSubmenu,
        toggleSidebar,
        toggleMobileSidebar,
        closeMobileSidebar,
        setIsHovered,
        toggleSubmenu,
        isSubmenuOpen,
      }}
    >
      {children}
    </SidebarContext.Provider>
  );
};

