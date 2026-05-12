import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import http from "../../../api/http";

export default function AddPrescription() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();

  // ✅ Prescription fields
  const [diagnosis, setDiagnosis] = useState("");
  const [notes, setNotes] = useState("");

  // ✅ Medicine form fields
  const [medicineName, setMedicineName] = useState("");
  const [dosage, setDosage] = useState("");
  const [duration, setDuration] = useState("");
  const [instructions, setInstructions] = useState("");

  const [medicines, setMedicines] = useState([]);

  const [prescriptionId, setPrescriptionId] = useState(null);
  const [msg, setMsg] = useState("");

  // ✅ STEP 1: Save Prescription
  const savePrescription = async () => {
    try {
      setMsg("");

      const res = await http.post("/prescription", {
        appointmentId: Number(appointmentId),
        prescriptionDate: new Date().toISOString().slice(0, 10),
        diagnosis,
        notes,
      });

      setPrescriptionId(res.data.prescriptionId);
      setMsg("✅ Prescription saved. Now add medicines");

    } catch (e) {
      console.error(e);
      const data = e?.response?.data;
      setMsg(data?.message || "❌ Failed to save prescription");
    }
  };

  // ✅ STEP 2: Add medicine locally (UI)
  const addMedicineToList = () => {
    if (!medicineName) return;

    const newMed = {
      medicineName,
      dosage,
      duration,
      instructions,
    };

    setMedicines([...medicines, newMed]);

    // reset form
    setMedicineName("");
    setDosage("");
    setDuration("");
    setInstructions("");
  };

  // ✅ STEP 3: Save medicines to backend
  const submitMedicines = async () => {
    try {
      for (const med of medicines) {
        await http.post(`/prescription/${prescriptionId}/medicine`, med);
      }

      setMsg("✅ Prescription + Medicines saved");

      // ✅ OPTIONAL: redirect
      setTimeout(() => navigate("/dashboard/doctor"), 1000);

    } catch (err) {
      console.error(err);
      setMsg("❌ Failed to save medicines");
    }
  };

  return (
    <div style={{ padding: 20 }}>

      <h2>Add Prescription 💊</h2>
      <p><b>Appointment ID:</b> {appointmentId}</p>

      {/* ✅ Prescription Form */}
      <div style={{ marginTop: 10 }}>
        <input
          style={{ width: 300 }}
          placeholder="Diagnosis"
          value={diagnosis}
          onChange={(e) => setDiagnosis(e.target.value)}
        />
      </div>

      <div style={{ marginTop: 10 }}>
        <textarea
          style={{ width: 300, height: 100 }}
          placeholder="Notes"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
        />
      </div>

      {!prescriptionId && (
        <button style={{ marginTop: 10 }} onClick={savePrescription}>
          Save Prescription ✅
        </button>
      )}

      {/* ✅ Medicines Section */}
      {prescriptionId && (
        <>
          <h3 style={{ marginTop: 20 }}>Add Medicines</h3>

          <input
            placeholder="Medicine Name"
            value={medicineName}
            onChange={(e) => setMedicineName(e.target.value)}
          />
          <input
            placeholder="Dosage"
            value={dosage}
            onChange={(e) => setDosage(e.target.value)}
          />
          <input
            placeholder="Duration"
            value={duration}
            onChange={(e) => setDuration(e.target.value)}
          />
          <input
            placeholder="Instructions"
            value={instructions}
            onChange={(e) => setInstructions(e.target.value)}
          />

          <button style={{ marginLeft: 10 }} onClick={addMedicineToList}>
            Add ✅
          </button>

          {/* ✅ LIST of medicines */}
          <table border="1" style={{ marginTop: 15 }}>
            <thead>
              <tr>
                <th>Name</th>
                <th>Dosage</th>
                <th>Duration</th>
                <th>Instructions</th>
              </tr>
            </thead>
            <tbody>
              {medicines.map((m, i) => (
                <tr key={i}>
                  <td>{m.medicineName}</td>
                  <td>{m.dosage}</td>
                  <td>{m.duration}</td>
                  <td>{m.instructions}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <button style={{ marginTop: 15 }} onClick={submitMedicines}>
            Submit All 💊
          </button>
        </>
      )}

      {msg && <p style={{ marginTop: 10 }}>{msg}</p>}
    </div>
  );
}
