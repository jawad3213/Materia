import { Routes, Route } from 'react-router-dom';
import UsersPage from './pages/UsersPage';
import CreateUserPage from './pages/CreateUserPage';
import UpdateUserPage from './pages/UpdateUserPage';
import UserDetailPage from './pages/UserDetailPage';

export default function UsersRoutes() {
  return (
    <Routes>
      <Route path="" element={<UsersPage />} />
      <Route path="list" element={<UsersPage />} />
      <Route path="create" element={<CreateUserPage />} />
      <Route path="create-user" element={<CreateUserPage />} />
      <Route path="onboard" element={<CreateUserPage />} />
      <Route path="edit/:id" element={<UpdateUserPage />} />
      <Route path="view/:id" element={<UserDetailPage />} />
      <Route path=":id" element={<UserDetailPage />} />
    </Routes>
  );
}
