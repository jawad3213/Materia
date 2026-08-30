import axios from 'axios';

// Create a basic Axios instance without any security/token layers
const axiosClient = axios.create({
  // Uses the URL from .env.development, falls back to localhost if missing
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1', 
  headers: {
    'Content-Type': 'application/json',
  },
});

// You can add simple interceptors here later if needed for logging
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default axiosClient;
