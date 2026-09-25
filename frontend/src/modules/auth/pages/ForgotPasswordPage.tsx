import PageMeta from '../../../shared/components/common/PageMeta';
import AuthLayout from '../layout/AuthLayout';
import ForgotPasswordForm from '../components/ForgotPasswordForm';

export default function ForgotPasswordPage() {
  return (
    <AuthLayout backgroundImage="https://ik.imagekit.io/jaouad/resetpassword.jpg">
      <PageMeta
        title="Forgot Password | Materia Admin"
        description="Reset your password to regain access to your Materia account."
      />
      <ForgotPasswordForm />
    </AuthLayout>
  );
}
