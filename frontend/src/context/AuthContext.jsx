import React, { createContext, useContext, useEffect, useState } from 'react';
import authService from '../services/authService';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => authService.getCurrentUser());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Listen for silent token refresh failures or session timeouts from Axios
    const handleLogoutEvent = () => {
      setUser(null);
    };

    window.addEventListener('auth-logout', handleLogoutEvent);
    return () => {
      window.removeEventListener('auth-logout', handleLogoutEvent);
    };
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const currentUser = await authService.login(email, password);
      setUser(currentUser);
      return currentUser;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      await authService.logout();
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const isAdmin = () => user?.role === 'ADMIN';
  const isManager = () => user?.role === 'SALES_MANAGER';
  const isExecutive = () => user?.role === 'SALES_EXECUTIVE';
  const hasRole = (roles) => roles.includes(user?.role);

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        isAuthenticated: !!user,
        isAdmin,
        isManager,
        isExecutive,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
