import { useState } from "react";
import { Container, Row, Col, Card, Button, Form, Alert } from "react-bootstrap";
import http from "../../api/http";
import { useNavigate } from "react-router-dom";
import "bootstrap-icons/font/bootstrap-icons.css";
import "../Login/Login.css";
 
export default function RegisterPage() {
  const navigate = useNavigate();
 
  // ✅ SaaS-grade regex
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const phoneRegex = /^[6-9]\d{9}$/; // Indian 10-digit (starts 6-9)
 
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
 
  // ✅ Field-level errors + touched (SaaS UX)
  const [fieldErrors, setFieldErrors] = useState({});
  const [touched, setTouched] = useState({});
 
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
 
  // ✅ helpers
  const normalizePhone = (value) => {
    const digits = (value || "").replace(/\D/g, "");
    // If user types +91xxxxxxxxxx, keep last 10 digits
    return digits.length > 10 ? digits.slice(-10) : digits;
  };
 
  const todayISO = new Date().toISOString().split("T")[0];
 
  const calculateAgeFromDob = (dobStr) => {
    if (!dobStr) return null;
    const dob = new Date(dobStr);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const m = today.getMonth() - dob.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) age--;
    return age;
  };
 
  const validateField = (name, value, nextForm) => {
    const f = nextForm || form;
 
    // Common validations (email, password always required after role select)
    if (name === "role") {
      if (!value) return "Select role";
      return "";
    }
 
    if (name === "email") {
      if (!value) return "Email required";
      if (!emailRegex.test(value)) return "Invalid email format";
      return "";
    }
 
    if (name === "password") {
      if (!value) return "Password required";
      if (value.length < 6) return "Password must be at least 6 characters";
      return "";
    }
 
    // Role-based validations
    if (f.role === "PATIENT") {
      if (name === "name") {
        if (!value) return "Name required";
        if (value.trim().length < 2) return "Name must be at least 2 characters";
        return "";
      }
 
      if (name === "phone") {
        const phone10 = normalizePhone(value);
        if (!value) return "Phone required";
        if (!phoneRegex.test(phone10))
          return "Invalid phone number (10 digits, starts with 6-9)";
        return "";
      }
 
      if (name === "age") {
        if (value === "" || value === null) return "Age required";
        const age = Number(value);
        if (isNaN(age) || age < 1 || age > 120)
          return "Age must be between 1 and 120";
 
        // If dob present, ensure it matches
        if (f.dob) {
          const dobAge = calculateAgeFromDob(f.dob);
          if (dobAge !== null && Math.abs(dobAge - age) > 1)
            return "Age does not match DOB";
        }
        return "";
      }
 
      if (name === "dob") {
        if (!value) return "DOB required";
        if (value > todayISO) return "DOB cannot be in future";
 
        // If age present, ensure it matches
        if (f.age) {
          const typedAge = Number(f.age);
          const dobAge = calculateAgeFromDob(value);
          if (!isNaN(typedAge) && dobAge !== null && Math.abs(dobAge - typedAge) > 1)
            return "Age does not match DOB";
        }
        return "";
      }
 
      if (name === "gender") {
        if (!value) return "Select gender";
        return "";
      }
 
      if (name === "address") {
        if (!value) return "Address required";
        if (value.trim().length < 5) return "Address must be at least 5 characters";
        return "";
      }
    }
 
    if (f.role === "DOCTOR") {
      if (name === "name") {
        if (!value) return "Name required";
        if (value.trim().length < 2) return "Name must be at least 2 characters";
        return "";
      }
 
      if (name === "phone") {
        const phone10 = normalizePhone(value);
        if (!value) return "Phone required";
        if (!phoneRegex.test(phone10)) return "Invalid phone number";
        return "";
      }
 
      if (name === "experience") {
        if (value === "" || value === null) return "Experience required";
        const exp = Number(value);
        if (isNaN(exp) || exp < 0 || exp > 60)
          return "Experience must be between 0-60 years";
        return "";
      }
 
      if (name === "qualification") {
        if (!value) return "Qualification required";
        if (value.trim().length < 2) return "Qualification must be at least 2 characters";
        return "";
      }
    }
 
    // STAFF has only email + password -> already handled above
    return "";
  };
 
  const validateAll = (nextForm) => {
    const f = nextForm || form;
    const errs = {};
 
    // Always validate role, email, password
    errs.role = validateField("role", f.role, f);
    errs.email = validateField("email", f.email, f);
    errs.password = validateField("password", f.password, f);
 
    if (f.role === "PATIENT") {
      errs.name = validateField("name", f.name, f);
      errs.phone = validateField("phone", f.phone, f);
      errs.age = validateField("age", f.age, f);
      errs.dob = validateField("dob", f.dob, f);
      errs.gender = validateField("gender", f.gender, f);
      errs.address = validateField("address", f.address, f);
    }
 
    if (f.role === "DOCTOR") {
      errs.name = validateField("name", f.name, f);
      errs.phone = validateField("phone", f.phone, f);
      errs.experience = validateField("experience", f.experience, f);
      errs.qualification = validateField("qualification", f.qualification, f);
    }
 
    // Remove empty keys
    Object.keys(errs).forEach((k) => {
      if (!errs[k]) delete errs[k];
    });
 
    return errs;
  };
 
  const handleChange = (e) => {
    const { name, value } = e.target;
 
    const nextForm = { ...form, [name]: value };
 
    // If role changes, reset role-specific fields + errors
    if (name === "role") {
      nextForm.name = "";
      nextForm.phone = "";
      nextForm.age = "";
      nextForm.dob = "";
      nextForm.gender = "";
      nextForm.address = "";
      nextForm.experience = "";
      nextForm.qualification = "";
      setTouched({});
      setFieldErrors({});
    }
 
    setForm(nextForm);
    setError("");
    setSuccess("");
 
    // Real-time validation only for touched fields
    if (touched[name]) {
      const msg = validateField(name, value, nextForm);
      setFieldErrors((prev) => {
        const updated = { ...prev, [name]: msg };
        if (!msg) delete updated[name];
        return updated;
      });
    }
 
    // Cross-field update: age <-> dob
    if (name === "age" && touched.dob) {
      const msg = validateField("dob", nextForm.dob, nextForm);
      setFieldErrors((prev) => {
        const updated = { ...prev, dob: msg };
        if (!msg) delete updated.dob;
        return updated;
      });
    }
 
    if (name === "dob" && touched.age) {
      const msg = validateField("age", nextForm.age, nextForm);
      setFieldErrors((prev) => {
        const updated = { ...prev, age: msg };
        if (!msg) delete updated.age;
        return updated;
      });
    }
  };
 
  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((prev) => ({ ...prev, [name]: true }));
 
    const msg = validateField(name, value, form);
    setFieldErrors((prev) => {
      const updated = { ...prev, [name]: msg };
      if (!msg) delete updated[name];
      return updated;
    });
  };
 
  const getSubmitError = () => {
    const errs = validateAll(form);
    if (!form.role) return "Select role";
    if (Object.keys(errs).length > 0) return "Please fix the highlighted fields";
    return "";
  };
 
  const handleSubmit = async (e) => {
    e.preventDefault();
 
    // mark all relevant fields as touched for feedback
    const mustTouch = { role: true, email: true, password: true };
    if (form.role === "PATIENT") {
      Object.assign(mustTouch, {
        name: true,
        phone: true,
        age: true,
        dob: true,
        gender: true,
        address: true,
      });
    }
    if (form.role === "DOCTOR") {
      Object.assign(mustTouch, {
        name: true,
        phone: true,
        experience: true,
        qualification: true,
      });
    }
    setTouched((prev) => ({ ...prev, ...mustTouch }));
 
    const errs = validateAll(form);
    setFieldErrors(errs);
 
    if (Object.keys(errs).length > 0) {
      setError("Please fix the highlighted fields");
      return;
    }
 
    try {
      // ✅ PATIENT SIGNUP
      if (form.role === "PATIENT") {
        await http.post("/patient/signup", {
          name: form.name.trim(),
          email: form.email.trim(),
          phone: normalizePhone(form.phone),
          password: form.password,
          age: Number(form.age),
          dob: form.dob,
          gender: form.gender,
          address: form.address.trim(),
        });
 
        setSuccess("Patient Registered ✅");
        setTimeout(() => navigate("/login/patient"), 1500);
      }
 
      // ✅ DOCTOR SIGNUP
      if (form.role === "DOCTOR") {
        await http.post("/doctor/signup", {
          name: form.name.trim(),
          email: form.email.trim(),
          phone: normalizePhone(form.phone),
          password: form.password,
          experience: Number(form.experience),
          qualification: form.qualification.trim(),
        });
 
        setSuccess("Doctor Application Submitted ✅");
        setTimeout(() => navigate("/login/doctor"), 1500);
      }
 
      // ✅ STAFF SIGNUP (AUTH‑SERVICE)
      if (form.role === "STAFF") {
        await http.post("/auth/signup", {
          email: form.email.trim(),
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
 
  const disableSubmit = !!getSubmitError();
 
  return (
    <Container fluid className="login-page">
      <Row className="min-vh-100">
        {/* LEFT */}
        <Col md={7} className="login-visual d-none d-md-block">
          <div className="login-overlay" />
        </Col>
 
        {/* RIGHT */}
        <Col md={5} className="d-flex flex-column p-4">
          {/* ✅ Back to Home */}
          <Button
            variant="link"
            className="auth-back-btn mb-2"
            onClick={() => navigate("/")}
          >
            <i className="bi bi-arrow-left me-2"></i>
            Back to Home
          </Button>
 
          <div className="d-flex align-items-center justify-content-center flex-grow-1">
            <Card className="login-card p-4 premium-card">
              <h4 className="mb-3 text-center fw-bold">Create Account</h4>
 
              {error && <Alert variant="danger">{error}</Alert>}
              {success && <Alert variant="success">{success}</Alert>}
 
              <Form onSubmit={handleSubmit} noValidate>
                {/* ROLE */}
                <Form.Select
                  className="mb-3"
                  name="role"
                  value={form.role}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  isInvalid={touched.role && !!fieldErrors.role}
                >
                  <option value="">Select role</option>
                  <option value="PATIENT">Patient</option>
                  <option value="DOCTOR">Doctor</option>
                  <option value="STAFF">Staff</option>
                </Form.Select>
                <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                  {touched.role && fieldErrors.role ? fieldErrors.role : ""}
                </Form.Control.Feedback>
 
                {/* COMMON */}
                <Form.Control
                  className="mb-3"
                  type="email"
                  name="email"
                  placeholder="Email"
                  value={form.email}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  isInvalid={touched.email && !!fieldErrors.email}
                  autoComplete="email"
                />
                <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                  {touched.email && fieldErrors.email ? fieldErrors.email : ""}
                </Form.Control.Feedback>
 
                <Form.Control
                  className="mb-3"
                  type="password"
                  name="password"
                  placeholder="Password (min 6 characters)"
                  value={form.password}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  isInvalid={touched.password && !!fieldErrors.password}
                  autoComplete="new-password"
                />
                <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                  {touched.password && fieldErrors.password ? fieldErrors.password : ""}
                </Form.Control.Feedback>
 
                {/* PATIENT */}
                {form.role === "PATIENT" && (
                  <>
                    <Form.Control
                      className="mb-3"
                      name="name"
                      placeholder="Full Name"
                      value={form.name}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.name && !!fieldErrors.name}
                      autoComplete="name"
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.name && fieldErrors.name ? fieldErrors.name : ""}
                    </Form.Control.Feedback>
 
                    <Form.Control
                      className="mb-3"
                      type="tel"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      name="phone"
                      placeholder="Phone (10-digit)"
                      value={form.phone}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.phone && !!fieldErrors.phone}
                      autoComplete="tel"
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.phone && fieldErrors.phone ? fieldErrors.phone : ""}
                    </Form.Control.Feedback>
 
                    <Form.Control
                      className="mb-3"
                      name="age"
                      type="number"
                      min="1"
                      max="120"
                      placeholder="Age"
                      value={form.age}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.age && !!fieldErrors.age}
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.age && fieldErrors.age ? fieldErrors.age : ""}
                    </Form.Control.Feedback>
 
                    <Form.Control
                      className="mb-3"
                      type="date"
                      name="dob"
                      value={form.dob}
                      max={todayISO}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.dob && !!fieldErrors.dob}
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.dob && fieldErrors.dob ? fieldErrors.dob : ""}
                    </Form.Control.Feedback>
 
                    <Form.Select
                      className="mb-3"
                      name="gender"
                      value={form.gender}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.gender && !!fieldErrors.gender}
                    >
                      <option value="">Gender</option>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                    </Form.Select>
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.gender && fieldErrors.gender ? fieldErrors.gender : ""}
                    </Form.Control.Feedback>
 
                    <Form.Control
                      className="mb-3"
                      name="address"
                      placeholder="Address"
                      value={form.address}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.address && !!fieldErrors.address}
                      autoComplete="street-address"
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.address && fieldErrors.address ? fieldErrors.address : ""}
                    </Form.Control.Feedback>
                  </>
                )}
 
                {/* DOCTOR */}
                {form.role === "DOCTOR" && (
                  <>
                    <Form.Control
                      className="mb-3"
                      name="name"
                      placeholder="Full Name"
                      value={form.name}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.name && !!fieldErrors.name}
                      autoComplete="name"
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.name && fieldErrors.name ? fieldErrors.name : ""}
                    </Form.Control.Feedback>
 
                    <Form.Control
                      className="mb-3"
                      type="tel"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      name="phone"
                      placeholder="Phone (10-digit)"
                      value={form.phone}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.phone && !!fieldErrors.phone}
                      autoComplete="tel"
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.phone && fieldErrors.phone ? fieldErrors.phone : ""}
                    </Form.Control.Feedback>
 
                    <Form.Control
                      className="mb-3"
                      type="number"
                      name="experience"
                      min="0"
                      max="60"
                      placeholder="Experience (years)"
                      value={form.experience}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.experience && !!fieldErrors.experience}
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.experience && fieldErrors.experience ? fieldErrors.experience : ""}
                    </Form.Control.Feedback>
 
                    <Form.Control
                      className="mb-3"
                      name="qualification"
                      placeholder="Qualification"
                      value={form.qualification}
                      onChange={handleChange}
                      onBlur={handleBlur}
                      isInvalid={touched.qualification && !!fieldErrors.qualification}
                    />
                    <Form.Control.Feedback type="invalid" className="mb-2 d-block">
                      {touched.qualification && fieldErrors.qualification ? fieldErrors.qualification : ""}
                    </Form.Control.Feedback>
                  </>
                )}
 
                <Button type="submit" className="w-100 mt-2" disabled={disableSubmit}>
                  Register
                </Button>
 
                {/* ✅ Already have account */}
                <div className="text-center mt-3">
                  <span className="text-muted small d-block mb-2">
                    Already have an account?
                  </span>
 
                  <Button
                    variant="outline-primary"
                    size="sm"
                    onClick={() => navigate("/login")}
                  >
                    Login
                  </Button>
                </div>
 
                {disableSubmit && (
                  <div className="text-muted small mt-2">
                    Tip: Fill required fields to enable Register.
                  </div>
                )}
              </Form>
            </Card>
          </div>
        </Col>
      </Row>
    </Container>
  );
}
 