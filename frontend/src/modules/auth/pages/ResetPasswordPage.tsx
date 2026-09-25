import { useSearchParams } from 'react-router-dom';
import PageMeta from '../../../shared/components/common/PageMeta';
import AuthLayout from '../layout/AuthLayout';
import ResetPasswordForm from '../components/ResetPasswordForm';
import NotFoundPage from '../../../shared/pages/NotFoundPage';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  // If the user visits /reset-password directly without a token parameter, show the 404 page
  if (!token || !token.trim()) {
    return <NotFoundPage />;
  }

  return (
    <AuthLayout backgroundImage="https://ik.imagekit.io/jaouad/resetpassword.jpg">
      <PageMeta
        title="Reset Password | Materia Admin"
        description="Choose a new secure password for your Materia account."
      />
      <ResetPasswordForm />
    </AuthLayout>
  );
}
