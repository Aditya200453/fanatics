import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";

export default function BookAppointment() {
  const navigate = useNavigate();

  const [specialities, setSpecialities] = useState([]);
  const [specialityId, setSpecialityId] = useState("");

  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState("");

  const [date, setDate] = useState("");
  const [time, setTime] = useState(""); // HH:mm from clock
  const [symptoms, setSymptoms] = useState("");

  const [msg, setMsg] = useState("");

  // ✅ Load specialities (YOUR controller uses GET /speciality/)
  useEffect(() => {
    (async () => {
      try {
        setMsg("");
        const res = await http.get("/speciality/"); // ✅ FIXED
        setSpecialities(res.data || []);
      } catch (e) {
        console.error(e);
        setMsg("❌ Failed to load specialities");
      }
    })();
  }, []);

  // ✅ Load doctors when speciality changes (YOUR controller uses /speciality/{id}/doctors)
  useEffect(() => {
    (async () => {
      try {
        setDoctors([]);
        setDoctorId("");
        if (!specialityId) return;

        setMsg("");
        const res = await http.get(`/speciality/${specialityId}/doctors`); // ✅ FIXED
        setDoctors(res.data || []);
      } catch (e) {
        console.error(e);
        setMsg("❌ Failed to load doctors for selected speciality");
      }
    })();
  }, [specialityId]);

  // ✅ Confirm booking
  const confirmBooking = async () => {
    try {
      setMsg("");

      const payload = {
        doctorId: Number(doctorId),
        appointmentDate: date,     // yyyy-MM-dd
        appointmentTime: time,     // HH:mm
        symptoms: symptoms || null
      };

      await http.post("/appointment/book", payload);

      setMsg("✅ Appointment booked successfully!");
      // Go to My Appointments automatically
      setTimeout(() => navigate("/dashboard/patient/appointments"), 800);

    } catch (e) {
      console.error(e);
      const data = e?.response?.data;
      setMsg(
        data?.message ||
        (typeof data === "string" ? data : "❌ Booking failed")
      );
    }
  };

  return (
    <div style={{ padding: 20, maxWidth: 520 }}>
      <h2>Book Appointment</h2>

      {/* ✅ Speciality */}
      <label>Speciality</label>
      <select
        value={specialityId}
        onChange={(e) => setSpecialityId(e.target.value)}
        style={{ width: "100%", marginBottom: 12 }}
      >
        <option value="">Select speciality</option>
        {specialities.map((s) => (
          <option key={s.specialityId} value={s.specialityId}>
            {s.specialityId} - {s.name}
          </option>
        ))}
      </select>

      {/* ✅ Doctors for that speciality */}
      <label>Doctor</label>
      <select
        value={doctorId}
        onChange={(e) => setDoctorId(e.target.value)}
        style={{ width: "100%", marginBottom: 12 }}
        disabled={!specialityId}
      >
        <option value="">Select doctor</option>
        {doctors.map((d) => (
          <option key={d.doctorId} value={d.doctorId}>
            {d.doctorId} - {d.name} ({d.qualification}) [{d.status}]
          </option>
        ))}
      </select>

      {/* ✅ Date */}
      <label>Date</label>
      <input
        type="date"
        value={date}
        onChange={(e) => setDate(e.target.value)}
        style={{ width: "100%", marginBottom: 12 }}
      />

      {/* ✅ Time (clock picker) */}
      <label>Time</label>
      <input
        type="time"
        value={time}
        onChange={(e) => setTime(e.target.value)}
        style={{ width: "100%", marginBottom: 12 }}
      />

      <label>Symptoms</label>
      <input
        value={symptoms}
        onChange={(e) => setSymptoms(e.target.value)}
        style={{ width: "100%", marginBottom: 12 }}
        placeholder="Symptoms"
      />

      <button
        onClick={confirmBooking}
        disabled={!specialityId || !doctorId || !date || !time}
      >
        Confirm Booking
      </button>

      {msg && <p style={{ marginTop: 12 }}>{msg}</p>}
    </div>
  );
}
