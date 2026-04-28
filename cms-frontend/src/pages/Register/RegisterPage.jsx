import { useState } from "react";
import { Container, Row, Col, Card, Button, Form } from "react-bootstrap";
import "./Register.css";

const COUNTRIES = {
  "+91": { flag: "🇮🇳", length: 10 },
  "+1": { flag: "🇺🇸", length: 10 },
  "+44": { flag: "🇬🇧", length: 10 },
};

export default function RegisterPage() {
  const [form, setForm] = useState({
    name: "",
    countryCode: "+91",
    phone: "",
    password: "",
    role: "",
  });

  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;

    if (name === "phone" && !/^\d*$/.test(value)) return;

    setForm({ ...form, [name]: value });
  };

  const validate = () => {
    const newErrors = {};
    const requiredLength = COUNTRIES[form.countryCode].length;

    if (!form.name.trim()) newErrors.name = "Name is required";

    if (!form.phone) {
      newErrors.phone = "Phone number is required";
    } else if (form.phone.length !== requiredLength) {
      newErrors.phone = `Phone number must be ${requiredLength} digits`;
    }

    if (!form.password || form.password.length < 6) {
      newErrors.password = "Password must be at least 6 characters";
    }

    if (!form.role) newErrors.role = "Please select a role";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    const payload = {
      name: form.name,
      phone: `${form.countryCode}${form.phone}`,
      password: form.password,
      role: form.role,
    };

    console.log("REGISTER PAYLOAD →", payload);

    // 🔴 send payload to backend here
  };

  return (
    <div className="register-page">
      <Container>
        <Row className="vh-100 align-items-center justify-content-center">
          <Col md={7} lg={5}>
            <Card className="register-card">
              <Card.Body className="p-4">
                <h3 className="fw-bold text-center mb-3">Create Account</h3>

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-3">
                    <Form.Label>Full Name</Form.Label>
                    <Form.Control
                      name="name"
                      value={form.name}
                      onChange={handleChange}
                      isInvalid={!!errors.name}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.name}
                    </Form.Control.Feedback>
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Phone Number</Form.Label>

                    <div className="d-flex gap-2">
                      <Form.Select
                        name="countryCode"
                        value={form.countryCode}
                        onChange={handleChange}
                        style={{ maxWidth: "120px" }}
                      >
                        {Object.entries(COUNTRIES).map(([code, c]) => (
                          <option key={code} value={code}>
                            {code} {c.flag}
                          </option>
                        ))}
                      </Form.Select>

                      <Form.Control
                        name="phone"
                        value={form.phone}
                        onChange={handleChange}
                        isInvalid={!!errors.phone}
                        placeholder="Phone number"
                      />
                    </div>

                    <Form.Control.Feedback type="invalid">
                      {errors.phone}
                    </Form.Control.Feedback>
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Password</Form.Label>
                    <Form.Control
                      type="password"
                      name="password"
                      value={form.password}
                      onChange={handleChange}
                      isInvalid={!!errors.password}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.password}
                    </Form.Control.Feedback>
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label>Register As</Form.Label>
                    <Form.Select
                      name="role"
                      value={form.role}
                      onChange={handleChange}
                      isInvalid={!!errors.role}
                    >
                      <option value="">Select role</option>
                      <option value="PATIENT">Patient</option>
                      <option value="DOCTOR">Doctor</option>
                    </Form.Select>
                    <Form.Control.Feedback type="invalid">
                      {errors.role}
                    </Form.Control.Feedback>
                  </Form.Group>

                  <Button type="submit" variant="info" className="w-100">
                    Register
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
