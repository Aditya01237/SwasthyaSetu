import React from "react";
import { useNavigate } from "react-router";

const DoctorProfilePage = () => {
  const doctor = JSON.parse(localStorage.getItem("doctor"));
  const navigate = useNavigate();

  if (!doctor) return null;

  const hospital = doctor.hospital;

  return (
    <div className="min-h-screen bg-[#090c12] text-white">

      {/* ══ HEADER ══ */}
      <header className="border-b border-white/[0.06] bg-[#090c12]/80 backdrop-blur-xl sticky top-0 z-30">
        <div className="max-w-4xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center text-white font-bold text-sm">
              D
            </div>
            <span className="text-sm font-semibold text-slate-200 tracking-tight">
              Doctor Portal
            </span>
          </div>
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-slate-500 text-sm hover:text-sky-400 transition-colors group"
          >
            <svg
              className="w-4 h-4 transition-transform group-hover:-translate-x-1"
              fill="none"
              stroke="currentColor"
              strokeWidth={2}
              viewBox="0 0 24 24"
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back
          </button>
        </div>
      </header>

      {/* Ambient glow */}
      <div className="fixed -top-32 left-1/2 -translate-x-1/2 w-[700px] h-64 bg-blue-500/[0.06] rounded-full blur-3xl pointer-events-none z-0" />

      <div className="relative z-10 max-w-4xl mx-auto px-6 py-10 pb-20">

        {/* ══ HERO CARD ══ */}
        <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 md:p-8 mb-6 flex flex-col md:flex-row items-start md:items-center gap-6">

          {/* Avatar */}
          <div className="relative flex-shrink-0">
            <div className="w-24 h-24 rounded-2xl bg-gradient-to-br from-blue-500/30 to-indigo-700/30 border border-blue-500/25 flex items-center justify-center font-bold text-4xl text-blue-400">
              {doctor.name?.charAt(0)}
            </div>
            <span className="absolute -bottom-1 -right-1 w-4 h-4 bg-emerald-400 rounded-full border-2 border-[#090c12] animate-pulse" />
          </div>

          {/* Info */}
          <div className="flex-1 space-y-2">
            <span className="inline-block px-3 py-1 text-[10px] font-medium tracking-[3px] uppercase text-sky-400 border border-sky-500/25 rounded-full bg-sky-500/5">
              {doctor.specialization}
            </span>
            <h1 className="text-3xl font-bold text-slate-50">{doctor.name}</h1>
            <div className="flex flex-wrap items-center gap-3">
              <span className="flex items-center gap-1.5 text-slate-400 text-xs">
                <span className="text-slate-600">✉️</span> {doctor.email}
              </span>
              {hospital && (
                <span className="flex items-center gap-1.5 text-slate-400 text-xs">
                  <span className="text-slate-600">🏥</span> {hospital.name}, {hospital.city}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* ══ STAT CARDS ══ */}
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5 hover:border-sky-500/20 transition-colors">
            <div className="w-10 h-10 rounded-xl bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-lg mb-3">🏅</div>
            <p className="text-2xl font-bold text-slate-100">{doctor.experience} yrs</p>
            <p className="text-slate-500 text-xs mt-1">Experience</p>
          </div>
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5 hover:border-emerald-500/20 transition-colors">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-lg mb-3">💰</div>
            <p className="text-2xl font-bold text-slate-100">₹{doctor.fee}</p>
            <p className="text-slate-500 text-xs mt-1">Consultation Fee</p>
          </div>
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5 hover:border-violet-500/20 transition-colors col-span-2 md:col-span-1">
            <div className="w-10 h-10 rounded-xl bg-violet-500/10 border border-violet-500/20 flex items-center justify-center text-lg mb-3">🆔</div>
            <p className="text-2xl font-bold text-slate-100">#{doctor.id}</p>
            <p className="text-slate-500 text-xs mt-1">Doctor ID</p>
          </div>
        </div>

        {/* ══ LOWER GRID ══ */}
        <div className="grid md:grid-cols-2 gap-5">

          {/* Personal Details */}
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6">
            <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wider mb-4">
              Personal Details
            </h2>
            <div className="space-y-0">
              {[
                { label: "Full Name", value: doctor.name },
                { label: "Email", value: doctor.email },
                { label: "Specialization", value: doctor.specialization },
                { label: "Experience", value: `${doctor.experience} years` },
                { label: "Consultation Fee", value: `₹${doctor.fee}` },
              ].map(({ label, value }) => (
                <div key={label} className="flex flex-col gap-0.5 py-3 border-b border-white/[0.05] last:border-0">
                  <span className="text-[10px] uppercase tracking-wider text-slate-600">{label}</span>
                  <span className="text-slate-300 text-sm">{value}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Hospital Details */}
          {hospital && (
            <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6">
              <h2 className="text-sm font-semibold text-slate-300 uppercase tracking-wider mb-4">
                Hospital
              </h2>

              {/* Hospital image */}
              {hospital.imageUrls?.[0] && (
                <img
                  src={hospital.imageUrls[0]}
                  alt={hospital.name}
                  className="w-full h-32 object-cover rounded-xl mb-4 opacity-80"
                />
              )}

              <div className="space-y-0">
                {[
                  { label: "Name", value: hospital.name },
                  { label: "Address", value: hospital.address },
                  { label: "City", value: hospital.city },
                  { label: "Email", value: hospital.email },
                  { label: "Phone", value: hospital.phone },
                  { label: "Rating", value: `⭐ ${hospital.rating} · ${hospital.totalReviews?.toLocaleString()} reviews` },
                  { label: "Open 24×7", value: hospital.isOpen24x7 ? "✅ Yes" : "❌ No" },
                ].map(({ label, value }) => (
                  <div key={label} className="flex flex-col gap-0.5 py-3 border-b border-white/[0.05] last:border-0">
                    <span className="text-[10px] uppercase tracking-wider text-slate-600">{label}</span>
                    <span className="text-slate-300 text-sm">{value}</span>
                  </div>
                ))}
              </div>

              {/* Services */}
              {hospital.services?.length > 0 && (
                <div className="mt-4">
                  <p className="text-[10px] uppercase tracking-wider text-slate-600 mb-2">Services</p>
                  <div className="flex flex-wrap gap-2">
                    {hospital.services.map((s) => (
                      <span
                        key={s}
                        className="text-[10px] px-2.5 py-1 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20"
                      >
                        {s}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* ══ DANGER ZONE ══ */}
        <div className="mt-6 bg-red-500/[0.03] border border-red-500/[0.1] rounded-2xl p-6 flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-red-400">Logout</p>
            <p className="text-xs text-slate-600 mt-0.5">Clear session and return to login</p>
          </div>
          <button
            onClick={() => {
              localStorage.clear();
              navigate("/login");
            }}
            className="text-xs font-medium text-red-400 border border-red-500/30 bg-red-500/10 hover:bg-red-500/20 px-4 py-2 rounded-xl transition-colors"
          >
            Logout →
          </button>
        </div>

      </div>
    </div>
  );
};

export default DoctorProfilePage;