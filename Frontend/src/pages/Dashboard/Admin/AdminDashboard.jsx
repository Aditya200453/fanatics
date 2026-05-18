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
  const [doctorSpecMap, setDoctorSpecMap] = useState({}); // ✅ doctorId -> ["Cardiology", ...]

  const [msg, setMsg] = useState("");

  /* ✅ NEW: Add speciality form */
  const [newSpecName, setNewSpecName] = useState("");
  const [newSpecDesc, setNewSpecDesc] = useState("");

  /* ===== SEARCH & FILTER STATE ===== */
  const [staffSearch, setStaffSearch] = useState("");
  const [doctorSearch, setDoctorSearch] = useState("");
  const [doctorStatus, setDoctorStatus] = useState("ALL");
  const [doctorSpeciality, setDoctorSpeciality] = useState("ALL");

  // ================= LOAD =================
  const load = async () => {
    try {
      setMsg("");

      const [docRes, staffRes, specRes] = await Promise.all([
        http.get("/doctor/admin/doctors"),
        http.get("/auth/admin/staff/pending"),
        http.get("/speciality/"),
      ]);

      const docs = docRes.data || [];
      const stf = staffRes.data || [];
      const specs = specRes.data || [];

      setDoctors(docs);
      setStaff(stf);
      setSpecialities(specs);

      // ✅ Build doctorId -> speciality names map using /speciality/{id}/doctors
      // (because doctor object doesn't contain specialityId)
      const map = {}; // doctorId -> [specName1, specName2]

      // fetch doctors for each speciality
      const lists = await Promise.all(
        specs.map((s) => http.get(`/speciality/${s.specialityId}/doctors`))
      );

      lists.forEach((resp, idx) => {
        const specName = specs[idx]?.name;
        (resp.data || []).forEach((d) => {
          const id = d.doctorId;
          if (!map[id]) map[id] = [];
          if (!map[id].includes(specName)) map[id].push(specName);
        });
      });

      setDoctorSpecMap(map);
    } catch (e) {
      console.error(e);
      setMsg("❌ Failed to load data");
    }
  };

  useEffect(() => {
    load();
  }, []);

  useEffect(() => {
    window.history.pushState(null, "", window.location.href);
    const blockBack = () => window.history.pushState(null, "", window.location.href);
    window.addEventListener("popstate", blockBack);
    return () => window.removeEventListener("popstate", blockBack);
  }, []);

  // ================= FILTERED DATA =================
  const filteredStaff = useMemo(() => {
    return staff.filter((s) =>
      (s.email || "").toLowerCase().includes(staffSearch.toLowerCase())
    );
  }, [staff, staffSearch]);

  const filteredDoctors = useMemo(() => {
    const q = doctorSearch.toLowerCase();

    return doctors
      .filter(
        (d) =>
          (d.name || "").toLowerCase().includes(q) ||
          (d.email || "").toLowerCase().includes(q)
      )
      .filter((d) => (doctorStatus === "ALL" ? true : d.status === doctorStatus))
      .filter((d) => {
        if (doctorSpeciality === "ALL") return true;

        // ✅ speciality filter based on doctorSpecMap (not d.specialityId)
        const names = doctorSpecMap[d.doctorId] || [];
        const selectedName =
          specialities.find((s) => String(s.specialityId) === String(doctorSpeciality))
            ?.name || "";

        return selectedName ? names.includes(selectedName) : false;
      });
  }, [doctors, doctorSearch, doctorStatus, doctorSpeciality, doctorSpecMap, specialities]);

  // ================= EXISTING ACTIONS =================
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

  // ================= ✅ NEW: ADD SPECIALITY =================
  const addSpeciality = async () => {
    if (!newSpecName.trim()) {
      setMsg("❌ Enter speciality name");
      return;
    }
    try {
      await http.post("/speciality/", {
        name: newSpecName.trim(),
        description: newSpecDesc.trim(),
      });
      setMsg("✅ Speciality added");
      setNewSpecName("");
      setNewSpecDesc("");
      load();
    } catch (e) {
      console.error(e);
      setMsg("❌ Failed to add speciality");
    }
  };

  // ================= ✅ NEW: DELETE SPECIALITY =================
  const deleteSpeciality = async (id) => {
    try {
      await http.delete(`/speciality/${id}`);
      setMsg("✅ Speciality deleted");
      load();
    } catch (e) {
      console.error(e);
      setMsg("❌ Cannot delete speciality (maybe mapped to doctors)");
    }
  };

  return (
    <div className="admin-container">
      {/* ================= HEADER ================= */}
      <div className="admin-header">
        <div className="admin-header-row">
          <div className="admin-header-left">
            <span className="pd-kicker">Admin Panel</span>
            <h2 className="admin-title">Admin Dashboard</h2>
            <p>Manage staff approvals and doctor onboarding</p>

            <div className="admin-hero-actions">
              <button className="admin-home-btn" onClick={() => navigate("/")}>
                ⌂ Go to Home
              </button>
            </div>
          </div>

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

      {msg && <div className="admin-msg">{msg}</div>}

      {/* ================= ✅ NEW: ADD SPECIALITY ================= */}
      <div className="card">
        <h3>Add Speciality</h3>
        <div className="admin-filter-bar">
          <input
            placeholder="Speciality Name"
            value={newSpecName}
            onChange={(e) => setNewSpecName(e.target.value)}
          />
          <input
            placeholder="Description"
            value={newSpecDesc}
            onChange={(e) => setNewSpecDesc(e.target.value)}
          />
          <button className="btn btn-success" onClick={addSpeciality}>
            Add
          </button>
        </div>
      </div>

      {/* ================= ✅ NEW: SPECIALITIES LIST + DELETE ================= */}
      <div className="card">
        <h3>Specialities</h3>

        <table>
          <thead>
            <tr>
              <th style={{ width: 80 }}>ID</th>
              <th>Name</th>
              <th>Description</th>
              <th style={{ width: 140 }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {specialities.length === 0 ? (
              <tr>
                <td colSpan="4">No specialities found</td>
              </tr>
            ) : (
              specialities.map((s) => (
                <tr key={s.specialityId}>
                  <td>{s.specialityId}</td>
                  <td>{s.name}</td>
                  <td>{s.description || "-"}</td>
                  <td>
                    <button
                      className="btn btn-danger"
                      onClick={() => deleteSpeciality(s.specialityId)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* ================= STAFF ================= */}
      <div className="card">
        <h3>Pending Staff Approvals</h3>

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
              <tr>
                <td colSpan="5">No matching staff</td>
              </tr>
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

        <div className="admin-filter-bar">
          <input
            placeholder="Search doctor by name or email"
            value={doctorSearch}
            onChange={(e) => setDoctorSearch(e.target.value)}
          />

          <select value={doctorStatus} onChange={(e) => setDoctorStatus(e.target.value)}>
            <option value="ALL">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="PENDING">Pending</option>
          </select>

          <select
            value={doctorSpeciality}
            onChange={(e) => setDoctorSpeciality(e.target.value)}
          >
            <option value="ALL">All Specialities</option>
            {specialities.map((s) => (
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
              <tr>
                <td colSpan="7">No matching doctors</td>
              </tr>
            ) : (
              filteredDoctors.map((doc) => (
                <tr key={doc.doctorId}>
                  <td>{doc.doctorId}</td>
                  <td>{doc.name}</td>
                  <td>{doc.email}</td>
                  <td>{doc.experience}</td>

                  <td className={doc.status === "ACTIVE" ? "status-active" : "status-pending"}>
                    {doc.status}
                  </td>

                  {/* ✅ Speciality display for ACTIVE using doctorSpecMap */}
                  <td>
                    {doc.status === "PENDING" ? (
                      <select
                        value={selectedSpecByDoctor[doc.doctorId] || ""}
                        onChange={(e) =>
                          setSelectedSpecByDoctor((prev) => ({
                            ...prev,
                            [doc.doctorId]: e.target.value,
                          }))
                        }
                      >
                        <option value="">Select</option>
                        {specialities.map((s) => (
                          <option key={s.specialityId} value={s.specialityId}>
                            {s.name}
                          </option>
                        ))}
                      </select>
                    ) : (
                      (doctorSpecMap[doc.doctorId] || []).join(", ") || "-"
                    )}
                  </td>

                  <td>
                    {doc.status === "PENDING" ? (
                      <button className="btn btn-success" onClick={() => approveDoctor(doc.doctorId)}>
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
      </div>
    </div>
  );
}