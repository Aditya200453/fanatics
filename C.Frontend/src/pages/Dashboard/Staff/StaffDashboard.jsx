import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./StaffDashboard.css";

export default function StaffDashboard() {
  const navigate = useNavigate();

  const TABS = useMemo(
    () => ({
      PATIENTS: "PATIENTS",
      DOCTORS: "DOCTORS",
      SPECIALITY: "SPECIALITY",
      APPOINTMENTS: "APPOINTMENTS",
      DIAGNOSTIC: "DIAGNOSTIC",
    }),
    []
  );

  const [activeTab, setActiveTab] = useState(TABS.PATIENTS);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");
  const [rows, setRows] = useState([]);

  // ---------------- PATIENTS ----------------
  const [patientId, setPatientId] = useState("");

  // ---------------- SPECIALITY ----------------
  const [specialities, setSpecialities] = useState([]);
  const [selectedSpecialityId, setSelectedSpecialityId] = useState("");
  const [mapDoctorId, setMapDoctorId] = useState("");
  const [unmapDoctorId, setUnmapDoctorId] = useState("");

  // ---------------- APPOINTMENTS ----------------
  const [apptPatientId, setApptPatientId] = useState("");
  const [apptDoctorId, setApptDoctorId] = useState("");
  const [apptDate, setApptDate] = useState("");

  // ---------------- DIAGNOSTIC ----------------
  const [testName, setTestName] = useState("");
  const [testCost, setTestCost] = useState("");
  const [testDesc, setTestDesc] = useState("");
  const [deleteTestId, setDeleteTestId] = useState("");

  const [assignPatientId, setAssignPatientId] = useState("");
  const [assignTestId, setAssignTestId] = useState("");
  const [assignDate, setAssignDate] = useState("");
  const [listPatientTestsId, setListPatientTestsId] = useState("");

  // ================= OVERVIEW COUNTS =================
  const [allPatientsCount, setAllPatientsCount] = useState(0);
  const [allDoctorsCount, setAllDoctorsCount] = useState(0);
  const [pendingAppointmentsCount, setPendingAppointmentsCount] = useState(0);
  const [allTestsCount, setAllTestsCount] = useState(0);

  const safeMsg = (err, fallback) => {
    const status = err?.response?.status;
    const data = err?.response?.data;
    const m =
      data?.message ||
      (typeof data === "string" ? data : "") ||
      fallback;
    return status ? `${m} (HTTP ${status})` : m;
  };

  const resetView = () => {
    setRows([]);
    setMsg("");
    setLoading(false);
  };

  // ---------------------------
  // Diagnostic fallback
  // ---------------------------
  const diagPaths = {
    testsPrimary: "/diagnostic/tests/",
    testsFallback: "/tests/",
    patientTestsPrimary: (p) => `/diagnostic/patients/${p}/tests`,
    patientTestsFallback: (p) => `/patients/${p}/tests`,
    assignPrimary: (p, t, d) => `/diagnostic/patients/${p}/tests/${t}?testDate=${d}`,
    assignFallback: (p, t, d) => `/patients/${p}/tests/${t}?testDate=${d}`,
  };

  async function tryGet(primaryUrl, fallbackUrl) {
    try {
      return await http.get(primaryUrl);
    } catch (e1) {
      if (e1?.response?.status === 404 && fallbackUrl) {
        return await http.get(fallbackUrl);
      }
      throw e1;
    }
  }

  async function tryPost(primaryUrl, body, fallbackUrl) {
    try {
      return await http.post(primaryUrl, body);
    } catch (e1) {
      if (e1?.response?.status === 404 && fallbackUrl) {
        return await http.post(fallbackUrl, body);
      }
      throw e1;
    }
  }

  async function tryDelete(primaryUrl, fallbackUrl) {
    try {
      return await http.delete(primaryUrl);
    } catch (e1) {
      if (e1?.response?.status === 404 && fallbackUrl) {
        return await http.delete(fallbackUrl);
      }
      throw e1;
    }
  }

  // ================= LOAD OVERVIEW =================
  const loadOverview = async () => {
    try {
      const [patientsRes, doctorsRes, pendingApptRes] = await Promise.all([
        http.get("/patient/all"),
        http.get("/doctor/all"),
        http.get("/appointment/staff/pending"),
      ]);

      setAllPatientsCount((patientsRes.data || []).length);
      setAllDoctorsCount((doctorsRes.data || []).length);
      setPendingAppointmentsCount((pendingApptRes.data || []).length);

      try {
        const testsRes = await tryGet(diagPaths.testsPrimary, diagPaths.testsFallback);
        setAllTestsCount((testsRes.data || []).length);
      } catch {
        setAllTestsCount(0);
      }
    } catch {
      // overview failure silently ignore; page still works
    }
  };

  // ---------------- AUTO LOAD ----------------
  useEffect(() => {
    loadAllPatients();
    preloadSpecialities();
    loadOverview();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
  
  // ---------------- PATIENT APIs ----------------
  const loadAllPatients = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get("/patient/all");
      setRows(res.data || []);
      setAllPatientsCount((res.data || []).length);
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load patients"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  const getPatientById = async () => {
    if (!patientId) return setMsg("Enter Patient ID");
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get(`/patient/${patientId}`);
      setRows(res.data ? [res.data] : []);
    } catch (err) {
      setMsg(safeMsg(err, "Patient not found"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  // ---------------- DOCTOR APIs ----------------
  const loadAllDoctors = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get("/doctor/all");
      setRows(res.data || []);
      setAllDoctorsCount((res.data || []).length);
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load doctors"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  // ---------------- SPECIALITY APIs ----------------
  const preloadSpecialities = async () => {
    try {
      const res = await http.get("/speciality/");
      setSpecialities(res.data || []);
    } catch {
      // ignore
    }
  };

  const loadSpecialities = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get("/speciality/");
      setSpecialities(res.data || []);
      setRows(res.data || []);
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load specialities"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  const listDoctorsBySpeciality = async () => {
    if (!selectedSpecialityId) return setMsg("Select a speciality first");
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get(`/speciality/${selectedSpecialityId}/doctors`);
      setRows(res.data || []);
    } catch (err) {
      setMsg(safeMsg(err, "Failed to list doctors by speciality"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  const mapDoctorToSpeciality = async () => {
    if (!selectedSpecialityId || !mapDoctorId) {
      return setMsg("Select speciality and enter doctorId to map");
    }
    setLoading(true);
    setMsg("");
    try {
      await http.post("/speciality/map", {
        specialityId: Number(selectedSpecialityId),
        doctorId: Number(mapDoctorId),
      });
      setMsg("✅ Doctor mapped");
      setMapDoctorId("");
      await listDoctorsBySpeciality();
    } catch (err) {
      setMsg(safeMsg(err, "Mapping failed"));
    } finally {
      setLoading(false);
    }
  };

  const unmapDoctorFromSpeciality = async () => {
    if (!selectedSpecialityId || !unmapDoctorId) {
      return setMsg("Select speciality and enter doctorId to unmap");
    }
    setLoading(true);
    setMsg("");
    try {
      await http.delete(
        `/speciality/map?specialityId=${Number(selectedSpecialityId)}&doctorId=${Number(unmapDoctorId)}`
      );
      setMsg("✅ Doctor unmapped");
      setUnmapDoctorId("");
      await listDoctorsBySpeciality();
    } catch (err) {
      setMsg(safeMsg(err, "Unmapping failed"));
    } finally {
      setLoading(false);
    }
  };

  // ---------------- APPOINTMENT APIs ----------------
  const loadPendingAppointments = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get("/appointment/staff/pending");
      setRows(res.data || []);
      setPendingAppointmentsCount((res.data || []).length);
      if (!res.data || res.data.length === 0) {
        setMsg("No pending appointments");
      }
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load pending appointments"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  const approveAppointment = async (appointmentId) => {
    setLoading(true);
    setMsg("");
    try {
      await http.put(`/appointment/staff/${appointmentId}/approve`);
      setMsg("✅ Appointment approved");
      await loadPendingAppointments();
      await loadOverview();
    } catch (err) {
      setMsg(safeMsg(err, "Approval failed"));
    } finally {
      setLoading(false);
    }
  };

  const listAppointmentsByPatient = async () => {
    if (!apptPatientId) return setMsg("Enter Patient ID");
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get(`/appointment/patient/${Number(apptPatientId)}`);
      setRows(res.data || []);
      if (!res.data || res.data.length === 0) {
        setMsg("No appointments for this patient");
      }
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load patient appointments"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  const listAppointmentsByDoctorAndDate = async () => {
    if (!apptDoctorId || !apptDate) {
      return setMsg("Enter Doctor ID and Date");
    }
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get(
        `/appointment/doctor/${Number(apptDoctorId)}?date=${apptDate}`
      );
      setRows(res.data || []);
      if (!res.data || res.data.length === 0) {
        setMsg("No appointments for this doctor on this date");
      }
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load doctor appointments"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  // ---------------- DIAGNOSTIC APIs ----------------
  const listAllTests = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await tryGet(diagPaths.testsPrimary, diagPaths.testsFallback);
      setRows(res.data || []);
      setAllTestsCount((res.data || []).length);
    } catch (err) {
      setRows([]);
      setMsg(
        safeMsg(
          err,
          "Diagnostic route not found at Gateway. Fix API Gateway route for /diagnostic/**"
        )
      );
    } finally {
      setLoading(false);
    }
  };

  const addNewTest = async () => {
    if (!testName) return setMsg("Enter test name");
    if (!testCost) return setMsg("Enter cost");

    setLoading(true);
    setMsg("");
    try {
      const body = {
        testName: testName.trim(),
        description: testDesc?.trim() || null,
        cost: Number(testCost),
      };

      await tryPost(diagPaths.testsPrimary, body, diagPaths.testsFallback);

      setMsg("✅ Test added");
      setTestName("");
      setTestDesc("");
      setTestCost("");
      await listAllTests();
      await loadOverview();
    } catch (err) {
      setMsg(safeMsg(err, "Failed to add test"));
    } finally {
      setLoading(false);
    }
  };

  const deleteTest = async () => {
    if (!deleteTestId) return setMsg("Enter testId to delete");
    setLoading(true);
    setMsg("");
    try {
      const primary = `${diagPaths.testsPrimary}?testId=${Number(deleteTestId)}`;
      const fallback = `${diagPaths.testsFallback}?testId=${Number(deleteTestId)}`;
      await tryDelete(primary, fallback);

      setMsg("✅ Test deleted");
      setDeleteTestId("");
      await listAllTests();
      await loadOverview();
    } catch (err) {
      setMsg(safeMsg(err, "Failed to delete test"));
    } finally {
      setLoading(false);
    }
  };

  const assignTestToPatient = async () => {
    if (!assignPatientId || !assignTestId || !assignDate) {
      return setMsg("Enter patientId, testId, and testDate");
    }

    setLoading(true);
    setMsg("");
    try {
      const p = Number(assignPatientId);
      const t = Number(assignTestId);
      const primary = diagPaths.assignPrimary(p, t, assignDate);
      const fallback = diagPaths.assignFallback(p, t, assignDate);

      const res = await tryPost(primary, null, fallback);
      setRows(res.data ? [res.data] : []);
      setMsg("✅ Test assigned to patient");
    } catch (err) {
      setMsg(safeMsg(err, "Failed to assign test"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  const listTestsForPatient = async () => {
    if (!listPatientTestsId) return setMsg("Enter patientId to list tests");
    setLoading(true);
    setMsg("");
    try {
      const p = Number(listPatientTestsId);
      const primary = diagPaths.patientTestsPrimary(p);
      const fallback = diagPaths.patientTestsFallback(p);

      const res = await tryGet(primary, fallback);
      setRows(res.data || []);
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load patient tests"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  // ---------------- TABLE RENDERERS ----------------
  const renderEmpty = (title, subtitle) => (
    <div className="staff-empty">
      <p>{title}</p>
      <span>{subtitle}</span>
    </div>
  );

  const renderTable = () => {
    if (loading) return <div className="staff-hint">Loading…</div>;
    if (!rows || rows.length === 0) {
      return renderEmpty(
        "No data available",
        "Results for this section will appear here."
      );
    }

    const cols = Object.keys(rows[0] || {});
    return (
      <div className="staff-table-wrap">
        <table className="staff-table">
          <thead>
            <tr>
              {cols.map((c) => (
                <th key={c}>{c}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((r, idx) => (
              <tr key={idx}>
                {cols.map((c) => {
                  const val = r?.[c];

                  if (String(c).toLowerCase().includes("status")) {
                    return (
                      <td key={c}>
                        <span
                          className={
                            String(val).toUpperCase() === "ACTIVE" ||
                            String(val).toUpperCase() === "BOOKED" ||
                            String(val).toUpperCase() === "COMPLETED"
                              ? "staff-badge success"
                              : "staff-badge warning"
                          }
                        >
                          {String(val ?? "")}
                        </span>
                      </td>
                    );
                  }

                  return <td key={c}>{String(val ?? "")}</td>;
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const renderPendingAppointmentsTable = () => {
    if (loading) return <div className="staff-hint">Loading…</div>;
    if (!rows || rows.length === 0) {
      return renderEmpty(
        "No pending appointments",
        "Pending approvals will appear here."
      );
    }

    return (
      <div className="staff-table-wrap">
        <table className="staff-table">
          <thead>
            <tr>
              <th>Appointment ID</th>
              <th>Patient ID</th>
              <th>Doctor ID</th>
              <th>Date</th>
              <th>Time</th>
              <th>Status</th>
              <th>Symptoms</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((a) => (
              <tr key={a.appointmentId}>
                <td>{a.appointmentId}</td>
                <td>{a.patientId}</td>
                <td>{a.doctorId}</td>
                <td>{a.appointmentDate}</td>
                <td>{a.appointmentTime}</td>
                <td>
                  <span
                    className={
                      a.status === "PENDING"
                        ? "staff-badge warning"
                        : "staff-badge success"
                    }
                  >
                    {a.status}
                  </span>
                </td>
                <td>{a.symptoms || "-"}</td>
                <td>
                  {a.status === "PENDING" ? (
                    <button onClick={() => approveAppointment(a.appointmentId)}>
                      Approve
                    </button>
                  ) : (
                    "—"
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const onTab = async (tab) => {
    setActiveTab(tab);
    resetView();

    if (tab === TABS.PATIENTS) await loadAllPatients();
    if (tab === TABS.DOCTORS) await loadAllDoctors();
    if (tab === TABS.SPECIALITY) await loadSpecialities();
    if (tab === TABS.DIAGNOSTIC) await listAllTests();
    if (tab === TABS.APPOINTMENTS) await loadPendingAppointments();
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login", { replace: true });
  };

  return (
    <div className="staff-container">

      {/* HEADER */}
      <div className="staff-header-row">
        <div className="staff-header">
          <span className="staff-kicker">Staff Console</span>
          <h2 className="staff-title">Staff Dashboard</h2>
          <p>Operational management for patients, doctors, appointments and diagnostics</p>

          <div className="staff-hero-actions">
            <button className="staff-home-btn" onClick={() => navigate("/")}>
              ⌂ Go to Home
            </button>
          </div>
        </div>

        <div className="staff-header-actions">
          <button className="staff-logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>

      {/* OVERVIEW CARDS */}
      <div className="staff-overview">
        <div className="staff-overview-card">
          <span>Total Patients</span>
          <p>{allPatientsCount}</p>
        </div>

        <div className="staff-overview-card">
          <span>Total Doctors</span>
          <p>{allDoctorsCount}</p>
        </div>

        <div className="staff-overview-card">
          <span>Pending Appointments</span>
          <p>{pendingAppointmentsCount}</p>
        </div>

        <div className="staff-overview-card">
          <span>Diagnostic Tests</span>
          <p>{allTestsCount}</p>
        </div>
      </div>

      {/* NAV TABS */}
      <div className="staff-tabs">
        {Object.values(TABS).map((tab) => (
          <button
            key={tab}
            className={activeTab === tab ? "active" : ""}
            onClick={() => onTab(tab)}
          >
            {tab}
          </button>
        ))}
      </div>

      {msg && <div className="staff-msg">{msg}</div>}

      {/* PATIENTS */}
      {activeTab === TABS.PATIENTS && (
        <div className="staff-card">
          <div className="staff-card-header">
            <h4>Patients</h4>
          </div>

          <div className="staff-actions">
            <button onClick={loadAllPatients}>List All Patients</button>
            <input
              value={patientId}
              onChange={(e) => setPatientId(e.target.value)}
              placeholder="Patient ID"
            />
            <button onClick={getPatientById} disabled={!patientId}>
              Find by ID
            </button>
          </div>

          {renderTable()}
        </div>
      )}

      {/* DOCTORS */}
      {activeTab === TABS.DOCTORS && (
        <div className="staff-card">
          <div className="staff-card-header">
            <h4>Doctors</h4>
          </div>

          <div className="staff-actions">
            <button onClick={loadAllDoctors}>List All Doctors</button>
          </div>

          {renderTable()}
        </div>
      )}

      {/* SPECIALITY */}
      {activeTab === TABS.SPECIALITY && (
        <div className="staff-card">
          <div className="staff-card-header">
            <h4>Speciality Mapping</h4>
          </div>

          <div className="staff-actions">
            <button onClick={loadSpecialities}>List All Specialities</button>

            <select
              value={selectedSpecialityId}
              onChange={(e) => setSelectedSpecialityId(e.target.value)}
            >
              <option value="">Select Speciality</option>
              {specialities.map((s) => (
                <option key={s.specialityId} value={s.specialityId}>
                  {s.name}
                </option>
              ))}
            </select>

            <button onClick={listDoctorsBySpeciality} disabled={!selectedSpecialityId}>
              List Doctors
            </button>
          </div>

          <div className="staff-actions">
            <input
              value={mapDoctorId}
              onChange={(e) => setMapDoctorId(e.target.value)}
              placeholder="Doctor ID to map"
            />
            <button onClick={mapDoctorToSpeciality} disabled={!selectedSpecialityId || !mapDoctorId}>
              Add Doctor
            </button>

            <input
              value={unmapDoctorId}
              onChange={(e) => setUnmapDoctorId(e.target.value)}
              placeholder="Doctor ID to unmap"
            />
            <button
              className="danger"
              onClick={unmapDoctorFromSpeciality}
              disabled={!selectedSpecialityId || !unmapDoctorId}
            >
              Remove Doctor
            </button>
          </div>

          {renderTable()}
        </div>
      )}

      {/* APPOINTMENTS */}
      {activeTab === TABS.APPOINTMENTS && (
        <div className="staff-card">
          <div className="staff-card-header">
            <h4>Appointments</h4>
          </div>

          <div className="staff-actions">
            <button onClick={loadPendingAppointments}>Load Pending Appointments</button>
          </div>

          {renderPendingAppointmentsTable()}

          <hr />

          <div className="staff-actions">
            <input
              value={apptPatientId}
              onChange={(e) => setApptPatientId(e.target.value)}
              placeholder="Patient ID"
            />
            <button onClick={listAppointmentsByPatient} disabled={!apptPatientId}>
              Appointments by Patient
            </button>
          </div>

          <div className="staff-actions">
            <input
              value={apptDoctorId}
              onChange={(e) => setApptDoctorId(e.target.value)}
              placeholder="Doctor ID"
            />
            <input
              type="date"
              value={apptDate}
              onChange={(e) => setApptDate(e.target.value)}
            />
            <button onClick={listAppointmentsByDoctorAndDate} disabled={!apptDoctorId || !apptDate}>
              Appointments by Doctor + Date
            </button>
          </div>

          {rows && rows.length > 0 && renderTable()}
        </div>
      )}

      {/* DIAGNOSTIC */}
      {activeTab === TABS.DIAGNOSTIC && (
        <div className="staff-card">
          <div className="staff-card-header">
            <h4>Diagnostic Management</h4>
          </div>

          <div className="staff-actions">
            <button onClick={listAllTests}>List All Tests</button>
          </div>

          <div className="staff-actions">
            <input
              value={testName}
              onChange={(e) => setTestName(e.target.value)}
              placeholder="Test Name"
            />
            <input
              value={testCost}
              onChange={(e) => setTestCost(e.target.value)}
              placeholder="Cost"
            />
            <input
              value={testDesc}
              onChange={(e) => setTestDesc(e.target.value)}
              placeholder="Description"
            />
            <button onClick={addNewTest} disabled={!testName || !testCost}>
              Add Test
            </button>
          </div>

          <div className="staff-actions">
            <input
              value={deleteTestId}
              onChange={(e) => setDeleteTestId(e.target.value)}
              placeholder="Test ID to delete"
            />
            <button className="danger" onClick={deleteTest} disabled={!deleteTestId}>
              Delete Test
            </button>
          </div>

          <div className="staff-actions">
            <input
              value={assignPatientId}
              onChange={(e) => setAssignPatientId(e.target.value)}
              placeholder="Patient ID"
            />
            <input
              value={assignTestId}
              onChange={(e) => setAssignTestId(e.target.value)}
              placeholder="Test ID"
            />
            <input
              type="date"
              value={assignDate}
              onChange={(e) => setAssignDate(e.target.value)}
            />
            <button onClick={assignTestToPatient} disabled={!assignPatientId || !assignTestId || !assignDate}>
              Assign Test
            </button>
          </div>

          <div className="staff-actions">
            <input
              value={listPatientTestsId}
              onChange={(e) => setListPatientTestsId(e.target.value)}
              placeholder="Patient ID to list tests"
            />
            <button onClick={listTestsForPatient} disabled={!listPatientTestsId}>
              List Patient Tests
            </button>
          </div>

          {renderTable()}
        </div>
      )}
    </div>
  );
}