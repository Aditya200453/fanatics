import { useEffect, useState } from "react";
import { Container, Row, Col, Card, Button, Table, Badge } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./PatientDashboard.css";

export default function PatientDashboard() {
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const me = await http.get("/patient/me");
        setPatient(me.data);

        const ap = await http.get("/appointment/my");
        setAppointments(ap.data || []);
      } catch (err) {
        const status = err?.response?.status;
        setError(status ? `Failed to load (${status})` : "Failed to load");
      }
    })();
  }, []);

  if (error) return <h3 className="pd-status">{error}</h3>;
  if (!patient) return <h3 className="pd-status">Loading...</h3>;

  const last3 = appointments.slice(0, 3);

  return (
    <div className="pd-root">
      <Container className="pd-container">

        <div className="pd-header pd-header-row">
          <div>
            <span className="pd-kicker">Patient Dashboard</span>
            <h2>Good to see you, {patient.name}</h2>
            <p>Track appointments, check recent activity, and book your next consultation easily.</p>
          </div>

          <div className="pd-header-chip">
            <span className="pd-chip-dot"></span>
            Health profile active
          </div>
        </div>

        {/* Info Cards */}
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
                  <Badge bg="success">
                    {patient.status ?? "ACTIVE"}
                  </Badge>
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

        {/* CTA Card */}
        <Card className="pd-cta mb-4">
          <Card.Body>
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

        {/* Recent Appointments */}
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
                </tr>
              </thead>
              <tbody>
                {last3.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="text-center">
                      No appointments yet
                    </td>
                  </tr>
                ) : (
                  last3.map((a) => (
                    <tr key={a.appointmentId}>
                      <td>{a.appointmentId}</td>
                      <td>{a.doctorId}</td>
                      <td>{a.appointmentDate}</td>
                      <td>{a.appointmentTime}</td>
                      <td>
                        <Badge bg="info">{a.status}</Badge>
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