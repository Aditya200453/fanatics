import { useEffect, useState } from "react";
import { Container, Card, Table, Button } from "react-bootstrap";
import { useParams, useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./ViewPrescription.css";
 
export default function ViewPrescription() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
 
  const [prescription, setPrescription] = useState(null);
  const [medicines, setMedicines] = useState([]);
  const [msg, setMsg] = useState("");
 
  useEffect(() => {
    loadPrescription();
  }, []);
 
  const loadPrescription = async () => {
    try {
      setMsg("");
 
      const res = await http.get(
        `/prescription/appointment/${appointmentId}`
      );
 
      if (!res.data || res.data.length === 0) {
        setMsg("No prescription available");
        return;
      }
 
      const pres = res.data[0];
      setPrescription(pres);
 
      const medRes = await http.get(
        `/prescription/${pres.prescriptionId}/medicine`
      );
      setMedicines(medRes.data || []);
 
    } catch (err) {
      console.error(err);
      setMsg("❌ Failed to load prescription");
    }
  };
 
  if (msg) return <h3 className="pd-status">{msg}</h3>;
  if (!prescription) return <h3 className="pd-status">Loading...</h3>;
 
  return (
    <div className="vp-root">
      <Container className="vp-container">
 
        {/* ================= HERO ================= */}
        <div className="vp-hero">
          <h2>Prescription Details 💊</h2>
          <p>Appointment ID: <b>{prescription.appointmentId}</b></p>
 
          <div className="vp-hero-actions">
            <Button
              variant="outline-primary"
              className="vp-back-btn"
              onClick={() => navigate("/dashboard/patient")}
            >
              ← Back to Dashboard
            </Button>
 
            <Button
              className="vp-print-btn"
              onClick={() => window.print()}
            >
              🖨 Print Prescription
            </Button>
          </div>
        </div>
 
        {/* ================= SUMMARY CARDS ================= */}
        <div className="vp-summary">
          <div className="vp-summary-card">
            <span>Date</span>
            <p>{prescription.prescriptionDate}</p>
          </div>
 
          <div className="vp-summary-card">
            <span>Diagnosis</span>
            <p>{prescription.diagnosis}</p>
          </div>
 
          <div className="vp-summary-card">
            <span>Doctor Notes</span>
            <p>{prescription.notes || "-"}</p>
          </div>
        </div>
 
        {/* ================= MEDICINES ================= */}
        <Card className="vp-meds-card">
          <Card.Body>
            <h5 className="mb-3">Prescribed Medicines</h5>
 
            {medicines.length === 0 ? (
              <p className="text-muted">No medicines prescribed</p>
            ) : (
              <Table hover responsive className="vp-table">
                <thead>
                  <tr>
                    <th>Medicine</th>
                    <th>Dosage</th>
                    <th>Duration</th>
                    <th>Instructions</th>
                  </tr>
                </thead>
                <tbody>
                  {medicines.map((m) => (
                    <tr key={m.prescriptionMedicineId}>
                      <td><b>{m.medicineName}</b></td>
                      <td>{m.dosage}</td>
                      <td>{m.duration}</td>
                      <td>{m.instructions}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Card.Body>
        </Card>
 
      </Container>
    </div>
  );
}
 