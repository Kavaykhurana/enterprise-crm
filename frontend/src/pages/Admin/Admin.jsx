import React, { useState } from 'react';
import authService from '../../services/authService';
import { UserPlus, Shield, User, Key, CheckCircle, AlertCircle } from 'lucide-react';
import './Admin.css';

const Admin = () => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('SALES_EXECUTIVE');
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const payload = { firstName, lastName, email, password, role };
      const response = await authService.register(payload);
      if (response.success) {
        setSuccess(`Successfully registered ${email} as ${role}!`);
        resetForm();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to register user account');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFirstName('');
    setLastName('');
    setEmail('');
    setPassword('');
    setRole('SALES_EXECUTIVE');
  };

  // Mock list of system-wide registered accounts for visual listing
  const systemUsers = [
    { email: 'admin@company.com', name: 'Super Admin', role: 'ADMIN', status: 'ACTIVE' },
    { email: 'mgr@example.com', name: 'Mgr User', role: 'SALES_MANAGER', status: 'ACTIVE' },
    { email: 'repa@example.com', name: 'Rep A', role: 'SALES_EXECUTIVE', status: 'ACTIVE' },
    { email: 'repb@example.com', name: 'Rep B', role: 'SALES_EXECUTIVE', status: 'ACTIVE' },
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Admin Console</h1>
          <p className="page-subtitle">Configure system users, control RBAC credentials, and monitor access</p>
        </div>
      </div>

      <div className="admin-grid">
        {/* User Registration Form */}
        <div className="card admin-form-card">
          <h3 className="card-title flex-between">
            <span>Register New System User</span>
            <UserPlus size={18} className="text-muted" />
          </h3>
          
          {success && (
            <div className="success-alert">
              <CheckCircle size={16} />
              <span>{success}</span>
            </div>
          )}

          {error && (
            <div className="error-alert">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleRegister}>
            <div className="form-row-double">
              <div className="form-group">
                <label className="form-label">First Name *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Last Name *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Email Address *</label>
              <input
                type="email"
                required
                className="form-control"
                placeholder="user@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password *</label>
              <input
                type="password"
                required
                className="form-control"
                placeholder="Minimum 8 characters..."
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">System Access Role *</label>
              <select className="form-control" value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="SALES_EXECUTIVE">Sales Executive (Restricted Access)</option>
                <option value="SALES_MANAGER">Sales Manager (Global Read/Write)</option>
                <option value="ADMIN">System Administrator (Full Bypass)</option>
              </select>
            </div>

            <button type="submit" disabled={loading} className="btn btn-primary btn-block">
              {loading ? 'Registering Account...' : 'Register User'}
            </button>
          </form>
        </div>

        {/* System accounts list */}
        <div className="card admin-users-list">
          <h3 className="card-title">Registered Accounts</h3>
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {systemUsers.map((u, idx) => (
                  <tr key={idx}>
                    <td><strong>{u.name}</strong></td>
                    <td>{u.email}</td>
                    <td>
                      <span className={`badge ${u.role === 'ADMIN' ? 'badge-danger' : u.role === 'SALES_MANAGER' ? 'badge-warning' : 'badge-info'}`}>
                        {u.role}
                      </span>
                    </td>
                    <td><span className="badge badge-success">{u.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Admin;
