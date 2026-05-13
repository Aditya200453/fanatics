import { useEffect, useMemo, useState } from "react";
import http from "../../../api/http";
import "./StaffDashboard.css";

export default function StaffDashboard() {
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
  const [apptDate, setApptDate] = useState(""); // YYYY-MM-DD

  // ---------------- DIAGNOSTIC ----------------
  const [testName, setTestName] = useState("");
  const [testCost, setTestCost] = useState("");
  const [testDesc, setTestDesc] = useState("");
  const [deleteTestId, setDeleteTestId] = useState("");

  const [assignPatientId, setAssignPatientId] = useState("");
  const [assignTestId, setAssignTestId] = useState("");
  const [assignDate, setAssignDate] = useState("");
  const [listPatientTestsId, setListPatientTestsId] = useState("");

  const safeMsg = (err, fallback) => {
    const status = err?.response?.status;
    const data = err?.response?.data;
    const m = data?.message || (typeof data === "string" ? data : "") || fallback;
    return status ? `${m} (HTTP ${status})` : m;
  };

  const resetView = () => {
    setRows([]);
    setMsg("");
    setLoading(false);
  };

  // ---------------------------
  // Diagnostic base-path fallback (kept)
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

  // ---------------- AUTO LOAD ----------------
  useEffect(() => {
    loadAllPatients();
    preloadSpecialities();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ---------------- PATIENT APIs ----------------
  const loadAllPatients = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get("/patient/all");
      setRows(res.data || []);
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
      // ✅ FIXED: was using "&amp;" in your pasted code (that breaks the request)
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

  // ---------------- APPOINTMENT APIs (NOW IMPLEMENTED) ----------------

  // ✅ Staff: pending queue
  const loadPendingAppointments = async () => {
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get("/appointment/staff/pending");
      setRows(res.data || []);
      if (!res.data || res.data.length === 0) setMsg("No pending appointments");
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load pending appointments"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  // ✅ Staff: approve PENDING -> BOOKED
  const approveAppointment = async (appointmentId) => {
    setLoading(true);
    setMsg("");
    try {
      await http.put(`/appointment/staff/${appointmentId}/approve`);
      setMsg("✅ Appointment approved");
      await loadPendingAppointments();
    } catch (err) {
      setMsg(safeMsg(err, "Approval failed"));
    } finally {
      setLoading(false);
    }
  };

  // ✅ Staff/Admin report: appointments of a patient
  const listAppointmentsByPatient = async () => {
    if (!apptPatientId) return setMsg("Enter Patient ID");
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get(`/appointment/patient/${Number(apptPatientId)}`);
      setRows(res.data || []);
      if (!res.data || res.data.length === 0) setMsg("No appointments for this patient");
    } catch (err) {
      setMsg(safeMsg(err, "Failed to load patient appointments"));
      setRows([]);
    } finally {
      setLoading(false);
    }
  };

  // ✅ Staff/Admin report: appointments of a doctor on date
  const listAppointmentsByDoctorAndDate = async () => {
    if (!apptDoctorId || !apptDate) return setMsg("Enter Doctor ID and Date (YYYY-MM-DD)");
    setLoading(true);
    setMsg("");
    try {
      const res = await http.get(
        `/appointment/doctor/${Number(apptDoctorId)}?date=${apptDate}`
      );
      setRows(res.data || []);
      if (!res.data || res.data.length === 0) setMsg("No appointments for this doctor on this date");
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
    } catch (err) {
      setRows([]);
      setMsg(
        safeMsg(
          err,
          "Diagnostic route not found at Gateway. Fix API Gateway route for /diagnostic/** (or StripPrefix mismatch)."
        )
      );
    } finally {
      setLoading(false);
    }
  };

  const addNewTest = async () => {
    if (!testName) return setMsg("Enter test name");
    if (!testCost) return setMsg("Enter cost (required)");

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
    } catch (err) {
      setMsg(
        safeMsg(
          err,
          "Failed to add test. Check: gateway route, endpoint path, and required fields (cost)."
        )
      );
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
    } catch (err) {
      setMsg(safeMsg(err, "Failed to delete test"));
    } finally {
      setLoading(false);
    }
  };

  const assignTestToPatient = async () => {
    if (!assignPatientId || !assignTestId || !assignDate) {
      return setMsg("Enter patientId, testId, and testDate (YYYY-MM-DD)");
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

  // ---------------- TABLE RENDER ----------------
  const renderTable = () => {
    if (loading) return <div className="staff-hint">Loading…</div>;
    if (!rows || rows.length === 0) return <div className="staff-hint">No data to show</div>;

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
                {cols.map((c) => (
                  <td key={c}>{String(r?.[c] ?? "")}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  // ✅ Special appointment table for pending approvals (includes Approve button)
  const renderPendingAppointmentsTable = () => {
    if (loading) return <div className="staff-hint">Loading…</div>;
    if (!rows || rows.length === 0) return <div className="staff-hint">No pending appointments</div>;

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
                <td style={{ fontWeight: 700, color: a.status === "PENDING" ? "orange" : "lightgreen" }}>
                  {a.status}
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

    // ✅ NEW: appointments tab default loads pending queue
    if (tab === TABS.APPOINTMENTS) {
      await loadPendingAppointments();
    }
  };

  return (
    <div className="staff-container">
      <div className="staff-header">
        <h2>Staff Dashboard</h2>
        <span className="staff-sub">Operational console</span>
      </div>

      <div className="staff-nav">
        <button className={activeTab === TABS.PATIENTS ? "active" : ""} onClick={() => onTab(TABS.PATIENTS)}>Patients</button>
        <button className={activeTab === TABS.DOCTORS ? "active" : ""} onClick={() => onTab(TABS.DOCTORS)}>Doctors</button>
        <button className={activeTab === TABS.SPECIALITY ? "active" : ""} onClick={() => onTab(TABS.SPECIALITY)}>Speciality</button>
        <button className={activeTab === TABS.APPOINTMENTS ? "active" : ""} onClick={() => onTab(TABS.APPOINTMENTS)}>Appointments</button>
        <button className={activeTab === TABS.DIAGNOSTIC ? "active" : ""} onClick={() => onTab(TABS.DIAGNOSTIC)}>Diagnostic</button>
      </div>

      {msg && <div className="staff-msg">{msg}</div>}

      {/* PATIENTS */}
      {activeTab === TABS.PATIENTS && (
        <div className="staff-panel">
          <div className="staff-row">
            <button onClick={loadAllPatients}>List All Patients</button>
            <input value={patientId} onChange={(e) => setPatientId(e.target.value)} placeholder="Patient ID" />
            <button onClick={getPatientById}>Find by ID</button>
          </div>
          {renderTable()}
        </div>
      )}

      {/* DOCTORS */}
      {activeTab === TABS.DOCTORS && (
        <div className="staff-panel">
          <div className="staff-row">
            <button onClick={loadAllDoctors}>List All Doctors</button>
          </div>
          {renderTable()}
        </div>
      )}

      {/* SPECIALITY */}
      {activeTab === TABS.SPECIALITY && (
        <div className="staff-panel">
          <div className="staff-row">
            <button onClick={loadSpecialities}>List All Specialities</button>
            <select value={selectedSpecialityId} onChange={(e) => setSelectedSpecialityId(e.target.value)}>
              <option value="">Select Speciality</option>
              {specialities.map((s) => (
                <option key={s.specialityId} value={s.specialityId}>{s.name}</option>
              ))}
            </select>
            <button onClick={listDoctorsBySpeciality}>List Doctors</button>
          </div>

          <div className="staff-row">
            <input value={mapDoctorId} onChange={(e) => setMapDoctorId(e.target.value)} placeholder="Doctor ID to map" />
            <button onClick={mapDoctorToSpeciality}>Add Doctor</button>

            <input value={unmapDoctorId} onChange={(e) => setUnmapDoctorId(e.target.value)} placeholder="Doctor ID to unmap" />
            <button className="danger" onClick={unmapDoctorFromSpeciality}>Remove Doctor</button>
          </div>

          {renderTable()}
        </div>
      )}

      {/* APPOINTMENTS (NOW WORKING) */}
      {activeTab === TABS.APPOINTMENTS && (
        <div className="staff-panel">
          <div className="staff-row">
            <button onClick={loadPendingAppointments}>Load Pending Appointments</button>
          </div>

          {/* Pending queue table with Approve button */}
          {renderPendingAppointmentsTable()}

          <hr />

          {/* Staff reports */}
          <div className="staff-row">
            <input value={apptPatientId} onChange={(e) => setApptPatientId(e.target.value)} placeholder="Patient ID" />
            <button onClick={listAppointmentsByPatient}>Appointments by Patient</button>
          </div>

          <div className="staff-row">
            <input value={apptDoctorId} onChange={(e) => setApptDoctorId(e.target.value)} placeholder="Doctor ID" />
            <input value={apptDate} onChange={(e) => setApptDate(e.target.value)} placeholder="YYYY-MM-DD" />
            <button onClick={listAppointmentsByDoctorAndDate}>Appointments by Doctor + Date</button>
          </div>

          {/* Results of report queries use generic table */}
          {rows && rows.length > 0 && renderTable()}
        </div>
      )}

      {/* DIAGNOSTIC */}
      {activeTab === TABS.DIAGNOSTIC && (
        <div className="staff-panel">
          <div className="staff-row">
            <button onClick={listAllTests}>List All Tests</button>
          </div>

          <div className="staff-row">
            <input value={testName} onChange={(e) => setTestName(e.target.value)} placeholder="Test Name" />
            <input value={testCost} onChange={(e) => setTestCost(e.target.value)} placeholder="Cost (required)" />
            <input value={testDesc} onChange={(e) => setTestDesc(e.target.value)} placeholder="Description (optional)" />
            <button onClick={addNewTest}>Add Test</button>
          </div>

          <div className="staff-row">
            <input value={deleteTestId} onChange={(e) => setDeleteTestId(e.target.value)} placeholder="Test ID to delete" />
            <button className="danger" onClick={deleteTest}>Delete Test</button>
          </div>

          <div className="staff-row">
            <input value={assignPatientId} onChange={(e) => setAssignPatientId(e.target.value)} placeholder="Patient ID" />
            <input value={assignTestId} onChange={(e) => setAssignTestId(e.target.value)} placeholder="Test ID" />
            <input value={assignDate} onChange={(e) => setAssignDate(e.target.value)} placeholder="YYYY-MM-DD" />
            <button onClick={assignTestToPatient}>Assign Test</button>
          </div>

          <div className="staff-row">
            <input value={listPatientTestsId} onChange={(e) => setListPatientTestsId(e.target.value)} placeholder="Patient ID to list tests" />
            <button onClick={listTestsForPatient}>List Patient Tests</button>
          </div>

          {renderTable()}
        </div>
      )}
    </div>
  );
}
