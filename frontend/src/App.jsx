import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import Sidebar from './components/layout/Sidebar';
import Navbar from './components/layout/Navbar';
import Breadcrumb from './components/layout/Breadcrumb';
import ProtectedRoute from './components/layout/ProtectedRoute';

// Pages
import Login from './pages/Login/Login';
import Dashboard from './pages/Dashboard/Dashboard';
import Customers from './pages/Customers/Customers';
import Leads from './pages/Leads/Leads';
import Tasks from './pages/Tasks/Tasks';
import Activity from './pages/Activity/Activity';
import Profile from './pages/Profile/Profile';
import Admin from './pages/Admin/Admin';
import NotFound from './pages/NotFound/NotFound';

// Styles
import './styles/global.css';

const AppLayout = () => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar />
        <Breadcrumb />
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/customers"
            element={
              <ProtectedRoute>
                <Customers />
              </ProtectedRoute>
            }
          />
          <Route
            path="/leads"
            element={
              <ProtectedRoute>
                <Leads />
              </ProtectedRoute>
            }
          />
          <Route
            path="/tasks"
            element={
              <ProtectedRoute>
                <Tasks />
              </ProtectedRoute>
            }
          />
          <Route
            path="/activity"
            element={
              <ProtectedRoute>
                <Activity />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <Admin />
              </ProtectedRoute>
            }
          />
          <Route path="/unauthorized" element={<NotFound type="403" />} />
          <Route path="*" element={<NotFound type="404" />} />
        </Routes>
      </div>
    </div>
  );
};

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <AppLayout />
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
