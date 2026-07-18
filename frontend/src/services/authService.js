import api from './api';

const authService = {
  login: async (email, password) => {
    const response = await api.post('/api/v1/auth/login', { email, password });
    if (response.success) {
      const { accessToken, refreshToken, userId, role } = response.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      const user = { email, userId, role };
      localStorage.setItem('user', JSON.stringify(user));
      return user;
    }
    throw new Error(response.message || 'Login failed');
  },

  register: async (requestData) => {
    return await api.post('/api/v1/auth/register', requestData);
  },

  logout: async () => {
    try {
      await api.post('/api/v1/auth/logout');
    } catch (e) {
      // Ignore network errors on logout to allow offline state cleanup
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
};

export default authService;
