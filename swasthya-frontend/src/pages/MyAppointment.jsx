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
      const res = await api.get(
        `/appointment/my?uhid=${localStorage.getItem("uhid")}`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        },
      );

      setAppointments(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const isUpcoming = (time) => {
    return new Date(time) > new Date();
  };

  // 🔥 NEW: Clean Date-Time Formatter
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

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
      <Navbar />

      <div className="max-w-4xl mt-10 mx-auto px-4">
        {/* ── Back button ── */}
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
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
          Back
        </button>

        {/* Header */}
        <h1 className="text-2xl font-bold text-white mb-6">My Appointments</h1>

        {/* Empty State */}
        {appointments.length === 0 && (
          <div className="text-center text-slate-500 mt-20">
            No appointments found 😕
          </div>
        )}

        {/* Appointment List */}
        <div className="space-y-4">
          {appointments.map((a) => {
            const upcoming = isUpcoming(a.time);
            const { date, time } = formatDateTime(a.time);

            return (
              <div
                key={a.id}
                onClick={() => navigate(`/appointment/${a.id}`)}
                className="bg-white/[0.03] border border-white/[0.08] rounded-2xl p-5 
                           hover:border-sky-500/30 hover:bg-white/[0.05] 
                           transition-all duration-200 cursor-pointer"
              >
                <div className="flex justify-between items-center">
                  {/* LEFT */}
                  <div className="space-y-3">
                    {/* Doctor */}
                    <h2 className="text-lg font-semibold text-white">
                      {a.doctorName}
                    </h2>

                    {/* 🔥 Premium Date-Time */}
                    <div className="flex items-center gap-6">
                      {/* Date */}
                      <div className="flex items-center gap-2 text-sm text-slate-300">
                        <div className="w-8 h-8 rounded-lg bg-sky-500/10 flex items-center justify-center">
                          📅
                        </div>
                        <span className="font-medium">{date}</span>
                      </div>

                      {/* Time */}
                      <div className="flex items-center gap-2 text-sm text-slate-300">
                        <div className="w-8 h-8 rounded-lg bg-violet-500/10 flex items-center justify-center">
                          ⏰
                        </div>
                        <span className="font-medium">{time}</span>
                      </div>
                    </div>
                  </div>

                  {/* STATUS */}
                  <div
                    className={`px-3 py-1 text-xs rounded-full font-medium 
                    ${
                      upcoming
                        ? "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20"
                        : "bg-red-500/10 text-red-400 border border-red-500/20"
                    }`}
                  >
                    {upcoming ? "Upcoming" : "Completed"}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default MyAppointment;
