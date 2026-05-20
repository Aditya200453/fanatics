import { Container, Row, Col } from "react-bootstrap";
import "./AuthLayout.css";

export default function AuthLayout({ children }) {
  return (
    <Container fluid className="vh-100">
      <Row className="h-100">

        {/* LEFT SIDE */}
        <Col md={6} className="d-none d-md-flex auth-left text-white">
          <div className="p-5 my-auto">
            <h1 className="fw-bold mb-4">ClinicCare</h1>

            <h4 className="mb-3">Why ClinicCare?</h4>
            <p>✅ Secure patient records</p>
            <p>✅ Faster appointments</p>
            <p>✅ Role‑based dashboards</p>
            <p>✅ One complete clinic system</p>
          </div>
        </Col>

        {/* RIGHT SIDE */}
        <Col md={6} className="d-flex align-items-center justify-content-center bg-light">
          {children}
        </Col>

      </Row>
    </Container>
  );
}
