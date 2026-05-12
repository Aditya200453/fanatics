import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./AdminDashboard.css";

export default function AdminDashboard() {
  const navigate = useNavigate();

  // ✅ DOCTORS
  const [doctors, setDoctors] = useState([]);
  const [loadingDoctors, setLoadingDoctors] = useState(true);

  // ✅ SPECIALITIES
  const [specialities, setSpecialities] = useState([]);

  // ✅ store selected speciality per doctor
  const [selectedSpecByDoctor, setSelectedSpecByDoctor] = useState({});

  // ✅ STAFF
  const [staff, setStaff] = useState([]);
  const [loadingStaff, setLoadingStaff] = useState(true);

  const [msg, setMsg] = useState("");

  // ------------ LOAD DOCTORS ------------
  const loadDoctors = async () => {
    setLoadingDoctors(true);
    try {
      const res = await http.get("/doctor/admin/doctors");
      setDoctors(res.data || []);
    } catch {
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
    } catch {
      setMsg("❌ Failed to load specialities");
    }
  };

  // ------------ LOAD STAFF ------------
  const loadPendingStaff = async () => {
    setLoadingStaff(true);
    try {
      const res = await http.get("/auth/admin/staff/pending");
      setStaff(res.data || []);
    } catch {
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

  // ✅ ✅ APPROVE + MAP COMBINED
  const approveAndMapDoctor = async (doctorId) => {
    setMsg("");

    const specialityId = selectedSpecByDoctor[doctorId];

    if (!specialityId) {
      setMsg("❌ Please select speciality first");
      return;
    }

    try {
      // ✅ Approve doctor
      await http.post(`/doctor/admin/approve/${doctorId}`);

      // ✅ Map doctor
      await http.post("/speciality/map", {
        doctorId: Number(doctorId),
        specialityId: Number(specialityId),
      });

      setMsg("✅ Doctor approved & mapped successfully");
      loadDoctors();
    } catch {
      setMsg("❌ Failed to approve/map doctor");
    }
  };

  return (
    <div className="admin-container">

      {/* HEADER */}
      <div className="admin-header-row">
        <h2 className="admin-title">Admin Dashboard ✅</h2>
        <button className="btn btn-secondary" onClick={() => navigate("/login")}>
          Login as Other
        </button>
      </div>

      {msg && <div className="admin-msg">{msg}</div>}

      {/* ✅ STAFF APPROVAL */}
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
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {staff.length === 0 ? (
                <tr><td colSpan="5">No pending staff</td></tr>
              ) : (
                staff.map((s) => (
                  <tr key={s.id}>
                    <td>{s.id}</td>
                    <td>{s.email}</td>
                    <td>{s.role}</td>
                    <td>{s.status}</td>
                    <td>
                      <button className="btn btn-success me-2">
                        Approve
                      </button>
                      <button className="btn btn-danger">
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

      {/* ✅ DOCTOR TABLE */}
      <div className="card">
        <h3>Doctors</h3>

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
                <th>Speciality</th> {/* ✅ NEW */}
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              {doctors.length === 0 ? (
                <tr><td colSpan="7">No doctors found</td></tr>
              ) : (
                doctors.map((doc) => (
                  <tr key={doc.doctorId}>
                    <td>{doc.doctorId}</td>
                    <td>{doc.name}</td>
                    <td>{doc.email}</td>
                    <td>{doc.experience}</td>

                    <td>
                      <span className={
                        doc.status === "ACTIVE"
                          ? "status-active"
                          : "status-pending"
                      }>
                        {doc.status}
                      </span>
                    </td>

                    {/* ✅ DROPDOWN INSIDE ROW */}
                    <td>
                      {doc.status === "PENDING" ? (
                        <select
                          value={selectedSpecByDoctor[doc.doctorId] || ""}
                          onChange={(e) =>
                            setSelectedSpecByDoctor(prev => ({
                              ...prev,
                              [doc.doctorId]: e.target.value
                            }))
                          }
                        >
                          <option value="">Select Speciality</option>
                          {specialities.map((s) => (
                            <option key={s.specialityId} value={s.specialityId}>
                              {s.name}
                            </option>
                          ))}
                        </select>
                      ) : "-"
                      }
                    </td>

                    <td>
                      {doc.status === "PENDING" ? (
                        <button
                          className="btn btn-success"
                          onClick={() => approveAndMapDoctor(doc.doctorId)}
                        >
                          Approve ✅
                        </button>
                      ) : "Approved"}
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
