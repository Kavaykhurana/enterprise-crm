import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Home, ChevronRight } from 'lucide-react';
import './Breadcrumb.css';

const Breadcrumb = () => {
  const location = useLocation();
  const pathnames = location.pathname.split('/').filter((x) => x);

  // Don't show breadcrumbs on login or root landing pages
  if (location.pathname === '/login' || location.pathname === '/') {
    return null;
  }

  return (
    <nav className="breadcrumb" aria-label="Breadcrumb">
      <Link to="/dashboard" className="breadcrumb-link home-link">
        <Home size={14} />
      </Link>
      {pathnames.map((value, index) => {
        const to = `/${pathnames.slice(0, index + 1).join('/')}`;
        const isLast = index === pathnames.length - 1;
        const displayName = value.charAt(0).toUpperCase() + value.slice(1);

        return (
          <React.Fragment key={to}>
            <ChevronRight size={12} className="breadcrumb-separator" />
            {isLast ? (
              <span className="breadcrumb-current">{displayName}</span>
            ) : (
              <Link to={to} className="breadcrumb-link">
                {displayName}
              </Link>
            )}
          </React.Fragment>
        );
      })}
    </nav>
  );
};

export default Breadcrumb;
