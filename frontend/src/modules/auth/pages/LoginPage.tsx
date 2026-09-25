import PageMeta from '../../../shared/components/common/PageMeta';
import AuthLayout from '../layout/AuthLayout';
import LoginForm from '../components/LoginForm';

export default function LoginPage() {
  return (
    <AuthLayout backgroundImage="https://ik.imagekit.io/jaouad/pexels-tiger-lily-4483772.jpg">
      <PageMeta
        title="Sign In | Materia Admin"
        description="Sign in to your Materia account to manage raw materials, suppliers, and categories."
      />
      <LoginForm />
    </AuthLayout>
  );
}
