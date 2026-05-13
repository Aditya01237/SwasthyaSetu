import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";

const StarRating = ({ rating = 4.7 }) => {
  const full = Math.floor(rating);
  const half = rating % 1 >= 0.5;
  return (
    <div className="flex items-center gap-1.5">
      <div className="flex gap-0.5">
        {[...Array(5)].map((_, i) => (
          <svg key={i} className={`w-3.5 h-3.5 ${i < full ? "text-amber-400" : i === full && half ? "text-amber-400/60" : "text-slate-700"}`} fill="currentColor" viewBox="0 0 20 20">
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
        ))}
      </div>
      <span className="text-slate-400 text-xs font-medium">{rating}</span>
    </div>
  );
};

const StatCard = ({ icon, label, value, sub, color = "sky", delay = 0 }) => {
  const accents = {
    sky: "from-sky-500/20 to-blue-600/20 border-sky-500/20 text-sky-400",
    green: "from-emerald-500/20 to-teal-600/20 border-emerald-500/20 text-emerald-400",
    amber: "from-amber-500/20 to-yellow-600/20 border-amber-500/20 text-amber-400",
    violet: "from-violet-500/20 to-purple-600/20 border-violet-500/20 text-violet-400",
  };
  return (
    <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5 flex flex-col gap-2 hover:border-sky-500/20 transition-colors duration-200 animate-fadeUp" style={{ animationDelay: `${delay}ms` }}>
      <div className={`w-10 h-10 rounded-xl bg-gradient-to-br border flex items-center justify-center text-lg ${accents[color]}`}>{icon}</div>
      <p className="font-display text-2xl font-semibold text-slate-100">{value}</p>
      <div>
        <p className="text-slate-400 text-xs font-medium">{label}</p>
        {sub && <p className="text-slate-600 text-[10px]">{sub}</p>}
      </div>
    </div>
  );
};

const InfoRow = ({ label, value }) => (
  <div className="flex flex-col gap-0.5 py-3 border-b border-white/[0.05] last:border-0">
    <span className="text-slate-600 text-[10px] uppercase tracking-wider">{label}</span>
    <span className="text-slate-300 text-sm">{value}</span>
  </div>
);

const generateSlots = (startTime, endTime) => {
  const toMinutes = (timeStr) => {
    const [hourMin, period] = timeStr.trim().split(" ");
    let [hours, minutes] = hourMin.split(":").map(Number);
    if (period === "PM" && hours !== 12) hours += 12;
    if (period === "AM" && hours === 12) hours = 0;
    return hours * 60 + minutes;
  };
  const toLabel = (totalMinutes) => {
    let hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    const period = hours >= 12 ? "PM" : "AM";
    if (hours > 12) hours -= 12;
    if (hours === 0) hours = 12;
    return `${hours}:${String(minutes).padStart(2, "0")} ${period}`;
  };
  const slots = [];
  let current = toMinutes(startTime);
  const end = toMinutes(endTime);
  while (current < end) {
    slots.push(toLabel(current));
    current += 20;
  }
  return slots;
};

const slotToMinutes = (slot) => {
  const [hourMin, period] = slot.trim().split(" ");
  let [hours, minutes] = hourMin.split(":").map(Number);
  if (period === "PM" && hours !== 12) hours += 12;
  if (period === "AM" && hours === 12) hours = 0;
  return hours * 60 + minutes;
};

