import { Container, Row, Col, Card, Button, Form } from "react-bootstrap";
import "./Login.css";

export default function BaseLogin({ title, subtitle }) {
  return (
    <div className="login-page">
      <Container fluid>
        <Row className="vh-100">

          {/* LEFT VISUAL */}
          <Col md={7} className="login-visual d-none d-md-flex">
            <div className="login-overlay" />
            <div className="login-text">
              <h1>ClinicCare</h1>
              <p>{subtitle}</p>
            </div>
          </Col>

          {/* RIGHT FORM */}
          <Col md={5} className="d-flex align-items-center justify-content-center">
            <Card className="login-card">
              <Card.Body>
                <h3 className="fw-bold mb-3">{title}</h3>

                <Form>
                  <Form.Group className="mb-3">
                    <Form.Label>Email</Form.Label>
                    <Form.Control type="email" placeholder="Enter email" />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>Password</Form.Label>
                    <Form.Control type="password" placeholder="Enter password" />
                  </Form.Group>

                  <Button variant="info" className="w-100">
                    Login
                  </Button>
                </Form>
              </Card.Body>
            </Card>
          </Col>

        </Row>
      </Container>
    </div>
  );
}