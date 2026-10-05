import { useContext } from 'react';
import { ThemeContext, type Theme, type ThemeContextType } from './themeContextValue';

/** The theme; outside a provider it toggles the `dark` class on the document directly. */
export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (!context) {
    return {
      theme: 'light',
      toggleTheme: () => {
        if (typeof document !== 'undefined') {
          document.documentElement.classList.toggle('dark');
        }
      },
      setTheme: (t: Theme) => {
        if (typeof document !== 'undefined') {
          if (t === 'dark') document.documentElement.classList.add('dark');
          else document.documentElement.classList.remove('dark');
        }
      },
    };
  }
  return context;
};
