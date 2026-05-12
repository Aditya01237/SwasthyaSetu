import { BrowserRouter, Routes, Route, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import PrivateRoute from "./components/PrivateRoute";
import Register from "./pages/Register";
import AppointmentDetails from "./pages/AppointmentDetails";
import DoctorProfilePage from "./pages/DoctorProfilePage";

function AppWrapper() {
  const [sessionExpired, setSessionExpired] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handler = () => setSessionExpired(true);
    window.addEventListener("doctorSessionExpired", handler);
    return () => window.removeEventListener("doctorSessionExpired", handler);
  }, []);

  const handleLoginRedirect = () => {
    localStorage.removeItem("doctorToken");
    localStorage.removeItem("doctor");
    setSessionExpired(false);
    navigate("/");
  };

  return (
    <>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
        <Route path="/appointment/:id" element={<AppointmentDetails />} />
        <Route path="/profile" element={<DoctorProfilePage />} />
      </Routes>

      {sessionExpired && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-[#1a1f2e] border border-white/[0.08] rounded-2xl p-6 w-full max-w-sm text-center space-y-4">
            <h2 className="text-lg font-semibold text-white">Session Expired</h2>
            <p className="text-sm text-slate-400">Your session has expired. Please login again.</p>
            <button
              onClick={handleLoginRedirect}
              className="w-full py-2.5 rounded-xl font-semibold text-white transition-all hover:opacity-90"
              style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
            >
              Login Again
            </button>
          </div>
        </div>
      )}
    </>
  );
}

const App = () => {
  return (
    <div className="font-sans bg-[#090c12] text-slate-200 min-h-screen">
      <BrowserRouter basename={import.meta.env.PROD ? "/doctor" : ""}>
        <AppWrapper />
      </BrowserRouter>
    </div>
  );
};

export default App;