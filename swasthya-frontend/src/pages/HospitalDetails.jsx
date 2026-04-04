import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";

/* ── Reusable tiny components ── */
const StarRating = ({ rating = 4.5 }) => {
  const full = Math.floor(rating);
  const half = rating % 1 >= 0.5;
  return (
    <div className="flex items-center gap-1.5">
      <div className="flex gap-0.5">
        {[...Array(5)].map((_, i) => (
          <svg
            key={i}
            className={`w-3.5 h-3.5 ${
              i < full
                ? "text-amber-400"
                : i === full && half
                ? "text-amber-400/60"
                : "text-slate-700"
            }`}
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
        ))}
      </div>
      <span className="text-slate-400 text-xs font-medium">{rating}</span>
    </div>
  );
};

const Badge = ({ children, color = "sky" }) => {
  const colors = {
    sky: "bg-sky-500/10 border-sky-500/20 text-sky-400",
    green: "bg-emerald-500/10 border-emerald-500/20 text-emerald-400",
    amber: "bg-amber-500/10 border-amber-500/20 text-amber-400",
    violet: "bg-violet-500/10 border-violet-500/20 text-violet-400",
  };
  return (
    <span
      className={`inline-flex items-center border px-3 py-1 rounded-full text-xs font-medium ${colors[color]}`}
    >
      {children}
    </span>
  );
};

const TabBtn = ({ active, onClick, children }) => (
  <button
    onClick={onClick}
    className={`relative py-3.5 px-1 text-sm font-medium capitalize transition-colors duration-200 ${
      active ? "text-sky-400" : "text-slate-500 hover:text-slate-300"
    }`}
  >
    {children}
    {active && (
      <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-sky-500 to-blue-500 rounded-full" />
    )}
  </button>
);

const Section = ({ title, children }) => (
  <div className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 animate-fadeUp">
    {title && (
      <h2 className="font-display text-lg font-semibold text-slate-100 mb-5">
        {title}
      </h2>
    )}
    {children}
  </div>
);

/* ── Loading skeleton ── */
const DetailsSkeleton = () => (
  <div className="animate-pulse space-y-6 max-w-6xl mx-auto px-6 pt-8">
    <div className="h-72 rounded-2xl bg-slate-800/60" />
    <div className="grid md:grid-cols-3 gap-4">
      {[...Array(3)].map((_, i) => (
        <div key={i} className="h-28 rounded-2xl bg-slate-800/60" />
      ))}
    </div>
  </div>
);

