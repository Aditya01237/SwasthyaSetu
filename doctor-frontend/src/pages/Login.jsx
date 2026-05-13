import { useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import logo from "../assets/logo.png";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      setLoading(true);
      const res = await api.post("/auth/doctor/login", { email, password });
      const { token, doctor } = res.data;
      localStorage.setItem("doctorToken", token);
      localStorage.setItem("doctor", JSON.stringify(doctor));
      navigate("/dashboard");
    } catch (err) {
      alert(err.response?.data || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{ fontFamily: "'Inter', sans-serif" }}
      className="min-h-screen bg-[#0f1117] flex flex-col items-center justify-center px-4"
    >

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
        className="w-full max-w-sm"
      >
        <div className="bg-[#1a1f2e] border border-white/[0.07] rounded-2xl p-8">

          {/* Logo */}
          <div className="flex flex-col items-center gap-2 mb-6">
            <div className="w-20 h-20 rounded-2xl bg-white p-2 flex items-center justify-center shadow-lg shadow-indigo-500/20">
              <img
                src={logo}
                alt="SwasthyaSetu"
                className="w-full h-full object-contain rounded-xl"
              />
            </div>
            <p className="text-white text-xl font-bold tracking-tight">SwasthyaSetu Doctor</p>
            <p className="text-sm text-gray-500">Login to your doctor portal</p>
          </div>

          {/* Toggle */}
          <div className="flex bg-[#111827] rounded-full p-1 mb-7">
            <button
              className="flex-1 py-2.5 rounded-full text-sm font-semibold text-white transition-all"
              style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
            >
              Login
            </button>
            <button
              onClick={() => navigate("/register")}
              className="flex-1 py-2.5 rounded-full text-sm font-medium text-gray-500 transition-all hover:text-gray-300"
            >
              Register
            </button>
          </div>

          {/* Fields */}
          <div className="space-y-4">
            <div>
              <label className="block text-[11px] font-semibold text-gray-500 tracking-widest uppercase mb-2">
                Email Address
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="doctor@hospital.com"
                className="w-full bg-[#111827] border border-white/[0.08] rounded-xl px-4 py-3 text-sm text-gray-200 placeholder-gray-600 outline-none transition-all focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/10"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-gray-500 tracking-widest uppercase mb-2">
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-[#111827] border border-white/[0.08] rounded-xl px-4 py-3 text-sm text-gray-200 placeholder-gray-600 outline-none transition-all focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/10"
              />
            </div>

            <button
              onClick={handleLogin}
              disabled={!email || !password || loading}
              className="w-full py-3 rounded-xl text-sm font-semibold text-white flex items-center justify-center gap-2 transition-all disabled:opacity-40 disabled:cursor-not-allowed hover:opacity-90"
              style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
            >
              {loading ? (
                <>
                  <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2" strokeOpacity="0.3" />
                    <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                  </svg>
                  Signing in...
                </>
              ) : (
                <>
                  Sign In
                  <svg className="w-4 h-4" viewBox="0 0 20 20" fill="none">
                    <path d="M3 10h14M10 4l7 6-7 6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </>
              )}
            </button>
          </div>

          <p className="text-center text-xs text-gray-700 mt-6">
            Authorized personnel only · MedPortal © 2026
          </p>
        </div>
      </motion.div>

      <style>{`@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');`}</style>
    </div>
  );
};

export default Login;
