import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'https://iamaurimas.xyz/api/minter/';

const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add a request interceptor to attach JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("jwt");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Add a response interceptor to handle expired JWT
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response.status == 401 && error.response.data == "Expired JWT") {
      localStorage.removeItem("jwt");
      localStorage.removeItem("wallet_connected");
      localStorage.removeItem("wallet_address");
      alert("Session expired. Please log in again.");
      window.location.reload();
    }
    return Promise.reject(error);
  }
);

export default apiClient;