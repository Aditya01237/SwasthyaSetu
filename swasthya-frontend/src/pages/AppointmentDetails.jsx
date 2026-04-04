import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { QRCodeCanvas } from "qrcode.react";
import api from "../api/axios";
import Navbar from "../components/Navbar";

const AppointmentDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDetails();
  }, []);

  const fetchDetails = async () => {
    try {
      const res = await api.get(`/appointment/details/${id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      setAppointment(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // 🔥 PREMIUM DATE FORMAT
  const formatDateTime = (time) => {
    if (!time) return { date: "—", time: "—" };

    const d = new Date(time);

    return {
      date: d.toLocaleDateString("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric",
      }),
      time: d.toLocaleTimeString("en-IN", {
        hour: "numeric",
        minute: "2-digit",
        hour12: true,
      }),
    };
  };

  const getStatus = () => {
    if (!appointment) return "Unknown";

    const now = new Date();
    const from = new Date(appointment.validFrom);
    const to = new Date(appointment.validTo);

    if (now < from) return "Upcoming";
    if (now > to) return "Expired";
    return "Active";
  };

  const getRemainingTime = () => {
    if (!appointment) return "";

    const now = new Date();
    const to = new Date(appointment.validTo);
    const diff = to - now;

    if (diff <= 0) return "Expired";

    const mins = Math.floor(diff / 60000);
    return `${mins} mins left`;
  };

  const status = getStatus();

  if (loading) {
    return <div className="text-white p-10">Loading...</div>;
  }

  if (!appointment) {
    return <div className="text-red-400 p-10">Appointment not found</div>;
  }

  const appointmentDT = formatDateTime(appointment.time);
  const fromDT = formatDateTime(appointment.validFrom);
  const toDT = formatDateTime(appointment.validTo);

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200">
      <Navbar />

      <div className="max-w-4xl mx-auto px-6 py-10 space-y-6">
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

        {/* MAIN CARD */}
        <div className="bg-white/[0.03] border border-white/[0.08] rounded-2xl p-6 flex flex-col md:flex-row gap-6">
          {/* LEFT */}
          <div className="flex-1 space-y-4">
            <h1 className="text-2xl font-bold text-white">
              {appointment.doctorName}
            </h1>

            {/* 🔥 PREMIUM DATE + TIME */}
            <div className="flex items-center gap-6">
              {/* DATE */}
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <div className="w-9 h-9 rounded-lg bg-sky-500/10 flex items-center justify-center">
                  📅
                </div>
                <div>
                  <p className="text-xs text-slate-500">Date</p>
                  <p className="font-medium">{appointmentDT.date}</p>
                </div>
              </div>

              {/* TIME */}
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <div className="w-9 h-9 rounded-lg bg-violet-500/10 flex items-center justify-center">
                  ⏰
                </div>
                <div>
                  <p className="text-xs text-slate-500">Time</p>
                  <p className="font-medium">{appointmentDT.time}</p>
                </div>
              </div>
            </div>

            {/* Hospital */}
            <p className="text-slate-500 text-sm">
              Hospital: {appointment.hospitalName}
            </p>

            {/* STATUS */}
            <div
              className={`inline-block px-3 py-1 text-xs rounded-full border
              ${
                status === "Active"
                  ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                  : status === "Upcoming"
                    ? "bg-blue-500/10 text-blue-400 border-blue-500/20"
                    : "bg-red-500/10 text-red-400 border-red-500/20"
              }`}
            >
              {status}
            </div>

            {/* COUNTDOWN */}
            {status === "Active" && (
              <p className="text-xs text-slate-400">⏳ {getRemainingTime()}</p>
            )}
          </div>

          {/* RIGHT: QR */}
          <div className="flex flex-col items-center justify-center gap-3">
            <div className="bg-white p-4 rounded-xl shadow-lg shadow-sky-500/10">
              <QRCodeCanvas value={appointment.qrToken} size={180} />
            </div>

            <p className="text-xs text-slate-500 text-center">
              Show this QR at hospital
            </p>
          </div>
        </div>

        {/* VALIDITY CARD */}
        <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5">
          <h2 className="text-sm text-slate-400 mb-4">QR Validity</h2>

          <div className="flex gap-8">
            {/* FROM */}
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-emerald-500/10 rounded-lg flex items-center justify-center">
                🟢
              </div>
              <div>
                <p className="text-xs text-slate-500">From</p>
                <p className="text-sm text-slate-300">
                  {fromDT.date}, {fromDT.time}
                </p>
              </div>
            </div>

            {/* TO */}
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-red-500/10 rounded-lg flex items-center justify-center">
                🔴
              </div>
              <div>
                <p className="text-xs text-slate-500">To</p>
                <p className="text-sm text-slate-300">
                  {toDT.date}, {toDT.time}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AppointmentDetails;
