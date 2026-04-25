import { Container, Row, Col, Card, Button, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import "./PatientDashboard.css";

export default function PatientDashboard() {
  const navigate = useNavigate();

  return (
    // ✅ ROOT BACKGROUND LAYER
    <div className="pd-root">
      <Container fluid className="px-4 py-4">

        {/* Greeting */}
        <div className="mb-4">
          <h2 className="pd-title">
            Hello, Rahul <span role="img" aria-label="wave">👋</span>
          </h2>
          <p className="pd-muted">
            Here’s your appointment and health overview
          </p>
        </div>

        {/* Stats cards */}
        <Row className="g-4 mb-4">
          <Col md={4}>
            <Card className="pd-glass-card h-100">
              <Card.Body>
                <h6 className="fw-bold">Upcoming Appointment</h6>
                <p className="mb-0">25 Apr 2026 • 10:30 AM</p>
                <small className="pd-muted">Dr. Sharma</small>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="pd-glass-card h-100">
              <Card.Body>
                <h6 className="fw-bold">Assigned Doctor</h6>
                <p className="mb-0">Dr. Anil Verma</p>
                <small className="pd-muted">Cardiology</small>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="pd-glass-card h-100">
              <Card.Body>
                <h6 className="fw-bold">Test Reports</h6>
                <p className="mb-0">2 Reports Available</p>
                <small className="pd-muted">Updated Apr 2026</small>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Book appointment */}
        <Card className="pd-glass-card mb-4">
          <Card.Body className="d-flex justify-content-between align-items-center">
            <div>
              <h5 className="fw-bold mb-1">Book a New Appointment</h5>
              <p className="pd-muted mb-0">
                Select speciality, doctor and available slot
              </p>
            </div>
            <Button
              variant="info"
              onClick={() => navigate("/dashboard/patient/book-appointment")}
            >
              Book Appointment
            </Button>
          </Card.Body>
        </Card>

        {/* Previous appointments */}
        <Card className="pd-glass-card">
          <Card.Body>
            <h5 className="fw-bold mb-3">Previous Appointments</h5>
            <Table hover responsive variant="dark">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Doctor</th>
                  <th>Speciality</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>15 Mar 2026</td>
                  <td>Dr. Gupta</td>
                  <td>Cardiology</td>
                  <td>Completed</td>
                </tr>
                <tr>
                  <td>02 Feb 2026</td>
                  <td>Dr. Rao</td>
                  <td>Orthopedics</td>
                  <td>Completed</td>
                </tr>
              </tbody>
            </Table>
          </Card.Body>
        </Card>

      </Container>
    </div>
  );
}
