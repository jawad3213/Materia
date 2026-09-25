import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { EyeCloseIcon, EyeIcon } from '../../../shared/icons';
import Label from '../../../shared/components/form/Label';
import Input from '../../../shared/components/form/input/InputField';
import Checkbox from '../../../shared/components/form/input/Checkbox';
import Button from '../../../shared/components/ui/button/Button';
import useAuth from '../hooks/useAuth';
import authService from '../services/authService';

export default function LoginForm() {
  const navigate = useNavigate();
  const { login, isLoading, error, clearError } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({});
  const [socialLoading, setSocialLoading] = useState<string | null>(null);

  // Restore remembered email on mount if previously saved
  useEffect(() => {
    const savedEmail = authService.getRememberedEmail();
    if (savedEmail) {
      setEmail(savedEmail);
      setRememberMe(true);
    }
  }, []);

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
    if (fieldErrors.email) {
      setFieldErrors((prev) => ({ ...prev, email: undefined }));
    }
    if (error) clearError();
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
    if (fieldErrors.password) {
      setFieldErrors((prev) => ({ ...prev, password: undefined }));
    }
    if (error) clearError();
  };

  const validate = (): boolean => {
    const errors: { email?: string; password?: string } = {};
    const trimmedEmail = email.trim();

    if (!trimmedEmail) {
      errors.email = 'Please enter your email address.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
      errors.email = 'Please enter a valid email address (e.g. info@gmail.com).';
    }

    if (!password) {
      errors.password = 'Please enter your password.';
    } else if (password.length < 6) {
      errors.password = 'Password must be at least 6 characters.';
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

    // Persist or clear remembered email
    if (rememberMe) {
      authService.setRememberedEmail(email.trim());
    } else {
      authService.setRememberedEmail(null);
    }

    try {
      await login({ email: email.trim(), password, rememberMe });
      navigate('/materials');
    } catch {
      // Error handled in AuthContext
    }
  };

  const handleSocialLogin = async (provider: string) => {
    try {
      setSocialLoading(provider);
      clearError();
      await login({
        email: `${provider.toLowerCase()}user@materia.com`,
        password: 'social-password',
        rememberMe,
      });
      navigate('/materials');
    } catch {
      // Error handled in AuthContext
    } finally {
      setSocialLoading(null);
    }
  };

  const isSubmitting = isLoading || !!socialLoading;

  return (
    <div className="w-full">
      {/* Title & Subtitle matching screenshot */}
      <div className="mb-6 sm:mb-8">
        <h1 className="mb-2 text-2xl sm:text-3xl font-bold tracking-tight text-gray-900 dark:text-white">
          Sign In
        </h1>
        <p className="text-sm sm:text-base text-gray-500 dark:text-gray-400">
          Enter your email and password to sign in!
        </p>
      </div>

      {/* API Error Banner */}
      {error && (
        <div className="mb-5 p-3.5 text-sm text-red-700 bg-red-50 rounded-xl border border-red-200 dark:bg-red-950/40 dark:border-red-800 dark:text-red-400 flex items-start gap-2">
          <svg className="size-5 shrink-0 text-red-500 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clipRule="evenodd" />
          </svg>
          <span>{error}</span>
        </div>
      )}

      {/* Social Logins matching screenshot */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
        <button
          type="button"
          onClick={() => handleSocialLogin('Google')}
          disabled={isSubmitting}
          className="inline-flex items-center justify-center gap-3 py-3 px-5 text-sm font-medium text-gray-700 transition-all bg-gray-100/80 rounded-xl hover:bg-gray-200 hover:text-gray-900 disabled:opacity-60 disabled:cursor-not-allowed dark:bg-white/5 dark:text-white/90 dark:hover:bg-white/10 border border-transparent dark:border-white/5"
        >
          {socialLoading === 'Google' ? (
            <span className="size-4 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
          ) : (
            <svg width="18" height="18" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18.7511 10.1944C18.7511 9.47495 18.6915 8.94995 18.5626 8.40552H10.1797V11.6527H15.1003C15.0011 12.4597 14.4654 13.675 13.2749 14.4916L13.2582 14.6003L15.9087 16.6126L16.0924 16.6305C17.7788 15.1041 18.7511 12.8583 18.7511 10.1944Z" fill="#4285F4" />
              <path d="M10.1788 18.75C12.5895 18.75 14.6133 17.9722 16.0915 16.6305L13.274 14.4916C12.5201 15.0068 11.5081 15.3666 10.1788 15.3666C7.81773 15.3666 5.81379 13.8402 5.09944 11.7305L4.99473 11.7392L2.23868 13.8295L2.20264 13.9277C3.67087 16.786 6.68674 18.75 10.1788 18.75Z" fill="#34A853" />
              <path d="M5.10014 11.7305C4.91165 11.186 4.80257 10.6027 4.80257 9.99992C4.80257 9.3971 4.91165 8.81379 5.09022 8.26935L5.08523 8.1534L2.29464 6.02954L2.20333 6.0721C1.5982 7.25823 1.25098 8.5902 1.25098 9.99992C1.25098 11.4096 1.5982 12.7415 2.20333 13.9277L5.10014 11.7305Z" fill="#FBBC05" />
              <path d="M10.1789 4.63331C11.8554 4.63331 12.9864 5.34303 13.6312 5.93612L16.1511 3.525C14.6035 2.11528 12.5895 1.25 10.1789 1.25C6.68676 1.25 3.67088 3.21387 2.20264 6.07218L5.08953 8.26943C5.81381 6.15972 7.81776 4.63331 10.1789 4.63331Z" fill="#EB4335" />
            </svg>
          )}
          <span>Sign in with Google</span>
        </button>

        <button
          type="button"
          onClick={() => handleSocialLogin('X')}
          disabled={isSubmitting}
          className="inline-flex items-center justify-center gap-3 py-3 px-5 text-sm font-medium text-gray-700 transition-all bg-gray-100/80 rounded-xl hover:bg-gray-200 hover:text-gray-900 disabled:opacity-60 disabled:cursor-not-allowed dark:bg-white/5 dark:text-white/90 dark:hover:bg-white/10 border border-transparent dark:border-white/5"
        >
          {socialLoading === 'X' ? (
            <span className="size-4 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
          ) : (
            <svg width="17" height="17" viewBox="0 0 21 20" fill="none" xmlns="http://www.w3.org/2000/svg" className="fill-current text-gray-800 dark:text-white/90">
              <path d="M15.6705 1.875H18.4272L12.4047 8.75833L19.4897 18.125H13.9422L9.59717 12.4442L4.62554 18.125H1.86721L8.30887 10.7625L1.51221 1.875H7.20054L11.128 7.0675L15.6705 1.875ZM14.703 16.475H16.2305L6.37054 3.43833H4.73137L14.703 16.475Z" />
            </svg>
          )}
          <span>Sign in with X</span>
        </button>
      </div>

      {/* Or divider matching screenshot */}
      <div className="relative py-4 sm:py-5">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-200 dark:border-gray-800"></div>
        </div>
        <div className="relative flex justify-center text-xs">
          <span className="px-3 text-gray-400 bg-white dark:bg-gray-900">
            Or
          </span>
        </div>
      </div>

      {/* Form Fields matching screenshot */}
      <form onSubmit={handleSubmit} noValidate>
        <div className="space-y-4">
          <div>
            <Label htmlFor="login-email">
              Email<span className="text-error-500">*</span>
            </Label>
            <Input
              id="login-email"
              name="email"
              type="email"
              placeholder="info@gmail.com"
              value={email}
              onChange={handleEmailChange}
              error={!!fieldErrors.email}
              hint={fieldErrors.email}
              disabled={isSubmitting}
            />
          </div>

          <div>
            <Label htmlFor="login-password">
              Password<span className="text-error-500">*</span>
            </Label>
            <div className="relative">
              <Input
                id="login-password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                value={password}
                onChange={handlePasswordChange}
                error={!!fieldErrors.password}
                hint={fieldErrors.password}
                disabled={isSubmitting}
              />
              <button
                type="button"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                onClick={() => setShowPassword(!showPassword)}
                className="absolute z-30 -translate-y-1/2 cursor-pointer right-3.5 top-1/2 p-1 text-gray-400 hover:text-gray-700 dark:text-gray-500 dark:hover:text-gray-300 transition-colors"
              >
                {showPassword ? (
                  <EyeIcon className="fill-current size-5" />
                ) : (
                  <EyeCloseIcon className="fill-current size-5" />
                )}
              </button>
            </div>
          </div>

          <div className="flex items-center justify-between pt-1">
            <div className="flex items-center gap-2.5">
              <Checkbox
                id="login-remember-me"
                checked={rememberMe}
                onChange={setRememberMe}
              />
              <label
                htmlFor="login-remember-me"
                className="text-sm font-normal text-gray-600 select-none cursor-pointer dark:text-gray-400"
              >
                Keep me logged in
              </label>
            </div>

            <Link
              to="/forgot-password"
              className="text-sm font-medium text-brand-500 hover:text-brand-600 dark:text-brand-400 transition-colors"
            >
              Forgot password?
            </Link>
          </div>

          <div className="pt-2">
            <Button
              className="w-full font-medium py-3.5 rounded-xl text-base shadow-theme-xs"
              size="md"
              disabled={isSubmitting}
            >
              {isLoading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="size-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Signing in...</span>
                </span>
              ) : (
                'Sign In'
              )}
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
}
