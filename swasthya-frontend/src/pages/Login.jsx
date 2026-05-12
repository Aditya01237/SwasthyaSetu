import { useState, useEffect } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";

const Login = () => {
  const [isRegister, setIsRegister] = useState(false);

  const [uhid, setUhid] = useState("");
  const [otp, setOtp] = useState("");
  const [step, setStep] = useState(1);
  const [maskedEmail, setMaskedEmail] = useState("");

  const [name, setName] = useState("");
  const [age, setAge] = useState("");
  const [gender, setGender] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");

  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("patientToken");

    if (token && window.location.pathname !== "/dashboard") {
      navigate("/dashboard", { replace: true });
    }
  }, []);

  const handleSendOtp = async () => {
    try {
      setLoading(true);
      const res = await api.post("/auth/send-otp", { uhid });
      setMaskedEmail(res.data?.maskedEmail || "");
      setStep(2);
    } catch (err) {
      console.error(err);
      alert(
        err.response?.data?.message ||
          err.response?.data ||
          err.message ||
          "OTP failed",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async () => {
    try {
      setLoading(true);
      const res = await api.post("/auth/verify-otp", { uhid, otp });
      const { token, patient } = res.data.data;
      localStorage.setItem("patientToken", token);
      localStorage.setItem("uhid", patient.uhid);
      localStorage.setItem("user", JSON.stringify(patient));
      navigate("/dashboard");
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || "Invalid OTP");
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
        email,
      });
      alert(
        `Registration successful!\nYour UHID: ${res.data.data.uhid}\nPlease save this for login.`,
      );
      setIsRegister(false);
      setStep(1);
    } catch (err) {
      alert(err.response?.data?.message || "Error");
    } finally {
      setLoading(false);
    }
  };

  const inputClass =
    "w-full px-4 py-3 bg-white/[0.05] border border-white/[0.08] rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-sky-500/60 focus:bg-white/[0.07] transition-all text-sm";

  return (
    <div className="min-h-screen bg-[#090c12] flex items-center justify-center px-4 relative overflow-hidden">
      {/* Glows */}
      <div className="absolute -top-40 left-1/2 -translate-x-1/2 w-[600px] h-64 bg-sky-500/10 blur-3xl rounded-full pointer-events-none" />
      <div className="absolute bottom-0 right-0 w-96 h-96 bg-blue-600/5 blur-3xl rounded-full pointer-events-none" />

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="w-full max-w-md"
      >
        {/* Card */}
        <div className="bg-white/[0.03] border border-white/[0.08] backdrop-blur-xl rounded-2xl shadow-2xl overflow-hidden">
          {/* Top accent bar */}
          <div className="h-1 w-full bg-gradient-to-r from-sky-500 to-blue-600" />

          <div className="p-8">
            {/* Logo */}
            <div className="flex justify-center mb-5">
              <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-sky-500 to-blue-600 flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-sky-500/20">
                S
              </div>
            </div>

            <h2 className="text-2xl font-bold text-center text-white tracking-tight">
              SwasthyaSetu
            </h2>
            <p className="text-sm text-slate-500 text-center mt-1 mb-7">
              {isRegister
                ? "Create your patient account"
                : "Login to your patient portal"}
            </p>

            {/* Toggle */}
            <div className="flex mb-7 bg-white/[0.04] border border-white/[0.07] rounded-xl p-1">
              <button
                onClick={() => {
                  setIsRegister(false);
                  setStep(1);
                }}
                className={`flex-1 py-2 text-sm font-medium rounded-lg transition-all duration-200
                  ${
                    !isRegister
                      ? "bg-gradient-to-r from-sky-500 to-blue-600 text-white shadow"
                      : "text-slate-500 hover:text-slate-300"
                  }`}
              >
                Login
              </button>
              <button
                onClick={() => setIsRegister(true)}
                className={`flex-1 py-2 text-sm font-medium rounded-lg transition-all duration-200
                  ${
                    isRegister
                      ? "bg-gradient-to-r from-sky-500 to-blue-600 text-white shadow"
                      : "text-slate-500 hover:text-slate-300"
                  }`}
              >
                Register
              </button>
            </div>

            <AnimatePresence mode="wait">
              {/* ── LOGIN ── */}
              {!isRegister && (
                <motion.div
                  key="login"
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: 10 }}
                  transition={{ duration: 0.2 }}
                >
                  {step === 1 && (
                    <div className="space-y-4">
                      <div>
                        <label className="text-[10px] uppercase tracking-wider text-slate-500 mb-1.5 block">
                          Patient UHID
                        </label>
                        <input
                          type="text"
                          placeholder="e.g. UHID1774349769452"
                          value={uhid}
                          onChange={(e) => setUhid(e.target.value)}
                          className={inputClass}
                        />
                      </div>

                      <button
                        onClick={handleSendOtp}
                        disabled={!uhid || loading}
                        className="w-full bg-gradient-to-r from-sky-500 to-blue-600 text-white py-3 rounded-xl font-medium text-sm
                                   hover:opacity-90 hover:-translate-y-px active:translate-y-0 transition-all
                                   disabled:opacity-50 disabled:cursor-not-allowed disabled:translate-y-0
                                   shadow-lg shadow-sky-500/20 flex items-center justify-center gap-2"
                      >
                        {loading ? (
                          <>
                            <svg
                              className="w-4 h-4 animate-spin"
                              fill="none"
                              viewBox="0 0 24 24"
                            >
                              <circle
                                className="opacity-25"
                                cx="12"
                                cy="12"
                                r="10"
                                stroke="currentColor"
                                strokeWidth="4"
                              />
                              <path
                                className="opacity-75"
                                fill="currentColor"
                                d="M4 12a8 8 0 018-8v8z"
                              />
                            </svg>
                            Sending OTP...
                          </>
                        ) : (
                          <>
                            <svg
                              className="w-4 h-4"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth={2}
                              viewBox="0 0 24 24"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                              />
                            </svg>
                            Send OTP to Email
                          </>
                        )}
                      </button>
                    </div>
                  )}

                  {step === 2 && (
                    <div className="space-y-4">
                      {/* Email hint */}
                      {maskedEmail && (
                        <div className="flex items-center gap-2 px-3 py-2.5 bg-sky-500/10 border border-sky-500/20 rounded-xl">
                          <svg
                            className="w-4 h-4 text-sky-400 flex-shrink-0"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth={2}
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                            />
                          </svg>
                          <p className="text-xs text-sky-300">
                            OTP sent to{" "}
                            <span className="font-semibold">{maskedEmail}</span>
                          </p>
                        </div>
                      )}

                      <div>
                        <label className="text-[10px] uppercase tracking-wider text-slate-500 mb-1.5 block">
                          Enter OTP
                        </label>
                        <input
                          type="text"
                          placeholder="6-digit OTP"
                          value={otp}
                          maxLength={6}
                          onChange={(e) =>
                            setOtp(e.target.value.replace(/\D/, ""))
                          }
                          className={`${inputClass} text-center text-xl tracking-[0.5em] font-bold`}
                        />
                      </div>

                      <button
                        onClick={handleVerifyOtp}
                        disabled={otp.length < 6 || loading}
                        className="w-full bg-gradient-to-r from-sky-500 to-blue-600 text-white py-3 rounded-xl font-medium text-sm
                                   hover:opacity-90 hover:-translate-y-px active:translate-y-0 transition-all
                                   disabled:opacity-50 disabled:cursor-not-allowed disabled:translate-y-0
                                   shadow-lg shadow-sky-500/20 flex items-center justify-center gap-2"
                      >
                        {loading ? (
                          <>
                            <svg
                              className="w-4 h-4 animate-spin"
                              fill="none"
                              viewBox="0 0 24 24"
                            >
                              <circle
                                className="opacity-25"
                                cx="12"
                                cy="12"
                                r="10"
                                stroke="currentColor"
                                strokeWidth="4"
                              />
                              <path
                                className="opacity-75"
                                fill="currentColor"
                                d="M4 12a8 8 0 018-8v8z"
                              />
                            </svg>
                            Verifying...
                          </>
                        ) : (
                          "Verify & Login"
                        )}
                      </button>

                      <button
                        onClick={() => {
                          setStep(1);
                          setOtp("");
                        }}
                        className="w-full text-xs text-slate-500 hover:text-sky-400 transition-colors py-1"
                      >
                        ← Change UHID
                      </button>
                    </div>
                  )}
                </motion.div>
              )}

              {/* ── REGISTER ── */}
              {isRegister && (
                <motion.div
                  key="register"
                  initial={{ opacity: 0, x: 10 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: -10 }}
                  transition={{ duration: 0.2 }}
                  className="space-y-3"
                >
                  {[
                    {
                      label: "Full Name",
                      placeholder: "Dr. John Doe",
                      value: name,
                      set: setName,
                      type: "text",
                    },
                    {
                      label: "Age",
                      placeholder: "25",
                      value: age,
                      set: setAge,
                      type: "number",
                    },
                    {
                      label: "Phone",
                      placeholder: "+91 98765 43210",
                      value: phone,
                      set: setPhone,
                      type: "tel",
                    },
                    {
                      label: "Email",
                      placeholder: "you@example.com",
                      value: email,
                      set: setEmail,
                      type: "email",
                    },
                  ].map(({ label, placeholder, value, set, type }) => (
                    <div key={label}>
                      <label className="text-[10px] uppercase tracking-wider text-slate-500 mb-1.5 block">
                        {label}
                      </label>
                      <input
                        type={type}
                        placeholder={placeholder}
                        value={value}
                        onChange={(e) => set(e.target.value)}
                        className={inputClass}
                      />
                    </div>
                  ))}

                  {/* Gender select */}
                  <div>
                    <label className="text-[10px] uppercase tracking-wider text-slate-500 mb-1.5 block">
                      Gender
                    </label>
                    <div className="flex gap-2">
                      {["Male", "Female", "Other"].map((g) => (
                        <button
                          key={g}
                          onClick={() => setGender(g)}
                          className={`flex-1 py-2.5 text-xs rounded-xl border transition-all font-medium
                            ${
                              gender === g
                                ? "bg-sky-500/20 border-sky-500/50 text-sky-300"
                                : "bg-white/[0.03] border-white/[0.08] text-slate-500 hover:border-white/20"
                            }`}
                        >
                          {g}
                        </button>
                      ))}
                    </div>
                  </div>

                  <button
                    onClick={handleRegister}
                    disabled={
                      loading || !name || !age || !gender || !phone || !email
                    }
                    className="w-full bg-gradient-to-r from-sky-500 to-blue-600 text-white py-3 rounded-xl font-medium text-sm
                               hover:opacity-90 hover:-translate-y-px active:translate-y-0 transition-all mt-1
                               disabled:opacity-50 disabled:cursor-not-allowed disabled:translate-y-0
                               shadow-lg shadow-sky-500/20 flex items-center justify-center gap-2"
                  >
                    {loading ? (
                      <>
                        <svg
                          className="w-4 h-4 animate-spin"
                          fill="none"
                          viewBox="0 0 24 24"
                        >
                          <circle
                            className="opacity-25"
                            cx="12"
                            cy="12"
                            r="10"
                            stroke="currentColor"
                            strokeWidth="4"
                          />
                          <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8v8z"
                          />
                        </svg>
                        Creating Account...
                      </>
                    ) : (
                      "Create Account"
                    )}
                  </button>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Footer */}
          <div className="px-8 pb-6 text-center">
            <p className="text-[11px] text-slate-600">
              Authorized patients only · SwasthyaSetu © 2026
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default Login;
