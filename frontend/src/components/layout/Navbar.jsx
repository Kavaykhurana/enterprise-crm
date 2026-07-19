import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { Bell, Sun, Moon, Search, ChevronDown, User, LogOut } from 'lucide-react';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [showProfileDropdown, setShowProfileDropdown] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);

  const getInitials = (email) => {
    if (!email) return 'U';
    return email.substring(0, 2).toUpperCase();
  };

  return (
    <header className="navbar">
      <div className="navbar-search">
        <Search size={18} className="search-icon" />
        <input type="text" placeholder="Search customer, lead, or tasks..." className="search-input" />
      </div>

      <div className="navbar-actions">
        {/* Theme Toggle */}
        <button className="navbar-action-btn" onClick={toggleTheme} aria-label="Toggle Theme">
          {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
        </button>

        {/* Notifications */}
        <div className="notification-wrapper">
          <button
            className="navbar-action-btn"
            onClick={() => setShowNotifications(!showNotifications)}
            aria-label="Notifications"
          >
            <Bell size={20} />
            <span className="notification-badge">3</span>
          </button>

          {showNotifications && (
            <div className="notification-dropdown">
              <div className="dropdown-header">Notifications</div>
              <div className="dropdown-item">
                <p className="notif-text"><strong>Lead Converted</strong>: Acme Corp converted by John Doe.</p>
                <span className="notif-time">2 mins ago</span>
              </div>
              <div className="dropdown-item">
                <p className="notif-text"><strong>Task Assigned</strong>: Complete proposal assigned to you.</p>
                <span className="notif-time">1 hr ago</span>
              </div>
              <div className="dropdown-item">
                <p className="notif-text"><strong>System Warning</strong>: Account lockout attempt detected from 192.168.1.1.</p>
                <span className="notif-time">4 hrs ago</span>
              </div>
            </div>
          )}
        </div>

        {/* User Profile */}
        {user && (
          <div className="profile-dropdown-wrapper">
            <button className="profile-trigger" onClick={() => setShowProfileDropdown(!showProfileDropdown)}>
              <div className="profile-avatar">{getInitials(user.email)}</div>
              <div className="profile-info-text">
                <span className="profile-email">{user.email}</span>
                <span className="profile-role">{user.role}</span>
              </div>
              <ChevronDown size={16} />
            </button>

            {showProfileDropdown && (
              <div className="profile-dropdown">
                <div className="dropdown-user-header">
                  <p className="dropdown-username">{user.email}</p>
                  <p className="dropdown-userrole">{user.role}</p>
                </div>
                <a href="/profile" className="dropdown-link">
                  <User size={16} />
                  <span>My Profile</span>
                </a>
                <button className="dropdown-link logout-link" onClick={logout}>
                  <LogOut size={16} />
                  <span>Logout</span>
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
};

export default Navbar;
