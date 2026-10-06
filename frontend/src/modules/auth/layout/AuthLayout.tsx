import React from 'react';
import { Link } from 'react-router-dom';

interface AuthLayoutProps {
  children: React.ReactNode;
  backgroundImage?: string;
}

export default function AuthLayout({
  children,
  backgroundImage = 'https://ik.imagekit.io/jaouad/pexels-tiger-lily-4483772.jpg',
}: AuthLayoutProps) {
  return (
    <div className="flex h-screen w-full overflow-hidden bg-white dark:bg-gray-900 transition-colors relative">
      {/* Back to Home Button (Extra Left / Top-Left) */}
      <Link
        to="/"
        aria-label="Back to home"
        title="Back to public home page"
        className="absolute top-4 left-4 sm:top-6 sm:left-6 z-30 inline-flex items-center gap-2 px-3 py-2 text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white bg-white/90 dark:bg-gray-800/90 backdrop-blur-md rounded-xl border border-gray-200/80 dark:border-gray-700/80 shadow-xs hover:shadow-md hover:bg-gray-50 dark:hover:bg-gray-750 transition-all group"
      >
        <svg
          className="size-4.5 transition-transform duration-200 group-hover:-translate-x-1 text-gray-500 dark:text-gray-400 group-hover:text-brand-500 dark:group-hover:text-brand-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2.2}
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          />
        </svg>
        <span className="font-medium text-xs sm:text-sm">Home</span>
      </Link>

      {/* Left Column: Form Section */}
      <div
        className="flex flex-col justify-between w-full lg:w-1/2 h-full overflow-y-auto no-scrollbar px-6 sm:px-12 md:px-16 lg:px-12 xl:px-20 py-4 sm:py-6"
        style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
      >
        {/* Top spacer */}
        <div className="w-full max-w-md mx-auto h-4" />

        {/* Center: Children Form */}
        <div className="w-full max-w-md mx-auto my-auto py-2 sm:py-4">
          {/* Centered Materia Wordmark Logo */}
          <div className="flex justify-center pb-4 mb-3 sm:pb-6 sm:mb-4">
            <Link
              to="/"
              className="inline-flex items-center transition-transform duration-200 hover:opacity-90"
              aria-label="Materia home"
            >
              <img
                src="/images/Black_White_Minimalist_Professional_Initial_Logo__2_-removebg-preview.png"
                alt="Materia"
                className="h-5.5 w-auto sm:h-6 object-contain dark:invert"
              />
            </Link>
          </div>

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
