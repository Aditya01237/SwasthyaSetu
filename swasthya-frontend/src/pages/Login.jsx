import { useState, useEffect } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

const Login = () => {
  const [uhid, setUhid] = useState("");
  const [otp, setOtp] = useState("");
  const [step, setStep] = useState(1); // 1 = send OTP, 2 = verify OTP
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  // 🔐 If already logged in → redirect
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      navigate("/dashboard");
    }
  }, [navigate]);

  // 📩 Send OTP
  const handleSendOtp = async () => {
    try {
      setLoading(true);

      await api.post("/auth/send-otp", { uhid });

      alert("OTP sent successfully");
      setStep(2);
    } catch (err) {
      alert(err.response?.data?.message || "Error sending OTP");
    } finally {
      setLoading(false);
    }
  };

  // ✅ Verify OTP
  const handleVerifyOtp = async () => {
    try {
      setLoading(true);

      const res = await api.post("/auth/verify-otp", {
        uhid: uhid,
        otp: otp,
      });

      // 🔐 Save token
      localStorage.setItem("token", res.data.data.token);

      // 🆔 Save UHID
      localStorage.setItem("uhid", uhid);

      // 🚀 Redirect
      navigate("/dashboard");

    } catch (err) {
      alert(err.response?.data?.message || "OTP verification failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-2xl shadow-md w-full max-w-md">
        <h2 className="text-2xl font-bold mb-6 text-center">
          Patient Login
        </h2>

        {step === 1 && (
          <>
            <input
              type="text"
              placeholder="Enter UHID"
              value={uhid}
              onChange={(e) => setUhid(e.target.value)}
              className="w-full p-3 border rounded-lg mb-4"
            />

            <button
              onClick={handleSendOtp}
              disabled={loading || !uhid}
              className="w-full bg-blue-600 text-white p-3 rounded-lg hover:bg-blue-700"
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
              className="w-full p-3 border rounded-lg mb-4"
            />

            <button
              onClick={handleVerifyOtp}
              disabled={loading || !otp}
              className="w-full bg-green-600 text-white p-3 rounded-lg hover:bg-green-700"
            >
              {loading ? "Verifying..." : "Verify OTP"}
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default Login;