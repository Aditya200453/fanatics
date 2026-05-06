import { Routes, Route, Navigate } from "react-router-dom";

import LandingPage from "./pages/Landing/LandingPage";
import RegisterPage from "./pages/Register/RegisterPage";

import AdminLogin from "./pages/Login/AdminLogin";
import DoctorLogin from "./pages/Login/DoctorLogin";
import PatientLogin from "./pages/Login/PatientLogin";

import PatientDashboard from "./pages/Dashboard/Patient/PatientDashboard";
import DoctorDashboard from "./pages/Dashboard/Doctor/DoctorDashboard";
import AdminDashboard from "./pages/Dashboard/Admin/AdminDashboard";

import BookAppointment from "./pages/Dashboard/Patient/BookAppointment";
import MyAppointments from "./pages/Dashboard/Patient/MyAppointments";

import ProtectedRoute from "./routes/ProtectedRoute";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route path="/login/admin" element={<AdminLogin />} />
      <Route path="/login/doctor" element={<DoctorLogin />} />
      <Route path="/login/patient" element={<PatientLogin />} />

      <Route
        path="/dashboard/patient"
        element={
          <ProtectedRoute allowedRoles={["PATIENT"]}>
            <PatientDashboard />
          </ProtectedRoute>
        }
      />

      <Route
        path="/dashboard/patient/book"
        element={
          <ProtectedRoute allowedRoles={["PATIENT"]}>
            <BookAppointment />
          </ProtectedRoute>
        }
      />
      <Route
  path="/dashboard/admin"
  element={
    <ProtectedRoute allowedRoles={["ADMIN"]}>
      <AdminDashboard />
    </ProtectedRoute>
  }
/>


      <Route
        path="/dashboard/patient/appointments"
        element={
          <ProtectedRoute allowedRoles={["PATIENT"]}>
            <MyAppointments />
          </ProtectedRoute>
        }
      />

      <Route
        path="/dashboard/doctor"
        element={
          <ProtectedRoute allowedRoles={["DOCTOR"]}>
            <DoctorDashboard />
          </ProtectedRoute>
        }
      />

      <Route
        path="/dashboard/admin"
        element={
          <ProtectedRoute allowedRoles={["ADMIN"]}>
            <AdminDashboard />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}