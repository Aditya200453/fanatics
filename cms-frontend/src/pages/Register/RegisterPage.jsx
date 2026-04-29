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
    email: "",
    dob: "",
    gender: "",
    city: "",
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

    if (!form.email) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      newErrors.email = "Invalid email format";
    }

    if (!form.dob) newErrors.dob = "Date of birth is required";

    if (!form.gender) newErrors.gender = "Please select gender";

    if (!form.city.trim()) newErrors.city = "City is required";

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
      email: form.email,
      dob: form.dob,
      gender: form.gender,
      address: {
        city: form.city,
      },
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
                  {/* Name */}
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

                  {/* Email */}
                  <Form.Group className="mb-3">
                    <Form.Label>Email</Form.Label>
                    <Form.Control
                      type="email"
                      name="email"
                      value={form.email}
                      onChange={handleChange}
                      isInvalid={!!errors.email}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.email}
                    </Form.Control.Feedback>
                  </Form.Group>

                  {/* DOB */}
                  <Form.Group className="mb-3">
                    <Form.Label>Date of Birth</Form.Label>
                    <Form.Control
                      type="date"
                      name="dob"
                      value={form.dob}
                      onChange={handleChange}
                      isInvalid={!!errors.dob}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.dob}
                    </Form.Control.Feedback>
                  </Form.Group>

                  {/* Gender */}
                  <Form.Group className="mb-3">
                    <Form.Label>Gender</Form.Label>
                    <Form.Select
                      name="gender"
                      value={form.gender}
                      onChange={handleChange}
                      isInvalid={!!errors.gender}
                    >
                      <option value="">Select gender</option>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="OTHER">Other</option>
                    </Form.Select>
                    <Form.Control.Feedback type="invalid">
                      {errors.gender}
                    </Form.Control.Feedback>
                  </Form.Group>

                  {/* City */}
                  <Form.Group className="mb-3">
                    <Form.Label>City</Form.Label>
                    <Form.Control
                      name="city"
                      value={form.city}
                      onChange={handleChange}
                      isInvalid={!!errors.city}
                      placeholder="City"
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.city}
                    </Form.Control.Feedback>
                  </Form.Group>

                  {/* Phone */}
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

                  {/* Password */}
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

                  {/* Role */}
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