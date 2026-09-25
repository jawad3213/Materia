import { useState, useEffect } from "react";

export type ToastVariant = "success" | "info" | "warning" | "error";

export interface ToastProps {
  variant: ToastVariant;
  message: string;
  onClose?: () => void;
  duration?: number; // Time in ms for the auto-dismiss progress bar
}

const icons = {
  success: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
    </svg>
  ),
  info: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
  warning: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
    </svg>
  ),
  error: (
    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
};

const variantStyles = {
  success: {
    bar: "bg-success-500",
    iconContainer: "bg-success-50 text-success-500 dark:bg-success-500/10",
  },
  info: {
    bar: "bg-blue-500",
    iconContainer: "bg-blue-50 text-blue-500 dark:bg-blue-500/10",
  },
  warning: {
    bar: "bg-warning-500",
    iconContainer: "bg-warning-50 text-warning-500 dark:bg-warning-500/10",
  },
  error: {
    bar: "bg-error-500",
    iconContainer: "bg-error-50 text-error-500 dark:bg-error-500/10",
  },
};

export default function Toast({ variant, message, onClose, duration }: ToastProps) {
  const styles = variantStyles[variant];
  const [progress, setProgress] = useState(100);

  useEffect(() => {
    if (duration) {
      // Start shrinking the bar shortly after mount
      const timer = setTimeout(() => {
        setProgress(0);
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [duration]);

  return (
    <div className="relative overflow-hidden rounded-lg border border-gray-200 bg-white p-4 shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 sm:max-w-sm">
      <div 
        className={`absolute bottom-0 left-0 h-1 ${styles.bar} transition-all ease-linear`}
        style={{
          width: `${progress}%`,
          transitionDuration: duration ? `${duration}ms` : '0ms'
        }}
      ></div>
      <button 
        onClick={onClose}
        className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
      >
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
      <div className="flex items-center gap-4 pr-6">
        <div className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full ${styles.iconContainer}`}>
          {icons[variant]}
        </div>
        <h5 className="text-sm font-medium text-gray-800 dark:text-white/90">
          {message}
        </h5>
      </div>
    </div>
  );
}
