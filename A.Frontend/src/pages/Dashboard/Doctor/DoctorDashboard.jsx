import { useEffect, useState } from "react";
import http from "../../../api/http";
import "./DoctorDashboard.css";

export default function DoctorDashboard() {

  const [doctor, setDoctor] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [dateFilter, setDateFilter] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const loadData = async () => {
      try {
        // ✅ Load doctor profile
        const profile = await http.get("/doctor/me");
        setDoctor(profile.data);

        // ✅ Load ONLY BOOKED appointments (backend enforced)
        const res = await http.get("/appointment/doctor/my");
        setAppointments(res.data || []);
        setFiltered(res.data || []);
      } catch (err) {
        console.error(err);
        setError("Failed to load data");
      }
    };
    loadData();
  }, []);

  const filterByDate = () => {
    if (!dateFilter) {
      setFiltered(appointments);
      return;
    }
    setFiltered(
      appointments.filter(a => a.appointmentDate === dateFilter)
    );
  };

  const statusStyle = (status) => ({
    fontWeight: 700,
    color: status === "BOOKED" ? "lightgreen" : "white"
  });

  if (error) return <h3>{error}</h3>;
  if (!doctor) return <h3>Loading...</h3>;

  return (
    <div className="dashboard">
      <h2>Doctor Dashboard</h2>

      {/* ✅ PROFILE CARD */}
      <div className="card profile-grid">
        <p><b>Name:</b> {doctor.name}</p>
        <p><b>Email:</b> {doctor.email}</p>
        <p><b>Experience:</b> {doctor.experience}</p>
        <p><b>Qualification:</b> {doctor.qualification}</p>
        <p><b>Status:</b> {doctor.status}</p>
      </div>

      {/* ✅ FILTER CARD */}
      <div className="card">
        <h3>Filter Appointments</h3>
        <div className="filter-row">
          <input
            type="date"
            value={dateFilter}
            onChange={(e) => setDateFilter(e.target.value)}
          />
          <button className="btn btn-primary" onClick={filterByDate}>
            Search
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => setFiltered(appointments)}
          >
            Reset
          </button>
        </div>
      </div>

      {/* ✅ APPOINTMENTS TABLE */}
      <div className="card">
        <h3>Confirmed Appointments</h3>

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Patient ID</th>
              <th>Date</th>
              <th>Time</th>
              <th>Status</th>
              <th>Symptoms</th>
              <th>Remarks</th>
            </tr>
          </thead>

          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan="7">No appointments found</td>
              </tr>
            ) : (
              filtered.map((a) => (
                <tr key={a.appointmentId}>
                  <td>{a.appointmentId}</td>
                  <td>{a.patientId}</td>
                  <td>{a.appointmentDate}</td>
                  <td>{a.appointmentTime}</td>
                  <td style={statusStyle(a.status)}>{a.status}</td>
                  <td>{a.symptoms || "-"}</td>
                  <td>{a.remarks || "-"}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}