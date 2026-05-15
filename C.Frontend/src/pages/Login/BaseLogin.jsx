import { useState } from "react";
import { Container, Row, Col, Card, Button, Form, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import http from "../../api/http";
import { saveAuth } from "../../auth/auth";
import "./Login.css";

export default function BaseLogin({ title, subtitle, expectedRole }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const res = await http.post("/auth/login", {
        email: form.email,
        password: form.password,
      });

      const { token, role } = res.data;
      saveAuth({ token, role });

      // Wrong login page protection
      if (expectedRole && role !== expectedRole) {
        setError(`You are logged in as ${role}. Please use the correct login page.`);
        return;
      }

      // Role-based redirect with replace:true
      if (role === "ADMIN") {
        navigate("/dashboard/admin", { replace: true });
      } else if (role === "DOCTOR") {
        navigate("/dashboard/doctor", { replace: true });
      } else if (role === "PATIENT") {
        navigate("/dashboard/patient", { replace: true });
      } else if (role === "STAFF") {
        navigate("/dashboard/staff", { replace: true });
      } else {
        navigate("/", { replace: true });
      }

    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data ||
        "Login failed. Check credentials or account status.";
      setError(msg);
    }
  };

  return (
    <Container fluid className="login-page">
      <Row className="min-vh-100">
        {/* LEFT */}
        <Col md={7} className="login-visual d-none d-md-block">
          <div className="login-overlay" />
        </Col>

        {/* RIGHT */}
        <Col md={5} className="d-flex align-items-center justify-content-center p-4">
          <Card className="login-card p-4">
            <h4 className="mb-3">{title}</h4>

            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Email</Form.Label>
                <Form.Control
                  name="email"
                  type="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="Enter email"
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  name="password"
                  type="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Enter password"
                  required
                />
              </Form.Group>

              <Button className="w-100" type="submit">
                Login
              </Button>

              {/* LOGIN AS OTHER */}
              <Button
                className="w-100 mt-2"
                variant="outline-secondary"
                type="button"
                onClick={() => navigate("/login", { replace: true })}
              >
                Login as Other
              </Button>
            </Form>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}
