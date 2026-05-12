import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./AdminDashboard.css";

export default function AdminDashboard() {
  const navigate = useNavigate();

  // ---------------- DOCTORS ----------------
  const [doctors, setDoctors] = useState([]);
  const [specialities, setSpecialities] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState("");
  const [selectedSpeciality, setSelectedSpeciality] = useState("");
  const [loadingDoctors, setLoadingDoctors] = useState(true);

  // ---------------- STAFF ----------------
  const [staff, setStaff] = useState([]);
  const [loadingStaff, setLoadingStaff] = useState(true);

  const [msg, setMsg] = useState("");

  // ------------ LOAD DOCTORS ------------
  const loadDoctors = async () => {
    setLoadingDoctors(true);
    setMsg("");
    try {
      const res = await http.get("/doctor/admin/doctors");
      setDoctors(res.data || []);
    } catch (err) {
      setMsg("❌ Failed to load doctors");
    } finally {
      setLoadingDoctors(false);
    }
  };

  // ------------ LOAD SPECIALITIES ------------
  const loadSpecialities = async () => {
    try {
      const res = await http.get("/speciality/");
      setSpecialities(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  // ------------ LOAD PENDING STAFF ------------
  const loadPendingStaff = async () => {
    setLoadingStaff(true);
    setMsg("");
    try {
      const res = await http.get("/auth/admin/staff/pending");
      setStaff(res.data || []);
    } catch (err) {
      setMsg("❌ Failed to load pending staff");
    } finally {
      setLoadingStaff(false);
    }
  };

  useEffect(() => {
    loadDoctors();
    loadSpecialities();
    loadPendingStaff();
  }, []);

  // ------------ APPROVE DOCTOR ------------
  const approveDoctor = async (doctorId) => {
    try {
      await http.post(`/doctor/admin/approve/${doctorId}`);
      setMsg("✅ Doctor approved successfully");
      loadDoctors();
    } catch {
      setMsg("❌ Doctor approval failed");
    }
  };

  // ------------ MAP DOCTOR -> SPECIALITY ------------
  const mapDoctor = async () => {
    if (!selectedDoctor || !selectedSpeciality) {
      setMsg("❌ Select doctor and speciality");
      return;
    }

    try {
      await http.post("/speciality/map", {
        doctorId: Number(selectedDoctor),
        specialityId: Number(selectedSpeciality),
      });
      setMsg("✅ Doctor mapped successfully");
    } catch {
      setMsg("❌ Mapping failed");
    }
  };

  // ------------ APPROVE STAFF ------------
  const approveStaff = async (id) => {
    try {
      await http.put(`/auth/admin/staff/${id}/approve`);
      setMsg("✅ Staff approved successfully");
      loadPendingStaff();
    } catch {
      setMsg("❌ Staff approval failed");
    }
  };

  // ------------ REJECT STAFF ------------
  const rejectStaff = async (id) => {
    try {
      await http.put(`/auth/admin/staff/${id}/reject`);
      setMsg("✅ Staff rejected/disabled");
      loadPendingStaff();
    } catch {
      setMsg("❌ Staff reject failed");
    }
  };

  return (
    <div className="admin-container">
      <div className="admin-header-row">
        <h2 className="admin-title">Admin Dashboard ✅</h2>

        {/* ✅ LOGIN AS OTHER */}
        <button
          className="btn btn-secondary"
          onClick={() => navigate("/login")}
        >
          Login as Other
        </button>
      </div>

      {msg && <div className="admin-msg">{msg}</div>}

      {/* REFRESH */}
      <div className="card">
        <button className="btn btn-secondary me-2" onClick={loadDoctors}>
          Refresh Doctors
        </button>
        <button className="btn btn-secondary" onClick={loadPendingStaff}>
          Refresh Staff
        </button>
      </div>

      {/* STAFF APPROVAL */}
      <div className="card">
        <h3>Pending Staff Approvals</h3>

        {loadingStaff ? (
          <h4>Loading staff...</h4>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th style={{ width: "220px" }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {staff.length === 0 ? (
                <tr>
                  <td colSpan="5">No pending staff</td>
                </tr>
              ) : (
                staff.map((s) => (
                  <tr key={s.id}>
                    <td>{s.id}</td>
                    <td>{s.email}</td>
                    <td>{s.role}</td>
                    <td>
                      <span className="status-pending">{s.status}</span>
                    </td>
                    <td>
                      <button
                        className="btn btn-success me-2"
                        onClick={() => approveStaff(s.id)}
                      >
                        Accept
                      </button>
                      <button
                        className="btn btn-danger"
                        onClick={() => rejectStaff(s.id)}
                      >
                        Reject
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* MAP DOCTOR -> SPECIALITY */}
      <div className="card">
        <h3>Assign Doctor to Speciality</h3>

        <div className="form-row">
          <select
            value={selectedDoctor}
            onChange={(e) => setSelectedDoctor(e.target.value)}
          >
            <option value="">Select Doctor</option>
            {doctors.map((d) => (
              <option key={d.doctorId} value={d.doctorId}>
                {d.name}
              </option>
            ))}
          </select>

          <select
            value={selectedSpeciality}
            onChange={(e) => setSelectedSpeciality(e.target.value)}
          >
            <option value="">Select Speciality</option>
            {specialities.map((s) => (
              <option key={s.specialityId} value={s.specialityId}>
                {s.name}
              </option>
            ))}
          </select>

          <button className="btn btn-primary" onClick={mapDoctor}>
            Map Doctor
          </button>
        </div>
      </div>

      {/* DOCTOR LIST */}
      <div className="card">
        {loadingDoctors ? (
          <h3>Loading doctors...</h3>
        ) : (
          <table>
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
                      <span
                        className={
                          doc.status === "ACTIVE"
                            ? "status-active"
                            : "status-pending"
                        }
                      >
                        {doc.status}
                      </span>
                    </td>
                    <td>
                      {doc.status === "PENDING" ? (
                        <button
                          className="btn btn-success"
                          onClick={() => approveDoctor(doc.doctorId)}
                        >
                          Approve
                        </button>
                      ) : (
                        "Approved"
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
