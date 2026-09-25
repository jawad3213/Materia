import { AuthProvider } from './providers/AuthProvider';
import { ThemeProvider } from '../shared/context/ThemeContext';
import AppRoutes from './routes/AppRoutes';

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <div className="min-h-screen bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100">
          <AppRoutes />
        </div>
      </AuthProvider>
    </ThemeProvider>
  );
}
