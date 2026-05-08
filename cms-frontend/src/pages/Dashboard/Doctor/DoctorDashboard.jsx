import { useEffect, useState } from "react";
import http from "../../../api/http";

export default function DoctorDashboard() {

  const [doctor, setDoctor] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [dateFilter, setDateFilter] = useState("");
  const [error, setError] = useState("");

  // ✅ Load doctor profile + appointments
  useEffect(() => {
    const loadData = async () => {
      try {
        const profile = await http.get("/doctor/me");
        setDoctor(profile.data);

        const res = await http.get("/appointment/doctor/my");
        setAppointments(res.data);
        setFiltered(res.data);

      } catch (err) {
        console.error(err);
        setError("Failed to load data");
      }
    };

    loadData();
  }, []);

  // ✅ Filter by date
  const filterByDate = () => {
    if (!dateFilter) {
      setFiltered(appointments);
      return;
    }

    const filteredData = appointments.filter(
      (a) => a.appointmentDate === dateFilter
    );

    setFiltered(filteredData);
  };

  if (error) return <h3>{error}</h3>;
  if (!doctor) return <h3>Loading...</h3>;

  return (
    <div style={{ padding: 20 }}>

      {/* ✅ PROFILE */}
      <h2>Doctor Dashboard ✅</h2>

      <div style={{ border: "1px solid #ccc", padding: 10, marginBottom: 20 }}>
        <p><b>Name:</b> {doctor.name}</p>
        <p><b>Email:</b> {doctor.email}</p>
        <p><b>Experience:</b> {doctor.experience}</p>
        <p><b>Qualification:</b> {doctor.qualification}</p>
        <p><b>Status:</b> {doctor.status}</p>
      </div>

      {/* ✅ FILTER */}
      <h3>Filter Appointments</h3>

      <input
        type="date"
        value={dateFilter}
        onChange={(e) => setDateFilter(e.target.value)}
      />

      <button onClick={filterByDate} style={{ marginLeft: 10 }}>
        Search
      </button>

      <button onClick={() => setFiltered(appointments)} style={{ marginLeft: 10 }}>
        Reset
      </button>

      {/* ✅ TABLE */}
      <h3 style={{ marginTop: 20 }}>Appointments</h3>

      <table border="1" cellPadding="10">
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
            <tr><td colSpan="7">No appointments found</td></tr>
          ) : (
            filtered.map((a) => (
              <tr key={a.appointmentId}>
                <td>{a.appointmentId}</td>
                <td>{a.patientId}</td>
                <td>{a.appointmentDate}</td>
                <td>{a.appointmentTime}</td>
                <td>{a.status}</td>
                <td>{a.symptoms || "-"}</td>
                <td>{a.remarks || "-"}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>

    </div>
  );
}