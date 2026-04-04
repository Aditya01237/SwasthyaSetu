import { useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

const Register = () => {
  const [form, setForm] = useState({
    name: "",
    specialization: "",
    experience: "",
    fee: "",
    email: "",
    password: "",
    hospitalId: ""
  });

  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleRegister = async () => {
    try {
      await api.post("/auth/doctor/register", form);
      alert("Doctor registered successfully 🎉");
      navigate("/");
    } catch (err) {
      alert(err.response?.data || "Error");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#090c12]">

      <div className="bg-white/[0.03] border border-white/[0.08] p-8 rounded-2xl w-full max-w-md">

        <h2 className="text-xl text-white mb-6 text-center">
          Doctor Registration
        </h2>

        {Object.keys(form).map((key) => (
          <input
            key={key}
            name={key}
            placeholder={key}
            value={form[key]}
            onChange={handleChange}
            className="w-full mb-3 p-3 rounded bg-white/[0.05] text-white"
          />
        ))}

        <button
          onClick={handleRegister}
          className="w-full bg-sky-500 py-3 rounded text-white mt-2"
        >
          Register
        </button>
      </div>
    </div>
  );
};

export default Register;