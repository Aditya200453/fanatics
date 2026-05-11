import { useEffect, useState } from "react";
import { Container, Row, Col, Card, Button, Table } from "react-bootstrap";
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
        const me = await http.get("/patient/me"); // requires Bearer token [1](https://insightgloballlc.sharepoint.com/sites/myIG/_layouts/15/Doc.aspx?sourcedoc=%7B41ECB743-3655-4E24-A394-AC0EBFCC7857%7D&file=Orientation%20Deck%202024.pptx&action=edit&mobileredirect=true&DefaultItemOpen=1)
        setPatient(me.data);

        // Optional: show last few appointments
        const ap = await http.get("/appointment/my");
        setAppointments(ap.data || []);
      } catch (err) {
        console.error(err);
        const status = err?.response?.status;
        setError(status ? `Failed to load (${status})` : "Failed to load");
      }
    })();
  }, []);

  if (error) return <h3 style={{ padding: 20 }}>{error}</h3>;
  if (!patient) return <h3 style={{ padding: 20 }}>Loading...</h3>;

  const last3 = appointments.slice(0, 3);

  return (
    <div className="pd-root">
      <Container fluid className="px-4 py-4">

        <div className="mb-4">
          <h2 className="pd-title">
            Hello, {patient.name} <span role="img" aria-label="wave">👋</span>
          </h2>
          <p className="pd-muted">
            Here’s your appointment and health overview
          </p>
        </div>

        <Row className="g-4 mb-4">
          <Col md={4}>
            <Card className="pd-glass-card h-100">
              <Card.Body>
                <h6 className="fw-bold">Email</h6>
                <p className="mb-0">{patient.email}</p>
                <small className="pd-muted">Patient ID: {patient.patientId ?? "-"}</small>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="pd-glass-card h-100">
              <Card.Body>
                <h6 className="fw-bold">Phone</h6>
                <p className="mb-0">{patient.phone}</p>
                <small className="pd-muted">Status: {patient.status ?? "ACTIVE"}</small>
              </Card.Body>
            </Card>
          </Col>

        

          <Col md={4}>
            <Card className="pd-glass-card h-100">
              <Card.Body>
                <h6 className="fw-bold">Appointments</h6>
                <p className="mb-0">{appointments.length} total</p>
                <small className="pd-muted">Stored in appointment table [2](https://insightgloballlc.sharepoint.com/sites/Automation/_layouts/15/Doc.aspx?sourcedoc=%7BE8A9615D-6E4E-4994-A1FC-C7FBA2A4EDD3%7D&file=IG_Standard_Billing_Process_SOP.docx&action=default&mobileredirect=true&DefaultItemOpen=1)</small>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        <Card className="pd-glass-card mb-4">
          <Card.Body className="d-flex justify-content-between align-items-center">
            <div>
              <h5 className="fw-bold mb-1">Book a New Appointment</h5>
              <p className="pd-muted mb-0">
                Select doctor, date and available slot
              </p>
            </div>

            <div style={{ display: "flex", gap: 10 }}>
              <Button variant="info" onClick={() => navigate("/dashboard/patient/book")}>
                Book Appointment
              </Button>
              <Button variant="outline-light" onClick={() => navigate("/dashboard/patient/appointments")}>
                My Appointments
              </Button>
            </div>
          </Card.Body>
        </Card>

        <Card className="pd-glass-card">
          <Card.Body>
            <h5 className="fw-bold mb-3">Recent Appointments</h5>

            <Table hover responsive variant="dark">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Doctor ID</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {last3.length === 0 ? (
                  <tr><td colSpan="5">No appointments yet</td></tr>
                ) : (
                  last3.map((a) => (
                    <tr key={a.appointmentId}>
                      <td>{a.appointmentId}</td>
                      <td>{a.doctorId}</td>
                      <td>{a.appointmentDate}</td>
                      <td>{a.appointmentTime}</td>
                      <td>{a.status}</td>
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