import { useState, useEffect } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";

const Login = () => {
  const [isRegister, setIsRegister] = useState(false);

  const [uhid, setUhid] = useState("");
  const [otp, setOtp] = useState("");
  const [step, setStep] = useState(1);

  const [name, setName] = useState("");
  const [age, setAge] = useState("");
  const [gender, setGender] = useState("");
  const [phone, setPhone] = useState("");

  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) navigate("/dashboard");
  }, [navigate]);

  const handleSendOtp = async () => {
    try {
      setLoading(true);
      await api.post("/auth/send-otp", { uhid });
      setStep(2);
    } catch (err) {
      alert(err.response?.data?.message || "Error");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async () => {
    try {
      setLoading(true);

      const res = await api.post("/auth/verify-otp", { uhid, otp });

      console.log(res.data); // 🔥 check this once

      const { token, patient } = res.data.data; // ✅ correct extraction

      localStorage.setItem("token", token);
      localStorage.setItem("uhid", patient.uhid); // better than manual
      localStorage.setItem("user", JSON.stringify(patient)); // 🔥 MOST IMPORTANT

      navigate("/dashboard");
    } catch (err) {
      alert(err.response?.data?.message || "Error");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    try {
      setLoading(true);
      const res = await api.post("/patient/register", {
        name,
        age,
        gender,
        phone,
      });

      alert(`UHID: ${res.data.data.uhid}`);
      setIsRegister(false);
    } catch (err) {
      alert(err.response?.data?.message || "Error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#090c12] flex items-center justify-center px-4 relative">
      {/* 🔥 Background Glow */}
      <div className="absolute -top-40 left-1/2 -translate-x-1/2 w-[600px] h-64 bg-sky-500/10 blur-3xl rounded-full" />

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md bg-white/[0.03] border border-white/[0.08] backdrop-blur-xl rounded-2xl p-8 shadow-xl"
      >
        {/* Logo */}
        <div className="flex justify-center mb-6">
          <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-sky-500 to-blue-600 flex items-center justify-center text-white font-bold text-xl">
            S
          </div>
        </div>

        {/* Title */}
        <h2 className="text-2xl font-semibold text-center text-white">
          SwasthyaSetu
        </h2>
        <p className="text-sm text-slate-400 text-center mb-6">
          {isRegister ? "Create your account" : "Login to continue"}
        </p>

        {/* Toggle */}
        <div className="flex mb-6 border border-white/[0.08] rounded-lg overflow-hidden">
          <button
            onClick={() => {
              setIsRegister(false);
              setStep(1);
            }}
            className={`flex-1 py-2 text-sm transition ${
              !isRegister
                ? "bg-sky-500 text-white"
                : "text-slate-400 hover:bg-white/[0.05]"
            }`}
          >
            Login
          </button>
          <button
            onClick={() => setIsRegister(true)}
            className={`flex-1 py-2 text-sm transition ${
              isRegister
                ? "bg-sky-500 text-white"
                : "text-slate-400 hover:bg-white/[0.05]"
            }`}
          >
            Register
          </button>
        </div>

        <motion.div
          key={isRegister ? "register" : "login"}
          initial={{ opacity: 0, x: 10 }}
          animate={{ opacity: 1, x: 0 }}
        >
          {/* LOGIN */}
          {!isRegister ? (
            <>
              {step === 1 && (
                <>
                  <input
                    type="text"
                    placeholder="Enter UHID"
                    value={uhid}
                    onChange={(e) => setUhid(e.target.value)}
                    className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl mb-4 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-sky-500"
                  />

                  <button
                    onClick={handleSendOtp}
                    disabled={!uhid || loading}
                    className="w-full bg-gradient-to-r from-sky-500 to-blue-600 text-white py-3 rounded-xl hover:opacity-90 transition"
                  >
                    {loading ? "Sending..." : "Send OTP"}
                  </button>
                </>
              )}

              {step === 2 && (
                <>
                  <input
                    type="text"
                    placeholder="Enter OTP"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl mb-4 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-sky-500"
                  />

                  <button
                    onClick={handleVerifyOtp}
                    disabled={!otp || loading}
                    className="w-full bg-gradient-to-r from-sky-500 to-blue-600 text-white py-3 rounded-xl hover:opacity-90 transition"
                  >
                    {loading ? "Verifying..." : "Verify OTP"}
                  </button>
                </>
              )}
            </>
          ) : (
            <>
              <input
                placeholder="Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl mb-3 text-slate-200"
              />

              <input
                placeholder="Age"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl mb-3 text-slate-200"
              />

              <input
                placeholder="Gender"
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl mb-3 text-slate-200"
              />

              <input
                placeholder="Phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full p-3 bg-white/[0.05] border border-white/[0.08] rounded-xl mb-4 text-slate-200"
              />

              <button
                onClick={handleRegister}
                disabled={loading}
                className="w-full bg-gradient-to-r from-sky-500 to-blue-600 text-white py-3 rounded-xl hover:opacity-90 transition"
              >
                {loading ? "Creating..." : "Create Account"}
              </button>
            </>
          )}
        </motion.div>
      </motion.div>
    </div>
  );
};

export default Login;
