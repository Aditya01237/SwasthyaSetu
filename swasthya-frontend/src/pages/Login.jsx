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

      localStorage.setItem("token", res.data.data.token);
      localStorage.setItem("uhid", uhid);

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
    <div className="min-h-screen flex items-center justify-center bg-blue-50 px-4">

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8"
      >

        {/* 🔵 Small Healthcare Illustration */}
        <div className="flex justify-center mb-6">
          <img
            src="https://cdn-icons-png.flaticon.com/512/2966/2966327.png"
            alt="health"
            className="w-16 h-16 opacity-90"
          />
        </div>

        {/* Title */}
        <h2 className="text-2xl font-semibold text-center text-gray-800">
          SwasthyaSetu
        </h2>
        <p className="text-sm text-gray-500 text-center mb-6">
          {isRegister ? "Create your account" : "Login to continue"}
        </p>

        {/* Toggle */}
        <div className="flex mb-6 border rounded-lg overflow-hidden">
          <button
            onClick={() => setIsRegister(false)}
            className={`flex-1 py-2 text-sm ${
              !isRegister ? "bg-blue-600 text-white" : "text-gray-600"
            }`}
          >
            Login
          </button>
          <button
            onClick={() => setIsRegister(true)}
            className={`flex-1 py-2 text-sm ${
              isRegister ? "bg-blue-600 text-white" : "text-gray-600"
            }`}
          >
            Register
          </button>
        </div>

        {/* Form Animation */}
        <motion.div
          key={isRegister ? "register" : "login"}
          initial={{ opacity: 0, x: 10 }}
          animate={{ opacity: 1, x: 0 }}
        >

          {!isRegister ? (
            <>
              {step === 1 && (
                <>
                  <input
                    type="text"
                    placeholder="Enter UHID"
                    value={uhid}
                    onChange={(e) => setUhid(e.target.value)}
                    className="w-full p-3 border rounded-lg mb-4 focus:ring-2 focus:ring-blue-500"
                  />

                  <button
                    onClick={handleSendOtp}
                    disabled={!uhid || loading}
                    className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition"
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
                    className="w-full p-3 border rounded-lg mb-4 focus:ring-2 focus:ring-blue-500"
                  />

                  <button
                    onClick={handleVerifyOtp}
                    disabled={!otp || loading}
                    className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition"
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
                className="w-full p-3 border rounded-lg mb-3"
              />

              <input
                placeholder="Age"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                className="w-full p-3 border rounded-lg mb-3"
              />

              <input
                placeholder="Gender"
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                className="w-full p-3 border rounded-lg mb-3"
              />

              <input
                placeholder="Phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="w-full p-3 border rounded-lg mb-4"
              />

              <button
                onClick={handleRegister}
                disabled={loading}
                className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition"
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