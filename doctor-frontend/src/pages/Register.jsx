import { useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import AuthToggle from "../components/AuthToggle";

const STEPS = [
  {
    id: "profile",
    label: "Profile",
    fields: [
      { name: "name", label: "Full Name", placeholder: "Dr. John Smith", type: "text" },
      { name: "specialization", label: "Specialization", placeholder: "e.g. Cardiology", type: "text" },
    ],
  },
  {
    id: "credentials",
    label: "Credentials",
    fields: [
      { name: "experience", label: "Years of Experience", placeholder: "e.g. 8", type: "number" },
      { name: "fee", label: "Consultation Fee (₹)", placeholder: "e.g. 500", type: "number" },
      { name: "hospitalId", label: "Hospital ID", placeholder: "HSP-XXXX", type: "text" },
    ],
  },
  {
    id: "account",
    label: "Account",
    fields: [
      { name: "email", label: "Email Address", placeholder: "doctor@hospital.com", type: "email" },
      { name: "password", label: "Password", placeholder: "Min. 8 characters", type: "password" },
    ],
  },
];

const InputField = ({ field, value, onChange }) => (
  <div>
    <label className="block text-[11px] font-semibold text-gray-500 tracking-widest uppercase mb-2">
      {field.label}
    </label>
    <input
      type={field.type}
      name={field.name}
      value={value}
      onChange={onChange}
      placeholder={field.placeholder}
      style={{ fontFamily: "'Inter', sans-serif" }}
      className="w-full bg-[#111827] border border-white/[0.08] rounded-xl px-4 py-3 text-sm text-gray-200 placeholder-gray-600 outline-none transition-all focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/10"
    />
  </div>
);

const Register = () => {
  const [form, setForm] = useState({
    name: "",
    specialization: "",
    experience: "",
    fee: "",
    email: "",
    password: "",
    hospitalId: "",
  });

  const [step, setStep] = useState(0);
  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [verified, setVerified] = useState(false);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const sendOtp = async () => {
    if (!form.email) return alert("Enter email first");
    try {
      setLoading(true);
      await api.post(`/auth/doctor/send-otp?email=${form.email}`);
      setOtpSent(true);
      alert("OTP sent 📩");
    } catch (err) {
      alert(err.response?.data || "Error sending OTP");
    } finally {
      setLoading(false);
    }
  };

  const verifyOtp = async () => {
    if (!otp) return alert("Enter OTP");
    try {
      setLoading(true);
      await api.post(`/auth/doctor/verify-otp?email=${form.email}&otp=${otp}`);
      setVerified(true);
      alert("Email verified ✅");
    } catch (err) {
      alert(err.response?.data || "Invalid OTP");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!verified) return alert("Verify email first");
    try {
      setLoading(true);
      await api.post("/auth/doctor/register", form);
      alert("Doctor registered 🎉");
      navigate("/");
    } catch (err) {
      alert(err.response?.data || "Error");
    } finally {
      setLoading(false);
    }
  };

  const isLastStep = step === STEPS.length - 1;
  const currentStepFields = STEPS[step].fields;
  const allCurrentFilled = currentStepFields.every((f) => form[f.name]?.toString().trim() !== "");

  return (
    <div
      style={{ fontFamily: "'Inter', sans-serif" }}
      className="min-h-screen bg-[#0f1117] flex flex-col items-center justify-center px-4 py-12"
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
            <div
              className="w-14 h-14 rounded-2xl flex items-center justify-center text-white text-2xl font-bold"
              style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
            >
              D
            </div>
            <p className="text-white text-xl font-bold tracking-tight">MedPortal</p>
            <p className="text-sm text-gray-500">New doctor registration</p>
          </div>

          {/* Toggle */}
          <div className="flex bg-[#111827] rounded-full p-1 mb-7">
            <button
              onClick={() => navigate("/")}
              className="flex-1 py-2.5 rounded-full text-sm font-medium text-gray-500 transition-all hover:text-gray-300"
            >
              Login
            </button>
            <button
              className="flex-1 py-2.5 rounded-full text-sm font-semibold text-white transition-all"
              style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
            >
              Register
            </button>
          </div>

          {/* Step indicators */}
          <div className="flex items-center mb-7">
            {STEPS.map((s, i) => (
              <div key={s.id} className="flex items-center" style={{ flex: i < STEPS.length - 1 ? "1 1 0%" : "0 0 auto" }}>
                <button
                  onClick={() => i < step && setStep(i)}
                  className="flex items-center gap-2"
                  style={{ cursor: i < step ? "pointer" : "default" }}
                >
                  <div
                    className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0 transition-all"
                    style={
                      i < step
                        ? { background: "linear-gradient(135deg, #3b82f6, #6366f1)", color: "#fff" }
                        : i === step
                        ? { background: "linear-gradient(135deg, #3b82f6, #6366f1)", color: "#fff", boxShadow: "0 0 0 3px rgba(99,102,241,0.25)" }
                        : { background: "#1f2937", color: "#4b5563", border: "1px solid rgba(255,255,255,0.08)" }
                    }
                  >
                    {i < step ? (
                      <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                        <path d="M2.5 6l2.5 2.5 5-5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    ) : (
                      i + 1
                    )}
                  </div>
                  <span
                    className="text-xs font-medium transition-colors"
                    style={{ color: i === step ? "#e5e7eb" : "#4b5563" }}
                  >
                    {s.label}
                  </span>
                </button>
                {i < STEPS.length - 1 && (
                  <div
                    className="flex-1 h-px mx-2 transition-all"
                    style={{ background: i < step ? "rgba(99,102,241,0.4)" : "rgba(255,255,255,0.07)" }}
                  />
                )}
              </div>
            ))}
          </div>

          {/* Step fields */}
          <AnimatePresence mode="wait">
            <motion.div
              key={step}
              initial={{ opacity: 0, x: 16 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -16 }}
              transition={{ duration: 0.2, ease: "easeOut" }}
              className="space-y-4"
            >
              {currentStepFields.map((field) => (
                <InputField
                  key={field.name}
                  field={field}
                  value={form[field.name]}
                  onChange={handleChange}
                />
              ))}

              {/* OTP section — only on last step */}
              {isLastStep && (
                <div
                  className="rounded-xl p-4 space-y-3"
                  style={{ background: "rgba(99,102,241,0.06)", border: "1px solid rgba(99,102,241,0.15)" }}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-[11px] font-semibold text-gray-500 tracking-widest uppercase">
                      Email Verification
                    </span>
                    {verified && (
                      <span className="text-[11px] font-semibold text-emerald-400 flex items-center gap-1">
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                          <circle cx="6" cy="6" r="5" stroke="currentColor" strokeWidth="1.2" />
                          <path d="M3.5 6l2 2 3.5-3.5" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                        Verified
                      </span>
                    )}
                  </div>

                  {!verified && (
                    <button
                      onClick={sendOtp}
                      disabled={!form.email || loading}
                      className="w-full py-2.5 rounded-xl text-sm font-medium transition-all disabled:opacity-30"
                      style={{
                        background: "rgba(99,102,241,0.12)",
                        border: "1px solid rgba(99,102,241,0.25)",
                        color: "#818cf8",
                      }}
                    >
                      {loading && !otpSent ? "Sending..." : otpSent ? "Resend OTP" : "Send OTP to Email →"}
                    </button>
                  )}

                  {otpSent && !verified && (
                    <div className="space-y-2">
                      <input
                        placeholder="Enter 6-digit OTP"
                        value={otp}
                        onChange={(e) => setOtp(e.target.value)}
                        className="w-full bg-[#111827] border border-white/[0.08] rounded-xl px-4 py-3 text-sm text-gray-200 placeholder-gray-600 outline-none text-center tracking-[0.3em] transition-all focus:border-indigo-500/50 focus:ring-2 focus:ring-indigo-500/10"
                        style={{ fontFamily: "'Inter', sans-serif" }}
                      />
                      <button
                        onClick={verifyOtp}
                        disabled={!otp || loading}
                        className="w-full py-2.5 rounded-xl text-sm font-medium transition-all disabled:opacity-30"
                        style={{
                          background: "rgba(99,102,241,0.15)",
                          border: "1px solid rgba(99,102,241,0.3)",
                          color: "#818cf8",
                        }}
                      >
                        {loading ? "Verifying..." : "Verify OTP"}
                      </button>
                    </div>
                  )}
                </div>
              )}
            </motion.div>
          </AnimatePresence>

          {/* Navigation */}
          <div className={`flex gap-3 mt-6 ${step === 0 ? "justify-end" : "justify-between"}`}>
            {step > 0 && (
              <button
                onClick={() => setStep(step - 1)}
                className="px-5 py-3 rounded-xl text-sm font-medium transition-all"
                style={{
                  background: "#111827",
                  border: "1px solid rgba(255,255,255,0.08)",
                  color: "#9ca3af",
                }}
              >
                ← Back
              </button>
            )}

            {!isLastStep ? (
              <button
                onClick={() => setStep(step + 1)}
                disabled={!allCurrentFilled}
                className="flex-1 py-3 rounded-xl text-sm font-semibold text-white transition-all disabled:opacity-30 hover:opacity-90"
                style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
              >
                Continue →
              </button>
            ) : (
              <button
                onClick={handleRegister}
                disabled={!verified || loading}
                className="flex-1 py-3 rounded-xl text-sm font-semibold text-white flex items-center justify-center gap-2 transition-all disabled:opacity-30 hover:opacity-90"
                style={{ background: "linear-gradient(135deg, #3b82f6, #6366f1)" }}
              >
                {loading ? (
                  <>
                    <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2" strokeOpacity="0.3" />
                      <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                    </svg>
                    Registering...
                  </>
                ) : (
                  "Complete Registration →"
                )}
              </button>
            )}
          </div>

          <p className="text-center text-[11px] text-gray-700 mt-4 tracking-widest uppercase">
            Step {step + 1} / {STEPS.length}
          </p>
        </div>
      </motion.div>

      <style>{`@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');`}</style>
    </div>
  );
};

export default Register;