/* ─────────────────────────────────────────────
   Main Component
───────────────────────────────────────────── */
const HospitalDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [hospital, setHospital] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [tab, setTab] = useState("overview");
  const [currentImage, setCurrentImage] = useState(0);
  const [loadingHospital, setLoadingHospital] = useState(true);

  useEffect(() => {
    fetchHospital();
    fetchDoctors();
  }, []);

  const fetchHospital = async () => {
    setLoadingHospital(true);
    try {
      const res = await api.get(`/hospital/${id}`);
      setHospital(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingHospital(false);
    }
  };

  const fetchDoctors = async () => {
    try {
      console.log("I am fetching doctor");
      const res = await api.get(`/doctor/hospital/${id}`);
      setDoctors(res.data.data);
      console.log(res);
    } catch (err) {
      console.error(err);
    }
  };

  if (loadingHospital) {
    return (
      <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
        <Navbar />
        <DetailsSkeleton />
      </div>
    );
  }

  if (!hospital) {
    return (
      <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans flex items-center justify-center">
        <p className="text-slate-500">Hospital not found.</p>
      </div>
    );
  }

  const images = hospital.imageUrls?.length
    ? hospital.imageUrls
    : ["https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=800&q=80"];

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
      <Navbar />

      {/* ── Ambient glow ── */}
      <div className="fixed -top-32 left-1/2 -translate-x-1/2 w-[700px] h-64 bg-sky-500/[0.07] rounded-full blur-3xl pointer-events-none z-0" />

      <div className="relative z-10 max-w-6xl mx-auto px-6 pt-10 pb-24">

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
            <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back
        </button>

        {/* ══════════════════════════════════════
            HERO SECTION
        ══════════════════════════════════════ */}
        <div className="grid md:grid-cols-5 gap-6 mb-8 animate-fadeUp">

          {/* ── Image slider (3 cols) ── */}
          <div className="md:col-span-3 space-y-3">
            {/* Main image */}
            <div className="relative h-72 md:h-80 rounded-2xl overflow-hidden border border-white/[0.07]">
              <img
                src={images[currentImage]}
                onError={(e) =>
                  (e.target.src =
                    "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=800&q=80")
                }
                alt={hospital.name}
                className="w-full h-full object-cover brightness-75 saturate-[0.85] transition-all duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-slate-950/60 via-transparent to-transparent" />

              {/* Image counter */}
              {images.length > 1 && (
                <div className="absolute bottom-3 right-3 bg-slate-950/70 border border-white/10 backdrop-blur-md rounded-lg px-2.5 py-1 text-slate-400 text-xs">
                  {currentImage + 1} / {images.length}
                </div>
              )}

              {/* Nav arrows */}
              {images.length > 1 && (
                <>
                  <button
                    onClick={() =>
                      setCurrentImage((p) => (p === 0 ? images.length - 1 : p - 1))
                    }
                    className="absolute left-3 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center rounded-full bg-slate-950/60 border border-white/10 text-slate-300 hover:text-white hover:border-sky-500/30 transition-all"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                    </svg>
                  </button>
                  <button
                    onClick={() =>
                      setCurrentImage((p) => (p === images.length - 1 ? 0 : p + 1))
                    }
                    className="absolute right-3 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center rounded-full bg-slate-950/60 border border-white/10 text-slate-300 hover:text-white hover:border-sky-500/30 transition-all"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                    </svg>
                  </button>
                </>
              )}
            </div>

            {/* Thumbnails */}
            {images.length > 1 && (
              <div className="flex gap-2 overflow-x-auto pb-1">
                {images.map((img, i) => (
                  <button
                    key={i}
                    onClick={() => setCurrentImage(i)}
                    className={`flex-shrink-0 w-16 h-16 rounded-xl overflow-hidden border-2 transition-all duration-200 ${
                      currentImage === i
                        ? "border-sky-500 opacity-100"
                        : "border-transparent opacity-50 hover:opacity-75"
                    }`}
                  >
                    <img
                      src={img}
                      alt=""
                      className="w-full h-full object-cover brightness-75"
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* ── Details panel (2 cols) ── */}
          <div className="md:col-span-2 bg-white/[0.02] border border-white/[0.07] rounded-2xl p-6 flex flex-col justify-between">
            <div className="space-y-4">
              {/* Label */}
              <span className="inline-block px-3 py-1 text-[10px] font-medium tracking-[3px] uppercase text-sky-400 border border-sky-500/25 rounded-full bg-sky-500/5">
                Multi-speciality Hospital
              </span>

              {/* Name */}
              <h1 className="font-display text-2xl font-bold text-slate-50 leading-snug">
                {hospital.name}
              </h1>

              {/* Location */}
              <div className="flex items-start gap-2 text-slate-500 text-sm">
                <svg className="w-4 h-4 mt-0.5 flex-shrink-0 text-sky-500/70" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                <span>
                  {hospital.address}, <span className="text-slate-400">{hospital.city}</span>
                </span>
              </div>

              {/* Rating row */}
              <div className="flex items-center gap-4">
                <StarRating rating={hospital.rating || 4.5} />
                <span className="text-slate-600 text-xs">
                  {hospital.totalReviews || 200} reviews
                </span>
              </div>

              {/* Open status */}
              <div className="flex items-center gap-2">
                <span
                  className={`w-2 h-2 rounded-full ${
                    hospital.isOpen24x7 ? "bg-emerald-400 animate-pulse" : "bg-red-500"
                  }`}
                />
                <span
                  className={`text-sm font-medium ${
                    hospital.isOpen24x7 ? "text-emerald-400" : "text-red-400"
                  }`}
                >
                  {hospital.isOpen24x7 ? "Open 24×7" : "Currently Closed"}
                </span>
              </div>

              {/* Services preview */}
              <div className="flex flex-wrap gap-2 pt-1">
                {hospital.services?.slice(0, 5).map((s, i) => (
                  <Badge key={i} color="sky">{s}</Badge>
                ))}
                {hospital.services?.length > 5 && (
                  <span className="inline-flex items-center border border-white/10 px-3 py-1 rounded-full text-xs text-slate-500 bg-white/[0.02]">
                    +{hospital.services.length - 5} more
                  </span>
                )}
              </div>
            </div>

            {/* CTA */}
            <div className="mt-6 space-y-2.5">
              <button className="w-full bg-white/[0.04] border border-white/[0.08] text-slate-300 text-sm font-medium py-2.5 rounded-xl hover:bg-white/[0.07] transition-all duration-200 flex items-center justify-center gap-2">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                Contact Hospital
              </button>
            </div>
          </div>
        </div>

        {/* ══════════════════════════════════════
            STAT CARDS
        ══════════════════════════════════════ */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8 animate-fadeUp" style={{ animationDelay: "80ms" }}>
          {[
            { label: "Rating", value: `${hospital.rating || 4.5}★`, sub: `${hospital.totalReviews || 200} reviews`, icon: "⭐" },
            { label: "Doctors", value: doctors.length || "—", sub: "on staff", icon: "👨‍⚕️" },
            { label: "Services", value: hospital.services?.length || "—", sub: "available", icon: "🏥" },
            { label: "Availability", value: hospital.isOpen24x7 ? "24×7" : "Limited", sub: hospital.isOpen24x7 ? "Always open" : "Check hours", icon: "🕐" },
          ].map((stat, i) => (
            <div
              key={i}
              className="bg-white/[0.02] border border-white/[0.07] rounded-2xl p-4 flex flex-col gap-1 hover:border-sky-500/20 transition-colors duration-200"
            >
              <span className="text-xl">{stat.icon}</span>
              <span className="font-display text-xl font-semibold text-slate-100">{stat.value}</span>
              <span className="text-slate-500 text-xs">{stat.label}</span>
              <span className="text-slate-600 text-[10px]">{stat.sub}</span>
            </div>
          ))}
        </div>

        {/* ══════════════════════════════════════
            TABS
        ══════════════════════════════════════ */}
        <div
          className="flex gap-6 border-b border-white/[0.07] mb-7 animate-fadeUp"
          style={{ animationDelay: "120ms" }}
        >
          {["overview", "doctors", "services"].map((t) => (
            <TabBtn key={t} active={tab === t} onClick={() => setTab(t)}>
              {t}
              {t === "doctors" && doctors.length > 0 && (
                <span className="ml-1.5 text-[10px] bg-sky-500/20 text-sky-400 border border-sky-500/20 px-1.5 py-0.5 rounded-full">
                  {doctors.length}
                </span>
              )}
            </TabBtn>
          ))}
        </div>

        {/* ══════════════════════════════════════
            TAB PANELS
        ══════════════════════════════════════ */}

        {/* ── Overview ── */}
        {tab === "overview" && (
          <div className="space-y-5">
            <Section title={`About ${hospital.name}`}>
              <p className="text-slate-400 text-sm leading-relaxed">
                {hospital.description ||
                  `${hospital.name} is one of the leading multi-specialty hospitals in ${hospital.city},
                  committed to delivering world-class healthcare with compassion and precision.
                  Equipped with state-of-the-art infrastructure and a team of expert specialists,
                  we serve thousands of patients every year.`}
              </p>
            </Section>

            {hospital.specializations?.length > 0 && (
              <Section title="Specializations">
                <div className="flex flex-wrap gap-2">
                  {hospital.specializations.map((s, i) => (
                    <Badge key={i} color="green">{s}</Badge>
                  ))}
                </div>
              </Section>
            )}

            {/* Quick info grid */}
            <Section title="Quick Info">
              <div className="grid md:grid-cols-2 gap-4">
                {[
                  { label: "City", value: hospital.city },
                  { label: "Address", value: hospital.address },
                  { label: "Rating", value: `${hospital.rating || 4.5} / 5` },
                  { label: "Open 24×7", value: hospital.isOpen24x7 ? "Yes" : "No" },
                ].map((item, i) => (
                  <div key={i} className="flex flex-col gap-0.5">
                    <span className="text-slate-600 text-xs uppercase tracking-wider">{item.label}</span>
                    <span className="text-slate-300 text-sm">{item.value}</span>
                  </div>
                ))}
              </div>
            </Section>
          </div>
        )}

        {/* ── Doctors ── */}
        {tab === "doctors" && (
          <div className="space-y-4">
            {doctors.length === 0 ? (
              <div className="text-center py-20">
                <div className="text-4xl mb-3 opacity-20">👨‍⚕️</div>
                <p className="text-slate-600 text-sm">No doctors listed for this hospital.</p>
              </div>
            ) : (
              <div className="grid md:grid-cols-2 gap-4">
                {doctors.map((doc, i) => (
                  <div
                    key={doc.id}
                    onClick={() => navigate(`/doctor/${doc.id}`)}
                    className="group bg-white/[0.02] border border-white/[0.07] rounded-2xl p-5 flex items-center justify-between cursor-pointer
                               hover:border-sky-500/30 hover:-translate-y-1
                               hover:shadow-[0_12px_40px_rgba(0,0,0,0.5),0_0_0_1px_rgba(14,165,233,0.08)]
                               transition-all duration-300 animate-fadeUp"
                    style={{ animationDelay: `${i * 60}ms` }}
                  >
                    <div className="flex items-center gap-4">
                      {/* Avatar placeholder */}
                      <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-sky-500/20 to-blue-600/20 border border-sky-500/20 flex items-center justify-center text-sky-400 text-lg font-display font-bold flex-shrink-0">
                        {doc.name?.[0] || "D"}
                      </div>
                      <div>
                        <h3 className="text-slate-100 font-semibold text-sm group-hover:text-sky-400 transition-colors">
                          {doc.name}
                        </h3>
                        <p className="text-slate-500 text-xs mt-0.5">{doc.specialization}</p>
                        <p className="text-slate-600 text-xs">{doc.experience} yrs experience</p>
                      </div>
                    </div>

                    <button
                      onClick={(e) => { e.stopPropagation(); navigate(`/doctor/${doc.id}`); }}
                      className="flex-shrink-0 flex items-center gap-1.5 bg-sky-500/10 border border-sky-500/20 text-sky-400
                                 px-3.5 py-2 rounded-xl text-xs font-medium
                                 transition-all duration-200 hover:bg-sky-500/20 hover:border-sky-400/40"
                    >
                      Book
                      <svg className="w-3 h-3" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M17 8l4 4m0 0l-4 4m4-4H3" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ── Services ── */}
        {tab === "services" && (
          <Section title="All Services">
            {hospital.services?.length === 0 ? (
              <p className="text-slate-600 text-sm">No services listed.</p>
            ) : (
              <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                {hospital.services?.map((s, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-3 bg-white/[0.02] border border-white/[0.07] rounded-xl px-4 py-3
                               hover:border-sky-500/20 hover:bg-sky-500/[0.03] transition-all duration-200 animate-fadeUp"
                    style={{ animationDelay: `${i * 30}ms` }}
                  >
                    <span className="w-1.5 h-1.5 rounded-full bg-sky-400 flex-shrink-0" />
                    <span className="text-slate-400 text-sm">{s}</span>
                  </div>
                ))}
              </div>
            )}
          </Section>
        )}
      </div>
    </div>
  );
};

export default HospitalDetails;