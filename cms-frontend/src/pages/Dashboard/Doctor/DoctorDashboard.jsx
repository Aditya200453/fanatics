import { useEffect, useState } from "react";
import http from "../../../api/http";
import "./DoctorDashboard.css";

export default function DoctorDashboard() {

  const [doctor, setDoctor] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await http.get("/doctor/me");
        setDoctor(res.data);
      } catch (err) {
        setError("Failed to load profile");
        console.error(err);
      }
    };

    fetchProfile();
  }, []);

  if (error) return <h3>{error}</h3>;
  if (!doctor) return <h3>Loading...</h3>;

  return (
    <div className="dashboard">
      <h2>Doctor Dashboard ✅</h2>

      <div className="card">
        <p><b>Name:</b> {doctor.name}</p>
        <p><b>Email:</b> {doctor.email}</p>
        <p><b>Phone:</b> {doctor.phone}</p>
        <p><b>Experience:</b> {doctor.experience} years</p>
        <p><b>Qualification:</b> {doctor.qualification}</p>
        <p><b>Status:</b> {doctor.status}</p>
      </div>
    </div>
  );
}