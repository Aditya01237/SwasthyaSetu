import { useNavigate, useLocation } from "react-router-dom";

const AuthToggle = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const isLogin = location.pathname === "/";

  return (
    <div className="w-full flex justify-center mb-8">
      <div className="relative w-full max-w-md bg-white/[0.05] border border-white/[0.1] rounded-full p-1 flex">

        {/* Sliding Background */}
        <div
          className={`absolute top-1 bottom-1 w-[48%] rounded-full bg-gradient-to-r from-blue-500 to-indigo-600 transition-all duration-300 ${
            isLogin ? "left-1" : "left-[51%]"
          }`}
        />

        {/* Login */}
        <button
          onClick={() => navigate("/")}
          className={`w-1/2 z-10 py-2 rounded-full text-sm font-medium transition ${
            isLogin ? "text-white" : "text-slate-400"
          }`}
        >
          Login
        </button>

        {/* Register */}
        <button
          onClick={() => navigate("/register")}
          className={`w-1/2 z-10 py-2 rounded-full text-sm font-medium transition ${
            !isLogin ? "text-white" : "text-slate-400"
          }`}
        >
          Register
        </button>
      </div>
    </div>
  );
};

export default AuthToggle;