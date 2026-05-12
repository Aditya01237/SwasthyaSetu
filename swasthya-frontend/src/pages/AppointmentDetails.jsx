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

  // 🔥 NEW STATES
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    fetchDetails();
  }, []);

  const fetchDetails = async () => {
    try {
      const res = await api.get(`/appointment/details/${id}`);
      setAppointment(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // 📅 FORMAT DATE
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

  // 🟢 STATUS
  const getStatus = () => {
    if (!appointment) return "Unknown";

    const now = new Date();
    const from = new Date(appointment.validFrom);
    const to = new Date(appointment.validTo);

    if (now < from) return "Upcoming";
    if (now > to) return "Expired";
    return "Active";
  };

  // ⏳ TIME LEFT
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

  // 📁 HANDLE FILE
  const handleFileUpload = (selectedFile) => {
    setFile(selectedFile);
  };

  // 🚀 UPLOAD API CALL
  const uploadPrescription = async () => {
    if (!file) {
      alert("Please select a file first");
      return;
    }

    try {
      setUploading(true);

      const formData = new FormData();
      formData.append("file", file);

      const res = await api.post(
        "/patient/upload-prescription",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      console.log(res.data);
      alert("Prescription uploaded successfully!");

      setFile(null);
    } catch (err) {
      console.error(err);

      if (err.response?.data?.message) {
        alert(err.response.data.message);
      } else {
        alert("Upload failed");
      }
    } finally {
      setUploading(false);
    }
  };

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
        {/* BACK BUTTON */}
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

            <div className="flex items-center gap-6">
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <div className="w-9 h-9 rounded-lg bg-sky-500/10 flex items-center justify-center">
                  📅
                </div>
                <div>
                  <p className="text-xs text-slate-500">Date</p>
                  <p className="font-medium">{appointmentDT.date}</p>
                </div>
              </div>

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

            <p className="text-slate-500 text-sm">
              Hospital: {appointment.hospitalName}
            </p>

            <div
              className={`inline-block px-3 py-1 text-xs rounded-full border
              ${status === "Active"
                  ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                  : status === "Upcoming"
                    ? "bg-blue-500/10 text-blue-400 border-blue-500/20"
                    : "bg-red-500/10 text-red-400 border-red-500/20"
                }`}
            >
              {status}
            </div>

            {status === "Active" && (
              <p className="text-xs text-slate-400">
                ⏳ {getRemainingTime()}
              </p>
            )}
          </div>

          {/* RIGHT */}
          <div className="flex flex-col items-center justify-center gap-3">
            {/* QR (only if not expired) */}
            {status !== "Expired" && (
              <>
                <div className="bg-white p-4 rounded-xl shadow-lg shadow-sky-500/10">
                  <QRCodeCanvas value={appointment.qrToken} size={180} />
                </div>

                <p className="text-xs text-slate-500 text-center">
                  Show this QR at hospital
                </p>
              </>
            )}

            {/* 🔥 UPLOAD SECTION */}
            {status === "Expired" && (
              <div className="flex flex-col items-center gap-3 mt-2">
                <p className="text-sm text-slate-400">
                  Upload your prescription
                </p>

                <label className="cursor-pointer bg-white/10 px-4 py-2 rounded-lg text-sm hover:bg-white/20">
                  Choose Image
                  <input
                    type="file"
                    accept="image/*"
                    hidden
                    onChange={(e) =>
                      handleFileUpload(e.target.files[0])
                    }
                  />
                </label>

                {file && (
                  <p className="text-xs text-green-400">
                    Selected: {file.name}
                  </p>
                )}

                <button
                  onClick={uploadPrescription}
                  disabled={uploading}
                  className="bg-sky-500 hover:bg-sky-600 disabled:opacity-50 text-white px-4 py-2 rounded-lg text-sm transition"
                >
                  {uploading ? "Uploading..." : "Upload Prescription"}
                </button>
              </div>
            )}
          </div>
        </div>

        {/* VALIDITY CARD */}
        <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5">
          <h2 className="text-sm text-slate-400 mb-4">QR Validity</h2>

          <div className="flex gap-8">
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