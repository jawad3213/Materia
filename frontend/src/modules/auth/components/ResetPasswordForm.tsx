import React, { useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { EyeCloseIcon, EyeIcon } from '../../../shared/icons';
import Label from '../../../shared/components/form/Label';
import Input from '../../../shared/components/form/input/InputField';
import Button from '../../../shared/components/ui/button/Button';
import useAuth from '../hooks/useAuth';

export default function ResetPasswordForm() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { confirmPasswordReset, isLoading, error, clearError } = useAuth();

  const urlToken = searchParams.get('token') || '';
  const urlEmail = searchParams.get('email') || '';

  const [token, setToken] = useState(urlToken);
  const [email, setEmail] = useState(urlEmail);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Automatically update state if URL query parameters change
  React.useEffect(() => {
    if (urlToken) setToken(urlToken);
    if (urlEmail) setEmail(urlEmail);
  }, [urlToken, urlEmail]);

  const hasTokenInUrl = !!urlToken.trim();

  const [fieldErrors, setFieldErrors] = useState<{
    token?: string;
    email?: string;
    password?: string;
    confirmPassword?: string;
  }>({});

  const validate = (): boolean => {
    const errors: typeof fieldErrors = {};

    if (!token.trim()) {
      errors.token = 'Please provide the reset code or token from your email.';
    }

    if (!email.trim()) {
      errors.email = 'Please enter your email address.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = 'Please enter a valid email address.';
    }

    if (!password) {
      errors.password = 'Please enter your new password.';
    } else if (password.length < 8) {
      errors.password = 'New password must be at least 8 characters long.';
    }

    if (!confirmPassword) {
      errors.confirmPassword = 'Please confirm your new password.';
    } else if (password !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match.';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    if (!validate()) {
      return;
    }

    try {
      const msg = await confirmPasswordReset({
        token: token.trim(),
        email: email.trim(),
        password,
        confirmPassword,
      });
      setSuccessMessage(msg || 'Your password has been successfully reset.');
      setIsSuccess(true);
    } catch {
      // Error handled by AuthContext
    }
  };

  return (
    <div className="w-full">
      {isSuccess ? (
        <div className="text-center py-6">
          <div className="inline-flex items-center justify-center size-14 mb-5 rounded-full bg-success-50 text-success-600 dark:bg-success-950/50 dark:text-success-400">
            <svg className="size-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>

          <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
            Password Reset Successfully
          </h1>
          <p className="text-sm text-gray-600 dark:text-gray-400 mb-6 leading-relaxed">
            {successMessage}
          </p>

          <button
            type="button"
            onClick={() => navigate('/login')}
            className="w-full py-3 px-4 text-center text-sm font-medium text-white bg-brand-500 rounded-lg shadow-theme-xs hover:bg-brand-600 transition"
          >
            Proceed to Sign In
          </button>
        </div>
      ) : (
        <>
          <div className="mb-5 sm:mb-6">
            <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
              Set New Password
            </h1>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Choose a secure password with at least 8 characters.
            </p>
          </div>

          {/* Email badge if arrived via email link */}
          {hasTokenInUrl && email && (
            <div className="mb-5 flex items-center gap-2.5 px-3.5 py-2.5 bg-brand-50/70 dark:bg-brand-950/40 border border-brand-200 dark:border-brand-800/60 rounded-xl text-xs text-brand-800 dark:text-brand-300">
              <svg className="size-4 shrink-0 text-brand-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
              <span>Resetting password for: <strong className="font-semibold text-brand-900 dark:text-brand-200">{email}</strong></span>
            </div>
          )}

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
            <div className="space-y-4">
              {/* New Password */}
                  <div>
                    <Label htmlFor="reset-new-password">
                      New Password <span className="text-error-500">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="reset-new-password"
                        name="password"
                        type={showPassword ? 'text' : 'password'}
                        placeholder="At least 8 characters"
                        value={password}
                        onChange={(e) => {
                          setPassword(e.target.value);
                          if (fieldErrors.password) setFieldErrors((p) => ({ ...p, password: undefined }));
                        }}
                        error={!!fieldErrors.password}
                        hint={fieldErrors.password}
                        disabled={isLoading}
                      />
                      <button
                        type="button"
                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute z-30 -translate-y-1/2 cursor-pointer right-3.5 top-1/2 p-1 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 transition-colors"
                      >
                        {showPassword ? (
                          <EyeIcon className="fill-current size-5" />
                        ) : (
                          <EyeCloseIcon className="fill-current size-5" />
                        )}
                      </button>
                    </div>
                  </div>

                  {/* Confirm New Password */}
                  <div>
                    <Label htmlFor="reset-confirm-password">
                      Confirm New Password <span className="text-error-500">*</span>
                    </Label>
                    <div className="relative">
                      <Input
                        id="reset-confirm-password"
                        name="confirmPassword"
                        type={showConfirmPassword ? 'text' : 'password'}
                        placeholder="Re-enter your new password"
                        value={confirmPassword}
                        onChange={(e) => {
                          setConfirmPassword(e.target.value);
                          if (fieldErrors.confirmPassword) {
                            setFieldErrors((p) => ({ ...p, confirmPassword: undefined }));
                          }
                        }}
                        error={!!fieldErrors.confirmPassword}
                        hint={fieldErrors.confirmPassword}
                        disabled={isLoading}
                      />
                      <button
                        type="button"
                        aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        className="absolute z-30 -translate-y-1/2 cursor-pointer right-3.5 top-1/2 p-1 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 transition-colors"
                      >
                        {showConfirmPassword ? (
                          <EyeIcon className="fill-current size-5" />
                        ) : (
                          <EyeCloseIcon className="fill-current size-5" />
                        )}
                      </button>
                    </div>
                  </div>

                  {/* Submit Button */}
                  <div className="pt-2">
                    <Button className="w-full font-medium" size="md" disabled={isLoading}>
                      {isLoading ? (
                        <span className="flex items-center justify-center gap-2">
                          <span className="size-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                          <span>Updating password...</span>
                        </span>
                      ) : (
                        'Reset Password'
                      )}
                    </Button>
                  </div>
                </div>
              </form>

              <div className="mt-6 pt-4 border-t border-gray-100 dark:border-gray-800">
                <p className="text-sm font-normal text-center text-gray-600 dark:text-gray-400 sm:text-start">
                  Need to request a new link?{' '}
                  <Link
                    to="/forgot-password"
                    className="text-brand-500 hover:text-brand-600 dark:text-brand-400 font-semibold transition-colors"
                  >
                    Forgot Password
                  </Link>
                </p>
              </div>
            </>
          )}
    </div>
  );
}
