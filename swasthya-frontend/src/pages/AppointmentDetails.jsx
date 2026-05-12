import React, { useEffect, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { QRCodeCanvas } from "qrcode.react";
import api from "../api/axios";
import Navbar from "../components/Navbar";

/* ── helpers ─────────────────────────────────────────────────────────────── */
const fmt = (time) => {
  if (!time) return { date: "—", time: "—" };
  const d = new Date(time);
  return {
    date: d.toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" }),
    time: d.toLocaleTimeString("en-IN", { hour: "numeric", minute: "2-digit", hour12: true }),
  };
};

const getStatus = (appt) => {
  if (!appt) return "Unknown";
  const now = new Date();
  if (now < new Date(appt.validFrom)) return "Upcoming";
  if (now > new Date(appt.validTo))   return "Expired";
  return "Active";
};

const getRemainingTime = (appt) => {
  if (!appt) return "";
  const diff = new Date(appt.validTo) - new Date();
  if (diff <= 0) return "Expired";
  return `${Math.floor(diff / 60000)} mins left`;
};

/* ── status badge style ──────────────────────────────────────────────────── */
const statusStyle = {
  Active:   "bg-emerald-500/10 text-emerald-400 border-emerald-500/20",
  Upcoming: "bg-blue-500/10 text-blue-400 border-blue-500/20",
  Expired:  "bg-red-500/10 text-red-400 border-red-500/20",
};

/* ════════════════════════════════════════════════════════════════════════════
   MAIN COMPONENT
════════════════════════════════════════════════════════════════════════════ */
const AppointmentDetails = () => {
  const { id }    = useParams();
  const navigate  = useNavigate();

  const [appointment, setAppointment]       = useState(null);
  const [loading, setLoading]               = useState(true);

  // prescription upload
  const [file, setFile]                     = useState(null);
  const [preview, setPreview]               = useState(null);
  const [uploading, setUploading]           = useState(false);
  const [result, setResult]                 = useState(null);   // AI response
  const [uploadError, setUploadError]       = useState(null);

  const fileInputRef = useRef(null);

  /* fetch ------------------------------------------------------------------ */
  useEffect(() => { fetchDetails(); }, []);

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

  /* file pick -------------------------------------------------------------- */
  const handleFilePick = (e) => {
    const f = e.target.files[0];
    if (!f) return;
    setFile(f);
    setResult(null);
    setUploadError(null);
    const reader = new FileReader();
    reader.onload = (ev) => setPreview(ev.target.result);
    reader.readAsDataURL(f);
  };

  /* upload ----------------------------------------------------------------- */
  const uploadPrescription = async () => {
    if (!file) return;
    try {
      setUploading(true);
      setUploadError(null);
      const form = new FormData();
      form.append("file", file);
      const res = await api.post("/patient/upload-prescription", form, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setResult(res.data);
      setFile(null);
      setPreview(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
    } catch (err) {
      console.error(err);
      setUploadError(err.response?.data?.message || "Upload failed. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  /* render guards ---------------------------------------------------------- */
  if (loading) {
    return (
      <div className="min-h-screen bg-[#090c12] flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-sky-500/30 border-t-sky-500 rounded-full animate-spin" />
          <p className="text-slate-500 text-sm">Loading appointment…</p>
        </div>
      </div>
    );
  }

  if (!appointment) {
    return (
      <div className="min-h-screen bg-[#090c12] flex items-center justify-center">
        <p className="text-red-400 text-sm">Appointment not found</p>
      </div>
    );
  }

  const status      = getStatus(appointment);
  const apptDT      = fmt(appointment.time);
  const fromDT      = fmt(appointment.validFrom);
  const toDT        = fmt(appointment.validTo);
  const qrScanned   = appointment.isValid === true;   // doctor scanned → show upload

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200">
      <Navbar />

      {/* Ambient glow */}
      <div className="fixed -top-32 left-1/2 -translate-x-1/2 w-[700px] h-64 bg-sky-500/[0.06] rounded-full blur-3xl pointer-events-none z-0" />

      <div className="relative z-10 max-w-4xl mx-auto px-6 py-10 space-y-6">

        {/* Back */}
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-slate-500 text-sm hover:text-sky-400 transition-colors group"
        >
          <svg className="w-4 h-4 transition-transform group-hover:-translate-x-1" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back
        </button>

        {/* ── MAIN CARD ──────────────────────────────────────────────────── */}
        <div className="bg-white/[0.03] border border-white/[0.08] rounded-2xl p-6 flex flex-col md:flex-row gap-6">

          {/* LEFT — appointment info */}
          <div className="flex-1 space-y-4">
            <h1 className="text-2xl font-bold text-white">{appointment.doctorName}</h1>

            <div className="flex flex-wrap items-center gap-4">
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <div className="w-9 h-9 rounded-lg bg-sky-500/10 flex items-center justify-center">📅</div>
                <div>
                  <p className="text-xs text-slate-500">Date</p>
                  <p className="font-medium">{apptDT.date}</p>
                </div>
              </div>
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <div className="w-9 h-9 rounded-lg bg-violet-500/10 flex items-center justify-center">⏰</div>
                <div>
                  <p className="text-xs text-slate-500">Time</p>
                  <p className="font-medium">{apptDT.time}</p>
                </div>
              </div>
            </div>

            <p className="text-slate-500 text-sm">Hospital: {appointment.hospitalName}</p>

            <div className={`inline-flex items-center gap-1.5 px-3 py-1 text-xs rounded-full border ${statusStyle[status]}`}>
              <span className={`w-1.5 h-1.5 rounded-full ${status === "Active" ? "bg-emerald-400 animate-pulse" : status === "Upcoming" ? "bg-blue-400" : "bg-red-400"}`} />
              {status}
            </div>

            {status === "Active" && (
              <p className="text-xs text-slate-400">⏳ {getRemainingTime(appointment)}</p>
            )}

            {/* QR-scanned badge */}
            {qrScanned && (
              <div className="flex items-center gap-2 px-3 py-2 rounded-xl bg-emerald-500/10 border border-emerald-500/20 w-fit">
                <svg className="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span className="text-xs text-emerald-400 font-medium">Doctor has verified your QR</span>
              </div>
            )}
          </div>

          {/* RIGHT — QR code */}
          <div className="flex flex-col items-center justify-center gap-3">
            {status !== "Expired" && !qrScanned && (
              <>
                <div className="bg-white p-4 rounded-xl shadow-lg shadow-sky-500/10">
                  <QRCodeCanvas value={appointment.qrToken} size={160} />
                </div>
                <p className="text-xs text-slate-500 text-center">Show this QR at hospital</p>
              </>
            )}

            {qrScanned && (
              <div className="flex flex-col items-center gap-2 text-center">
                <div className="w-20 h-20 rounded-full bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-4xl">✅</div>
                <p className="text-xs text-emerald-400 font-medium">QR Verified</p>
                <p className="text-xs text-slate-600">Upload prescription below</p>
              </div>
            )}

            {status === "Expired" && !qrScanned && (
              <div className="flex flex-col items-center gap-2 text-center">
                <div className="w-20 h-20 rounded-full bg-red-500/10 border border-red-500/20 flex items-center justify-center text-4xl">⌛</div>
                <p className="text-xs text-red-400">QR Expired</p>
              </div>
            )}
          </div>
        </div>

        {/* ── PRESCRIPTION UPLOAD (only after QR scanned) ────────────────── */}
        {qrScanned && (
          <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 space-y-5">

            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-xl">💊</div>
              <div>
                <h2 className="text-base font-semibold text-slate-100">Upload Prescription</h2>
                <p className="text-xs text-slate-500 mt-0.5">Take a photo of your prescription — our AI will extract medicines and save your record</p>
              </div>
            </div>

            {/* Drop zone / file picker */}
            {!result && (
              <label
                className={`relative flex flex-col items-center justify-center border-2 border-dashed rounded-2xl cursor-pointer transition-all duration-200 min-h-[200px]
                  ${file ? "border-sky-500/50 bg-sky-500/[0.04]" : "border-white/[0.10] bg-white/[0.01] hover:border-sky-500/30 hover:bg-sky-500/[0.02]"}`}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  hidden
                  onChange={handleFilePick}
                />

                {preview ? (
                  <img src={preview} alt="Prescription preview" className="max-h-64 rounded-xl object-contain" />
                ) : (
                  <div className="flex flex-col items-center gap-3 p-8">
                    <div className="w-14 h-14 rounded-2xl bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-3xl">📄</div>
                    <p className="text-sm font-medium text-slate-300">Click or tap to upload prescription</p>
                    <p className="text-xs text-slate-600">JPG, PNG, HEIC supported</p>
                  </div>
                )}

                {file && (
                  <div className="absolute bottom-3 left-3 right-3 flex items-center gap-2 bg-slate-900/80 backdrop-blur rounded-lg px-3 py-2">
                    <svg className="w-4 h-4 text-sky-400 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <span className="text-xs text-slate-300 truncate flex-1">{file.name}</span>
                    <span className="text-xs text-slate-500 flex-shrink-0">{(file.size / 1024).toFixed(0)} KB</span>
                  </div>
                )}
              </label>
            )}

            {/* Error */}
            {uploadError && (
              <div className="flex items-start gap-2 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/20">
                <svg className="w-4 h-4 text-red-400 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p className="text-xs text-red-400">{uploadError}</p>
              </div>
            )}

            {/* Actions */}
            {!result && (
              <div className="flex gap-3">
                {file && (
                  <button
                    onClick={() => { setFile(null); setPreview(null); setUploadError(null); if (fileInputRef.current) fileInputRef.current.value = ""; }}
                    className="flex-1 py-2.5 rounded-xl border border-white/[0.08] text-slate-400 text-sm hover:bg-white/[0.04] transition-all"
                  >
                    Remove
                  </button>
                )}
                <button
                  onClick={uploadPrescription}
                  disabled={!file || uploading}
                  className="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-blue-600 text-white text-sm font-medium
                             hover:opacity-90 hover:-translate-y-px active:translate-y-0 transition-all
                             disabled:opacity-40 disabled:cursor-not-allowed disabled:translate-y-0
                             flex items-center justify-center gap-2"
                >
                  {uploading ? (
                    <>
                      <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z" />
                      </svg>
                      Analysing Prescription…
                    </>
                  ) : (
                    <>
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                      </svg>
                      Analyse &amp; Save
                    </>
                  )}
                </button>
              </div>
            )}

            {/* ── AI RESULT ─────────────────────────────────────────────── */}
            {result && (
              <div className="space-y-4 animate-fadeUp">
                <div className="flex items-center gap-2">
                  <div className="w-5 h-5 rounded-full bg-emerald-500/20 flex items-center justify-center">
                    <svg className="w-3 h-3 text-emerald-400" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <p className="text-sm font-medium text-emerald-400">Prescription saved to your medical records</p>
                </div>

                {/* Diagnosis */}
                <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-violet-500/10 border border-violet-500/20">
                  <span className="text-xl">🩺</span>
                  <div>
                    <p className="text-[10px] uppercase tracking-wider text-slate-500">Diagnosis</p>
                    <p className="text-sm font-semibold text-violet-300">{result.diagnosis || result.disease || "—"}</p>
                  </div>
                </div>

                {/* Medicines */}
                <div>
                  <p className="text-xs uppercase tracking-wider text-slate-500 mb-3 font-medium">Medicines Prescribed</p>
                  <div className="space-y-2">
                    {(result.medicines || []).map((med, i) => (
                      <div key={i} className="flex items-start gap-4 px-4 py-3 rounded-xl bg-white/[0.03] border border-white/[0.07]">
                        <div className="w-8 h-8 rounded-lg bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400 text-xs font-bold flex-shrink-0">
                          {i + 1}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold text-slate-100">{med.name}</p>
                          <div className="flex flex-wrap gap-2 mt-1">
                            <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
                              {med.dosage}
                            </span>
                            <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-400">
                              {med.frequency}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Raw text */}
                {result.rawText && (
                  <details className="group">
                    <summary className="text-xs text-slate-600 cursor-pointer hover:text-slate-400 transition-colors select-none">
                      View raw OCR text ▾
                    </summary>
                    <pre className="mt-2 text-xs text-slate-500 bg-white/[0.02] border border-white/[0.06] rounded-xl p-4 whitespace-pre-wrap font-mono overflow-x-auto">
                      {result.rawText}
                    </pre>
                  </details>
                )}

                {/* Upload another */}
                <button
                  onClick={() => { setResult(null); setFile(null); setPreview(null); setUploadError(null); }}
                  className="w-full py-2.5 rounded-xl border border-white/[0.08] text-slate-400 text-sm hover:bg-white/[0.04] transition-all"
                >
                  + Upload another prescription
                </button>
              </div>
            )}
          </div>
        )}

        {/* ── QR VALIDITY CARD ───────────────────────────────────────────── */}
        <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5">
          <h2 className="text-sm text-slate-400 mb-4 font-medium">QR Validity Window</h2>
          <div className="flex flex-wrap gap-8">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-emerald-500/10 border border-emerald-500/20 rounded-lg flex items-center justify-center text-sm">🟢</div>
              <div>
                <p className="text-xs text-slate-500">From</p>
                <p className="text-sm text-slate-300">{fromDT.date}, {fromDT.time}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-red-500/10 border border-red-500/20 rounded-lg flex items-center justify-center text-sm">🔴</div>
              <div>
                <p className="text-xs text-slate-500">To</p>
                <p className="text-sm text-slate-300">{toDT.date}, {toDT.time}</p>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default AppointmentDetails;