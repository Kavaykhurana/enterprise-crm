import api from './api';

const dashboardService = {
  getMetrics: async () => {
    return await api.get('/api/v1/dashboard/metrics');
  },
};

export default dashboardService;
