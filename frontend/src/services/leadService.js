import api from './api';

const leadService = {
  createLead: async (leadData) => {
    return await api.post('/api/v1/leads', leadData);
  },

  updateLead: async (id, leadData) => {
    return await api.put(`/api/v1/leads/${id}`, leadData);
  },

  getLeadById: async (id) => {
    return await api.get(`/api/v1/leads/${id}`);
  },

  searchLeads: async (params = {}) => {
    return await api.get('/api/v1/leads', { params });
  },

  updateLeadStatus: async (id, status) => {
    return await api.put(`/api/v1/leads/${id}/status`, null, { params: { status } });
  },

  deleteLead: async (id) => {
    return await api.delete(`/api/v1/leads/${id}`);
  },

  restoreLead: async (id) => {
    return await api.put(`/api/v1/leads/${id}/restore`);
  },

  assignLead: async (id, assignedSalesRepId) => {
    return await api.put(`/api/v1/leads/${id}/assign`, { assignedSalesRepId });
  },

  convertLead: async (id, conversionData) => {
    return await api.post(`/api/v1/leads/${id}/convert`, conversionData);
  },
};

export default leadService;
