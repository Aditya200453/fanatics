import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./AdminDashboard.css";

export default function AdminDashboard() {
  const navigate = useNavigate();

  const [doctors, setDoctors] = useState([]);
  const [staff, setStaff] = useState([]);
  const [specialities, setSpecialities] = useState([]);
  const [selectedSpecByDoctor, setSelectedSpecByDoctor] = useState({});
  const [msg, setMsg] = useState("");

  /* ===== SEARCH & FILTER STATE ===== */
  const [staffSearch, setStaffSearch] = useState("");
  const [doctorSearch, setDoctorSearch] = useState("");
  const [doctorStatus, setDoctorStatus] = useState("ALL");
  const [doctorSpeciality, setDoctorSpeciality] = useState("ALL");

  // ================= LOAD =================
  const load = async () => {
    try {
      const [docRes, staffRes, specRes] = await Promise.all([
        http.get("/doctor/admin/doctors"),
        http.get("/auth/admin/staff/pending"),
        http.get("/speciality/")
      ]);

      setDoctors(docRes.data || []);
      setStaff(staffRes.data || []);
      setSpecialities(specRes.data || []);
    } catch {
      setMsg("❌ Failed to load data");
    }
  };

  useEffect(() => {
    load();
  }, []);
  useEffect(() => {
    window.history.pushState(null, "", window.location.href);

    const blockBack = () => {
      window.history.pushState(null, "", window.location.href);
    };

    window.addEventListener("popstate", blockBack);

    return () => {
      window.removeEventListener("popstate", blockBack);
    };
  }, []);

  // ================= FILTERED DATA =================

  const filteredStaff = useMemo(() => {
    return staff.filter(s =>
      s.email.toLowerCase().includes(staffSearch.toLowerCase())
    );
  }, [staff, staffSearch]);

  const filteredDoctors = useMemo(() => {
    return doctors
      .filter(d =>
        d.name.toLowerCase().includes(doctorSearch.toLowerCase()) ||
        d.email.toLowerCase().includes(doctorSearch.toLowerCase())
      )
      .filter(d =>
        doctorStatus === "ALL" ? true : d.status === doctorStatus
      )
      .filter(d =>
        doctorSpeciality === "ALL"
          ? true
          : String(d.specialityId) === doctorSpeciality
      );
  }, [doctors, doctorSearch, doctorStatus, doctorSpeciality]);

  // ================= ACTIONS =================

  const approveStaff = async (id) => {
    await http.put(`/auth/admin/staff/${id}/approve`);
    setMsg("✅ Staff approved");
    load();
  };

  const rejectStaff = async (id) => {
    await http.put(`/auth/admin/staff/${id}/reject`);
    setMsg("❌ Staff rejected");
    load();
  };

  const approveDoctor = async (doctorId) => {
    const specId = selectedSpecByDoctor[doctorId];
    if (!specId) {
      setMsg("❌ Select speciality first");
      return;
    }

    await http.post(`/doctor/admin/approve/${doctorId}`);
    await http.post("/speciality/map", {
      doctorId,
      specialityId: Number(specId),
    });

    setMsg("✅ Doctor approved & mapped");
    load();
  };

  return (
    <div className="admin-container">

{/* ================= HEADER ================= */}
<div className="admin-header">
  <div className="admin-header-row">

    {/* LEFT SIDE */}
    <div className="admin-header-left">
      <span className="pd-kicker">Admin Panel</span>
      <h2 className="admin-title">Admin Dashboard</h2>
      <p>Manage staff approvals and doctor onboarding</p>

      {/* BACK TO HOME */}
      <div className="admin-hero-actions">
        <button
          className="admin-home-btn"
          onClick={() => navigate("/")}
        >
          ⌂ Go to Home
        </button>
      </div>
    </div>

    {/* RIGHT SIDE */}
    <div className="admin-header-actions">

      <button
        className="admin-logout-btn"
        onClick={() => {
          localStorage.clear();
          navigate("/login");
        }}
      >
        Logout
      </button>
    </div>

  </div>
</div>
      {/* ================= STAFF ================= */}
      <div className="card">
        <h3>Pending Staff Approvals</h3>

        {/* SEARCH */}
        <div className="admin-filter-bar">
          <input
            placeholder="Search staff by email"
            value={staffSearch}
            onChange={(e) => setStaffSearch(e.target.value)}
          />
        </div>

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
            {filteredStaff.length === 0 ? (
              <tr><td colSpan="5">No matching staff</td></tr>
            ) : (
              filteredStaff.map((s) => (
                <tr key={s.id}>
                  <td>{s.id}</td>
                  <td>{s.email}</td>
                  <td>{s.role}</td>
                  <td className="status-pending">{s.status}</td>
                  <td>
                    <button className="btn btn-success me-2" onClick={() => approveStaff(s.id)}>
                      Approve
                    </button>
                    <button className="btn btn-danger" onClick={() => rejectStaff(s.id)}>
                      Reject
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* ================= DOCTORS ================= */}
      <div className="card">
        <h3>Doctors</h3>

        {/* SEARCH + FILTER */}
        <div className="admin-filter-bar">
          <input
            placeholder="Search doctor by name or email"
            value={doctorSearch}
            onChange={(e) => setDoctorSearch(e.target.value)}
          />

          <select onChange={(e) => setDoctorStatus(e.target.value)}>
            <option value="ALL">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="PENDING">Pending</option>
          </select>

          <select onChange={(e) => setDoctorSpeciality(e.target.value)}>
            <option value="ALL">All Specialities</option>
            {specialities.map(s => (
              <option key={s.specialityId} value={s.specialityId}>
                {s.name}
              </option>
            ))}
          </select>
        </div>

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Experience</th>
              <th>Status</th>
              <th>Speciality</th>
              <th>Action</th>
            </tr>
          </thead>

          <tbody>
            {filteredDoctors.length === 0 ? (
              <tr><td colSpan="7">No matching doctors</td></tr>
            ) : (
              filteredDoctors.map(doc => (
                <tr key={doc.doctorId}>
                  <td>{doc.doctorId}</td>
                  <td>{doc.name}</td>
                  <td>{doc.email}</td>
                  <td>{doc.experience}</td>

                  <td className={doc.status === "ACTIVE" ? "status-active" : "status-pending"}>
                    {doc.status}
                  </td>

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
                        <option value="">Select</option>
                        {specialities.map(s => (
                          <option key={s.specialityId} value={s.specialityId}>
                            {s.name}
                          </option>
                        ))}
                      </select>
                    ) : "-"}
                  </td>

                  <td>
                    {doc.status === "PENDING" ? (
                      <button className="btn btn-success" onClick={() => approveDoctor(doc.doctorId)}>
                        Approve
                      </button>
                    ) : "Approved"}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

    </div>
  );
}