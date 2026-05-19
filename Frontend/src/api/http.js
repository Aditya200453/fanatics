import axios from "axios";

const http = axios.create({
  baseURL: "http://localhost:8076",
  // baseURL: "http://api-gateway:8076",
  headers: { "Content-Type": "application/json" },
});

http.interceptors.request.use((config) => {
  const url = config.url || "";

  const isPublic =
    url.startsWith("/auth/login") ||
    url.startsWith("/auth/signup") ||
    url.startsWith("/doctor/signup") ||
    url.startsWith("/patient/signup");

  if (!isPublic) {
    const token = localStorage.getItem("token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
  } else {
    delete config.headers.Authorization;
  }

  return config;
});

export default http;