import { Navigate } from "react-router-dom";
import { isLoggedIn, getRole } from "../auth/auth";

export default function ProtectedRoute({ allowedRoles, children }) {
  if (!isLoggedIn()) return <Navigate to="/" replace />;

  const role = getRole();
  if (allowedRoles && !allowedRoles.includes(role)) {
    return <Navigate to="/" replace />;
  }
  return children;
}