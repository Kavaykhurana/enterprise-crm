import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { User, Shield, Key, Mail } from 'lucide-react';
import './Profile.css';

const Profile = () => {
  const { user } = useAuth();

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Profile</h1>
          <p className="page-subtitle">Manage your personal CRM identity and security settings</p>
        </div>
      </div>

      <div className="profile-grid">
        {/* Profile Card */}
        <div className="card profile-info-card">
          <div className="profile-hero">
            <div className="profile-avatar-large">
              {user?.email ? user.email.substring(0, 2).toUpperCase() : 'U'}
            </div>
            <h3>{user?.email}</h3>
            <span className="badge badge-info">{user?.role}</span>
          </div>

          <div className="profile-details-list">
            <div className="detail-row">
              <div className="detail-label-box">
                <Mail size={16} />
                <span>Email Address</span>
              </div>
              <span className="detail-value">{user?.email}</span>
            </div>
            <div className="detail-row">
              <div className="detail-label-box">
                <Shield size={16} />
                <span>Assigned Role</span>
              </div>
              <span className="detail-value">{user?.role}</span>
            </div>
            <div className="detail-row">
              <div className="detail-label-box">
                <Key size={16} />
                <span>User UUID</span>
              </div>
              <span className="detail-value font-code">{user?.userId || 'N/A'}</span>
            </div>
          </div>
        </div>

        {/* Security / Password placeholder panel */}
        <div className="card security-settings-card">
          <h3 className="card-title">Security & Credentials</h3>
          <p className="settings-desc">To update your password or request authorization bypass levels, please contact the System Administrator.</p>
          <div className="form-group">
            <label className="form-label">Active Session Status</label>
            <input type="text" disabled className="form-control" value="ACTIVE - AUTHORIZED" />
          </div>
          <div className="form-group">
            <label className="form-label">Client IP Address</label>
            <input type="text" disabled className="form-control" value="127.0.0.1 (Localhost)" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
