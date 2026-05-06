import { useState } from "react";
import { Card, Button, Form, Alert } from "react-bootstrap";
import http from "../../api/http";
import { useNavigate } from "react-router-dom";

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
    if (!form.name) return "Name required";
    if (!form.email) return "Email required";
    if (!form.phone) return "Phone required";

    // ✅ PATIENT PASSWORD
    if (form.role === "PATIENT") {
      if (!form.password || form.password.length < 6)
        return "Password min 6 chars";
      if (!form.age) return "Age required";
    }

    // ✅ ✅ DOCTOR PASSWORD VALIDATION (NEW)
    if (form.role === "DOCTOR") {
      if (!form.password || form.password.length < 6)
        return "Password min 6 chars";   // ✅ ADDED
      if (!form.experience || isNaN(form.experience))
        return "Valid experience required (number)";
    }

    return "";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const v = validate();
    if (v) {
      setError(v);
      return;
    }

    try {
      let res;

      // ✅ PATIENT API
      if (form.role === "PATIENT") {
        const payload = {
          name: form.name,
          email: form.email,
          phone: form.phone,
          password: form.password,
          age: Number(form.age),
          dob: form.dob,
          gender: form.gender,
          address: form.address,
        };

        res = await http.post("/patient/signup", payload);

        setSuccess("Patient Registered ✅");
        setTimeout(() => navigate("/login/patient"), 1500);
      }

      // ✅ ✅ DOCTOR API (FIXED)
      else if (form.role === "DOCTOR") {
        const payload = {
          name: form.name,
          email: form.email,
          phone: form.phone,
          experience: parseInt(form.experience, 10),
          qualification: form.qualification,

          password: form.password,   // ✅ ✅ VERY IMPORTANT FIX
        };

        res = await http.post("/doctor/signup", payload);

        setSuccess("Doctor Application Submitted ✅ (Wait for approval)");
        setTimeout(() => navigate("/login/doctor"), 1500);
      }

    } catch (err) {
      console.error("🔴 REGISTER ERROR:", err);

      const data = err?.response?.data;

      const msg =
        data?.message ||
        (typeof data === "string" ? data : JSON.stringify(data)) ||
        err?.message ||
        "Registration failed";

      setError(msg);
    }
  };

  return (
    <Card className="p-4">
      <h4>Create Account</h4>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <Form onSubmit={handleSubmit}>

        <Form.Select name="role" onChange={handleChange}>
          <option value="">Select role</option>
          <option value="PATIENT">Patient</option>
          <option value="DOCTOR">Doctor</option>
        </Form.Select>

        <Form.Control className="mt-2" name="name" placeholder="Name" onChange={handleChange} />
        <Form.Control className="mt-2" name="email" placeholder="Email" onChange={handleChange} />
        <Form.Control className="mt-2" name="phone" placeholder="Phone" onChange={handleChange} />

        {/* ✅ COMMON PASSWORD FOR BOTH */}
        {(form.role === "PATIENT" || form.role === "DOCTOR") && (
          <Form.Control
            className="mt-2"
            type="password"
            name="password"
            placeholder="Password"
            onChange={handleChange}
          />
        )}

        {/* ✅ PATIENT */}
        {form.role === "PATIENT" && (
          <>
            <Form.Control className="mt-2" name="age" placeholder="Age" onChange={handleChange} />
            <Form.Control className="mt-2" type="date" name="dob" onChange={handleChange} />

            <Form.Select className="mt-2" name="gender" onChange={handleChange}>
              <option value="">Gender</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
            </Form.Select>

            <Form.Control className="mt-2" name="address" placeholder="Address" onChange={handleChange} />
          </>
        )}

        {/* ✅ DOCTOR */}
        {form.role === "DOCTOR" && (
          <>
            <Form.Control
              className="mt-2"
              type="number"
              name="experience"
              placeholder="Experience (years)"
              onChange={handleChange}
            />
            <Form.Control
              className="mt-2"
              name="qualification"
              placeholder="Qualification"
              onChange={handleChange}
            />
          </>
        )}

        <Button className="mt-3 w-100" type="submit">
          Register
        </Button>
      </Form>
    </Card>
  );
}
