import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import PrivateRoute from "./components/PrivateRoute";
import Register from "./pages/Register";
import AppointmentDetails from "./pages/AppointmentDetails";
import DoctorProfilePage from "./pages/DoctorProfilePage";

const App = () => {
  return (
    <div className="font-sans bg-[#090c12] text-slate-200 min-h-screen">
      <BrowserRouter basename="/doctor">
        <Routes>

          {/* 🔐 Login */}
          <Route path="/" element={<Login />} />

          <Route path="/register" element={<Register />} />

          {/* 🔒 Protected Routes */}
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            }
          />

          <Route path="/appointment/:id" element={<AppointmentDetails />} />

          <Route path="/profile" element={<DoctorProfilePage />} />

        </Routes>
      </BrowserRouter>
    </div>
  );
};

export default App;