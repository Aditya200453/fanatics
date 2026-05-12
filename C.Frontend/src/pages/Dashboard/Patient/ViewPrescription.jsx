import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import http from "../../../api/http";

export default function ViewPrescription() {

  const { appointmentId } = useParams();

  const [prescription, setPrescription] = useState(null);
  const [medicines, setMedicines] = useState([]);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    loadPrescription();
  }, []);

  const loadPrescription = async () => {
    try {
      setMsg("");

      // ✅ GET prescription by appointment
      const res = await http.get(`/prescription/appointment/${appointmentId}`);

      if (!res.data || res.data.length === 0) {
        setMsg("No prescription available");
        return;
      }

      const pres = res.data[0];  // ✅ one prescription per appointment
      setPrescription(pres);

      // ✅ GET medicines using prescriptionId
      const medRes = await http.get(`/prescription/${pres.prescriptionId}/medicine`);
      setMedicines(medRes.data || []);

    } catch (err) {
      console.error(err);
      setMsg("❌ Failed to load prescription");
    }
  };

  if (msg) return <h3>{msg}</h3>;
  if (!prescription) return <h3>Loading...</h3>;

  return (
    <div style={{ padding: 20 }}>
      <h2>Prescription Details 💊</h2>

      {/* ✅ MAIN PRESCRIPTION */}
      <div style={{ marginTop: 20 }}>
        <p><b>Appointment ID:</b> {prescription.appointmentId}</p>
        <p><b>Date:</b> {prescription.prescriptionDate}</p>
        <p><b>Diagnosis:</b> {prescription.diagnosis}</p>
        <p><b>Notes:</b> {prescription.notes}</p>
      </div>

      {/* ✅ MEDICINES */}
      <h3 style={{ marginTop: 30 }}>Medicines</h3>

      {medicines.length === 0 ? (
        <p>No medicines</p>
      ) : (
        <table border="1" cellPadding="10" style={{ marginTop: 10 }}>
          <thead>
            <tr>
              <th>Name</th>
              <th>Dosage</th>
              <th>Duration</th>
              <th>Instructions</th>
            </tr>
          </thead>
          <tbody>
            {medicines.map((m) => (
              <tr key={m.prescriptionMedicineId}>
                <td>{m.medicineName}</td>
                <td>{m.dosage}</td>
                <td>{m.duration}</td>
                <td>{m.instructions}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}