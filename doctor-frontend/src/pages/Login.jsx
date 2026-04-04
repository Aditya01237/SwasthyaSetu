import { useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";

const Login = () => {
  const [email, setEmail] = useState("");   // ✅ FIXED
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      setLoading(true);

      const res = await api.post("/auth/doctor/login", {  // ✅ FIX endpoint
        email,        // ✅ FIXED
        password,
      });

      const { token, doctor } = res.data;

      localStorage.setItem("token", token);
      localStorage.setItem("doctor", JSON.stringify(doctor));
      

      navigate("/dashboard");
    } catch (err) {
      console.log(err);
      alert(err.response?.data || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#090c12] flex items-center justify-center px-4 relative">
      
      {/* Glow */}
      <div className="absolute -top-40 left-1/2 -translate-x-1/2 w-[600px] h-64 bg-blue-500/10 blur-3xl rounded-full" />

      <motion.div
        initial={{ opacity: 0, y: 40 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md bg-white/[0.03] border border-white/[0.08] backdrop-blur-xl rounded-2xl p-8 shadow-xl"
      >

        {/* Logo */}
        <div className="flex justify-center mb-6">
          <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center text-white font-bold text-xl">
            D
          </div>
        </div>

        <h2 className="text-2xl font-semibold text-center text-white">
          Doctor Portal
        </h2>
        <p className="text-sm text-slate-400 text-center mb-6">
          Login to manage appointments
        </p>

        <div className="space-y-4">

          {/* ✅ EMAIL INPUT */}
          <input
            type="email"
            placeholder="Enter Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
          />

          <button
            onClick={handleLogin}
            disabled={!email || !password || loading}
            className="w-full bg-gradient-to-r from-blue-500 to-indigo-600 text-white py-3 rounded-xl hover:opacity-90 transition"
          >
            {loading ? "Logging in..." : "Login"}
          </button>

          <p
            onClick={() => navigate("/register")}
            className="text-sky-400 text-sm mt-4 text-center cursor-pointer"
          >
            New doctor? Register here
          </p>
        </div>

        <p className="text-xs text-slate-500 text-center mt-6">
          Authorized medical personnel only
        </p>
      </motion.div>
    </div>
  );
};

export default Login;