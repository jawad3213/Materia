import React from 'react';
import { Modal } from './index';

interface DeleteConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  isDeleting?: boolean;
}

const WavyBg = ({ className }: { className: string }) => (
  <svg className={`absolute w-full h-full ${className}`} viewBox="0 0 100 100" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
     <path d="M50 0C55 0 59 2.5 62 6.5L66 11.5C68.5 14.5 73.5 15.5 77.5 14.5L83.5 13C88.5 11.5 93.5 14.5 95 19.5L96.5 25.5C97.5 29.5 100.5 32 100 36.5L100 42.5C99 47 100.5 51 98.5 54.5L95.5 59.5C92.5 63 92 68.5 94.5 72.5L97.5 77.5C100.5 82 98.5 87.5 93.5 89.5L88 91.5C84 93 81.5 96.5 80.5 100H50H19.5C18.5 96.5 16 93 12 91.5L6.5 89.5C1.5 87.5-0.5 82 2.5 77.5L5.5 72.5C8 68.5 7.5 63 4.5 59.5L1.5 54.5C-0.5 51 1 47 0 42.5V36.5C-0.5 32 2.5 29.5 3.5 25.5L5 19.5C6.5 14.5 11.5 11.5 16.5 13L22.5 14.5C26.5 15.5 31.5 14.5 34 11.5L38 6.5C41 2.5 45 0 50 0Z" />
  </svg>
);

export default function DeleteConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  isDeleting = false
}: DeleteConfirmModalProps) {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      className="max-w-[600px] p-6 lg:p-10 text-center"
    >
      <div className="flex flex-col items-center gap-4">
        <div className="relative flex items-center justify-center w-[84px] h-[84px] mb-1 text-error-500">
          <WavyBg className="text-error-50 dark:text-error-500/15" />
          <div className="relative z-10">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M24 8L8 24M8 8L24 24" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
        </div>

        <h4 className="text-2xl md:text-[32px] md:leading-[1.2] font-bold text-gray-800 dark:text-white/90">
          {title}
        </h4>

        <p className="text-base text-gray-500 dark:text-gray-400">
          {message}
        </p>

        <div className="flex items-center gap-3 mt-4 w-full justify-center">
          <button
            onClick={onClose}
            disabled={isDeleting}
            className="px-6 py-3 text-base font-medium transition rounded-lg bg-gray-100 hover:bg-gray-200 text-gray-700 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700 w-1/2 sm:w-auto cursor-pointer"
          >
            No
          </button>
          <button
            onClick={onConfirm}
            disabled={isDeleting}
            className="px-6 py-3 text-base font-medium transition rounded-lg bg-red-600 hover:bg-red-700 active:bg-red-800 text-white shadow-sm w-1/2 sm:w-auto cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isDeleting ? "Deleting..." : "Yes"}
          </button>
        </div>
      </div>
    </Modal>
  );
}
