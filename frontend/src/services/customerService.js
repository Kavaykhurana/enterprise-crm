import api from './api';

const customerService = {
  createCustomer: async (customerData) => {
    return await api.post('/api/v1/customers', customerData);
  },

  updateCustomer: async (id, customerData) => {
    return await api.put(`/api/v1/customers/${id}`, customerData);
  },

  getCustomerById: async (id) => {
    return await api.get(`/api/v1/customers/${id}`);
  },

  searchCustomers: async (params = {}) => {
    return await api.get('/api/v1/customers', { params });
  },

  deleteCustomer: async (id) => {
    return await api.delete(`/api/v1/customers/${id}`);
  },

  restoreCustomer: async (id) => {
    return await api.put(`/api/v1/customers/${id}/restore`);
  },

  assignCustomer: async (id, assignedSalesRepId) => {
    return await api.put(`/api/v1/customers/${id}/assign`, { assignedSalesRepId });
  },
};

export default customerService;
