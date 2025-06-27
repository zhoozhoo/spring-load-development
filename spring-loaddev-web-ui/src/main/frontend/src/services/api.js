import axios from 'axios';

// API Gateway base URL - now properly uses environment variable
const API_BASE_URL = process.env.REACT_APP_API_GATEWAY_URL || 'http://localhost:8080';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Important for sending authentication cookies
  timeout: 10000, // 10 second timeout
});

// Add request interceptor for authentication and CSRF
api.interceptors.request.use(
  (config) => {
    // Add CSRF token if available
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    if (csrfToken && csrfHeader) {
      config.headers[csrfHeader] = csrfToken;
    }

    // Add any auth tokens here if needed
    const authToken = localStorage.getItem('authToken');
    if (authToken) {
      config.headers.Authorization = `Bearer ${authToken}`;
    }

    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Enhanced error handling
    if (error.response?.status === 401) {
      // Clear any stored auth tokens
      localStorage.removeItem('authToken');
      // Redirect to login if unauthorized
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Handle forbidden access
      console.error('Access forbidden:', error.response.data);
    } else if (error.response?.status === 404) {
      console.error('Resource not found:', error.response.data);
    } else if (error.response?.status >= 500) {
      console.error('Server error:', error.response.data);
    } else if (error.code === 'ECONNABORTED') {
      console.error('Request timeout');
    } else if (!error.response) {
      console.error('Network error - no response received');
    }
    
    return Promise.reject(error);
  }
);

export default api;
