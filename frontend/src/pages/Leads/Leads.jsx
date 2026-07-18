import React, { useEffect, useState } from 'react';
import leadService from '../../services/leadService';
import { useAuth } from '../../context/AuthContext';
import { Search, Plus, Edit2, Trash2, UserCheck, Shuffle, ChevronLeft, ChevronRight, CheckCircle, AlertCircle } from 'lucide-react';
import './Leads.css';

const Leads = () => {
  const { isAdmin, isManager } = useAuth();

  // State
  const [leads, setLeads] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [loading, setLoading] = useState(true);

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [showConvertModal, setShowConvertModal] = useState(false);
  const [selectedLead, setSelectedLead] = useState(null);

  // Lead Form
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [source, setSource] = useState('WEB');
  const [priority, setPriority] = useState('MEDIUM');
  const [status, setStatus] = useState('NEW');

  // Convert Form
  const [createOpportunity, setCreateOpportunity] = useState(false);
  const [oppName, setOppName] = useState('');
  const [oppRevenue, setOppRevenue] = useState(50000);
  const [oppProbability, setOppProbability] = useState(30);
  const [oppCloseDate, setOppCloseDate] = useState('');

  // General Form management
  const [assignedSalesRepId, setAssignedSalesRepId] = useState('');
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  useEffect(() => {
    fetchLeads();
  }, [page, statusFilter]);

  const fetchLeads = async () => {
    setLoading(true);
    try {
      const params = { page, size: 10, sortBy: 'createdAt', sortDir: 'desc' };
      if (search) params.email = search; // Search by email or last name in backend
      if (statusFilter) params.leadStatus = statusFilter;

      const response = await leadService.searchLeads(params);
      if (response.success) {
        setLeads(response.data.content);
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
    fetchLeads();
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to soft-delete this lead?')) {
      try {
        await leadService.deleteLead(id);
        fetchLeads();
      } catch (err) {
        alert('Deletion failed');
      }
    }
  };

  const openConvertModal = (lead) => {
    if (lead.status === 'CONVERTED') {
      alert('Lead is already converted.');
      return;
    }
    setSelectedLead(lead);
    setOppName(`${lead.companyName || lead.lastName} - License`);
    setOppCloseDate(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]); // Default 30 days out
    setShowConvertModal(true);
  };

  const handleCreateLead = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormLoading(true);

    try {
      const payload = { firstName, lastName, email, companyName, source, priority, status };
      const res = await leadService.createLead(payload);
      if (res.success) {
        setShowCreateModal(false);
        resetForm();
        fetchLeads();
      }
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create lead');
    } finally {
      setFormLoading(false);
    }
  };

  const handleConvertLead = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormLoading(true);

    try {
      const payload = {
        createOpportunity,
        opportunity: createOpportunity
          ? {
              name: oppName,
              expectedRevenue: oppRevenue,
              probability: oppProbability,
              expectedCloseDate: oppCloseDate,
            }
          : null,
      };

      const res = await leadService.convertLead(selectedLead.id, payload);
      if (res.success) {
        setShowConvertModal(false);
        resetForm();
        fetchLeads();
        alert('Lead successfully converted to Customer!');
      }
    } catch (err) {
      setFormError(err.response?.data?.message || 'Conversion transaction rolled back');
    } finally {
      setFormLoading(false);
    }
  };

  const handleAssignOwner = async (e) => {
    e.preventDefault();
    setFormLoading(true);
    setFormError('');

    try {
      await leadService.assignLead(selectedLead.id, assignedSalesRepId);
      setShowAssignModal(false);
      resetForm();
      fetchLeads();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Assignment failed');
    } finally {
      setFormLoading(false);
    }
  };

  const resetForm = () => {
    setFirstName('');
    setLastName('');
    setEmail('');
    setCompanyName('');
    setSource('WEB');
    setPriority('MEDIUM');
    setStatus('NEW');
    setCreateOpportunity(false);
    setOppName('');
    setOppRevenue(50000);
    setOppProbability(30);
    setAssignedSalesRepId('');
    setSelectedLead(null);
    setFormError('');
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Leads Pipeline</h1>
          <p className="page-subtitle">Qualify contacts, track sources, and convert opportunities</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
          <Plus size={16} />
          <span>Add Lead</span>
        </button>
      </div>

      {/* Query Filters */}
      <div className="card filter-card">
        <form onSubmit={handleSearchSubmit} className="filters-row">
          <div className="search-box-wrapper">
            <Search size={16} className="search-icon-inside" />
            <input
              type="text"
              placeholder="Search by prospect email..."
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
              <option value="NEW">New</option>
              <option value="CONTACTED">Contacted</option>
              <option value="QUALIFIED">Qualified</option>
              <option value="UNQUALIFIED">Unqualified</option>
              <option value="CONVERTED">Converted</option>
            </select>
          </div>
        </form>
      </div>

      {/* Table Data */}
      {loading ? (
        <div className="skeleton-loader-container">
          <div className="skeleton-line"></div>
          <div className="skeleton-line"></div>
        </div>
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Prospect Name</th>
                <th>Email</th>
                <th>Company</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Rep Assigned</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {leads.length > 0 ? (
                leads.map((lead) => (
                  <tr key={lead.id}>
                    <td><strong>{lead.firstName} {lead.lastName}</strong></td>
                    <td>{lead.email}</td>
                    <td>{lead.companyName || '--'}</td>
                    <td>
                      <span className={`badge ${lead.priority === 'HIGH' ? 'badge-danger' : lead.priority === 'MEDIUM' ? 'badge-warning' : 'badge-info'}`}>
                        {lead.priority}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${lead.status === 'CONVERTED' ? 'badge-success' : 'badge-info'}`}>
                        {lead.status}
                      </span>
                    </td>
                    <td>{lead.assignedSalesRepId ? lead.assignedSalesRepId.substring(0, 8) : 'Unassigned'}</td>
                    <td>
                      <div className="actions-cell">
                        {lead.status !== 'CONVERTED' && (
                          <button className="btn-action-convert" onClick={() => openConvertModal(lead)} title="Convert to Customer">
                            <Shuffle size={14} />
                            <span>Convert</span>
                          </button>
                        )}
                        {(isAdmin() || isManager()) && (
                          <button
                            className="icon-btn"
                            onClick={() => { setSelectedLead(lead); setShowAssignModal(true); }}
                            title="Assign Rep"
                          >
                            <UserCheck size={15} />
                          </button>
                        )}
                        <button className="icon-btn text-danger" onClick={() => handleDelete(lead.id)} title="Delete">
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="text-center">No leads found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination controls */}
      <div className="pagination-wrapper flex-between">
        <span className="pagination-text">Showing {leads.length} of {totalElements} leads</span>
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

      {/* CREATE LEAD MODAL */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3 className="modal-title">Create Prospect Lead</h3>
            {formError && <div className="form-error-alert">{formError}</div>}
            <form onSubmit={handleCreateLead}>
              <div className="form-row-double">
                <div className="form-group">
                  <label className="form-label">First Name</label>
                  <input type="text" className="form-control" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Last Name *</label>
                  <input type="text" required className="form-control" value={lastName} onChange={(e) => setLastName(e.target.value)} />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Email Address *</label>
                <input type="email" required className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Company Name</label>
                <input type="text" className="form-control" value={companyName} onChange={(e) => setCompanyName(e.target.value)} />
              </div>
              <div className="form-row-double">
                <div className="form-group">
                  <label className="form-label">Priority</label>
                  <select className="form-control" value={priority} onChange={(e) => setPriority(e.target.value)}>
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Lead Source</label>
                  <select className="form-control" value={source} onChange={(e) => setSource(e.target.value)}>
                    <option value="WEB">Direct Web</option>
                    <option value="REFERRAL">Referral</option>
                    <option value="CONFERENCE">Conference</option>
                    <option value="COLD_OUTBOUND">Outbound Campaign</option>
                  </select>
                </div>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowCreateModal(false); resetForm(); }}>Cancel</button>
                <button type="submit" disabled={formLoading} className="btn btn-primary">{formLoading ? 'Creating...' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* CONVERT LEAD TRANSACTIONAL MODAL */}
      {showConvertModal && (
        <div className="modal-overlay">
          <div className="modal-box convert-modal-box">
            <h3 className="modal-title">Convert Lead to Customer</h3>
            <p className="convert-intro-text">
              Converting <strong>{selectedLead?.firstName} {selectedLead?.lastName}</strong> will create a Customer account and map a Primary Contact.
            </p>
            {formError && <div className="form-error-alert">{formError}</div>}
            <form onSubmit={handleConvertLead}>
              <div className="form-group checkbox-group">
                <label className="remember-me">
                  <input
                    type="checkbox"
                    checked={createOpportunity}
                    onChange={(e) => setCreateOpportunity(e.target.checked)}
                  />
                  <span><strong>Create opportunity stub</strong> inside same transaction</span>
                </label>
              </div>

              {createOpportunity && (
                <div className="opportunity-fields-box animation-fade-in">
                  <div className="form-group">
                    <label className="form-label">Opportunity Deal Name *</label>
                    <input
                      type="text"
                      required={createOpportunity}
                      className="form-control"
                      value={oppName}
                      onChange={(e) => setOppName(e.target.value)}
                    />
                  </div>
                  <div className="form-row-double">
                    <div className="form-group">
                      <label className="form-label">Expected Revenue ($) *</label>
                      <input
                        type="number"
                        required={createOpportunity}
                        min="0"
                        className="form-control"
                        value={oppRevenue}
                        onChange={(e) => setOppRevenue(Number(e.target.value))}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Probability (%) *</label>
                      <input
                        type="number"
                        required={createOpportunity}
                        min="0"
                        max="100"
                        className="form-control"
                        value={oppProbability}
                        onChange={(e) => setOppProbability(Number(e.target.value))}
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Expected Close Date *</label>
                    <input
                      type="date"
                      required={createOpportunity}
                      className="form-control"
                      value={oppCloseDate}
                      onChange={(e) => setOppCloseDate(e.target.value)}
                    />
                  </div>
                </div>
              )}

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowConvertModal(false); resetForm(); }}>Cancel</button>
                <button type="submit" disabled={formLoading} className="btn btn-primary">
                  {formLoading ? 'Executing Transaction...' : 'Confirm Conversion'}
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
            <h3 className="modal-title">Transfer Lead Ownership</h3>
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

export default Leads;
