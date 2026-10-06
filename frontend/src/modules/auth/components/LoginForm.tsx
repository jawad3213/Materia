import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { EyeCloseIcon, EyeIcon } from '../../../shared/icons';
import Label from '../../../shared/components/form/Label';
import Input from '../../../shared/components/form/input/InputField';
import Checkbox from '../../../shared/components/form/input/Checkbox';
import Button from '../../../shared/components/ui/button/Button';
import useAuth from '../hooks/useAuth';
import authService from '../services/authService';

const DEV_TEST_ACCOUNTS = [
  {
    role: 'Admin',
    email: 'admin@materia.com',
    password: 'admin123',
    badgeColor: 'bg-purple-50 text-purple-700 border-purple-200 hover:bg-purple-100 dark:bg-purple-950/40 dark:text-purple-300 dark:border-purple-800',
    dotColor: 'bg-purple-500',
  },
  {
    role: 'Purchaser',
    email: 'purchaser@materia.com',
    password: 'user123',
    badgeColor: 'bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100 dark:bg-blue-950/40 dark:text-blue-300 dark:border-blue-800',
    dotColor: 'bg-blue-500',
  },
  {
    role: 'Receiver',
    email: 'receiver@materia.com',
    password: 'user123',
    badgeColor: 'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800',
    dotColor: 'bg-emerald-500',
  },
];

export default function LoginForm() {
  const navigate = useNavigate();
  const { login, isLoading, error, clearError } = useAuth();

  // A remembered email is restored when the form opens.
  const [email, setEmail] = useState(() => authService.getRememberedEmail() || '');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(
    () => !!authService.getRememberedEmail() || localStorage.getItem('materia_remember_me') !== 'false'
  );
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({});
  const [activeDevRole, setActiveDevRole] = useState<string | null>(null);

  const handleSelectTestAccount = (acc: typeof DEV_TEST_ACCOUNTS[0]) => {
    setEmail(acc.email);
    setPassword(acc.password);
    setActiveDevRole(acc.role);
    setFieldErrors({});
    if (error) clearError();
    try {
      navigator.clipboard.writeText(`${acc.email} | ${acc.password}`);
    } catch {
      // ignore clipboard error
    }
  };

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

  const isSubmitting = isLoading;

  return (
    <div className="w-full">
      {/* Title & Subtitle */}
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

      {/* Dev Mode Test Account Chips */}
      <div className="mb-5 p-3 rounded-2xl bg-gray-50/90 dark:bg-white/[0.03] border border-dashed border-gray-200 dark:border-gray-800">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[11px] font-semibold uppercase tracking-wider text-gray-400 dark:text-gray-500 flex items-center gap-1.5">
            <span className="inline-block w-2 h-2 rounded-full bg-amber-400 animate-pulse"></span>
            Dev Test Accounts
          </span>
          {activeDevRole && (
            <span className="text-[11px] text-green-600 dark:text-green-400 font-medium">
              Filled {activeDevRole}!
            </span>
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          {DEV_TEST_ACCOUNTS.map((acc) => {
            const isSelected = activeDevRole === acc.role;
            return (
              <button
                key={acc.role}
                type="button"
                onClick={() => handleSelectTestAccount(acc)}
                title={`Click to fill ${acc.role} (${acc.email})`}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all cursor-pointer shadow-xs active:scale-95 ${
                  acc.badgeColor
                } ${isSelected ? 'ring-2 ring-brand-500 ring-offset-1' : ''}`}
              >
                <span className={`w-1.5 h-1.5 rounded-full ${acc.dotColor}`} />
                <span>{acc.role}</span>
                <span className="text-[10px] opacity-60 font-mono">({acc.password})</span>
              </button>
            );
          })}
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
              type="submit"
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
