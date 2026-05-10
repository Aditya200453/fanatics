import axios from "axios";

const http = axios.create({
  baseURL: "http://localhost:8077",
  headers: { "Content-Type": "application/json" },
});

// ✅ ALWAYS attach JWT token (admin/doctor/patient protected calls)
http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default http;