const convertTo24Hour = (time) => {
  const [hourMin, period] = time.split(" ");
  let [hours, minutes] = hourMin.split(":").map(Number);
  if (period === "PM" && hours !== 12) hours += 12;
  if (period === "AM" && hours === 12) hours = 0;
  return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:00`;
};

const slotToBookedKey = (slot) => convertTo24Hour(slot).slice(0, 5);

// If today → filter past slots (with 20min buffer). If future date → show all.
const getAvailableSlots = (slots, selectedDate) => {
  if (!selectedDate) return [];
  const today = new Date().toISOString().split("T")[0];
  if (selectedDate !== today) return slots;
  const now = new Date();
  const currentMinutes = now.getHours() * 60 + now.getMinutes() + 20;
  return slots.filter((slot) => slotToMinutes(slot) > currentMinutes);
};

const ALL_MORNING = generateSlots("10:00 AM", "1:00 PM");
const ALL_EVENING = generateSlots("4:00 PM", "11:00 PM");

const DoctorProfile = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [doctor, setDoctor] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showBooking, setShowBooking] = useState(false);
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedSlot, setSelectedSlot] = useState("");
  const [bookedSlotKeys, setBookedSlotKeys] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [slotError, setSlotError] = useState("");
  const [loadingBooking, setLoadingBooking] = useState(false);

  useEffect(() => { fetchDoctor(); }, []);

  useEffect(() => {
    if (!showBooking || !selectedDate || !id) {
      setBookedSlotKeys([]);
      setSlotError("");
      return;
    }

    let ignore = false;

    const fetchBookedSlots = async () => {
      try {
        setLoadingSlots(true);
        setSlotError("");
        const res = await api.get("/appointment/slots/booked", {
          params: { doctorId: id, date: selectedDate },
        });
        if (!ignore) {
          setBookedSlotKeys(Array.isArray(res.data) ? res.data : []);
        }
      } catch (err) {
        console.error(err);
        if (!ignore) {
          setBookedSlotKeys([]);
          setSlotError("Could not refresh booked slots. Please try another date or reopen booking.");
        }
      } finally {
        if (!ignore) setLoadingSlots(false);
      }
    };

    fetchBookedSlots();

    return () => {
      ignore = true;
    };
  }, [showBooking, selectedDate, id]);

  const fetchDoctor = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/doctor/${id}`);
      setDoctor(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleBooking = async () => {
    if (!selectedDate || !selectedSlot) { alert("Please select date and slot"); return; }
    try {
      setLoadingBooking(true);
      await api.post("/appointment/book", {
        uhid: localStorage.getItem("uhid"),
        hospitalId: doctor.hospitalId,
        doctorId: id,
        appointmentTime: `${selectedDate}T${convertTo24Hour(selectedSlot)}`,
      });
      setShowBooking(false);
      setSelectedDate("");
      setSelectedSlot("");
      alert("Appointment booked successfully 🎉");
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || "Booking failed");
    } finally {
      setLoadingBooking(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
        <Navbar />
        <div className="max-w-5xl mx-auto px-6 pt-10 animate-pulse space-y-6">
          <div className="h-48 rounded-2xl bg-slate-800/60" />
          <div className="grid grid-cols-3 gap-4">
            {[...Array(3)].map((_, i) => <div key={i} className="h-32 rounded-2xl bg-slate-800/60" />)}
          </div>
        </div>
      </div>
    );
  }

  if (!doctor) {
    return (
      <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans flex items-center justify-center">
        <p className="text-slate-500">Doctor not found.</p>
      </div>
    );
  }

  const bookedSlotSet = new Set(bookedSlotKeys);
  const morningSlots = loadingSlots || slotError
    ? []
    : getAvailableSlots(ALL_MORNING, selectedDate).filter((slot) => !bookedSlotSet.has(slotToBookedKey(slot)));
  const eveningSlots = loadingSlots || slotError
    ? []
    : getAvailableSlots(ALL_EVENING, selectedDate).filter((slot) => !bookedSlotSet.has(slotToBookedKey(slot)));
  const noSlotsAvailable = selectedDate && !loadingSlots && !slotError && morningSlots.length === 0 && eveningSlots.length === 0;

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
      <Navbar />
      <div className="fixed -top-32 left-1/2 -translate-x-1/2 w-[700px] h-64 bg-sky-500/[0.07] rounded-full blur-3xl pointer-events-none z-0" />

      <div className="relative z-10 max-w-5xl mx-auto px-6 pt-10 pb-24">
        <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-slate-500 text-sm hover:text-sky-400 transition-colors mb-8 group">
          <svg className="w-4 h-4 transition-transform group-hover:-translate-x-1" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back
        </button>

        {/* Hero */}
        <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 md:p-8 mb-6 flex flex-col md:flex-row items-start md:items-center gap-6 animate-fadeUp">
          <div className="relative flex-shrink-0">
            <div className="w-24 h-24 rounded-2xl bg-gradient-to-br from-sky-500/30 to-blue-700/30 border border-sky-500/25 flex items-center justify-center font-display text-4xl font-bold text-sky-400">
              {doctor.name?.charAt(0)}
            </div>
            <span className="absolute -bottom-1 -right-1 w-4 h-4 bg-emerald-400 rounded-full border-2 border-[#090c12] animate-pulse" />
          </div>
          <div className="flex-1 space-y-2">
            <span className="inline-block px-3 py-1 text-[10px] font-medium tracking-[3px] uppercase text-sky-400 border border-sky-500/25 rounded-full bg-sky-500/5">{doctor.specialization}</span>
            <h1 className="font-display text-3xl font-bold text-slate-50">Dr. {doctor.name}</h1>
            <div className="flex flex-wrap items-center gap-4">
              <StarRating rating={doctor.rating || 4.7} />
              {doctor.hospitalName && (
                <span className="flex items-center gap-1.5 text-slate-500 text-xs">
                  <svg className="w-3.5 h-3.5 text-sky-500/70" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                  {doctor.hospitalName}
                </span>
              )}
            </div>
          </div>
          <div className="w-full md:w-auto flex-shrink-0 space-y-2.5 md:min-w-[180px]">
            <button onClick={() => setShowBooking(true)} className="w-full bg-gradient-to-r from-sky-500 to-blue-600 text-white text-sm font-medium py-3 px-6 rounded-xl hover:opacity-90 hover:-translate-y-px active:translate-y-0 transition-all duration-200 flex items-center justify-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              Book Appointment
            </button>
            <button className="w-full bg-white/[0.04] border border-white/[0.08] text-slate-300 text-sm font-medium py-2.5 px-6 rounded-xl hover:bg-white/[0.07] transition-all duration-200 flex items-center justify-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
              Message
            </button>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <StatCard delay={0} icon="🏅" label="Experience" value={`${doctor.experience} yrs`} sub="in practice" color="sky" />
          <StatCard delay={60} icon="💰" label="Consultation Fee" value={`₹${doctor.fee}`} sub="per session" color="green" />
          <StatCard delay={120} icon="⭐" label="Patient Rating" value={doctor.rating || "4.7"} sub="out of 5" color="amber" />
          <StatCard delay={180} icon="👥" label="Patients" value={doctor.totalPatients || "500+"} sub="treated" color="violet" />
        </div>

        {/* Lower Grid */}
        <div className="grid md:grid-cols-3 gap-5">
          <div className="md:col-span-2 space-y-5">
            <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 animate-fadeUp" style={{ animationDelay: "200ms" }}>
              <h2 className="font-display text-lg font-semibold text-slate-100 mb-3">About</h2>
              <p className="text-slate-400 text-sm leading-relaxed">
                {doctor.about || `Dr. ${doctor.name} is a highly skilled ${doctor.specialization} with over ${doctor.experience}+ years of clinical experience. Renowned for a patient-first approach, precision in diagnosis, and consistently delivering exceptional treatment outcomes. Trusted by hundreds of patients across ${doctor.hospitalName || "the region"}.`}
              </p>
            </div>
            <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 animate-fadeUp" style={{ animationDelay: "240ms" }}>
              <h2 className="font-display text-lg font-semibold text-slate-100 mb-4">Available Timings</h2>
              <div className="space-y-2">
                {(doctor.timings || [
                  { day: "Monday – Friday", time: "10:00 AM – 1:00 PM" },
                  { day: "Monday – Friday", time: "4:00 PM – 11:00 PM" },
                  { day: "Saturday", time: "10:00 AM – 2:00 PM" },
                  { day: "Sunday", time: "Closed" },
                ]).map((slot, i) => (
                  <div key={i} className={`flex items-center justify-between px-4 py-3 rounded-xl border text-sm ${slot.time === "Closed" ? "border-red-500/10 bg-red-500/[0.03] text-red-400/60" : "border-white/[0.06] bg-white/[0.02] text-slate-300"}`}>
                    <span className="text-slate-500 text-xs">{slot.day}</span>
                    <span className="font-medium">{slot.time}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="space-y-5">
            <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 animate-fadeUp" style={{ animationDelay: "260ms" }}>
              <h2 className="font-display text-lg font-semibold text-slate-100 mb-1">Details</h2>
              <InfoRow label="Specialization" value={doctor.specialization} />
              <InfoRow label="Experience" value={`${doctor.experience} years`} />
              <InfoRow label="Hospital" value={doctor.hospitalName || "—"} />
              <InfoRow label="Consultation" value={`₹${doctor.fee}`} />
              <InfoRow label="Languages" value={doctor.languages || "English, Hindi"} />
            </div>
            {(doctor.qualifications || ["MBBS", "MD – " + doctor.specialization])?.length > 0 && (
              <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 animate-fadeUp" style={{ animationDelay: "300ms" }}>
                <h2 className="font-display text-lg font-semibold text-slate-100 mb-3">Qualifications</h2>
                <div className="space-y-2">
                  {(doctor.qualifications || ["MBBS", "MD – " + doctor.specialization]).map((q, i) => (
                    <div key={i} className="flex items-center gap-2.5 text-sm text-slate-400">
                      <span className="w-1.5 h-1.5 rounded-full bg-sky-400 flex-shrink-0" />
                      {q}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Booking Modal */}
      {showBooking && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 px-4">
          <div className="bg-[#0f141c] border border-white/[0.08] rounded-2xl p-6 w-full max-w-md animate-fadeUp max-h-[90vh] overflow-y-auto">
            <h2 className="font-display text-lg font-semibold text-slate-100 mb-4">Book Appointment</h2>

            {/* Date */}
            <div className="mb-5">
              <label className="text-xs text-slate-500 uppercase tracking-wider">Select Date</label>
              <input
                type="date"
                min={new Date().toISOString().split("T")[0]}
                value={selectedDate}
                onChange={(e) => {
                  setSelectedDate(e.target.value);
                  setSelectedSlot("");
                  setBookedSlotKeys([]);
                  setSlotError("");
                  setLoadingSlots(Boolean(e.target.value));
                }}
                className="w-full mt-1.5 bg-white/[0.05] border border-white/[0.08] rounded-xl px-3 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-sky-500/50"
              />
            </div>

            {/* No date hint */}
            {!selectedDate && (
              <div className="mb-4 py-4 rounded-xl bg-white/[0.02] border border-white/[0.06] text-xs text-slate-500 text-center">
                📅 Select a date to see available slots
              </div>
            )}

            {selectedDate && loadingSlots && (
              <div className="mb-4 py-4 rounded-xl bg-sky-500/[0.05] border border-sky-500/20 text-xs text-sky-300 text-center">
                Checking booked slots...
              </div>
            )}

            {slotError && (
              <div className="mb-4 py-4 rounded-xl bg-red-500/[0.05] border border-red-500/20 text-xs text-red-400 text-center">
                {slotError}
              </div>
            )}

            {/* No slots */}
            {noSlotsAvailable && (
              <div className="mb-4 py-4 rounded-xl bg-red-500/[0.05] border border-red-500/20 text-xs text-red-400 text-center">
                No slots available for this date. Please select another date.
              </div>
            )}

            {/* Morning Slots */}
            {morningSlots.length > 0 && (
              <div className="mb-4">
                <label className="text-xs text-slate-500 uppercase tracking-wider flex items-center gap-2 mb-2">
                  <span className="text-amber-400">☀️</span> Morning
                  <span className="text-slate-600">(10:00 AM – 1:00 PM)</span>
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {morningSlots.map((slot) => (
                    <button key={slot} onClick={() => setSelectedSlot(slot)}
                      className={`text-xs px-3 py-2 rounded-lg border transition-all duration-150 ${selectedSlot === slot ? "bg-sky-500 text-white border-sky-500 shadow-lg shadow-sky-500/20" : "bg-white/[0.03] border-white/[0.08] text-slate-400 hover:border-sky-500/30 hover:text-slate-300"}`}>
                      {slot}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {morningSlots.length > 0 && eveningSlots.length > 0 && (
              <div className="border-t border-white/[0.06] my-4" />
            )}

            {/* Evening Slots */}
            {eveningSlots.length > 0 && (
              <div className="mb-5">
                <label className="text-xs text-slate-500 uppercase tracking-wider flex items-center gap-2 mb-2">
                  <span className="text-violet-400">🌆</span> Evening
                  <span className="text-slate-600">(4:00 PM – 11:00 PM)</span>
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {eveningSlots.map((slot) => (
                    <button key={slot} onClick={() => setSelectedSlot(slot)}
                      className={`text-xs px-3 py-2 rounded-lg border transition-all duration-150 ${selectedSlot === slot ? "bg-sky-500 text-white border-sky-500 shadow-lg shadow-sky-500/20" : "bg-white/[0.03] border-white/[0.08] text-slate-400 hover:border-sky-500/30 hover:text-slate-300"}`}>
                      {slot}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Selected summary */}
            {selectedSlot && (
              <div className="mb-4 px-3 py-2 rounded-xl bg-sky-500/10 border border-sky-500/20 text-xs text-sky-300 flex items-center gap-2">
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Selected: <span className="font-medium">{selectedSlot}</span>
                {selectedDate && <> on <span className="font-medium">{new Date(selectedDate + "T00:00:00").toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" })}</span></>}
              </div>
            )}

            {/* Actions */}
            <div className="flex gap-2 mt-2">
              <button onClick={() => { setShowBooking(false); setSelectedSlot(""); setSelectedDate(""); }}
                className="flex-1 py-2.5 rounded-xl bg-white/[0.05] text-slate-400 hover:bg-white/[0.08] text-sm transition-colors">
                Cancel
              </button>
              <button onClick={handleBooking} disabled={loadingBooking || loadingSlots || Boolean(slotError) || !selectedSlot || !selectedDate}
                className="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-sky-500 to-blue-600 text-white hover:opacity-90 text-sm font-medium transition-opacity disabled:opacity-60">
                {loadingBooking ? "Booking..." : "Confirm"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorProfile;
