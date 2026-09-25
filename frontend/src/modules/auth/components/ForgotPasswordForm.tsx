import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Label from '../../../shared/components/form/Label';
import Input from '../../../shared/components/form/input/InputField';
import Button from '../../../shared/components/ui/button/Button';
import useAuth from '../hooks/useAuth';

export default function ForgotPasswordForm() {
  const { resetPassword, isLoading, error, clearError } = useAuth();
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    setEmailError(null);

    const trimmedEmail = email.trim();
    if (!trimmedEmail) {
      setEmailError('Please enter your email address.');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmedEmail)) {
      setEmailError('Please enter a valid email address (e.g. name@company.com).');
      return;
    }

    try {
      const msg = await resetPassword({ email: trimmedEmail });
      setSuccessMessage(msg || `Password reset link sent to ${trimmedEmail}`);
      setIsSuccess(true);
    } catch {
      // Error handled by AuthContext
    }
  };

  const handleResend = async () => {
    if (!email.trim()) return;
    clearError();
    try {
      await resetPassword({ email: email.trim() });
    } catch {
      // Error handled
    }
  };

  return (
    <div className="w-full">
      {isSuccess ? (
            <div className="text-center py-4">
              <div className="inline-flex items-center justify-center size-14 mb-5 rounded-full bg-success-50 text-success-600 dark:bg-success-950/50 dark:text-success-400">
                <svg className="size-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>

              <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
                Check Your Inbox
              </h1>
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-6 leading-relaxed">
                {successMessage}
              </p>

              <div className="space-y-3">
                <button
                  type="button"
                  onClick={handleResend}
                  disabled={isLoading}
                  className="w-full py-3 px-4 text-center text-sm font-medium text-white bg-brand-500 rounded-lg shadow-theme-xs hover:bg-brand-600 transition disabled:opacity-50"
                >
                  {isLoading ? 'Resending...' : "Resend Email"}
                </button>
              </div>

              <div className="mt-8 pt-4 border-t border-gray-100 dark:border-gray-800">
                <Link
                  to="/login"
                  className="text-sm font-medium text-brand-500 hover:text-brand-600 dark:text-brand-400"
                >
                  Return to Sign In
                </Link>
              </div>
            </div>
          ) : (
            <>
              <div className="mb-5 sm:mb-8">
                <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
                  Forgot Your Password?
                </h1>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Enter the email address linked to your account, and we&apos;ll send you a link to reset your password.
                </p>
              </div>

              {/* API Error Banner */}
              {error && (
                <div className="mb-5 p-3.5 text-sm text-red-700 bg-red-50 rounded-lg border border-red-200 dark:bg-red-950/40 dark:border-red-800 dark:text-red-400 flex items-start gap-2">
                  <svg className="size-5 shrink-0 text-red-500 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clipRule="evenodd" />
                  </svg>
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleSubmit} noValidate>
                <div className="space-y-5">
                  <div>
                    <Label htmlFor="forgot-email">
                      Email Address <span className="text-error-500">*</span>
                    </Label>
                    <Input
                      id="forgot-email"
                      name="email"
                      type="email"
                      placeholder="name@company.com"
                      value={email}
                      onChange={(e) => {
                        setEmail(e.target.value);
                        if (emailError) setEmailError(null);
                        if (error) clearError();
                      }}
                      error={!!emailError}
                      hint={emailError || undefined}
                      disabled={isLoading}
                    />
                  </div>

                  <div>
                    <Button className="w-full font-medium" size="md" disabled={isLoading}>
                      {isLoading ? (
                        <span className="flex items-center justify-center gap-2">
                          <span className="size-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                          <span>Sending link...</span>
                        </span>
                      ) : (
                        'Send Reset Link'
                      )}
                    </Button>
                  </div>
                </div>
              </form>

              <div className="mt-6 pt-4 border-t border-gray-100 dark:border-gray-800">
                <p className="text-sm font-normal text-center text-gray-600 dark:text-gray-400 sm:text-start">
                  Remember your password?{' '}
                  <Link
                    to="/login"
                    className="text-brand-500 hover:text-brand-600 dark:text-brand-400 font-semibold transition-colors"
                  >
                    Sign In
                  </Link>
                </p>
              </div>
            </>
          )}
    </div>
  );
}
