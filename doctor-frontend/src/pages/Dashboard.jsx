import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import api from "../api/axios";

const Dashboard = () => {
  const doctor = JSON.parse(localStorage.getItem("doctor"));
  const navigate = useNavigate();

  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAppointments = async () => {
    try {
      if (!doctor?.id) {
        console.error("Doctor ID missing");
        return;
      }
      const res = await api.get(`/appointment/doctor/today?doctorId=${doctor.id}`);
      setAppointments(res.data);
    } catch (err) {
      console.error("ERROR:", err.response?.data || err.message);
      alert("Failed to load appointments");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  if (!doctor) return null;

  const getStatus = (appt) => {
    const now = new Date();
    const apptTime = new Date(appt.time);
    if (appt.isValid) return "COMPLETED";
    if (apptTime > now) return "UPCOMING";
    if (apptTime <= now) return "WAITING";
    return "EXPIRED";
  };

  const sortedAppointments = [...appointments].sort((a, b) => {
    const order = { UPCOMING: 1, WAITING: 2, COMPLETED: 3, EXPIRED: 4 };
    const statusA = getStatus(a);
    const statusB = getStatus(b);
    if (order[statusA] !== order[statusB]) return order[statusA] - order[statusB];
    return new Date(a.time) - new Date(b.time);
  });

  const total = appointments.length;
  const completed = appointments.filter((a) => a.isValid).length;
  const waiting = appointments.filter((a) => {
    const now = new Date();
    const time = new Date(a.time);
    return !a.isValid && time <= now;
  }).length;
  const upcoming = appointments.filter((a) => {
    const now = new Date();
    const time = new Date(a.time);
    return !a.isValid && time > now;
  }).length;

  const today = new Date().toLocaleDateString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });

  const statusStyle = {
    COMPLETED: "bg-green-500/10 text-green-400 border border-green-500/20",
    UPCOMING: "bg-blue-500/10 text-blue-400 border border-blue-500/20",
    WAITING: "bg-yellow-500/10 text-yellow-400 border border-yellow-500/20",
    EXPIRED: "bg-red-500/10 text-red-400 border border-red-500/20",
  };

  return (
    <div className="min-h-screen bg-[#090c12] text-white">

      {/* ══════════════════════════════════
          HEADER / NAVBAR
      ══════════════════════════════════ */}
      <header className="border-b border-white/[0.06] bg-[#090c12]/80 backdrop-blur-xl sticky top-0 z-30">
        <div className="max-w-5xl mx-auto px-6 h-16 flex items-center justify-between">

          {/* Logo + Brand */}
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center text-white font-bold text-sm">
              D
            </div>
            <span className="text-sm font-semibold text-slate-200 tracking-tight">
              Doctor Portal
            </span>
          </div>

          {/* Center: today's date */}
          <p className="hidden sm:block text-xs text-slate-500">{today}</p>

          {/* Right: doctor info + logout */}
          <div className="flex items-center gap-3">
            <div className="hidden sm:flex flex-col items-end">
              <span className="text-sm font-medium text-slate-200">{doctor.name}</span>
              <span className="text-[10px] text-sky-400">{doctor.specialization}</span>
            </div>
            <button
              onClick={() => navigate("/profile")}
              className="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-500/30 to-blue-700/30 border border-sky-500/25 flex items-center justify-center font-bold text-sky-400 text-sm hover:border-sky-400/50 transition-colors"
              title="View Profile"
            >
              {doctor.name?.charAt(0)}
            </button>
            <button
              onClick={() => {
                localStorage.clear();
                navigate("/");
              }}
              className="text-xs text-slate-500 hover:text-red-400 transition-colors px-2 py-1 rounded-lg hover:bg-red-500/10"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* ══════════════════════════════════
          AMBIENT GLOW
      ══════════════════════════════════ */}
      <div className="fixed -top-32 left-1/2 -translate-x-1/2 w-[700px] h-64 bg-blue-500/[0.06] rounded-full blur-3xl pointer-events-none z-0" />

      {/* ══════════════════════════════════
          MAIN CONTENT
      ══════════════════════════════════ */}
      <div className="relative z-10 max-w-5xl mx-auto px-6 py-10">

        {/* ── Welcome Banner ── */}
        <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl px-6 py-5 mb-8 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <p className="text-xs uppercase tracking-widest text-slate-500 mb-1 font-medium">
              Good {new Date().getHours() < 12 ? "Morning" : new Date().getHours() < 17 ? "Afternoon" : "Evening"}
            </p>
            <h1 className="text-2xl font-bold tracking-tight">
              Dr. {doctor.name} <span className="text-2xl">👨‍⚕️</span>
            </h1>
            <div className="flex flex-wrap items-center gap-2 mt-2">
              <span className="text-xs bg-sky-500/10 text-sky-400 px-3 py-1 rounded-full border border-sky-500/20">
                {doctor.specialization}
              </span>
              <span className="text-xs bg-white/[0.04] text-slate-400 px-3 py-1 rounded-full border border-white/[0.07]">
                {doctor.experience} yrs experience
              </span>
              {doctor.hospital && (
                <span className="text-xs bg-white/[0.04] text-slate-400 px-3 py-1 rounded-full border border-white/[0.07]">
                  🏥 {doctor.hospital?.name}, {doctor.hospital?.city}
                </span>
              )}
            </div>
          </div>
          <button
            onClick={fetchAppointments}
            className="flex-shrink-0 flex items-center gap-2 text-xs text-slate-400 hover:text-sky-400 bg-white/[0.03] border border-white/[0.07] hover:border-sky-500/30 px-4 py-2 rounded-xl transition-all"
          >
            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refresh
          </button>
        </div>

        {/* ── Stats ── */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          <div className="bg-white/[0.02] border border-white/[0.07] p-5 rounded-2xl hover:border-slate-500/30 transition-colors">
            <p className="text-[10px] uppercase tracking-wider text-slate-500 mb-2">Total Today</p>
            <h2 className="text-3xl font-bold text-slate-100">{total}</h2>
            <p className="text-[10px] text-slate-600 mt-1">appointments</p>
          </div>
          <div className="bg-blue-500/[0.04] border border-blue-500/[0.12] p-5 rounded-2xl hover:border-blue-500/25 transition-colors">
            <p className="text-[10px] uppercase tracking-wider text-blue-400 mb-2">Upcoming</p>
            <h2 className="text-3xl font-bold text-blue-400">{upcoming}</h2>
            <p className="text-[10px] text-blue-900 mt-1">scheduled</p>
          </div>
          <div className="bg-yellow-500/[0.04] border border-yellow-500/[0.12] p-5 rounded-2xl hover:border-yellow-500/25 transition-colors">
            <p className="text-[10px] uppercase tracking-wider text-yellow-400 mb-2">Waiting</p>
            <h2 className="text-3xl font-bold text-yellow-400">{waiting}</h2>
            <p className="text-[10px] text-yellow-900 mt-1">to be seen</p>
          </div>
          <div className="bg-green-500/[0.04] border border-green-500/[0.12] p-5 rounded-2xl hover:border-green-500/25 transition-colors">
            <p className="text-[10px] uppercase tracking-wider text-green-400 mb-2">Completed</p>
            <h2 className="text-3xl font-bold text-green-400">{completed}</h2>
            <p className="text-[10px] text-green-900 mt-1">done today</p>
          </div>
        </div>

        {/* ── Appointment List ── */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold text-slate-400 uppercase tracking-wider">
              Today's Appointments
            </h2>
            {!loading && (
              <span className="text-xs text-slate-600 bg-white/[0.03] border border-white/[0.06] px-2.5 py-1 rounded-full">
                {sortedAppointments.length} total
              </span>
            )}
          </div>

          {loading ? (
            <div className="space-y-3">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="h-16 rounded-xl bg-slate-800/40 animate-pulse" />
              ))}
            </div>
          ) : sortedAppointments.length === 0 ? (
            <div className="text-center py-16 text-slate-500">
              <p className="text-4xl mb-3">📭</p>
              <p className="text-sm">No appointments today</p>
            </div>
          ) : (
            <div className="space-y-3">
              {sortedAppointments.map((appt, idx) => {
                const status = getStatus(appt);
                return (
                  <div
                    key={appt.id}
                    className="bg-white/[0.02] border border-white/[0.07] px-5 py-4 rounded-xl 
                               flex justify-between items-center 
                               hover:border-sky-500/20 hover:bg-white/[0.04] transition-all duration-200"
                  >
                    {/* Left */}
                    <div className="flex items-center gap-4">
                      <div className="w-8 h-8 rounded-lg bg-white/[0.04] border border-white/[0.07] flex items-center justify-center text-xs font-semibold text-slate-400">
                        {idx + 1}
                      </div>
                      <div>
                        <p className="font-medium text-slate-100 text-sm">{appt.patientName}</p>
                        <p className="text-xs text-slate-500 mt-0.5">
                          {new Date(appt.time).toLocaleTimeString([], {
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </p>
                      </div>
                    </div>

                    {/* Right */}
                    <div className="flex items-center gap-3">
                      <span className={`text-[10px] px-2.5 py-1 rounded-full font-medium ${statusStyle[status]}`}>
                        {status}
                      </span>
                      <button
                        onClick={() => navigate(`/appointment/${appt.id}`)}
                        className="bg-sky-500/10 text-sky-400 border border-sky-500/20 px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-sky-500/20 transition-colors"
                      >
                        Open →
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;