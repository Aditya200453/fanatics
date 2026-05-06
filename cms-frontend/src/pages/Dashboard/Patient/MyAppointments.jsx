import { useEffect, useState } from "react";
import http from "../../../api/http";

export default function MyAppointments() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      setError("");
      const res = await http.get("/appointment/my");
      setItems(res.data || []);
    } catch (err) {
      console.error(err);
      setError("❌ Failed to load appointments");
    }
  };

  useEffect(() => {
    load();
  }, []);

  if (error) return <h3 style={{ padding: 20 }}>{error}</h3>;

  return (
    <div style={{ padding: 20 }}>
      <h2>My Appointments</h2>

      <button onClick={load} style={{ marginBottom: 10 }}>
        Refresh
      </button>

      <table border="1" cellPadding="10">
        <thead>
          <tr>
            <th>ID</th>
            <th>Doctor ID</th>
            <th>Date</th>
            <th>Time</th>
            <th>Status</th>
            <th>Symptoms</th>
            <th>Remarks</th>
          </tr>
        </thead>
        <tbody>
          {items.length === 0 ? (
            <tr><td colSpan="7">No appointments yet</td></tr>
          ) : (
            items.map((a) => (
              <tr key={a.appointmentId}>
                <td>{a.appointmentId}</td>
                <td>{a.doctorId}</td>
                <td>{a.appointmentDate}</td>
                <td>{a.appointmentTime}</td>
                <td>{a.status}</td>
                <td>{a.symptoms ?? "-"}</td>
                <td>{a.remarks ?? "-"}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
