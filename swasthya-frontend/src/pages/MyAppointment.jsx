import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";

const MyAppointment = () => {
  const [appointments, setAppointments] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      const res = await api.get(`/appointment/my`, {
        params: { uhid: localStorage.getItem("uhid") },
      });
      setAppointments(res.data);
    } catch (err) {
      console.error("Error:", err.response?.data);
    }
  };

  const isUpcoming = (time) => new Date(time) > new Date();

  const formatDateTime = (time) => {
    const date = new Date(time);
    return {
      date: date.toLocaleDateString("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric",
      }),
      time: date.toLocaleTimeString("en-IN", {
        hour: "numeric",
        minute: "2-digit",
        hour12: true,
      }),
    };
  };

  const upcomingList = appointments.filter((a) => isUpcoming(a.time));
  const pastList = appointments.filter((a) => !isUpcoming(a.time));

  const AppointmentCard = ({ a, past = false }) => {
    const { date, time } = formatDateTime(a.time);
    return (
      <div
        onClick={() => navigate(`/appointment/${a.id}`)}
        className={`bg-white/[0.03] border rounded-2xl p-5 
          hover:bg-white/[0.05] transition-all duration-200 cursor-pointer
          ${past
            ? "border-white/[0.06] opacity-70 hover:opacity-100 hover:border-slate-500/30"
            : "border-white/[0.08] hover:border-sky-500/30"
          }`}
      >
        <div className="flex justify-between items-center">
          <div className="space-y-3">
            <h2 className="text-lg font-semibold text-white">{a.doctorName}</h2>
            <div className="flex items-center gap-6">
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <div className="w-8 h-8 rounded-lg bg-sky-500/10 flex items-center justify-center">
                  📅
                </div>
                <span className="font-medium">{date}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <div className="w-8 h-8 rounded-lg bg-violet-500/10 flex items-center justify-center">
                  ⏰
                </div>
                <span className="font-medium">{time}</span>
              </div>
            </div>
          </div>

          <div
            className={`px-3 py-1 text-xs rounded-full font-medium 
              ${past
                ? "bg-slate-500/10 text-slate-400 border border-slate-500/20"
                : "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20"
              }`}
          >
            {past ? "Completed" : "Upcoming"}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
      <Navbar />

      <div className="max-w-4xl mt-10 mx-auto px-4 pb-16">
        {/* Back */}
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-slate-500 text-sm hover:text-sky-400 transition-colors mb-8 group"
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

        <h1 className="text-2xl font-bold text-white mb-8">My Appointments</h1>

        {appointments.length === 0 && (
          <div className="text-center text-slate-500 mt-20">
            No appointments found 😕
          </div>
        )}

        {/* Upcoming */}
        {upcomingList.length > 0 && (
          <div className="mb-8">
            <div className="flex items-center gap-2 mb-4">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <h2 className="text-sm font-semibold text-emerald-400 uppercase tracking-wider">
                Upcoming
              </h2>
              <span className="ml-1 text-xs text-slate-600 bg-white/[0.04] px-2 py-0.5 rounded-full border border-white/[0.06]">
                {upcomingList.length}
              </span>
            </div>
            <div className="space-y-4">
              {upcomingList.map((a) => (
                <AppointmentCard key={a.id} a={a} past={false} />
              ))}
            </div>
          </div>
        )}

        {/* Divider */}
        {upcomingList.length > 0 && pastList.length > 0 && (
          <div className="border-t border-white/[0.06] my-8" />
        )}

        {/* Past */}
        {pastList.length > 0 && (
          <div>
            <div className="flex items-center gap-2 mb-4">
              <span className="w-2 h-2 rounded-full bg-slate-500" />
              <h2 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">
                Past
              </h2>
              <span className="ml-1 text-xs text-slate-600 bg-white/[0.04] px-2 py-0.5 rounded-full border border-white/[0.06]">
                {pastList.length}
              </span>
            </div>
            <div className="space-y-4">
              {pastList.map((a) => (
                <AppointmentCard key={a.id} a={a} past={true} />
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyAppointment;