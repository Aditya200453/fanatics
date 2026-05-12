import { getToken } from "../../../auth/auth";

function decodeJwt(token) {
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export default function StaffDashboard() {
  const token = getToken();
  const decoded = token ? decodeJwt(token) : null;

  const email = decoded?.sub || "";
  const storedName = localStorage.getItem("staff_name") || ""; // optional local welcome

  const display = storedName || email || "Staff";

  return (
    <div style={{ padding: "30px" }}>
      <h2>Staff Dashboard</h2>
      <h5 style={{ color: "#666" }}>Welcome, {display} 👋</h5>

      <hr />

      <ul>
        <li>View doctors</li>
        <li>Manage appointments</li>
        <li>Assist admin operations</li>
      </ul>
    </div>
  );
}