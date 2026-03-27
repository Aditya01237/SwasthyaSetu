import React, { useEffect, useState } from "react";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import { useNavigate } from "react-router-dom";

const SkeletonCard = ({ delay = 0 }) => (
  <div
    className="bg-slate-900 border border-white/5 rounded-2xl overflow-hidden animate-fadeUp"
    style={{ animationDelay: `${delay}ms` }}
  >
    <div className="h-44 bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 bg-[length:400%_100%] animate-shimmer" />
    <div className="p-5 flex flex-col gap-3">
      <div className="h-5 w-2/3 rounded-md bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 bg-[length:400%_100%] animate-shimmer" />
      <div className="h-3.5 w-2/5 rounded-md bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 bg-[length:400%_100%] animate-shimmer" />
      <div className="h-9 w-full mt-2 rounded-xl bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 bg-[length:400%_100%] animate-shimmer" />
    </div>
  </div>
);

const StarRating = ({ rating = 4.5 }) => {
  const full = Math.floor(rating);
  const half = rating % 1 >= 0.5;
  return (
    <div className="flex items-center gap-1.5">
      <div className="flex gap-0.5">
        {[...Array(5)].map((_, i) => (
          <svg
            key={i}
            className={`w-3.5 h-3.5 ${i < full ? "text-amber-400" : i === full && half ? "text-amber-400/60" : "text-slate-700"}`}
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
        ))}
      </div>
      <span className="text-slate-500 text-xs">{rating}</span>
    </div>
  );
};

const HospitalCard = ({ h, index, onClick }) => (
  <div
    className="group bg-slate-900 border border-white/[0.07] rounded-2xl overflow-hidden cursor-pointer
               transition-all duration-300 ease-out
               hover:-translate-y-2 hover:border-sky-500/30
               hover:shadow-[0_20px_60px_rgba(0,0,0,0.6),0_0_0_1px_rgba(14,165,233,0.1)]
               animate-fadeUp"
    style={{ animationDelay: `${index * 70}ms` }}
    onClick={onClick}
  >
    {/* Image */}
    <div className="relative h-44 overflow-hidden">
      <img
        src={h.imageUrls?.[0] || "https://images.unsplash.com/photo-1586773860418-d37222d8fce3?w=600&q=80"}
        alt={h.name}
        className="w-full h-full object-cover brightness-75 saturate-[0.9] transition-transform duration-500 group-hover:scale-105"
      />
      {/* Gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent" />
      {/* Rating badge */}
      <div className="absolute top-3 right-3 flex items-center gap-1 bg-slate-950/70 border border-white/10 backdrop-blur-md rounded-lg px-2.5 py-1 text-amber-400 text-xs font-medium">
        ⭐ {h.rating || "4.5"}
      </div>
      {/* City pill */}
      <div className="absolute bottom-3 left-3 flex items-center gap-1.5 text-xs text-slate-300">
        <span className="w-1.5 h-1.5 rounded-full bg-sky-400 flex-shrink-0" />
        {h.city}
      </div>
    </div>

    {/* Body */}
    <div className="p-5">
      <h2 className="font-display text-lg font-semibold text-slate-100 leading-snug mb-1 truncate">
        {h.name}
      </h2>
      <p className="text-slate-500 text-xs mb-4 truncate">{h.address}</p>

      <div className="h-px bg-white/5 mb-4" />

      <div className="flex items-center justify-between gap-3">
        <StarRating rating={h.rating || 4.5} />
        <button
          onClick={(e) => { e.stopPropagation(); onClick(); }}
          className="flex items-center gap-1.5 bg-sky-500/10 border border-sky-500/20 text-sky-400
                     px-4 py-2 rounded-xl text-xs font-medium whitespace-nowrap
                     transition-all duration-200 hover:bg-sky-500/20 hover:border-sky-400/40 hover:-translate-y-px"
        >
          View Details
          <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M17 8l4 4m0 0l-4 4m4-4H3" />
          </svg>
        </button>
      </div>
    </div>
  </div>
);

