import { useState } from "react";
import { Container, Card, Button, Form, Table } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import http from "../../../api/http";
import "./AddPrescription.css";
 
export default function AddPrescription() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
 
  /* Prescription */
  const [diagnosis, setDiagnosis] = useState("");
  const [notes, setNotes] = useState("");
 
  /* Medicine fields */
  const [medicineName, setMedicineName] = useState("");
  const [dosage, setDosage] = useState("");
  const [duration, setDuration] = useState("");
  const [instructions, setInstructions] = useState("");
 
  const [medicines, setMedicines] = useState([]);
  const [prescriptionId, setPrescriptionId] = useState(null);
  const [msg, setMsg] = useState("");
 
  /* ========== STEP 1: SAVE PRESCRIPTION ========== */
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
      setMsg("✅ Prescription saved. Now add medicines.");
 
    } catch (e) {
      console.error(e);
      const data = e?.response?.data;
      setMsg(data?.message || "❌ Failed to save prescription");
    }
  };
 
  /* ========== STEP 2: ADD MEDICINE LOCALLY ========== */
  const addMedicineToList = () => {
    if (!medicineName) return;
 
    setMedicines([
      ...medicines,
      { medicineName, dosage, duration, instructions },
    ]);
 
    setMedicineName("");
    setDosage("");
    setDuration("");
    setInstructions("");
  };
 
  /* ========== STEP 3: SUBMIT MEDICINES ========== */
  const submitMedicines = async () => {
    try {
      for (const med of medicines) {
        await http.post(`/prescription/${prescriptionId}/medicine`, med);
      }
 
      setMsg("✅ Prescription and medicines saved");
 
      setTimeout(() => navigate("/dashboard/doctor"), 1000);
 
    } catch {
      setMsg("❌ Failed to save medicines");
    }
  };
 
  return (
    <div className="ap-root">
      <Container className="ap-container">
 
        {/* ================= HEADER ================= */}
        <div className="ap-header">
          <div>
            <span className="ap-kicker">Add Prescription</span>
            <h2>Appointment #{appointmentId}</h2>
            <p>
              Enter diagnosis, notes, and prescribed medicines for the patient.
            </p>
          </div>
 
          <Button
            variant="outline-primary"
            onClick={() => navigate("/dashboard/doctor")}
          >
            ← Back to Dashboard
          </Button>
        </div>
 
        {/* ================= PRESCRIPTION CARD ================= */}
        <Card className="ap-card">
          <Card.Body>
 
            <h5 className="ap-section-title">Prescription Details</h5>
 
            <Form.Group className="mb-3">
              <Form.Label>Diagnosis</Form.Label>
              <Form.Control
                value={diagnosis}
                onChange={(e) => setDiagnosis(e.target.value)}
                placeholder="Enter diagnosis"
              />
            </Form.Group>
 
            <Form.Group className="mb-4">
              <Form.Label>Doctor Notes</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Additional notes (optional)"
              />
            </Form.Group>
 
            {!prescriptionId && (
              <Button onClick={savePrescription}>
                Save Prescription
              </Button>
            )}
 
          </Card.Body>
        </Card>
 
        {/* ================= MEDICINES CARD ================= */}
        {prescriptionId && (
          <Card className="ap-card">
            <Card.Body>
 
              <h5 className="ap-section-title">Add Medicines</h5>
 
              <div className="ap-medicine-form">
                <Form.Control
                  placeholder="Medicine name"
                  value={medicineName}
                  onChange={(e) => setMedicineName(e.target.value)}
                />
                <Form.Control
                  placeholder="Dosage"
                  value={dosage}
                  onChange={(e) => setDosage(e.target.value)}
                />
                <Form.Control
                  placeholder="Duration"
                  value={duration}
                  onChange={(e) => setDuration(e.target.value)}
                />
                <Form.Control
                  placeholder="Instructions"
                  value={instructions}
                  onChange={(e) => setInstructions(e.target.value)}
                />
 
                <Button onClick={addMedicineToList}>
                  Add
                </Button>
              </div>
 
              {/* Medicine Table */}
              {medicines.length > 0 && (
                <Table responsive hover className="ap-table">
                  <thead>
                    <tr>
                      <th>Medicine</th>
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
                </Table>
              )}
 
              <Button
                className="mt-3"
                variant="success"
                onClick={submitMedicines}
              >
                Submit Prescription 💊
              </Button>
 
            </Card.Body>
          </Card>
        )}
 
        {msg && <div className="ap-msg">{msg}</div>}
 
      </Container>
    </div>
  );
}