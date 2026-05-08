import { useEffect, useState } from "react";
import http from "../../../api/http";

export default function AdminDashboard() {
  const [doctors, setDoctors] = useState([]);
  const [specialities, setSpecialities] = useState([]);

  const [selectedDoctor, setSelectedDoctor] = useState("");
  const [selectedSpeciality, setSelectedSpeciality] = useState("");

  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState("");

  // ✅ LOAD DOCTORS
  const loadDoctors = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get("/doctor/admin/doctors");
      setDoctors(res.data || []);
    } catch (err) {
      console.error("Load doctors error:", err);
      const status = err?.response?.status;
      const data = err?.response?.data;

      const text =
        data?.message ||
        (typeof data === "string" ? data : JSON.stringify(data)) ||
        err?.message ||
        "Failed to load doctors";

      setMsg(status ? `❌ ${text} (${status})` : `❌ ${text}`);
    } finally {
      setLoading(false);
    }
  };

  // ✅ LOAD SPECIALITIES
  const loadSpecialities = async () => {
    try {
      const res = await http.get("/speciality/");
      setSpecialities(res.data || []);
    } catch (err) {
      console.error("Load specialities error:", err);
    }
  };

  useEffect(() => {
    loadDoctors();
    loadSpecialities();
  }, []);

  // ✅ APPROVE DOCTOR
  const approveDoctor = async (doctorId) => {
    setMsg("");
    try {
      await http.post(`/doctor/admin/approve/${doctorId}`);
      setMsg("✅ Doctor approved successfully");
      await loadDoctors();
    } catch (err) {
      console.error("Approve error:", err);

      const status = err?.response?.status;
      const data = err?.response?.data;

      const text =
        data?.message ||
        (typeof data === "string" ? data : JSON.stringify(data)) ||
        err?.message ||
        "Approval failed";

      setMsg(status ? `❌ ${text} (${status})` : `❌ ${text}`);
    }
  };

  // ✅ MAP DOCTOR TO SPECIALITY
  const mapDoctor = async () => {
    setMsg("");

    if (!selectedDoctor || !selectedSpeciality) {
      setMsg("❌ Select doctor and speciality");
      return;
    }

    try {
      await http.post("/speciality/map", {
        doctorId: Number(selectedDoctor),
        specialityId: Number(selectedSpeciality),
      });

      setMsg("✅ Doctor mapped to speciality");
    } catch (err) {
      console.error("Mapping error:", err);
      setMsg("❌ Mapping failed");
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>Admin Dashboard ✅</h2>

      {msg && <p style={{ marginTop: 10 }}>{msg}</p>}

      {/* ✅ REFRESH BUTTON */}
      <div style={{ marginTop: 10, marginBottom: 10 }}>
        <button onClick={loadDoctors}>Refresh</button>
      </div>

      {/* ✅ ✅ NEW SECTION → DOCTOR MAPPING */}
      <h3>Assign Doctor to Speciality</h3>

      <div style={{ marginBottom: 15 }}>
        {/* Doctor Dropdown */}
        <select onChange={(e) => setSelectedDoctor(e.target.value)}>
          <option value="">Select Doctor</option>
          {doctors.map((d) => (
            <option key={d.doctorId} value={d.doctorId}>
              {d.name} ({d.doctorId})
            </option>
          ))}
        </select>

        {/* Speciality Dropdown */}
        <select
          onChange={(e) => setSelectedSpeciality(e.target.value)}
          style={{ marginLeft: 10 }}
        >
          <option value="">Select Speciality</option>
          {specialities.map((s) => (
            <option key={s.specialityId} value={s.specialityId}>
              {s.name}
            </option>
          ))}
        </select>

        <button onClick={mapDoctor} style={{ marginLeft: 10 }}>
          Map Doctor ✅
        </button>
      </div>

      {/* ✅ DOCTOR TABLE */}
      {loading ? (
        <h3>Loading doctors...</h3>
      ) : (
        <table border="1" cellPadding="10" style={{ width: "100%" }}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Experience</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>
            {doctors.length === 0 ? (
              <tr>
                <td colSpan="6">No doctors found</td>
              </tr>
            ) : (
              doctors.map((doc) => (
                <tr key={doc.doctorId}>
                  <td>{doc.doctorId}</td>
                  <td>{doc.name}</td>
                  <td>{doc.email}</td>
                  <td>{doc.experience}</td>
                  <td>
                    <b
                      style={{
                        color:
                          doc.status === "ACTIVE" ? "green" : "orange",
                      }}
                    >
                      {doc.status}
                    </b>
                  </td>
                  <td>
                    {doc.status === "PENDING" ? (
                      <button
                        onClick={() => approveDoctor(doc.doctorId)}
                      >
                        Approve ✅
                      </button>
                    ) : (
                      <span>Approved</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}