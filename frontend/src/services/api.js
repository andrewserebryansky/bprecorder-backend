import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const recordingsApi = {
  getAll: () => api.get('/recordings'),
  getById: (id) => api.get(`/recordings/${id}`),
  getByBusinessPartner: (businessPartner) => api.get(`/recordings/partner/${encodeURIComponent(businessPartner)}`),
  initSampleData: () => api.post('/recordings/init-sample-data'),
};

export default api;