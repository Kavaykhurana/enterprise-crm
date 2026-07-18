import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, user, hasRole } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    // Redirect to login page and preserve original path
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !hasRole(allowedRoles)) {
    // Redirect to unauthorized 403 route
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;
