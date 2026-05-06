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

      // Login response has token + role [2](https://insightgloballlc-my.sharepoint.com/personal/pantham_jashwanth_insightglobal_com/Documents/Microsoft%20Teams%20Chat%20Files/signups%20and%20logins.pdf?web=1)
      const { token, role } = res.data;

      saveAuth({ token, role });

      // if user used wrong login page
      if (expectedRole && role !== expectedRole) {
        setError(`You are logged in as ${role}. Please use the correct login page.`);
        return;
      }

      if (role === "ADMIN") navigate("/dashboard/admin");
      else if (role === "DOCTOR") navigate("/dashboard/doctor");
      else if (role === "PATIENT") navigate("/dashboard/patient");
      else navigate("/");
    } catch (err) {
      const data = err?.response?.data;

      const msg =
        data?.message ||
        (typeof data === "string" ? data : JSON.stringify(data)) ||
        "Login failed. Check credentials or account status.";

      setError(msg);
    }
  };

  return (
    <Container fluid className="login-page">
      <Row className="min-vh-100">
        {/* LEFT VISUAL */}
        <Col md={7} className="login-visual d-none d-md-block">
          <div className="login-overlay" />
          <div className="login-text">
            <h2>ClinicCare</h2>
            <p>{subtitle}</p>
          </div>
        </Col>

        {/* RIGHT FORM */}
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

              <Button type="submit" className="w-100">
                Login
              </Button>
            </Form>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}