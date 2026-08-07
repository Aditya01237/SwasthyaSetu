import { useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import logo from "../assets/logo.png";

const Register = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [verified, setVerified] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const sendOtp = async () => {
    if (!email) return alert("Enter the email used in your hospital invitation");
    try {
      setLoading(true);
      await api.post(`/auth/doctor/send-otp?email=${encodeURIComponent(email)}`);
      setOtpSent(true);
      alert("OTP sent to your invited email address");
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || "No active hospital invitation was found");
    } finally {
      setLoading(false);
    }
  };

  const verifyOtp = async () => {
    if (!otp) return alert("Enter OTP");
    try {
      setLoading(true);
      await api.post(
        `/auth/doctor/verify-otp?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}`,
      );
      setVerified(true);
      alert("Email verified ✅");
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || "Invalid OTP");
    } finally {
      setLoading(false);
    }
  };

  const activateAccount = async () => {
    if (!verified) return alert("Verify your invited email first");
    if (password.length < 8) return alert("Password must be at least 8 characters");
    try {
      setLoading(true);
      await api.post("/auth/doctor/register", { email, password });
      alert("Doctor account activated 🎉");
      navigate("/");
    } catch (err) {
      alert(err.response?.data?.message || err.response?.data || "Account activation failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0f1117] flex items-center justify-center px-4" style={{ fontFamily: "'Inter', sans-serif" }}>
      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35 }}
        className="w-full max-w-sm"
      >
        <div className="bg-[#1a1f2e] border border-white/[0.07] rounded-2xl p-8">
          <div className="flex flex-col items-center gap-2 mb-6">
            <div className="w-20 h-20 rounded-2xl bg-white p-2 flex items-center justify-center">
              <img src={logo} alt="SwasthyaSetu" className="w-full h-full object-contain rounded-xl" />
            </div>
            <p className="text-white text-xl font-bold">Activate Doctor Account</p>
            <p className="text-sm text-gray-500 text-center">
              Your hospital must invite you before you can create an account.
            </p>
          </div>

          <div className="space-y-4">
            <div>
              <label className="block text-[11px] font-semibold text-gray-500 tracking-widest uppercase mb-2">Invited Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => { setEmail(e.target.value); setOtpSent(false); setVerified(false); }}
                placeholder="doctor@hospital.com"
                disabled={verified}
                className="w-full bg-[#111827] border border-white/[0.08] rounded-xl px-4 py-3 text-sm text-gray-200 placeholder-gray-600 outline-none focus:border-indigo-500/50 disabled:opacity-60"
              />
            </div>

            {!verified && (
              <button
                onClick={sendOtp}
                disabled={!email || loading}
                className="w-full py-3 rounded-xl text-sm font-semibold text-indigo-300 border border-indigo-500/25 bg-indigo-500/10 disabled:opacity-40"
              >
                {otpSent ? "Resend OTP" : "Send OTP"}
              </button>
            )}

            {otpSent && !verified && (
              <>
                <input
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  placeholder="6-digit OTP"
                  inputMode="numeric"
                  className="w-full bg-[#111827] border border-white/[0.08] rounded-xl px-4 py-3 text-sm text-gray-200 text-center tracking-[0.3em] outline-none focus:border-indigo-500/50"
                />
                <button
                  onClick={verifyOtp}
                  disabled={!otp || loading}
                  className="w-full py-3 rounded-xl text-sm font-semibold text-indigo-300 border border-indigo-500/25 bg-indigo-500/10 disabled:opacity-40"
                >
                  Verify Email
                </button>
              </>
            )}

            {verified && (
              <>
                <div className="text-xs text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 rounded-xl px-4 py-3">
                  ✓ Invitation email verified
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-gray-500 tracking-widest uppercase mb-2">Create Password</label>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Minimum 8 characters"
                    className="w-full bg-[#111827] border border-white/[0.08] rounded-xl px-4 py-3 text-sm text-gray-200 placeholder-gray-600 outline-none focus:border-indigo-500/50"
                  />
                </div>
                <button
                  onClick={activateAccount}
                  disabled={password.length < 8 || loading}
                  className="w-full py-3 rounded-xl text-sm font-semibold text-white disabled:opacity-40"
                  style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
                >
                  {loading ? "Activating..." : "Activate Account"}
                </button>
              </>
            )}
          </div>

          <button onClick={() => navigate("/")} className="w-full mt-5 text-xs text-gray-500 hover:text-gray-300">
            Already activated? Back to login
          </button>
        </div>
      </motion.div>
    </div>
  );
};

export default Register;
