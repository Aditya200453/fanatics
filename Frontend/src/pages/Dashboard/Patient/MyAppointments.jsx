import { useEffect, useState } from "react";
import { Container, Card, Button, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import http from "../../../api/http";
import "./MyAppointments.css";

export default function MyAppointments() {
  const navigate = useNavigate();

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

  return (
    <div className="ma-root">
      <Container fluid className="px-4 py-4">

        {/* ================= HERO HEADER ================= */}
        <div className="ma-hero">
          <h2>My Appointments</h2>
          <p>Review your appointments, statuses, and prescriptions</p>

          <div className="ma-hero-actions">
            <Button
              variant="outline-primary"
              className="ma-back-btn"
              onClick={() => navigate("/dashboard/patient")}
            >
              ← Back to Dashboard
            </Button>

            <Button
              className="ma-refresh-btn"
              onClick={load}
            >
              Refresh
            </Button>
          </div>
        </div>

        {/* ================= FLOATING CARD ================= */}
        <Card className="ma-card">
          <Card.Body>
            <div className="ma-table-wrapper">
              <Table hover responsive className="ma-table">
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
                      <td colSpan="8" className="text-center py-4">
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

                        <td>
                          <span className={`ma-status ${a.status.toLowerCase()}`}>
                            {a.status}
                          </span>
                        </td>

                        <td>{a.symptoms ?? "-"}</td>
                        <td>{a.remarks ?? "-"}</td>

                        <td>
                          {a.status === "COMPLETED" ? (
                            <Button
                              size="sm"
                              className="ma-view-btn"
                              onClick={() =>
                                navigate(`/patient/prescription/${a.appointmentId}`)
                              }
                            >
                              View 💊
                            </Button>
                          ) : (
                            <span className="text-muted">-</span>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Table>
            </div>
          </Card.Body>
        </Card>

      </Container>
    </div>
  );
}