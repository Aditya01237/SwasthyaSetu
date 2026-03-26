import { useState } from "react";
import { useNavigate } from "react-router-dom";

const Navbar = () => {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  return (
    <nav className="sticky top-0 z-50 backdrop-blur-xl bg-[#090c12]/80 border-b border-white/[0.06] px-6 py-3 flex justify-between items-center">

      {/* 🔷 Logo */}
      <div
        className="flex items-center gap-3 cursor-pointer group"
        onClick={() => navigate("/dashboard")}
      >
        <div className="w-9 h-9 bg-gradient-to-br from-sky-500 to-blue-600 text-white flex items-center justify-center rounded-xl font-bold shadow-md group-hover:scale-105 transition">
          S
        </div>
        <h1 className="text-lg font-semibold text-slate-200 tracking-wide group-hover:text-sky-400 transition">
          SwasthyaSetu
        </h1>
      </div>

      {/* 👤 Profile */}
      <div className="relative">
        <button
          onClick={() => setOpen(!open)}
          className="flex items-center gap-2 bg-white/[0.04] border border-white/[0.08] text-slate-300 px-4 py-2 rounded-xl text-sm
                     hover:bg-white/[0.06] hover:border-sky-500/30 hover:text-white
                     transition-all duration-200"
        >
          Profile
          <svg className="w-4 h-4 opacity-70" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        {/* Dropdown */}
        {open && (
          <div className="absolute right-0 mt-3 w-52 bg-[#0f141c]/95 backdrop-blur-xl border border-white/[0.08] rounded-2xl shadow-[0_20px_50px_rgba(0,0,0,0.6)] overflow-hidden animate-fadeUp">

            <button
              onClick={() => navigate("/profile")}
              className="w-full text-left px-4 py-3 text-sm text-slate-300 hover:bg-white/[0.05] hover:text-white transition"
            >
              👤 Update Profile
            </button>

            <button
              onClick={() => navigate("/dashboard")}
              className="w-full text-left px-4 py-3 text-sm text-slate-300 hover:bg-white/[0.05] hover:text-white transition"
            >
              📊 View History
            </button>

            <button
              onClick={() => navigate("/qr-audit")}
              className="w-full text-left px-4 py-3 text-sm text-slate-300 hover:bg-white/[0.05] hover:text-white transition"
            >
              🔍 QR Audit
            </button>

            <div className="h-px bg-white/[0.06]" />

            <button
              onClick={handleLogout}
              className="w-full text-left px-4 py-3 text-sm text-red-400 hover:bg-red-500/10 transition"
            >
              🚪 Logout
            </button>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;