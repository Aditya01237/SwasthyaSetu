import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState, useRef } from "react";
import { Html5Qrcode } from "html5-qrcode";
import api from "../api/axios";

const AppointmentDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const doctor = JSON.parse(localStorage.getItem("doctor"));

  const [appointment, setAppointment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isScanning, setIsScanning] = useState(false);
  const [verified, setVerified] = useState(false);
  const [scanData, setScanData] = useState(null);

  const scannerRef = useRef(null);
  const isProcessingRef = useRef(false);

  useEffect(() => {
    fetchAppointment();
  }, []);

  const fetchAppointment = async () => {
    try {
      const res = await api.get(`/appointment/doctor/${id}?doctorId=${doctor.id}`);
      setAppointment(res.data);
      if (res.data.isValid === true) setVerified(true);
    } catch (err) {
      console.error(err);
      alert("Failed to load appointment");
    } finally {
      setLoading(false);
    }
  };

  const stopScanner = async () => {
    if (scannerRef.current) {
      try { await scannerRef.current.stop(); } catch {}
      try { scannerRef.current.clear(); } catch {}
      scannerRef.current = null;
    }
    const readerEl = document.getElementById("reader");
    if (readerEl) readerEl.innerHTML = "";
    isProcessingRef.current = false;
    setIsScanning(false);
  };

  useEffect(() => {
    if (!isScanning) return;

    // Prevent double-init (React 18 Strict Mode)
    if (scannerRef.current) return;

    isProcessingRef.current = false;

    const readerEl = document.getElementById("reader");
    if (readerEl) readerEl.innerHTML = "";

    const qrScanner = new Html5Qrcode("reader");
    scannerRef.current = qrScanner;

    qrScanner.start(
      { facingMode: "environment" },
      { fps: 10, qrbox: 250 },
      async (decodedText) => {
        if (isProcessingRef.current) return;
        isProcessingRef.current = true;

        // Stop camera immediately
        await stopScanner();

        // Then call API
        try {
          const res = await api.post("/qr/scan", {
            token: decodedText,
            doctorId: doctor.id,
          });
          setScanData(res.data.data);
          setVerified(true);
        } catch (err) {
          console.error(err);
          alert(err.response?.data?.message || "❌ Invalid QR");
        }
      },
      (error) => console.warn("QR Error:", error)
    ).catch((err) => {
      console.error("Camera start failed:", err);
      alert("Could not access camera. Please check permissions.");
      scannerRef.current = null;
      setIsScanning(false);
    });

    return () => {
      // Cleanup on unmount
      if (scannerRef.current) {
        scannerRef.current.stop().catch(() => {});
        scannerRef.current = null;
      }
      const readerEl = document.getElementById("reader");
      if (readerEl) readerEl.innerHTML = "";
    };
  }, [isScanning]);

  if (loading)
    return (
      <div className="min-h-screen bg-[#090c12] flex items-center justify-center">
        <div className="text-slate-400 text-sm animate-pulse">Loading appointment...</div>
      </div>
    );

  if (!appointment)
    return (
      <div className="min-h-screen bg-[#090c12] flex items-center justify-center">
        <div className="text-red-400 text-sm">Appointment not found</div>
      </div>
    );

  return (
    <div className="min-h-screen bg-[#090c12] text-white px-6 py-8 max-w-5xl mx-auto">

      {/* BACK + HEADER */}
      <div className="mb-8">
        <button
          onClick={() => navigate(-1)}
          className="text-xs text-slate-500 hover:text-sky-400 transition-colors flex items-center gap-1.5 mb-6 group"
        >
          <span className="group-hover:-translate-x-0.5 transition-transform">←</span>
          Back to Dashboard
        </button>
        <p className="text-xs uppercase tracking-widest text-slate-500 mb-1">Appointment Details</p>
        <h1 className="text-2xl font-bold tracking-tight">
          {appointment.name || "Patient"}
        </h1>
      </div>

      <div className="border-t border-white/[0.06] mb-8" />

      <div className="grid md:grid-cols-2 gap-6">

        {/* LEFT — PATIENT INFO */}
        <div className="bg-white/[0.03] border border-white/[0.07] p-6 rounded-2xl space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold uppercase tracking-widest text-slate-400">
              Patient Info
            </h2>
            <span className={`text-xs px-3 py-1 rounded-full font-medium border ${
              verified
                ? "bg-green-500/10 text-green-400 border-green-500/20"
                : "bg-yellow-500/10 text-yellow-400 border-yellow-500/20"
            }`}>
              {verified ? "✓ Verified" : "Pending"}
            </span>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400 font-bold text-xl flex-shrink-0">
              {(appointment.name || "P").charAt(0).toUpperCase()}
            </div>
            <div>
              <p className="text-lg font-semibold">{appointment.name || "—"}</p>
              <p className="text-xs text-slate-500 mt-0.5">
                {appointment.gender} • {appointment.age} yrs
              </p>
            </div>
          </div>

          <div className="flex items-start gap-3 bg-white/[0.02] border border-white/[0.05] rounded-xl p-3">
            <span className="text-base">⏰</span>
            <div>
              <p className="text-xs text-slate-500 mb-0.5">Appointment Time</p>
              <p className="text-sm text-slate-200">
                {new Date(appointment.time).toLocaleString()}
              </p>
            </div>
          </div>

          {scanData && (
            <div className="space-y-4">
              <div className="border-t border-white/[0.06]" />
              <div className="flex items-center gap-2">
                <span className="text-xs px-2.5 py-1 rounded-full bg-sky-500/10 text-sky-400 border border-sky-500/20 font-medium uppercase tracking-wide">
                  {scanData.status}
                </span>
              </div>
              {scanData.message && (
                <div className="bg-sky-500/5 border border-sky-500/10 rounded-xl px-4 py-3 text-xs text-sky-300">
                  💬 {scanData.message}
                </div>
              )}
              <div>
                <h3 className="text-sm font-semibold uppercase tracking-widest text-slate-400 mb-3">
                  Medical Records
                </h3>
                {scanData.records && scanData.records.length > 0 ? (
                  <ul className="space-y-2">
                    {scanData.records.map((rec, index) => (
                      <li key={index} className="bg-white/[0.04] border border-white/[0.07] px-4 py-3 rounded-xl text-sm text-slate-300 space-y-1.5">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="text-slate-600 text-xs">#{index + 1}</span>
                          <span className="text-xs text-slate-500">
                            {new Date(rec.recordDate).toLocaleDateString()}
                          </span>
                        </div>
                        {rec.diagnosis && (
                          <p><span className="text-slate-500 text-xs">Diagnosis: </span>{rec.diagnosis}</p>
                        )}
                        {rec.prescription && (
                          <p><span className="text-slate-500 text-xs">Prescription: </span>{rec.prescription}</p>
                        )}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className="text-center py-8 border border-white/[0.05] rounded-xl bg-white/[0.02]">
                    <p className="text-2xl mb-2">📋</p>
                    <p className="text-slate-500 text-sm">No records found</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* RIGHT — QR SCANNER */}
        <div className="bg-white/[0.03] border border-white/[0.07] p-6 rounded-2xl flex flex-col">
          <div className="mb-6">
            <h2 className="text-sm font-semibold uppercase tracking-widest text-slate-400">
              QR Verification
            </h2>
            <p className="text-xs text-slate-600 mt-1">
              {verified
                ? "Patient identity has been confirmed"
                : "Scan the patient's QR code to verify identity before consultation"}
            </p>
          </div>

          <div className="flex-1 flex flex-col items-center justify-center">

            {verified && !scanData && (
              <div className="text-center space-y-4 w-full">
                <div className="w-20 h-20 rounded-full bg-green-500/10 border border-green-500/20 flex items-center justify-center mx-auto text-4xl">✅</div>
                <div>
                  <p className="text-green-400 font-semibold text-lg">Already Verified</p>
                  <p className="text-xs text-slate-600 mt-1">This appointment was previously verified</p>
                </div>
              </div>
            )}

            {verified && scanData && (
              <div className="text-center space-y-4 w-full">
                <div className="w-20 h-20 rounded-full bg-green-500/10 border border-green-500/20 flex items-center justify-center mx-auto text-4xl">✅</div>
                <div>
                  <p className="text-green-400 font-semibold text-lg">Patient Verified</p>
                  <p className="text-xs text-slate-600 mt-1">Scanned just now</p>
                </div>
              </div>
            )}

            {!verified && !isScanning && (
              <div className="text-center space-y-5">
                <div className="w-20 h-20 rounded-2xl bg-sky-500/5 border border-sky-500/10 flex items-center justify-center mx-auto text-4xl">📷</div>
                <div>
                  <p className="text-slate-300 text-sm font-medium mb-1">Ready to scan</p>
                  <p className="text-xs text-slate-600">Point camera at patient's QR code</p>
                </div>
                <button
                  onClick={() => setIsScanning(true)}
                  className="bg-sky-500/15 text-sky-400 border border-sky-500/25 px-8 py-3 rounded-xl hover:bg-sky-500/25 hover:border-sky-500/40 transition-all font-medium text-sm"
                >
                  Start Scanning
                </button>
              </div>
            )}

            {!verified && isScanning && (
              <div className="w-full flex flex-col items-center gap-4">
                <div
                  id="reader"
                  className="w-full max-w-sm rounded-xl overflow-hidden border border-white/[0.07]"
                  style={{ minHeight: "300px" }}
                />
                <button
                  onClick={stopScanner}
                  className="bg-red-500/10 text-red-400 border border-red-500/20 px-6 py-2 rounded-xl hover:bg-red-500/20 transition-all text-sm font-medium"
                >
                  Cancel Scan
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AppointmentDetails;