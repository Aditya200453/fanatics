import { useEffect, useState } from "react";
import { Container, Card, Button, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom"; // ✅ ADD THIS
import http from "../../../api/http";


export default function MyAppointments() {

  const navigate = useNavigate(); // ✅ ADD THIS

  const [items, setItems] = useState([]);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      setError("");
      const res = await http.get("/appointment/my");
      setItems(res.data || []);
    } catch (err) {
      console.error(err);
      setError("❌ Failed to load appointments");
    }
  };

  useEffect(() => {
    load();
  }, []);

  if (error) return <h3 className="p-4">{error}</h3>;

  const statusStyle = (status) => ({
    fontWeight: 700,
    color:
      status === "PENDING"
        ? "orange"
        : status === "BOOKED"
        ? "lightgreen"
        : status === "COMPLETED"
        ? "white"
        : "white",
  });

  return (
    <div className="pd-root">
      <Container fluid className="px-4 py-4">
        <Card className="pd-glass-card">
          <Card.Body>

            <div className="d-flex justify-content-between align-items-center mb-3">
              <h4 className="fw-bold mb-0">My Appointments</h4>
              <Button variant="info" onClick={load}>
                Refresh
              </Button>
            </div>

            <Table hover responsive variant="dark">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Doctor ID</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Symptoms</th>
                  <th>Remarks</th>
                  <th>Prescription</th>
                </tr>
              </thead>

              <tbody>
                {items.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="text-center">
                      No appointments yet
                    </td>
                  </tr>
                ) : (
                  items.map((a) => (
                    <tr key={a.appointmentId}>

                      <td>{a.appointmentId}</td>
                      <td>{a.doctorId}</td>
                      <td>{a.appointmentDate}</td>
                      <td>{a.appointmentTime}</td>

                      <td style={statusStyle(a.status)}>
                        {a.status}
                      </td>

                      <td>{a.symptoms ?? "-"}</td>
                      <td>{a.remarks ?? "-"}</td>

                      {/* ✅ ✅ FINAL FIX HERE */}
                      <td>
                        {a.status === "COMPLETED" ? (
                          <button
                            onClick={() =>
                              navigate(`/patient/prescription/${a.appointmentId}`)
                            }
                          >
                            View 💊
                          </button>
                        ) : (
                          "-"
                        )}
                      </td>

                    </tr>
                  ))
                )}
              </tbody>

            </Table>

          </Card.Body>
        </Card>
      </Container>
    </div>
  );
}