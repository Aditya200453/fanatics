import { Routes, Route } from "react-router-dom";
import LandingPage from "./pages/Landing/LandingPage";
import AdminLogin from "./pages/Login/AdminLogin";
import DoctorLogin from "./pages/Login/DoctorLogin";
import PatientLogin from "./pages/Login/PatientLogin";
import OtherLogin from "./pages/Login/OthersLogin";
import PatientDashboard from "./pages/Dashboard/Patient/PatientDashboard";
import RegisterPage from "./pages/Register/RegisterPage";
export default function App() {
  return (
  <Routes>
  <Route path="/" element={<LandingPage />} />

  <Route path="/login/admin" element={<AdminLogin />} />
  <Route path="/login/doctor" element={<DoctorLogin />} />
  <Route path="/login/patient" element={<PatientLogin />} />
  <Route path="/login/other" element={<OtherLogin />} /> 
  <Route path="/dashboard/patient" element={<PatientDashboard />} />
  <Route path="/register" element={<RegisterPage />} />
</Routes>

  );
}