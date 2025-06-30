// src/services/api.ts
import axios from 'axios';

// if you ever set VITE_API_URL in .env, it'll use that;
// otherwise it'll hit /api which Vite proxies above
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
});

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
