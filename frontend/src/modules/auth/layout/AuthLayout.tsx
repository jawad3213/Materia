import React from 'react';
import { useTheme } from '../../../shared/context/ThemeContext';

interface AuthLayoutProps {
  children: React.ReactNode;
  backgroundImage?: string;
}

export default function AuthLayout({
  children,
  backgroundImage = 'https://ik.imagekit.io/jaouad/pexels-tiger-lily-4483772.jpg',
}: AuthLayoutProps) {
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="flex h-screen w-full overflow-hidden bg-white dark:bg-gray-900 transition-colors">
      {/* Left Column: Form Section */}
      <div
        className="flex flex-col justify-between w-full lg:w-1/2 h-full overflow-y-auto no-scrollbar px-6 sm:px-12 md:px-16 lg:px-12 xl:px-20 py-4 sm:py-6"
        style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
      >
        {/* Top Header Bar (Mobile Theme Toggle only on small screens) */}
        <div className="flex items-center justify-end w-full max-w-md mx-auto">
          <button
            type="button"
            onClick={toggleTheme}
            aria-label="Toggle theme"
            className="lg:hidden p-2 text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-gray-200 transition-colors rounded-lg bg-gray-100 dark:bg-gray-800"
          >
            {theme === 'dark' ? (
              <svg className="size-5 text-amber-400" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clipRule="evenodd" />
              </svg>
            ) : (
              <svg className="size-5 text-gray-700" fill="currentColor" viewBox="0 0 20 20">
                <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
              </svg>
            )}
          </button>
        </div>

        {/* Center: Children Form */}
        <div className="w-full max-w-md mx-auto my-auto py-2 sm:py-4">
          {children}
        </div>

        {/* Bottom Spacer to keep form vertically centered */}
        <div className="w-full max-w-md mx-auto h-2" />
      </div>

      {/* Right Column: Visual Photo Banner - 100% Pure Photo (Locked at 100vh) */}
      <div className="hidden lg:block lg:w-1/2 h-full relative overflow-hidden select-none bg-gray-900">
        {/* Background photo */}
        {backgroundImage && (
          <img
            src={backgroundImage}
            alt="Materia warehouse"
            className="absolute inset-0 w-full h-full object-cover object-center"
          />
        )}
      </div>
    </div>
  );
}
