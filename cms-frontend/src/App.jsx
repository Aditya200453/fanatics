import { Routes, Route, Navigate } from "react-router-dom";

// Pages
import LandingPage from "./pages/Landing/LandingPage";
import RegisterPage from "./pages/Register/RegisterPage";

// Login Pages
import AdminLogin from "./pages/Login/AdminLogin";
import DoctorLogin from "./pages/Login/DoctorLogin";
import PatientLogin from "./pages/Login/PatientLogin";

// Dashboards (✅ ACTUAL IMPLEMENTATIONS)
import PatientDashboard from "./pages/Dashboard/Patient/PatientDashboard";
import DoctorDashboard from "./pages/Dashboard/Doctor/DoctorDashboard";
import AdminDashboard from "./pages/Dashboard/Admin/AdminDashboard";

// Protected Route
import ProtectedRoute from "./routes/ProtectedRoute";

export default function App() {
  return (
    <Routes>

      {/* ✅ Landing Page */}
      <Route path="/" element={<LandingPage />} />

      {/* ✅ Register Page */}
      <Route path="/register" element={<RegisterPage />} />

      {/* ✅ Login Routes */}
      <Route path="/login/admin" element={<AdminLogin />} />
      <Route path="/login/doctor" element={<DoctorLogin />} />
      <Route path="/login/patient" element={<PatientLogin />} />

      {/* ✅ PATIENT DASHBOARD */}
      <Route
        path="/dashboard/patient"
        element={
          <ProtectedRoute allowedRoles={["PATIENT"]}>
            <PatientDashboard />
          </ProtectedRoute>
        }
      />

      {/* ✅ DOCTOR DASHBOARD */}
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


      {/* ✅ Fallback Route */}
      <Route path="*" element={<Navigate to="/" replace />} />

    </Routes>
  );
}