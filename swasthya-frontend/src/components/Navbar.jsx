import { useState, useRef, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";

const Navbar = () => {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const dropdownRef = useRef(null);

  // ✅ Get user from localStorage
  const user = JSON.parse(localStorage.getItem("user"));
  console.log(user);

  const handleLogout = () => {
    localStorage.clear();
    navigate("/");
  };

  // ✅ Close dropdown on outside click
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const menuItem =
    "w-full flex items-center gap-3 px-4 py-2.5 text-sm text-slate-400 hover:bg-sky-500/10 hover:text-sky-300 rounded-lg transition-all duration-150";

  const navLinks = [{ label: "MyAppointments", path: "/appointment" }];

  return (
    <nav className="sticky top-0 z-50 backdrop-blur-2xl bg-[#090c12]/90 border-b border-white/[0.06] px-6 h-14 flex items-center justify-between">
      {/* 🔷 LEFT: LOGO + NAV LINKS */}
      <div className="flex items-center gap-8">
        {/* Logo */}
        <div
          className="flex items-center gap-2.5 cursor-pointer group"
          onClick={() => navigate("/dashboard")}
        >
          <div className="w-8 h-8 bg-gradient-to-br from-sky-400 to-blue-600 rounded-lg flex items-center justify-center font-bold text-white text-sm shadow-lg shadow-sky-500/20 group-hover:scale-105 transition">
            S
          </div>
          <span className="text-[15px] font-semibold text-slate-200 tracking-wide group-hover:text-white">
            SwasthyaSetu
          </span>
        </div>
      </div>

      {/* 🔷 RIGHT: PROFILE */}
      <div className="relative flex gap-6" ref={dropdownRef}>
        {/* Nav Links */}
        <div className="hidden md:flex items-center gap-6">
          {navLinks.map((link) => {
            const active = location.pathname === link.path;
            return (
              <button
                key={link.path}
                onClick={() => navigate(link.path)}
                className={`text-sm transition ${
                  active
                    ? "text-sky-400 font-medium"
                    : "text-slate-400 hover:text-white"
                }`}
              >
                {link.label}
              </button>
            );
          })}
        </div>
        <button
          onClick={() => setOpen(!open)}
          className="flex items-center gap-2.5 group"
        >
          {/* Avatar */}
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-sky-500/30 to-blue-600/30 border border-sky-500/30 flex items-center justify-center text-sky-300 text-xs font-semibold group-hover:border-sky-400/60 transition">
            {user?.name?.charAt(0)?.toUpperCase() || "U"}
          </div>

          {/* Name (hidden on small screens) */}
          <span className="hidden md:block text-sm text-slate-300 group-hover:text-white transition">
            {user?.name || "User"}
          </span>

          {/* Arrow */}
          <svg
            className={`w-3.5 h-3.5 text-slate-500 transition-transform ${
              open ? "rotate-180" : ""
            }`}
            fill="none"
            stroke="currentColor"
            strokeWidth={2.5}
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </button>

        {/* 🔥 DROPDOWN */}
        {open && (
          <div className="absolute right-0 mt-3 w-56 bg-[#0d1117] border border-white/[0.08] rounded-2xl shadow-[0_24px_60px_rgba(0,0,0,0.7)] overflow-hidden animate-fadeUp">
            {/* User Info */}
            <div className="px-4 py-3 flex items-center gap-2 border-b border-white/[0.06]">
              <p className="text-xs text-slate-500">Name :</p>
              <p className="text-sm text-slate-200 font-medium truncate">
                {user?.name || "Guest"}
              </p>
            </div>

            <div className="p-1.5 flex flex-col gap-0.5">
              <button
                onClick={() => {
                  navigate("/profile");
                  setOpen(false);
                }}
                className={menuItem}
              >
                👤 My Profile
              </button>
              <button
                onClick={() => {
                  navigate("/qr-audit");
                  setOpen(false);
                }}
                className={menuItem}
              >
                🔍 QR Audit
              </button>

              <div className="h-px bg-white/[0.06] my-1" />

              <button
                onClick={handleLogout}
                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:bg-red-500/10 rounded-lg transition"
              >
                🚪 Logout
              </button>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
