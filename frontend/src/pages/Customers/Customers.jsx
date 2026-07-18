import React, { useEffect, useState } from 'react';
import customerService from '../../services/customerService';
import { useAuth } from '../../context/AuthContext';
import { Search, Filter, Plus, Edit2, Trash2, RefreshCw, ChevronLeft, ChevronRight, UserCheck } from 'lucide-react';
import './Customers.css';

const Customers = () => {
  const { isAdmin, isManager } = useAuth();
  
  // State for Customer Listing
  const [customers, setCustomers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [sizeFilter, setSizeFilter] = useState('');
  const [loading, setLoading] = useState(true);

  // Modals state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState(null);

  // Form states
  const [companyName, setCompanyName] = useState('');
  const [taxIdentifier, setTaxIdentifier] = useState('');
  const [companySize, setCompanySize] = useState('SMALL');
  const [customerStatus, setCustomerStatus] = useState('ACTIVE');
  const [assignedSalesRepId, setAssignedSalesRepId] = useState('');
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  useEffect(() => {
    fetchCustomers();
  }, [page, sortBy, sortDir, statusFilter, sizeFilter]);

  const fetchCustomers = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 10,
        sortBy,
        sortDir,
      };
      if (search) params.companyName = search;
      if (statusFilter) params.customerStatus = statusFilter;
      if (sizeFilter) params.companySize = sizeFilter;

      const response = await customerService.searchCustomers(params);
      if (response.success) {
        setCustomers(response.data.content);
        setTotalPages(response.data.totalPages);
        setTotalElements(response.data.totalElements);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchCustomers();
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to soft-delete this customer?')) {
      try {
        await customerService.deleteCustomer(id);
        fetchCustomers();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete customer');
      }
    }
  };

  const handleRestore = async (id) => {
    try {
      await customerService.restoreCustomer(id);
      fetchCustomers();
      alert('Customer restored successfully');
    } catch (err) {
      alert(err.response?.data?.message || 'Restore failed due to conflict');
    }
  };

  const openEditModal = (cust) => {
    setSelectedCustomer(cust);
    setCompanyName(cust.companyName);
    setTaxIdentifier(cust.taxIdentifier || '');
    setCompanySize(cust.companySize);
    setCustomerStatus(cust.customerStatus);
    setShowEditModal(true);
  };

  const handleCreateCustomer = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormLoading(true);

    try {
      const payload = { companyName, taxIdentifier, companySize, customerStatus };
      const res = await customerService.createCustomer(payload);
      if (res.success) {
        setShowCreateModal(false);
        resetForm();
        fetchCustomers();
      }
    } catch (err) {
      setFormError(err.response?.data?.message || 'Validation failed');
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateCustomer = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormLoading(true);

    try {
      const payload = { companyName, taxIdentifier, companySize, customerStatus };
      const res = await customerService.updateCustomer(selectedCustomer.id, payload);
      if (res.success) {
        setShowEditModal(false);
        resetForm();
        fetchCustomers();
      }
    } catch (err) {
      setFormError(err.response?.data?.message || 'Validation failed');
    } finally {
      setFormLoading(false);
    }
  };

  const handleAssignOwner = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');

    try {
      await customerService.assignCustomer(selectedCustomer.id, assignedSalesRepId);
      setShowAssignModal(false);
      resetForm();
      fetchCustomers();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Assignment failed');
    } finally {
      setFormLoading(false);
    }
  };

  const resetForm = () => {
    setCompanyName('');
    setTaxIdentifier('');
    setCompanySize('SMALL');
    setCustomerStatus('ACTIVE');
    setAssignedSalesRepId('');
    setSelectedCustomer(null);
    setFormError('');
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Customers</h1>
          <p className="page-subtitle">Manage company details, tax records, and accounts</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
          <Plus size={16} />
          <span>Add Customer</span>
        </button>
      </div>

      {/* Query Filters */}
      <div className="card filter-card">
        <form onSubmit={handleSearchSubmit} className="filters-row">
          <div className="search-box-wrapper">
            <Search size={16} className="search-icon-inside" />
            <input
              type="text"
              placeholder="Search by company name..."
              className="form-control search-input-field"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="select-filters">
            <select
              className="form-control select-filter"
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>

            <select
              className="form-control select-filter"
              value={sizeFilter}
              onChange={(e) => { setSizeFilter(e.target.value); setPage(0); }}
            >
              <option value="">All Sizes</option>
              <option value="SMALL">Small</option>
              <option value="MEDIUM">Medium</option>
              <option value="ENTERPRISE">Enterprise</option>
            </select>
          </div>
        </form>
      </div>

      {/* Table Data */}
      {loading ? (
        <div className="skeleton-loader-container">
          <div className="skeleton-line"></div>
          <div className="skeleton-line"></div>
          <div className="skeleton-line"></div>
        </div>
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Company Name</th>
                <th>Tax Identifier</th>
                <th>Company Size</th>
                <th>Status</th>
                <th>Owner (Rep)</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {customers.length > 0 ? (
                customers.map((cust) => (
                  <tr key={cust.id}>
                    <td><strong>{cust.companyName}</strong></td>
                    <td>{cust.taxIdentifier || '--'}</td>
                    <td><span className="size-indicator">{cust.companySize}</span></td>
                    <td>
                      <span className={`badge ${cust.customerStatus === 'ACTIVE' ? 'badge-success' : 'badge-danger'}`}>
                        {cust.customerStatus}
                      </span>
                    </td>
                    <td>{cust.assignedSalesRepId ? cust.assignedSalesRepId.substring(0, 8) : 'Unassigned'}</td>
                    <td>
                      <div className="actions-cell">
                        <button className="icon-btn" onClick={() => openEditModal(cust)} title="Edit">
                          <Edit2 size={15} />
                        </button>
                        {(isAdmin() || isManager()) && (
                          <button
                            className="icon-btn"
                            onClick={() => { setSelectedCustomer(cust); setShowAssignModal(true); }}
                            title="Assign Rep"
                          >
                            <UserCheck size={15} />
                          </button>
                        )}
                        <button className="icon-btn text-danger" onClick={() => handleDelete(cust.id)} title="Delete">
                          <Trash2 size={15} />
                        </button>
                        {cust.deletedAt && (
                          <button className="icon-btn text-success" onClick={() => handleRestore(cust.id)} title="Restore">
                            <RefreshCw size={15} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="text-center">No customers found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination controls */}
      <div className="pagination-wrapper flex-between">
        <span className="pagination-text">Showing {customers.length} of {totalElements} customers</span>
        <div className="pagination-actions">
          <button className="btn btn-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>
            <ChevronLeft size={16} />
            <span>Previous</span>
          </button>
          <span className="page-num">Page {page + 1} of {totalPages || 1}</span>
          <button className="btn btn-secondary" disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>
            <span>Next</span>
            <ChevronRight size={16} />
          </button>
        </div>
      </div>

      {/* CREATE MODAL */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3 className="modal-title">Create Customer</h3>
            {formError && <div className="form-error-alert">{formError}</div>}
            <form onSubmit={handleCreateCustomer}>
              <div className="form-group">
                <label className="form-label">Company Name *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Tax Identifier</label>
                <input
                  type="text"
                  className="form-control"
                  value={taxIdentifier}
                  onChange={(e) => setTaxIdentifier(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Company Size</label>
                <select className="form-control" value={companySize} onChange={(e) => setCompanySize(e.target.value)}>
                  <option value="SMALL">Small</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="ENTERPRISE">Enterprise</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Customer Status</label>
                <select className="form-control" value={customerStatus} onChange={(e) => setCustomerStatus(e.target.value)}>
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowCreateModal(false); resetForm(); }}>Cancel</button>
                <button type="submit" disabled={formLoading} className="btn btn-primary">
                  {formLoading ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {showEditModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3 className="modal-title">Edit Customer</h3>
            {formError && <div className="form-error-alert">{formError}</div>}
            <form onSubmit={handleUpdateCustomer}>
              <div className="form-group">
                <label className="form-label">Company Name *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Tax Identifier</label>
                <input
                  type="text"
                  className="form-control"
                  value={taxIdentifier}
                  onChange={(e) => setTaxIdentifier(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Company Size</label>
                <select className="form-control" value={companySize} onChange={(e) => setCompanySize(e.target.value)}>
                  <option value="SMALL">Small</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="ENTERPRISE">Enterprise</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Customer Status</label>
                <select className="form-control" value={customerStatus} onChange={(e) => setCustomerStatus(e.target.value)}>
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowEditModal(false); resetForm(); }}>Cancel</button>
                <button type="submit" disabled={formLoading} className="btn btn-primary">
                  {formLoading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ASSIGN OWNER MODAL */}
      {showAssignModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3 className="modal-title">Transfer Customer Ownership</h3>
            {formError && <div className="form-error-alert">{formError}</div>}
            <form onSubmit={handleAssignOwner}>
              <div className="form-group">
                <label className="form-label">Sales Representative UUID *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="Enter User UUID..."
                  value={assignedSalesRepId}
                  onChange={(e) => setAssignedSalesRepId(e.target.value)}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowAssignModal(false); resetForm(); }}>Cancel</button>
                <button type="submit" disabled={formLoading} className="btn btn-primary">
                  {formLoading ? 'Reassigning...' : 'Reassign'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Customers;
