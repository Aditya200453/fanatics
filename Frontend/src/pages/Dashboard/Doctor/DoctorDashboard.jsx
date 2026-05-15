import { useEffect, useMemo, useState } from "react";
import { Container, Card, Button, Table, Form } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./DoctorDashboard.css";

export default function DoctorDashboard() {
  const navigate = useNavigate();

  const [doctor, setDoctor] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [dateFilter, setDateFilter] = useState("");
  const [error, setError] = useState("");
  const [remarksMap, setRemarksMap] = useState({});

  const today = new Date().toISOString().slice(0, 10);

  /* ================= LOAD DATA ================= */
  useEffect(() => {
    (async () => {
      try {
        const profile = await http.get("/doctor/me");
        setDoctor(profile.data);

        const res = await http.get("/appointment/doctor/my");
        const data = res.data || [];

        setAppointments(data);
        setFiltered(data);

        const init = {};
        data.forEach(a => (init[a.appointmentId] = a.remarks || ""));
        setRemarksMap(init);
      } catch {
        setError("❌ Failed to load data");
      }
    })();
  }, []);

  /* ================= BLOCK BROWSER BACK ================= */
  useEffect(() => {
    window.history.pushState(null, "", window.location.href);

    const blockBack = () => {
      window.history.pushState(null, "", window.location.href);
    };

    window.addEventListener("popstate", blockBack);
    return () => window.removeEventListener("popstate", blockBack);
  }, []);

  /* ================= LOGOUT ================= */
  const handleLogout = () => {
    localStorage.clear();
    navigate("/login", { replace: true });
  };

  /* ================= DERIVED DATA ================= */
  const todaysCount = useMemo(
    () => appointments.filter(a => a.appointmentDate === today).length,
    [appointments]
  );

  const uniquePatients = useMemo(
    () => new Set(appointments.map(a => a.patientId)).size,
    [appointments]
  );

  const sortedAppointments = useMemo(() => {
    return [
      ...filtered.filter(a => a.appointmentDate === today),
      ...filtered.filter(a => a.appointmentDate !== today),
    ];
  }, [filtered, today]);

  /* ================= FILTER ================= */
  const filterByDate = () => {
    if (!dateFilter) {
      setFiltered(appointments);
      return;
    }
    setFiltered(appointments.filter(a => a.appointmentDate === dateFilter));
  };

  /* ================= SAVE REMARKS ================= */
  const saveRemarks = async (id) => {
    try {
      await http.put(`/appointment/doctor/remarks/${id}`, remarksMap[id], {
        headers: { "Content-Type": "text/plain" },
      });
    } catch {}
  };

  if (error) return <h3 className="pd-status">{error}</h3>;
  if (!doctor) return <h3 className="pd-status">Loading...</h3>;

  return (
    <div className="dd-root">
      <Container className="dd-container">

        {/* ================= HEADER ================= */}
        <div className="dd-header">

          <div className="dd-header-left">
            <span className="dd-kicker">Doctor Dashboard</span>

            <h2 className="dd-title">
              Welcome, <span>Dr. {doctor.name}</span>
            </h2>

            <p>
              Manage appointments, review patient symptoms, and prescribe medicines.
            </p>

            {/* ✅ SAME AS PATIENT DASHBOARD */}
            <div className="dd-hero-actions">
              <Button
                variant="outline-primary"
                className="dd-home-btn"
                onClick={() => navigate("/")}
              >
                ⌂ Go to Home
              </Button>
            </div>
          </div>

          {/* ✅ RIGHT SIDE ACTIONS (SAME PATTERN) */}
          <div className="dd-header-right">
            <span className="dd-stat-chip active">● Active</span>
            <span className="dd-stat-chip">
              {appointments.length} Appointments
            </span>

            <Button
              variant="outline-danger"
              size="sm"
              onClick={handleLogout}
            >
              Logout
            </Button>
          </div>

        </div>

        {/* ================= TODAY OVERVIEW ================= */}
        <div className="dd-today-strip">
          <div className="dd-today-card">
            <span>Today's Appointments</span>
            <p>{todaysCount}</p>
          </div>

          <div className="dd-today-card">
            <span>Total Patients</span>
            <p>{uniquePatients}</p>
          </div>

          <div className="dd-today-card">
            <span>Upcoming</span>
            <p>{appointments.length}</p>
          </div>
        </div>

        {/* ================= PROFILE ================= */}
        <Card className="dd-profile-card">
          <Card.Body>
            <div className="dd-profile-grid">
              <div><span>Email</span><p>{doctor.email}</p></div>
              <div><span>Qualification</span><p>{doctor.qualification}</p></div>
              <div><span>Experience</span><p>{doctor.experience} yrs</p></div>
            </div>
          </Card.Body>
        </Card>

        {/* ================= FILTER ================= */}
        <Card className="dd-filter-card">
          <Card.Body className="dd-filter-row">
            <div>
              <span className="dd-filter-title">Filter by date</span>
              <Form.Control
                type="date"
                value={dateFilter}
                onChange={(e) => setDateFilter(e.target.value)}
              />
            </div>

            <div className="dd-filter-actions">
              <Button onClick={filterByDate}>Search</Button>
              <Button
                variant="outline-secondary"
                onClick={() => {
                  setFiltered(appointments);
                  setDateFilter("");
                }}
              >
                Reset
              </Button>
            </div>
          </Card.Body>
        </Card>

        {/* ================= APPOINTMENTS ================= */}
        <Card className="dd-card">
          <Card.Body>
            <h5 className="mb-3">Appointments</h5>

            <Table responsive hover className="pd-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Patient</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Symptoms</th>
                  <th>Remarks</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sortedAppointments.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="dd-empty">
                      <p>No appointments scheduled</p>
                      <span>You are all caught up 🎉</span>
                    </td>
                  </tr>
                ) : (
                  sortedAppointments.map((a) => (
                    <tr
                      key={a.appointmentId}
                      className={a.appointmentDate === today ? "dd-row-today" : ""}
                    >
                      <td>{a.appointmentId}</td>
                      <td>{a.patientId}</td>
                      <td>{a.appointmentDate}</td>
                      <td>{a.appointmentTime}</td>

                      <td>
                        <span className={`pd-status ${a.status.toLowerCase()}`}>
                          {a.status}
                        </span>
                      </td>

                      <td>{a.symptoms || "-"}</td>

                      <td>
                        <Form.Control
                          size="sm"
                          value={remarksMap[a.appointmentId] || ""}
                          onChange={(e) =>
                            setRemarksMap({
                              ...remarksMap,
                              [a.appointmentId]: e.target.value,
                            })
                          }
                        />
                      </td>

                      <td className="dd-actions">
                        <Button
                          size="sm"
                          onClick={() => saveRemarks(a.appointmentId)}
                        >
                          Save
                        </Button>

                        <Button
                          size="sm"
                          variant="outline-primary"
                          onClick={() =>
                            navigate(`/doctor/prescription/${a.appointmentId}`)
                          }
                        >
                          Add Prescription 💊
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </Table>
          </Card.Body>
        </Card>

      </Container>
    </div>
  );
}