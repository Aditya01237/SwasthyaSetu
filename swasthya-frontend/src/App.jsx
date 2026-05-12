import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import PrivateRoute from "./components/PrivateRoute";
import Profile from "./pages/Profile";
import QrAudit from "./pages/QrAudit";
import HospitalDetails from "./pages/HospitalDetails";
import DoctorProfile from "./pages/DoctorProfile";
import MyAppointment from "./pages/MyAppointment";
import AppointmentDetails from "./pages/AppointmentDetails";

// 🔥 Wrapper needed to use navigate
function AppWrapper() {
  const [sessionExpired, setSessionExpired] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handler = () => setSessionExpired(true);

    window.addEventListener("sessionExpired", handler);

    return () => {
      window.removeEventListener("sessionExpired", handler);
    };
  }, []);

  const handleLoginRedirect = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("uhid");
    setSessionExpired(false);
    navigate("/");
  };

  return (
    <>
      <Routes>
        <Route path="/" element={<Login />} />

        <Route
          path="/dashboard"
          element={
            <PrivateRoute>
              <Dashboard />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile"
          element={
            <PrivateRoute>
              <Profile />
            </PrivateRoute>
          }
        />

        <Route
          path="/appointment"
          element={
            <PrivateRoute>
              <MyAppointment />
            </PrivateRoute>
          }
        />

        <Route
          path="/appointment/:id"
          element={
            <PrivateRoute>
              <AppointmentDetails />
            </PrivateRoute>
          }
        />

        <Route
          path="/qr-audit"
          element={
            <PrivateRoute>
              <QrAudit />
            </PrivateRoute>
          }
        />

        <Route
          path="/hospital/:id"
          element={
            <PrivateRoute>
              <HospitalDetails />
            </PrivateRoute>
          }
        />

        <Route
          path="/doctor/:id"
          element={
            <PrivateRoute>
              <DoctorProfile />
            </PrivateRoute>
          }
        />
      </Routes>

      {/* 🔥 SESSION EXPIRED MODAL */}
      {sessionExpired && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-[#0f141c] border border-white/[0.08] rounded-2xl p-6 w-full max-w-sm text-center space-y-4">

            <h2 className="text-lg font-semibold text-white">
              Session Expired
            </h2>

            <p className="text-sm text-slate-400">
              Your session has expired. Please login again.
            </p>

            <button
              onClick={handleLoginRedirect}
              className="w-full py-2 rounded-xl bg-gradient-to-r from-sky-500 to-blue-600 text-white hover:opacity-90"
            >
              Login Again
            </button>
          </div>
        </div>
      )}
    </>
  );
}

function App() {
  return (
    <BrowserRouter basename={import.meta.env.PROD ? "/patient" : "/"}>
      <AppWrapper />
    </BrowserRouter>
  );
}

export default App;