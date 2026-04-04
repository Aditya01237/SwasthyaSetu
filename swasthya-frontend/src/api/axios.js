import axios from "axios";

const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
});

// ✅ REQUEST INTERCEPTOR
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// ✅ RESPONSE INTERCEPTOR (MAIN LOGIC)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 500) {
      // your backend throws RuntimeException → comes as 500

      if (
        error.response.data?.message?.includes("Session expired") ||
        error.response.data?.message?.includes("Invalid token")
      ) {
        window.dispatchEvent(new Event("sessionExpired"));
      }
    }

    return Promise.reject(error);
  }
);

export default api;