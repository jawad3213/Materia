import React, { useState } from 'react';
import useAuth from '../hooks/useAuth';
import { EyeIcon, EyeCloseIcon } from '../../../shared/icons';

export const ForceChangePasswordModal: React.FC = () => {
  const { user, changePassword, logout } = useAuth();

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // If user is not authenticated or does not need to change password, do not render
  if (!user || !user.mustChangePassword) {
    return null;
  }

  // Password requirements
  const hasMinLength = newPassword.length >= 8;
  const hasUppercase = /[A-Z]/.test(newPassword);
  const hasLowercase = /[a-z]/.test(newPassword);
  const hasNumberOrSpecial = /[0-9!@#$%^&*(),.?":{}|<>]/.test(newPassword);
  const isMatching = newPassword && confirmPassword && newPassword === confirmPassword;

  const isFormValid =
    hasMinLength &&
    hasUppercase &&
    hasLowercase &&
    hasNumberOrSpecial &&
    isMatching;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!hasMinLength) {
      setError('New password must be at least 8 characters long.');
      return;
    }

    if (!isMatching) {
      setError('New passwords do not match. Please verify.');
      return;
    }

    setIsSubmitting(true);
    try {
      const message = await changePassword({
        currentPassword: currentPassword.trim() || undefined,
        newPassword: newPassword.trim(),
        confirmPassword: confirmPassword.trim(),
      });

      setSuccessMessage(message || 'Password successfully updated!');
      // State in AuthContext automatically clears mustChangePassword
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.detail ||
        err.message ||
        'Failed to update password. Please check your credentials and try again.';
      setError(msg);
      setIsSubmitting(false);
    }
  };

  const handleSignOut = async () => {
    await logout();
  };

  return (
    <div className="fixed inset-0 z-[999999] flex items-center justify-center p-4 bg-gray-900/80 backdrop-blur-md animate-fadeIn">
      <div
        className="w-full max-w-lg overflow-hidden bg-white border border-gray-200 shadow-2xl rounded-2xl dark:bg-gray-900 dark:border-gray-800"
        role="dialog"
        aria-modal="true"
        aria-labelledby="first-login-modal-title"
      >
        {/* Header Ribbon */}
        <div className="relative p-6 border-b border-gray-100 bg-gradient-to-r from-blue-600 via-indigo-600 to-brand-600 dark:border-gray-800">
          <div className="flex items-center gap-4">
            <div className="flex items-center justify-center w-12 h-12 text-white bg-white/20 rounded-xl backdrop-blur-sm shrink-0">
              <svg
                className="w-6 h-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                />
              </svg>
            </div>
            <div>
              <span className="inline-block px-2.5 py-0.5 text-[10px] font-bold tracking-wider text-blue-100 uppercase bg-blue-500/30 rounded-full mb-1">
                First-Time Setup
              </span>
              <h2
                id="first-login-modal-title"
                className="text-lg font-bold text-white sm:text-xl"
              >
                Create Your New Password
              </h2>
            </div>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-6">
          {successMessage ? (
            <div className="py-8 text-center space-y-3">
              <div className="flex items-center justify-center w-16 h-16 mx-auto text-emerald-600 bg-emerald-100 rounded-full dark:bg-emerald-950/60 dark:text-emerald-400">
                <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                Password Successfully Updated!
              </h3>
              <p className="text-sm text-gray-500 dark:text-gray-400 max-w-sm mx-auto">
                Your account is now fully secured. You have immediate access to your workspace.
              </p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <p className="text-sm text-gray-600 dark:text-gray-300">
                Hello <strong className="font-semibold text-gray-900 dark:text-white">{user.name || user.email}</strong>,
                your account was provisioned by an administrator. Please set your new permanent password to continue.
              </p>

              {error && (
                <div className="p-3.5 text-sm text-red-700 bg-red-50 border border-red-200 rounded-xl dark:bg-red-950/40 dark:border-red-900/60 dark:text-red-300">
                  <div className="flex items-center gap-2">
                    <svg className="w-5 h-5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fillRule="evenodd"
                        d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                        clipRule="evenodd"
                      />
                    </svg>
                    <span>{error}</span>
                  </div>
                </div>
              )}

              {/* Current / Temporary Password */}
              <div>
                <label className="block mb-1.5 text-xs font-semibold text-gray-700 uppercase tracking-wider dark:text-gray-300">
                  Current / Temporary Password
                </label>
                <div className="relative">
                  <input
                    type={showCurrentPassword ? 'text' : 'password'}
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    placeholder="Enter the password from your email"
                    className="w-full h-11 px-3.5 pr-11 text-sm rounded-xl border border-gray-200 bg-gray-50/50 dark:bg-gray-800/50 dark:border-gray-700 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                  />
                  <button
                    type="button"
                    onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                    className="absolute inset-y-0 right-0 flex items-center px-3.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                    tabIndex={-1}
                  >
                    {showCurrentPassword ? (
                      <EyeCloseIcon className="w-5 h-5" />
                    ) : (
                      <EyeIcon className="w-5 h-5" />
                    )}
                  </button>
                </div>
              </div>

              {/* New Password */}
              <div>
                <label className="block mb-1.5 text-xs font-semibold text-gray-700 uppercase tracking-wider dark:text-gray-300">
                  New Personal Password
                </label>
                <div className="relative">
                  <input
                    type={showNewPassword ? 'text' : 'password'}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="At least 8 characters"
                    required
                    className="w-full h-11 px-3.5 pr-11 text-sm rounded-xl border border-gray-200 bg-gray-50/50 dark:bg-gray-800/50 dark:border-gray-700 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPassword(!showNewPassword)}
                    className="absolute inset-y-0 right-0 flex items-center px-3.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                    tabIndex={-1}
                  >
                    {showNewPassword ? (
                      <EyeCloseIcon className="w-5 h-5" />
                    ) : (
                      <EyeIcon className="w-5 h-5" />
                    )}
                  </button>
                </div>
              </div>

              {/* Confirm New Password */}
              <div>
                <label className="block mb-1.5 text-xs font-semibold text-gray-700 uppercase tracking-wider dark:text-gray-300">
                  Confirm New Password
                </label>
                <div className="relative">
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Repeat new password"
                    required
                    className="w-full h-11 px-3.5 pr-11 text-sm rounded-xl border border-gray-200 bg-gray-50/50 dark:bg-gray-800/50 dark:border-gray-700 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute inset-y-0 right-0 flex items-center px-3.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                    tabIndex={-1}
                  >
                    {showConfirmPassword ? (
                      <EyeCloseIcon className="w-5 h-5" />
                    ) : (
                      <EyeIcon className="w-5 h-5" />
                    )}
                  </button>
                </div>
              </div>

              {/* Password checklist */}
              <div className="p-3 bg-gray-50 rounded-xl dark:bg-gray-800/40 text-xs space-y-1.5 text-gray-500 dark:text-gray-400">
                <div className="flex items-center gap-2">
                  <span className={`w-1.5 h-1.5 rounded-full ${hasMinLength ? 'bg-emerald-500' : 'bg-gray-300 dark:bg-gray-600'}`} />
                  <span className={hasMinLength ? 'text-emerald-700 dark:text-emerald-400 font-medium' : ''}>
                    At least 8 characters
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`w-1.5 h-1.5 rounded-full ${hasUppercase && hasLowercase ? 'bg-emerald-500' : 'bg-gray-300 dark:bg-gray-600'}`} />
                  <span className={hasUppercase && hasLowercase ? 'text-emerald-700 dark:text-emerald-400 font-medium' : ''}>
                    Uppercase and lowercase letters
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`w-1.5 h-1.5 rounded-full ${hasNumberOrSpecial ? 'bg-emerald-500' : 'bg-gray-300 dark:bg-gray-600'}`} />
                  <span className={hasNumberOrSpecial ? 'text-emerald-700 dark:text-emerald-400 font-medium' : ''}>
                    At least one number or special character
                  </span>
                </div>
                {confirmPassword && (
                  <div className="flex items-center gap-2">
                    <span className={`w-1.5 h-1.5 rounded-full ${isMatching ? 'bg-emerald-500' : 'bg-red-500'}`} />
                    <span className={isMatching ? 'text-emerald-700 dark:text-emerald-400 font-medium' : 'text-red-600 dark:text-red-400 font-medium'}>
                      {isMatching ? 'Passwords match' : 'Passwords do not match'}
                    </span>
                  </div>
                )}
              </div>

              {/* Action Buttons */}
              <div className="pt-2 flex items-center justify-between gap-3">
                <button
                  type="button"
                  onClick={handleSignOut}
                  className="px-3.5 py-2 text-xs font-medium text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 transition-colors"
                >
                  Sign Out
                </button>

                <button
                  type="submit"
                  disabled={!isFormValid || isSubmitting}
                  className="flex items-center justify-center gap-2 px-6 py-2.5 text-sm font-semibold text-white bg-blue-600 rounded-xl hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md hover:shadow-blue-500/25"
                >
                  {isSubmitting ? (
                    <>
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                      <span>Updating...</span>
                    </>
                  ) : (
                    <span>Save Password & Continue</span>
                  )}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForceChangePasswordModal;
