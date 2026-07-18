import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Briefcase,
  CheckSquare,
  History,
  User,
  Settings,
  LogOut,
  ChevronLeft,
  ChevronRight,
  ShieldCheck
} from 'lucide-react';
import './Sidebar.css';

const Sidebar = () => {
  const { user, logout, isAdmin } = useAuth();
  const [isCollapsed, setIsCollapsed] = useState(false);

  const toggleSidebar = () => {
    setIsCollapsed(!isCollapsed);
  };

  const navItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/customers', label: 'Customers', icon: Users },
    { to: '/leads', label: 'Leads', icon: Briefcase },
    { to: '/tasks', label: 'Tasks', icon: CheckSquare },
    { to: '/activity', label: 'Activity', icon: History },
    { to: '/profile', label: 'Profile', icon: User },
  ];

  if (isAdmin()) {
    navItems.push({ to: '/admin', label: 'Admin', icon: ShieldCheck });
  }

  return (
    <aside className={`sidebar ${isCollapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-header">
        {!isCollapsed && <span className="logo-text">Enterprise CRM</span>}
        <button className="collapse-btn" onClick={toggleSidebar}>
          {isCollapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
        </button>
      </div>

      <nav className="sidebar-nav">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-link-item ${isActive ? 'active' : ''}`}
            >
              <Icon size={20} className="nav-icon" />
              {!isCollapsed && <span className="nav-label">{item.label}</span>}
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <button className="logout-btn" onClick={logout}>
          <LogOut size={20} className="nav-icon" />
          {!isCollapsed && <span className="nav-label">Logout</span>}
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
