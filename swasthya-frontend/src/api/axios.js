import axios from "axios";

const api = axios.create({
  baseURL: "/api",
});

// attach token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("patientToken");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      if (error.response.status === 401 || error.response.status === 403) {
        console.log("Patient session expired");

        const token = localStorage.getItem("patientToken");
        // Only treat as session expiry if user was previously logged in
        if (token) {
          localStorage.removeItem("patientToken");
          window.dispatchEvent(new Event("sessionExpired"));
        } else {
          // Not logged in — navigate to login via React Router
          window.location.href = "/";
        }
      }
    }

    return Promise.reject(error);
  }
);

export default api;