const Dashboard = () => {
  const [hospitals, setHospitals] = useState([]);
  const [search, setSearch]       = useState("");
  const [city, setCity]           = useState("");
  const [loading, setLoading]     = useState(false);
  const navigate = useNavigate();

  useEffect(() => { fetchHospitals(); }, []);

  const fetchHospitals = async () => {
    setLoading(true);
    try {
      const res = await api.get("/hospital/list", { params: { name: search, city } });
      setHospitals(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#090c12] text-slate-200 font-sans">
      <Navbar />

      {/* ── Hero ── */}
      <div className="relative max-w-5xl mx-auto px-6 pt-14 pb-10 overflow-hidden">
        {/* Ambient glow */}
        <div className="absolute -top-20 left-1/2 -translate-x-1/2 w-[600px] h-64 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />

        <span className="inline-block mb-4 px-3 py-1 text-[11px] font-medium tracking-[3px] uppercase text-sky-400 border border-sky-500/25 rounded-full bg-sky-500/5">
          Healthcare Directory
        </span>
        <h1 className="font-display text-4xl sm:text-5xl font-bold text-slate-50 leading-tight mb-3">
          Find the Right <span className="text-sky-400">Hospital</span>
          <br />Near You
        </h1>
        <p className="text-slate-500 text-sm font-light max-w-sm">
          Browse verified hospitals, compare ratings, and book with confidence.
        </p>
      </div>

      {/* ── Search Bar ── */}
      <div className="max-w-5xl mx-auto px-6 mb-10">
        <div
          className="flex items-center gap-3 bg-white/[0.03] border border-white/[0.08] rounded-2xl px-4 py-2.5
                     focus-within:border-sky-500/40 focus-within:shadow-[0_0_0_4px_rgba(14,165,233,0.06)]
                     transition-all duration-200"
        >
          <svg className="w-4 h-4 text-slate-600 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <circle cx="11" cy="11" r="8" /><path strokeLinecap="round" d="M21 21l-4.35-4.35" />
          </svg>

          <input
            type="text"
            placeholder="Hospital name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && fetchHospitals()}
            className="flex-1 bg-transparent border-none outline-none text-slate-200 text-sm placeholder:text-slate-600 py-2"
          />

          <div className="w-px h-7 bg-white/10 flex-shrink-0" />

          <svg className="w-4 h-4 text-slate-600 flex-shrink-0" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>

          <input
            type="text"
            placeholder="City..."
            value={city}
            onChange={(e) => setCity(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && fetchHospitals()}
            className="flex-1 bg-transparent border-none outline-none text-slate-200 text-sm placeholder:text-slate-600 py-2"
          />

          <button
            onClick={fetchHospitals}
            className="bg-gradient-to-r from-sky-500 to-blue-600 text-white text-sm font-medium
                       px-6 py-2.5 rounded-xl whitespace-nowrap
                       transition-all duration-200 hover:opacity-90 hover:-translate-y-px active:translate-y-0"
          >
            Search
          </button>
        </div>
      </div>

      {/* ── Results chips ── */}
      {!loading && hospitals.length > 0 && (
        <div className="max-w-5xl mx-auto px-6 mb-5 flex gap-2 flex-wrap">
          {city && (
            <span className="px-3 py-1.5 rounded-full text-xs border border-white/[0.08] bg-white/[0.02] text-slate-500">
              in <span className="text-slate-300 font-medium">{city}</span>
            </span>
          )}
          {search && (
            <span className="px-3 py-1.5 rounded-full text-xs border border-white/[0.08] bg-white/[0.02] text-slate-500">
              matching <span className="text-slate-300 font-medium">"{search}"</span>
            </span>
          )}
        </div>
      )}

      {/* ── Skeletons ── */}
      {loading && (
        <div className="max-w-5xl mx-auto px-6 pb-20 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {[...Array(6)].map((_, i) => <SkeletonCard key={i} delay={i * 80} />)}
        </div>
      )}

      {/* ── Grid ── */}
      {!loading && hospitals.length > 0 && (
        <div className="max-w-5xl mx-auto px-6 pb-20 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {hospitals.map((h, i) => (
            <HospitalCard
              key={h.id}
              h={h}
              index={i}
              onClick={() => navigate(`/hospital/${h.id}`)}
            />
          ))}
        </div>
      )}

      {/* ── Empty state ── */}
      {!loading && hospitals.length === 0 && (
        <div className="max-w-5xl mx-auto px-6 py-24 text-center">
          <div className="text-5xl mb-4 opacity-20">🏥</div>
          <p className="text-slate-600 text-sm">No hospitals found. Try a different search.</p>
        </div>
      )}
    </div>
  );
};

export default Dashboard;