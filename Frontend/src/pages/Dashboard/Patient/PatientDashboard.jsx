import { useEffect, useState } from "react";
import { Container, Row, Col, Card, Button, Table, Badge } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./PatientDashboard.css";

export default function PatientDashboard() {
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [doctorMap, setDoctorMap] = useState({}); // ✅ doctorId -> doctorName
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try {
        // 1) patient profile
        const me = await http.get("/patient/me");
        setPatient(me.data);

        // 2) appointments
        const ap = await http.get("/appointment/my");
        const appts = ap.data || [];
        setAppointments(appts);

        // 3) build doctorId->name securely using PUBLIC speciality doctors endpoints
        //    - Get all specialities
        const sp = await http.get("/speciality/");
        const specialities = sp.data || [];

        // 4) for each speciality, fetch doctors (public endpoint)
        const doctorsLists = await Promise.all(
          specialities.map((s) => http.get(`/speciality/${s.specialityId}/doctors`))
        );

        // 5) merge into unique map (no duplicates)
        const map = {};
        doctorsLists.forEach((res) => {
          (res.data || []).forEach((d) => {
            if (d?.doctorId != null && !map[d.doctorId]) {
              map[d.doctorId] = d.name; // store first occurrence
            }
          });
        });

        setDoctorMap(map);
      } catch (err) {
        const status = err?.response?.status;
        setError(status ? `Failed to load (${status})` : "Failed to load");
      }
    })();
  }, []);

  useEffect(() => {
    window.history.pushState(null, "", window.location.href);
    const blockBack = () => window.history.pushState(null, "", window.location.href);
    window.addEventListener("popstate", blockBack);
    return () => window.removeEventListener("popstate", blockBack);
  }, []);

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login");
  };

  if (error) return <h3 className="pd-status">{error}</h3>;
  if (!patient) return <h3 className="pd-status">Loading...</h3>;

  const last3 = appointments.slice(0, 3);

  return (
    <div className="pd-root">
      <Container className="pd-container">

        {/* ===== HEADER ===== */}
        <div className="pd-header pd-header-row">
          <div>
            <span className="pd-kicker">Patient Dashboard</span>
            <h2>Good to see you, {patient.name}</h2>
            <p>
              Track appointments, check recent activity, and book your next
              consultation easily.
            </p>

            <div className="pd-hero-actions">
              <Button
                variant="outline-primary"
                className="pd-home-btn"
                onClick={() => navigate("/")}
              >
                ⌂ Go to Home
              </Button>
            </div>
          </div>

          <div className="pd-header-actions">
            <div className="pd-header-chip">
              <span className="pd-chip-dot"></span>
              Health profile active
            </div>

            <Button variant="outline-danger" size="sm" onClick={handleLogout}>
              Logout
            </Button>
          </div>
        </div>

        {/* ===== CTA CARD ===== */}
        <Card className="pd-cta pd-cta-primary mb-4">
          <Card.Body className="d-flex justify-content-between align-items-center flex-wrap">
            <div>
              <h5>Book a New Appointment</h5>
              <p>Select a doctor, date & time slot to confirm your visit.</p>
            </div>

            <div className="pd-actions">
              <Button onClick={() => navigate("/dashboard/patient/book")}>
                Book Appointment
              </Button>

              <Button
                variant="outline-primary"
                onClick={() => navigate("/dashboard/patient/appointments")}
              >
                My Appointments
              </Button>
            </div>
          </Card.Body>
        </Card>

        {/* ===== INFO CARDS ===== */}
        <Row className="g-4 mb-4">
          <Col md={4}>
            <Card className="pd-card">
              <Card.Body>
                <h6>Email</h6>
                <p>{patient.email}</p>
                <small>Patient ID: {patient.patientId ?? "-"}</small>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="pd-card">
              <Card.Body>
                <h6>Phone</h6>
                <p>{patient.phone}</p>
                <small>
                  Status:{" "}
                  <Badge bg="success">{patient.status ?? "ACTIVE"}</Badge>
                </small>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="pd-card">
              <Card.Body>
                <h6>Appointments</h6>
                <p>{appointments.length} total</p>
                <small>All your bookings</small>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* ===== RECENT APPOINTMENTS ===== */}
        <Card className="pd-card">
          <Card.Body>
            <h5 className="mb-3">Recent Appointments</h5>

            <Table responsive hover className="pd-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Doctor</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Prescription</th>
                </tr>
              </thead>

              <tbody>
                {last3.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="text-center">
                      No appointments yet
                    </td>
                  </tr>
                ) : (
                  last3.map((a) => (
                    <tr key={a.appointmentId}>
                      <td>{a.appointmentId}</td>

                      {/* ✅ Doctor NAME from safe map (no secured doctor API) */}
                      <td>{doctorMap[a.doctorId] || `Doctor #${a.doctorId}`}</td>

                      <td>{a.appointmentDate}</td>
                      <td>{a.appointmentTime}</td>

                      <td>
                        <span className={`pd-status ${a.status.toLowerCase()}`}>
                          {a.status}
                        </span>
                      </td>

                      <td>
                        {a.status === "COMPLETED" ? (
                          <Button
                            size="sm"
                            variant="outline-info"
                            onClick={() =>
                              navigate(`/patient/prescription/${a.appointmentId}`)
                            }
                          >
                            View 💊
                          </Button>
                        ) : (
                          "-"
                        )}
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