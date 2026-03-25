import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import PrivateRoute from "./components/PrivateRoute";
import Profile from "./pages/Profile";
import QrAudit from "./pages/QrAudit";
import HospitalDetails from "./pages/HospitalDetails";


function App() {
  return (
    <BrowserRouter>
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
        <Route path="/profile" element={<Profile />} />
        <Route path="/qr-audit" element={<QrAudit />} />
        <Route path="/hospital/:id" element={<HospitalDetails />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
