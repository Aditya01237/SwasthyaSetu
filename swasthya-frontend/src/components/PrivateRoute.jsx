import React from "react";
import { Navigate } from "react-router-dom";

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem("patientToken"); // ✅ FIX

  return token ? children : <Navigate to="/" replace />;
};

export default PrivateRoute;