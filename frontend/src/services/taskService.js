import api from './api';

const taskService = {
  createTask: async (taskData) => {
    return await api.post('/api/v1/tasks', taskData);
  },

  updateTask: async (id, taskData) => {
    return await api.put(`/api/v1/tasks/${id}`, taskData);
  },

  getTaskById: async (id) => {
    return await api.get(`/api/v1/tasks/${id}`);
  },

  deleteTask: async (id) => {
    return await api.delete(`/api/v1/tasks/${id}`);
  },

  addComment: async (taskId, content) => {
    return await api.post(`/api/v1/tasks/${taskId}/comments`, { content });
  },

  getComments: async (taskId) => {
    return await api.get(`/api/v1/tasks/${taskId}/comments`);
  },

  getTasksBySalesRep: async (assignedUserId) => {
    // Standard fetch (filtered by assigned sales rep)
    return await api.get('/api/v1/tasks', { params: { assignedUserId } });
  },
};

export default taskService;
