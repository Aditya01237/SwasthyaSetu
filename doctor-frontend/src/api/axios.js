import axios from "axios";

const api = axios.create({
  baseURL: "/api",
});

// 🔥 Attach doctor token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("doctorToken");

  console.log("TOKEN BEING SENT:", token); // debug

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

// 🔥 Handle auth errors
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      console.log("Doctor session expired");

      localStorage.removeItem("doctorToken");
      localStorage.removeItem("doctor");

      window.location.href = "/";
    }

    return Promise.reject(err);
  }
);

export default api;