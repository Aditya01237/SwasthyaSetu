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
    <nav className="bg-white shadow-md px-6 py-4 flex justify-between items-center">

      {/* 🔷 Logo */}
      <div
        className="flex items-center gap-2 cursor-pointer"
        onClick={() => navigate("/dashboard")}
      >
        <div className="w-8 h-8 bg-blue-600 text-white flex items-center justify-center rounded-lg font-bold">
          S
        </div>
        <h1 className="text-xl font-semibold text-gray-800">
          SwasthyaSetu
        </h1>
      </div>

      {/* 👤 Profile */}
      <div className="relative">
        <button
          onClick={() => setOpen(!open)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
        >
          Profile ▾
        </button>

        {/* Dropdown */}
        {open && (
          <div className="absolute right-0 mt-2 w-48 bg-white border rounded-lg shadow-lg">

            <button
              onClick={() => navigate("/profile")}
              className="w-full text-left px-4 py-2 hover:bg-gray-100"
            >
              Update Profile
            </button>

            <button
              onClick={() => navigate("/dashboard")}
              className="w-full text-left px-4 py-2 hover:bg-gray-100"
            >
              View History
            </button>

            <button
              onClick={() => navigate("/qr-audit")}
              className="w-full text-left px-4 py-2 hover:bg-gray-100"
            >
              QR Audit
            </button>

            <button
              onClick={handleLogout}
              className="w-full text-left px-4 py-2 text-red-500 hover:bg-red-50"
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;