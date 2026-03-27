import React from "react";
import Navbar from "../components/Navbar";

const Profile = () => {
  const user = JSON.parse(localStorage.getItem("user") || "{}");

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
      <Navbar />

      {/* 🔥 Background glow */}
      <div className="fixed -top-32 left-1/2 -translate-x-1/2 w-[700px] h-64 bg-sky-500/[0.07] rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-5xl mx-auto px-6 pt-10 pb-20 space-y-6">

        {/* HEADER CARD */}
        <div className="bg-white/[0.03] border border-white/[0.08] rounded-2xl p-6 flex items-center gap-6">

          {/* Avatar */}
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-sky-500/30 to-blue-700/30 border border-sky-500/30 flex items-center justify-center text-3xl font-bold text-sky-400">
            {user?.name?.charAt(0)?.toUpperCase() || "U"}
          </div>

          {/* Info */}
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-white">
              {user?.name || "User"}
            </h1>

            <p className="text-slate-400 text-sm mt-1">
              UHID: <span className="text-sky-400">{user?.uhid}</span>
            </p>
          </div>

          {/* Edit button (future use) */}
          <button className="bg-white/[0.05] border border-white/[0.08] text-slate-300 px-4 py-2 rounded-xl text-sm hover:bg-white/[0.08] transition">
            ✏️ Edit
          </button>
        </div>

        {/* INFO GRID */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">

          {/* Age */}
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5">
            <p className="text-xs text-slate-500 uppercase tracking-wide">
              Age
            </p>
            <p className="text-xl font-semibold text-white mt-2">
              {user?.age || "—"}
            </p>
          </div>

          {/* Gender */}
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5">
            <p className="text-xs text-slate-500 uppercase tracking-wide">
              Gender
            </p>
            <p className="text-xl font-semibold text-white mt-2">
              {user?.gender || "—"}
            </p>
          </div>

          {/* Phone */}
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5">
            <p className="text-xs text-slate-500 uppercase tracking-wide">
              Phone
            </p>
            <p className="text-xl font-semibold text-white mt-2">
              {user?.phone || "—"}
            </p>
          </div>
        </div>

        {/* EXTRA SECTION */}
        <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6">
          <h2 className="text-lg font-semibold text-white mb-3">
            Account Info
          </h2>

          <div className="space-y-3 text-sm text-slate-400">
            <p>
              🆔 UHID: <span className="text-slate-300">{user?.uhid}</span>
            </p>
            <p>
              📱 Phone: <span className="text-slate-300">{user?.phone}</span>
            </p>
            <p>
              👤 Name: <span className="text-slate-300">{user?.name}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;