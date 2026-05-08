import { useEffect, useState } from "react";
import { Container, Card, Button, Form } from "react-bootstrap";
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

  useEffect(() => {
    (async () => {
      try {
        const res = await http.get("/speciality/");
        setSpecialities(res.data || []);
      } catch {
        setMsg("❌ Failed to load specialities");
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      if (!specialityId) return;
      try {
        const res = await http.get(`/speciality/${specialityId}/doctors`);
        setDoctors(res.data || []);
      } catch {
        setMsg("❌ Failed to load doctors");
      }
    })();
  }, [specialityId]);

  const confirmBooking = async () => {
    try {
      await http.post("/appointment/book", {
        doctorId: Number(doctorId),
        appointmentDate: date,
        appointmentTime: time,
        symptoms: symptoms || null,
      });

      setMsg("✅ Appointment booked successfully!");
      setTimeout(() => navigate("/dashboard/patient/appointments"), 800);
    } catch {
      setMsg("❌ Booking failed");
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
              <p>
                Select speciality, doctor and preferred time slot
              </p>
            </div>

            {/* FORM */}
            <Form className="ba-form">
              <div className="ba-section">
                <Form.Group>
                  <Form.Label>Speciality</Form.Label>
                  <Form.Select
                    value={specialityId}
                    onChange={(e) => setSpecialityId(e.target.value)}
                  >
                    <option value="">Select speciality</option>
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
                    disabled={!specialityId}
                  >
                    <option value="">Select doctor</option>
                    {doctors.map((d) => (
                      <option key={d.doctorId} value={d.doctorId}>
                        {d.name} ({d.qualification})
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
                disabled={!specialityId || !doctorId || !date || !time}
                onClick={confirmBooking}
              >
                Confirm Booking
              </Button>

              {msg && <div className="ba-msg">{msg}</div>}
            </Form>
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}