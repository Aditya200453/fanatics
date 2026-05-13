import { useState } from "react";
import {
  Container,
  Row,
  Col,
  Card,
  Button,
  Form,
  Alert
} from "react-bootstrap";
import http from "../../api/http";
import { useNavigate } from "react-router-dom";
import "../Login/Login.css";

export default function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    role: "",
    name: "",
    email: "",
    phone: "",
    password: "",
    age: "",
    dob: "",
    gender: "",
    address: "",
    experience: "",
    qualification: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
    setSuccess("");
  };

  const validate = () => {
    if (!form.role) return "Select role";
    if (!form.email) return "Email required";
    if (!form.password || form.password.length < 6)
      return "Password min 6 chars";

    if (form.role === "PATIENT") {
      if (!form.name) return "Name required";
      if (!form.phone) return "Phone required";
      if (!form.age) return "Age required";
    }

    if (form.role === "DOCTOR") {
      if (!form.name) return "Name required";
      if (!form.phone) return "Phone required";
      if (!form.experience || isNaN(form.experience))
        return "Valid experience required";
    }

    // ✅ STAFF needs only email + password
    return "";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const v = validate();
    if (v) return setError(v);

    try {
      // ✅ PATIENT SIGNUP
      if (form.role === "PATIENT") {
        await http.post("/patient/signup", {
          name: form.name,
          email: form.email,
          phone: form.phone,
          password: form.password,
          age: Number(form.age),
          dob: form.dob,
          gender: form.gender,
          address: form.address,
        });

        setSuccess("Patient Registered ✅");
        setTimeout(() => navigate("/login/patient"), 1500);
      }

      // ✅ DOCTOR SIGNUP
      if (form.role === "DOCTOR") {
        await http.post("/doctor/signup", {
          name: form.name,
          email: form.email,
          phone: form.phone,
          password: form.password,
          experience: Number(form.experience),
          qualification: form.qualification,
        });

        setSuccess("Doctor Application Submitted ✅");
        setTimeout(() => navigate("/login/doctor"), 1500);
      }

      // ✅ STAFF SIGNUP (AUTH‑SERVICE)
      if (form.role === "STAFF") {
        await http.post("/auth/signup", {
          email: form.email,
          password: form.password,
          role: "STAFF",
        });

        setSuccess("Staff registered ✅ Awaiting admin approval");
        setTimeout(() => navigate("/login/staff"), 1500);
      }

    } catch (err) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data ||
        "Registration failed"
      );
    }
  };

  return (
    <Container fluid className="login-page">
      <Row className="min-vh-100">
        {/* LEFT */}
        <Col md={7} className="login-visual d-none d-md-block">
          <div className="login-overlay" />
          <div className="login-text">
            <h2>ClinicCare</h2>
            <p>Secure clinic access & role‑based registration</p>
          </div>
        </Col>

        {/* RIGHT */}
        <Col md={5} className="d-flex align-items-center justify-content-center">
          <Card className="login-card p-4">
            <h4 className="mb-3">Create Account</h4>

            {error && <Alert variant="danger">{error}</Alert>}
            {success && <Alert variant="success">{success}</Alert>}

            <Form onSubmit={handleSubmit}>
              <Form.Select
                className="mb-3"
                name="role"
                onChange={handleChange}
              >
                <option value="">Select role</option>
                <option value="PATIENT">Patient</option>
                <option value="DOCTOR">Doctor</option>
                <option value="STAFF">Staff</option>
              </Form.Select>

              {/* COMMON */}
              <Form.Control
                className="mb-3"
                name="email"
                placeholder="Email"
                onChange={handleChange}
              />
              <Form.Control
                className="mb-3"
                type="password"
                name="password"
                placeholder="Password"
                onChange={handleChange}
              />

              {/* PATIENT */}
              {form.role === "PATIENT" && (
                <>
                  <Form.Control className="mb-3" name="name" placeholder="Name" onChange={handleChange} />
                  <Form.Control className="mb-3" name="phone" placeholder="Phone" onChange={handleChange} />
                  <Form.Control className="mb-3" name="age" placeholder="Age" onChange={handleChange} />
                  <Form.Control className="mb-3" type="date" name="dob" onChange={handleChange} />
                  <Form.Select className="mb-3" name="gender" onChange={handleChange}>
                    <option value="">Gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                  </Form.Select>
                  <Form.Control className="mb-3" name="address" placeholder="Address" onChange={handleChange} />
                </>
              )}

              {/* DOCTOR */}
              {form.role === "DOCTOR" && (
                <>
                  <Form.Control className="mb-3" name="name" placeholder="Name" onChange={handleChange} />
                  <Form.Control className="mb-3" name="phone" placeholder="Phone" onChange={handleChange} />
                  <Form.Control className="mb-3" type="number" name="experience" placeholder="Experience (years)" onChange={handleChange} />
                  <Form.Control className="mb-3" name="qualification" placeholder="Qualification" onChange={handleChange} />
                </>
              )}

              <Button type="submit" className="w-100 mt-2">
                Register
              </Button>
            </Form>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}