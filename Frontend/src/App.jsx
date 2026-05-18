import { Routes, Route, Navigate } from "react-router-dom";
 
/* PUBLIC */
import LandingPage from "./pages/Landing/LandingPage";
import RegisterPage from "./pages/Register/RegisterPage";
 
/* LOGIN */
import AdminLogin from "./pages/Login/AdminLogin";
import DoctorLogin from "./pages/Login/DoctorLogin";
import PatientLogin from "./pages/Login/PatientLogin";
import OthersLogin from "./pages/Login/OthersLogin";
 
/* DASHBOARDS */
import PatientDashboard from "./pages/Dashboard/Patient/PatientDashboard";
import DoctorDashboard from "./pages/Dashboard/Doctor/DoctorDashboard";
import AdminDashboard from "./pages/Dashboard/Admin/AdminDashboard";
import StaffDashboard from "./pages/Dashboard/Staff/StaffDashboard";
 
/* FEATURES */
import BookAppointment from "./pages/Dashboard/Patient/BookAppointment";
import MyAppointments from "./pages/Dashboard/Patient/MyAppointments";
import AddPrescription from "./pages/Dashboard/Doctor/AddPrescription";
import ViewPrescription from "./pages/Dashboard/Patient/ViewPrescription";
 
/* ROUTE GUARD */
import ProtectedRoute from "./routes/ProtectedRoute";
 
/* ✅ CHATBOT */
import ChatbotWidget from "./Chatbot/ChatbotWidget";
 
export default function App() {
  return (
    <>
      <Routes>
        {/* ========== PUBLIC ========== */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/register" element={<RegisterPage />} />
 
        {/* ========== LOGIN ========== */}
 
        {/* ✅ GENERIC LOGIN */}
        <Route
          path="/login"
          element={<OthersLogin title="Login to ClinicCare" />}
        />
 
        {/* ✅ ROLE-SPECIFIC LOGIN */}
        <Route path="/login/admin" element={<AdminLogin />} />
        <Route path="/login/doctor" element={<DoctorLogin />} />
        <Route path="/login/patient" element={<PatientLogin />} />
 
        {/* ✅ FIXED STAFF LOGIN */}
        <Route
          path="/login/staff"
          element={
            <OthersLogin
              title="Staff Login"
              expectedRole="STAFF"
            />
          }
        />
 
        {/* ========== PATIENT ========== */}
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
 
        <Route
          path="/patient/prescription/:appointmentId"
          element={<ViewPrescription />}
        />
 
        {/* ========== DOCTOR ========== */}
        <Route
          path="/dashboard/doctor"
          element={
            <ProtectedRoute allowedRoles={["DOCTOR"]}>
              <DoctorDashboard />
            </ProtectedRoute>
          }
        />
 
        <Route
          path="/doctor/prescription/:appointmentId"
          element={
            <ProtectedRoute allowedRoles={["DOCTOR"]}>
              <AddPrescription />
            </ProtectedRoute>
          }
        />
 
        {/* ========== ADMIN ========== */}
        <Route
          path="/dashboard/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
 
        {/* ========== STAFF ========== */}
        <Route
          path="/dashboard/staff"
          element={
            <ProtectedRoute allowedRoles={["STAFF"]}>
              <StaffDashboard />
            </ProtectedRoute>
          }
        />
 
        {/* ========== FALLBACK ========== */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
 
      {/* ✅ CHATBOT ON ALL PAGES */}
      <ChatbotWidget
        agent={{
          mode: "whatsapp",
          value:
            "https://wa.me/919999999999?text=Hi%20I%20need%20help%20with%20my%20clinic%20query",
        }}
      />
    </>
  );
}
 