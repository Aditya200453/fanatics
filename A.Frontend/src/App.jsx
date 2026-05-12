import { Routes, Route, Navigate } from "react-router-dom";

// PUBLIC
import LandingPage from "./pages/Landing/LandingPage";
import RegisterPage from "./pages/Register/RegisterPage";

// LOGIN
import AdminLogin from "./pages/Login/AdminLogin";
import DoctorLogin from "./pages/Login/DoctorLogin";
import PatientLogin from "./pages/Login/PatientLogin";   
//import OtherLogin from "./pages/Login/OtherLogin"; // ✅ staff login wrapper
import OthersLogin from "./pages/Login/OthersLogin";   // ✅ role chooser 

// DASHBOARDS
import PatientDashboard from "./pages/Dashboard/Patient/PatientDashboard";
import DoctorDashboard from "./pages/Dashboard/Doctor/DoctorDashboard";
import AdminDashboard from "./pages/Dashboard/Admin/AdminDashboard";
import StaffDashboard from "./pages/Dashboard/Staff/StaffDashboard";

// PATIENT FEATURES
import BookAppointment from "./pages/Dashboard/Patient/BookAppointment";
import MyAppointments from "./pages/Dashboard/Patient/MyAppointments";

// ROUTE GUARD
import ProtectedRoute from "./routes/ProtectedRoute";

export default function App() {
  return (
    <Routes>
      {/* ===================== PUBLIC ===================== */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* ============== LOGIN ROLE SELECT ================= */}
      {/* ✅ Used by "Login as Other" */}
      <Route path="/login" element={<OthersLogin />} />

      {/* ================ ROLE LOGINS ===================== */}
      <Route path="/login/admin" element={<AdminLogin />} />
      <Route path="/login/doctor" element={<DoctorLogin />} />
      <Route path="/login/patient" element={<PatientLogin />} />

      {/* ✅ STAFF LOGIN via BaseLogin */}
      <Route path="/login/staff" element={<OthersLogin />} />

      {/* ================= PATIENT ======================== */}
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
        path="/dashboard/patient/appointments"
        element={
          <ProtectedRoute allowedRoles={["PATIENT"]}>
            <MyAppointments />
          </ProtectedRoute>
        }
      />

      {/* ================= DOCTOR ========================= */}
      <Route
        path="/dashboard/doctor"
        element={
          <ProtectedRoute allowedRoles={["DOCTOR"]}>
            <DoctorDashboard />
          </ProtectedRoute>
        }
      />

      {/* ================= ADMIN ========================== */}
      <Route
        path="/dashboard/admin"
        element={
          <ProtectedRoute allowedRoles={["ADMIN"]}>
            <AdminDashboard />
          </ProtectedRoute>
        }
      />

      {/* ================= STAFF ========================== */}
      <Route
        path="/dashboard/staff"
        element={
          <ProtectedRoute allowedRoles={["STAFF"]}>
            <StaffDashboard />
          </ProtectedRoute>
        }
      />

      {/* ================= FALLBACK ======================= */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
