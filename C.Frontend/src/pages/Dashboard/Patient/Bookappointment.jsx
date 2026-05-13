import { useEffect, useMemo, useState } from "react";
import { Container, Card, Button, Form, Badge } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./BookAppointment.css";

function pad(n) {
  return String(n).padStart(2, "0");
}
function toYMD(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
function startOfMonth(d) {
  return new Date(d.getFullYear(), d.getMonth(), 1);
}
function daysInMonth(d) {
  return new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
}
function sameDay(a, b) {
  return a && b && a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}
function isPastDay(d) {
  const today = new Date();
  today.setHours(0,0,0,0);
  const x = new Date(d);
  x.setHours(0,0,0,0);
  return x < today;
}
function formatSlotLabel(t24) {
  const [hh, mm] = t24.split(":").map(Number);
  const ampm = hh >= 12 ? "pm" : "am";
  const hh12 = hh % 12 === 0 ? 12 : hh % 12;
  return `${pad(hh12)}:${pad(mm)} ${ampm}`;
}

export default function BookAppointment() {
  const navigate = useNavigate();

  const [specialities, setSpecialities] = useState([]);
  const [specialityId, setSpecialityId] = useState("");

  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState("");

  // We'll store date as YYYY-MM-DD (same as your backend expects)
  const [date, setDate] = useState("");
  const [time, setTime] = useState("");

  const [symptoms, setSymptoms] = useState("");
  const [msg, setMsg] = useState("");

  // Calendar state
  const [monthCursor, setMonthCursor] = useState(() => startOfMonth(new Date()));
  const selectedDateObj = useMemo(() => (date ? new Date(date + "T00:00:00") : null), [date]);

  const timeSlots = useMemo(
    () => ["09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00"],
    []
  );

  useEffect(() => {
    (async () => {
      try {
        const res = await http.get("/speciality/");
        setSpecialities(res.data || []);
        setMsg("");
      } catch (err) {
        setMsg("❌ Failed to load specialities (backend returned error/403)");
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      if (!specialityId) return;
      try {
        const res = await http.get(`/speciality/${specialityId}/doctors`);
        setDoctors(res.data || []);
        setMsg("");
      } catch (err) {
        setMsg("❌ Failed to load doctors (backend returned error/403)");
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

  // calendar grid data
  const cal = useMemo(() => {
    const first = startOfMonth(monthCursor);
    const total = daysInMonth(monthCursor);
    const startWeekday = first.getDay(); // 0 Sun ... 6 Sat
    const cells = [];
    // blanks
    for (let i = 0; i < startWeekday; i++) cells.push(null);
    for (let d = 1; d <= total; d++) {
      cells.push(new Date(first.getFullYear(), first.getMonth(), d));
    }
    // pad to complete weeks (optional)
    while (cells.length % 7 !== 0) cells.push(null);
    return cells;
  }, [monthCursor]);

  const monthTitle = useMemo(() => {
    const m = monthCursor.toLocaleString("en-US", { month: "long" });
    return `${m} ${monthCursor.getFullYear()}`;
  }, [monthCursor]);

  const stepBadge = useMemo(() => {
    if (!specialityId) return "Step 1/5: Select speciality";
    if (!doctorId) return "Step 2/5: Select doctor";
    if (!date) return "Step 3/5: Select date";
    if (!time) return "Step 4/5: Select time";
    return "Step 5/5: Confirm";
  }, [specialityId, doctorId, date, time]);

  return (
    <div className="pd-root ba-root">
      <Container className="ba-container">
        {/* HERO TITLE CENTER (like your reference) */}
        <div className="ba-heroCenter">
          <h2>Make an Appointment</h2>
          <p>Choose speciality, doctor, date & time slot</p>
          <Badge bg="info" className="ba-step">{stepBadge}</Badge>
        </div>

        {/* Top filters row (keeps your functionality) */}
        <Card className="pd-card ba-wideCard">
          <Card.Body>
            <div className="ba-topFilters">
              <Form.Group className="ba-field">
                <Form.Label>Speciality</Form.Label>
                <Form.Select
                  value={specialityId}
                  onChange={(e) => {
                    setSpecialityId(e.target.value);
                    setDoctorId("");
                    setTime("");
                  }}
                >
                  <option value="">Select speciality</option>
                  {specialities.map((s) => (
                    <option key={s.specialityId} value={s.specialityId}>
                      {s.name}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>

              <Form.Group className="ba-field">
                <Form.Label>Doctor</Form.Label>
                <Form.Select
                  value={doctorId}
                  onChange={(e) => {
                    setDoctorId(e.target.value);
                    setTime("");
                  }}
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

              <Form.Group className="ba-field">
                <Form.Label>Symptoms (Optional)</Form.Label>
                <Form.Control
                  placeholder="Describe your symptoms"
                  value={symptoms}
                  onChange={(e) => setSymptoms(e.target.value)}
                />
              </Form.Group>
            </div>

            {/* Main grid exactly like reference: Left calendar, Right slots */}
            <div className="ba-grid">
              {/* LEFT: Calendar */}
              <div className="ba-calendar">
                <div className="ba-calHeader">
                  <div className="ba-calTitle">Select Date</div>
                  <div className="ba-calNav">
                    <button
                      type="button"
                      className="ba-navBtn"
                      onClick={() => setMonthCursor(new Date(monthCursor.getFullYear(), monthCursor.getMonth() - 1, 1))}
                      aria-label="Prev month"
                    >
                      ‹
                    </button>
                    <span className="ba-month">{monthTitle}</span>
                    <button
                      type="button"
                      className="ba-navBtn"
                      onClick={() => setMonthCursor(new Date(monthCursor.getFullYear(), monthCursor.getMonth() + 1, 1))}
                      aria-label="Next month"
                    >
                      ›
                    </button>
                  </div>
                </div>

                <div className="ba-weekdays">
                  {["Sun","Mon","Tue","Wed","Thu","Fri","Sat"].map((w) => (
                    <div key={w} className="ba-wd">{w}</div>
                  ))}
                </div>

                <div className="ba-days">
                  {cal.map((d, idx) => {
                    if (!d) return <div key={idx} className="ba-day empty" />;
                    const disabled = !doctorId || isPastDay(d);
                    const active = sameDay(d, selectedDateObj);
                    return (
                      <button
                        type="button"
                        key={idx}
                        className={`ba-dayBtn ${active ? "active" : ""}`}
                        disabled={disabled}
                        onClick={() => {
                          setDate(toYMD(d));
                          setTime("");
                        }}
                        title={toYMD(d)}
                      >
                        {d.getDate()}
                      </button>
                    );
                  })}
                </div>

                <div className="ba-calHint">
                  {doctorId ? "Pick any available date" : "Select doctor to enable date selection"}
                </div>
              </div>

              {/* RIGHT: Slots */}
              <div className="ba-slotsPanel">
                <div className="ba-slotHeader">
                  <div className="ba-calTitle">Select Time</div>
                  <div className="ba-slotHint">
                    {date ? `For ${date}` : "Select date first"}
                  </div>
                </div>

                <div className={`ba-slotGrid ${(!date || !doctorId) ? "disabled" : ""}`}>
                  {timeSlots.map((t) => (
                    <button
                      key={t}
                      type="button"
                      className={`ba-slotBtn ${time === t ? "active" : ""}`}
                      disabled={!date || !doctorId}
                      onClick={() => setTime(t)}
                    >
                      {formatSlotLabel(t)}
                    </button>
                  ))}
                </div>

                <Button
                  className="ba-cta"
                  variant="primary"
                  disabled={!specialityId || !doctorId || !date || !time}
                  onClick={confirmBooking}
                >
                  Get Appointment
                </Button>
              </div>
            </div>

            {msg && <div className="ba-msg">{msg}</div>}
          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}