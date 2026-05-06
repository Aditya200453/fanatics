import { useEffect, useState } from "react";
import http from "../../../api/http";

export default function AdminDashboard() {

  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);

  // ✅ Fetch doctors
  const loadDoctors = async () => {
    try {
      const res = await http.get("/doctor/admin/doctors");
      setDoctors(res.data);
    } catch (err) {
      console.error("Error loading doctors", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDoctors();
  }, []);

  // ✅ Approve doctor
  const approveDoctor = async (id) => {
    try {
      await http.post(`/doctor/admin/approve/${id}`);
      alert("Doctor Approved ✅");

      // reload list
      loadDoctors();

    } catch (err) {
      console.error("Approval failed", err);
      alert("Approval failed ❌");
    }
  };

  if (loading) return <h3>Loading doctors...</h3>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>Admin Dashboard ✅</h2>

      <table border="1" cellPadding="10">
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
          {doctors.map((doc) => (
            <tr key={doc.doctorId}>
              <td>{doc.doctorId}</td>
              <td>{doc.name}</td>
              <td>{doc.email}</td>
              <td>{doc.experience}</td>
              <td>
                <b style={{ color: doc.status === "ACTIVE" ? "green" : "orange" }}>
                  {doc.status}
                </b>
              </td>

              <td>
                {doc.status === "PENDING" && (
                  <button onClick={() => approveDoctor(doc.doctorId)}>
                    Approve ✅
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}