import { useEffect, useState } from "react";
import { Container, Card, Button, Form, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./BookAppointment.css";

export default function BookAppointment() {
  const navigate = useNavigate();

  const [specialities, setSpecialities] = useState([]);
  const [specialityId, setSpecialityId] = useState("");

  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState("");

  const [date, setDate] = useState("");
  const [time, setTime] = useState("");
  const [symptoms, setSymptoms] = useState("");

  const [msg, setMsg] = useState("");
  const [variant, setVariant] = useState("danger");

  const [loadingSpec, setLoadingSpec] = useState(false);
  const [loadingDocs, setLoadingDocs] = useState(false);
  const [booking, setBooking] = useState(false);

  const showError = (err, fallback) => {
    const status = err?.response?.status;
    const data = err?.response?.data;
    const text =
      data?.message ||
      (typeof data === "string" ? data : "") ||
      fallback;

    setVariant("danger");
    setMsg(status ? `${text} (HTTP ${status})` : text);
  };

  const showSuccess = (text) => {
    setVariant("success");
    setMsg(text);
  };

  // ✅ Load all specialities (PATIENT must be allowed at gateway)
  useEffect(() => {
    (async () => {
      setLoadingSpec(true);
      setMsg("");
      try {
        const res = await http.get("/speciality/");
        setSpecialities(res.data || []);
      } catch (err) {
        showError(err, "Failed to load specialities");
      } finally {
        setLoadingSpec(false);
      }
    })();
  }, []);

  // ✅ Load doctors when speciality changes
  useEffect(() => {
    (async () => {
      // reset dependent selections
      setDoctors([]);
      setDoctorId("");

      if (!specialityId) return;

      setLoadingDocs(true);
      setMsg("");

      try {
        const res = await http.get(`/speciality/${specialityId}/doctors`);
        setDoctors(res.data || []);

        if (!res.data || res.data.length === 0) {
          setVariant("danger");
          setMsg("No doctors mapped to this speciality yet.");
        }
      } catch (err) {
        showError(err, "Failed to load doctors");
      } finally {
        setLoadingDocs(false);
      }
    })();
  }, [specialityId]);

  // ✅ Book appointment
  const confirmBooking = async () => {
    if (!specialityId || !doctorId || !date || !time) {
      setVariant("danger");
      setMsg("Please select speciality, doctor, date and time.");
      return;
    }

    setBooking(true);
    setMsg("");

    try {
      await http.post("/appointment/book", {
        doctorId: Number(doctorId),
        appointmentDate: date,   // LocalDate ✅
        appointmentTime: time,   // LocalTime HH:mm ✅
        symptoms: symptoms?.trim() || null,
      });

      // ✅ NEW message aligned to your PENDING → approval workflow
      showSuccess("✅ Appointment request sent. Waiting for staff approval.");
      setTimeout(() => navigate("/dashboard/patient/appointments"), 800);
    } catch (err) {
      showError(err, "Booking failed");
    } finally {
      setBooking(false);
    }
  };

  return (
    <div className="pd-root ba-root">
      {/* background overlay same as dashboard */}
      <div className="ba-bg-overlay" />

      <Container fluid className="d-flex justify-content-center align-items-start pt-5">
        <Card className="pd-glass-card ba-card">
          <Card.Body>
            {/* HEADER */}
            <div className="ba-header">
              <h3>Book Appointment</h3>
              <p>Select speciality, doctor and preferred time slot</p>
            </div>

            {msg && <Alert variant={variant}>{msg}</Alert>}

            {/* FORM */}
            <Form className="ba-form">
              <div className="ba-section">
                <Form.Group>
                  <Form.Label>Speciality</Form.Label>
                  <Form.Select
                    value={specialityId}
                    onChange={(e) => setSpecialityId(e.target.value)}
                    disabled={loadingSpec}
                  >
                    <option value="">
                      {loadingSpec ? "Loading..." : "Select speciality"}
                    </option>
                    {specialities.map((s) => (
                      <option key={s.specialityId} value={s.specialityId}>
                        {s.name}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>

                <Form.Group>
                  <Form.Label>Doctor</Form.Label>
                  <Form.Select
                    value={doctorId}
                    onChange={(e) => setDoctorId(e.target.value)}
                    disabled={!specialityId || loadingDocs || doctors.length === 0}
                  >
                    <option value="">
                      {loadingDocs
                        ? "Loading..."
                        : doctors.length === 0
                        ? "No doctors available"
                        : "Select doctor"}
                    </option>
                    {doctors.map((d) => (
                      <option key={d.doctorId} value={d.doctorId}>
                        {d.name}
                        {d.qualification ? ` (${d.qualification})` : ""}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </div>

              <div className="ba-section">
                <Form.Group>
                  <Form.Label>Date</Form.Label>
                  <Form.Control
                    type="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                  />
                </Form.Group>

                <Form.Group>
                  <Form.Label>Time</Form.Label>
                  <Form.Control
                    type="time"
                    value={time}
                    onChange={(e) => setTime(e.target.value)}
                  />
                </Form.Group>
              </div>

              <div className="ba-section">
                <Form.Group>
                  <Form.Label>Symptoms</Form.Label>
                  <Form.Control
                    placeholder="Optional"
                    value={symptoms}
                    onChange={(e) => setSymptoms(e.target.value)}
                  />
                </Form.Group>
              </div>

              {/* CTA */}
              <Button
                variant="info"
                className="ba-btn"
                disabled={
                  booking ||
                  !specialityId ||
                  !doctorId ||
                  !date ||
                  !time ||
                  doctors.length === 0
                }
                onClick={confirmBooking}
              >
                {booking ? "Booking..." : "Confirm Booking"}
              </Button>
            </Form>
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}
