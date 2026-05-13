import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";   // ✅ ADD THIS
import http from "../../../api/http";
import "./DoctorDashboard.css";

export default function DoctorDashboard() {

  const navigate = useNavigate();   // ✅ ADD THIS

  const [doctor, setDoctor] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [dateFilter, setDateFilter] = useState("");
  const [error, setError] = useState("");

  const [remarksMap, setRemarksMap] = useState({});

  // ✅ Load data
  useEffect(() => {
    const loadData = async () => {
      try {
        const profile = await http.get("/doctor/me");
        setDoctor(profile.data);

        const res = await http.get("/appointment/doctor/my");

        setAppointments(res.data || []);
        setFiltered(res.data || []);

        const initialRemarks = {};
        (res.data || []).forEach(a => {
          initialRemarks[a.appointmentId] = a.remarks || "";
        });
        setRemarksMap(initialRemarks);

      } catch (err) {
        console.error(err);
        setError("❌ Failed to load data");
      }
    };

    loadData();
  }, []);

  // ✅ Filter
  const filterByDate = () => {
    if (!dateFilter) {
      setFiltered(appointments);
      return;
    }

    setFiltered(
      appointments.filter(a => a.appointmentDate === dateFilter)
    );
  };

  // ✅ SAVE REMARKS
  const saveRemarks = async (id) => {
    try {
      await http.put(`/appointment/doctor/remarks/${id}`, remarksMap[id], {
        headers: {
          "Content-Type": "text/plain"
        }
      });

      alert("✅ Remarks saved");
    } catch (err) {
      console.error(err);
      alert("❌ Failed to save remarks");
    }
  };

  if (error) return <h3>{error}</h3>;
  if (!doctor) return <h3>Loading...</h3>;

  return (
    <div className="dashboard">

      {/* PROFILE */}
      <h2>Doctor Dashboard ✅</h2>

      <div className="card">
        <p><b>Name:</b> {doctor.name}</p>
        <p><b>Email:</b> {doctor.email}</p>
        <p><b>Experience:</b> {doctor.experience}</p>
        <p><b>Qualification:</b> {doctor.qualification}</p>
        <p><b>Status:</b> {doctor.status}</p>
      </div>

      {/* FILTER */}
      <h3>Filter Appointments</h3>

      <input
        type="date"
        value={dateFilter}
        onChange={(e) => setDateFilter(e.target.value)}
      />

      <button onClick={filterByDate}>Search</button>
      <button onClick={() => setFiltered(appointments)}>Reset</button>

      {/* TABLE */}
      <h3>Appointments</h3>

      <table border="1" cellPadding="10">
        <thead>
          <tr>
            <th>ID</th>
            <th>Patient ID</th>
            <th>Date</th>
            <th>Time Slot</th>
            <th>Status</th>
            <th>Symptoms</th>
            <th>Remarks</th>
            <th>Action</th>  {/* ✅ HERE WE ADD BUTTON */}
          </tr>
        </thead>

        <tbody>
          {filtered.length === 0 ? (
            <tr><td colSpan="8">No appointments</td></tr>
          ) : (
            filtered.map((a) => (
              <tr key={a.appointmentId}>

                <td>{a.appointmentId}</td>
                <td>{a.patientId}</td>
                <td>{a.appointmentDate}</td>

                <td>
                  {a.appointmentTime}
                  {a.appointmentEndTime
                    ? ` - ${a.appointmentEndTime}`
                    : ""}
                </td>

                <td>{a.status}</td>
                <td>{a.symptoms || "-"}</td>

                {/* ✅ Remarks input */}
                <td>
                  <input
                    type="text"
                    value={remarksMap[a.appointmentId] || ""}
                    onChange={(e) =>
                      setRemarksMap({
                        ...remarksMap,
                        [a.appointmentId]: e.target.value
                      })
                    }
                  />
                </td>

                {/* ✅ ✅ ACTION COLUMN */}
                <td>

                  {/* Save Remarks */}
                  <button onClick={() => saveRemarks(a.appointmentId)}>
                    Save ✅
                  </button>

                  {/* ✅ ✅ ADD PRESCRIPTION BUTTON */}
                  <button
                    style={{ marginLeft: 10 }}
                    onClick={() =>
                      navigate(`/doctor/prescription/${a.appointmentId}`)
                    }
                  >
                    Add Prescription 💊
                  </button>

                </td>

              </tr>
            ))
          )}
        </tbody>
      </table>

    </div>
  );
}