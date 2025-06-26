import axios from 'axios';

// API Gateway base URL - change this to point to your API Gateway
const API_BASE_URL = process.env.REACT_APP_API_GATEWAY_URL || 'http://localhost:8080';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Important for sending authentication cookies
});

// Add request interceptor for authentication
api.interceptors.request.use(
  (config) => {
    // Add any auth tokens here if needed
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login if unauthorized
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Handle forbidden access
      console.error('Access forbidden:', error.response.data);
    }
    return Promise.reject(error);
  }
);

export default api;
