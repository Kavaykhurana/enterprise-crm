import React from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';
import './NotFound.css';

const NotFound = ({ type = '404' }) => {
  const is403 = type === '403';

  return (
    <div className="error-page-container">
      <div className="card error-page-card">
        <AlertCircle size={64} className={is403 ? 'text-danger' : 'text-warning'} />
        <h1 className="error-title">{is403 ? '403 - Forbidden' : '404 - Not Found'}</h1>
        <p className="error-message">
          {is403
            ? "You do not have the required access permissions to view this resource. Please contact your manager."
            : "The page you are looking for does not exist or has been relocated to another workspace."}
        </p>
        <Link to="/dashboard" className="btn btn-primary">
          Return to Dashboard
        </Link>
      </div>
    </div>
  );
};

export default NotFound;
