import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../api/axios";
import Navbar from "../components/Navbar";

const HospitalDetails = () => {
  const { id } = useParams();

  const [hospital, setHospital] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [tab, setTab] = useState("overview");
  const [currentImage, setCurrentImage] = useState(0);

  useEffect(() => {
    fetchHospital();
    fetchDoctors();
  }, []);

  const fetchHospital = async () => {
    const res = await api.get(`/hospital/${id}`);
    setHospital(res.data.data);
  };

  const fetchDoctors = async () => {
    const res = await api.get(`/doctor/by-hospital/${id}`);
    setDoctors(res.data.data);
  };

  if (!hospital) return <p className="text-center mt-10">Loading...</p>;

  return (
    <>
      <Navbar />
      <div className="bg-gray-50 min-h-screen">
        {/* 🔥 HEADER (SLIDER + DETAILS) */}
        <div className="bg-white p-6 shadow-sm">
          <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-6">
            {/* 🖼️ LEFT → IMAGE SLIDER */}
            <div>
              {/* Main Image */}
              <img
                src={hospital.imageUrls?.[currentImage]}
                onError={(e) => (e.target.src = "https://picsum.photos/600")}
                className="w-full h-72 object-cover rounded-xl"
              />

              {/* Thumbnails */}
              <div className="flex gap-2 mt-3">
                {hospital.imageUrls?.map((img, i) => (
                  <img
                    key={i}
                    src={img}
                    onClick={() => setCurrentImage(i)}
                    className={`w-20 h-20 object-cover rounded cursor-pointer border ${currentImage === i ? "border-blue-500" : ""}`}
                  />
                ))}
              </div>
            </div>

            {/* 📋 RIGHT → DETAILS */}
            <div className="flex flex-col justify-between">
              <div>
                <h1 className="text-2xl font-bold">{hospital.name}</h1>

                <p className="text-gray-500 text-sm">
                  Multi-speciality Hospital
                </p>

                <p className="text-gray-600 mt-1">{hospital.city}</p>
                <p className="text-gray-600 text-sm">{hospital.address}</p>

                {/* Rating */}
                <div className="flex items-center gap-3 mt-3">
                  <span className="bg-green-500 text-white px-2 py-1 rounded text-sm">
                    {hospital.rating || 4.5} ★
                  </span>
                  <span className="text-gray-500 text-sm">
                    ({hospital.totalReviews || 200} reviews)
                  </span>
                </div>

                {/* Availability */}
                <p className="mt-2 text-green-600 font-medium">
                  {hospital.isOpen24x7 ? "Open 24x7" : "Closed"}
                </p>

                {/* Services (preview) */}
                <div className="flex flex-wrap gap-2 mt-4">
                  {hospital.services?.slice(0, 6).map((s, i) => (
                    <span
                      key={i}
                      className="bg-blue-100 text-blue-700 px-3 py-1 rounded-full text-sm"
                    >
                      {s}
                    </span>
                  ))}
                </div>
              </div>

              {/* CTA */}
              <button className="mt-6 bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700">
                📅 Book Appointment
              </button>
            </div>
          </div>
        </div>

        {/* 🔥 TABS */}
        <div className="bg-white border-t">
          <div className="max-w-6xl mx-auto flex gap-6 px-6">
            {["overview", "doctors", "services"].map((t) => (
              <button
                key={t}
                onClick={() => setTab(t)}
                className={`py-3 capitalize ${
                  tab === t
                    ? "border-b-2 border-blue-600 text-blue-600"
                    : "text-gray-500"
                }`}
              >
                {t} {t === "doctors" && `(${doctors.length})`}
              </button>
            ))}
          </div>
        </div>

        <div className="max-w-6xl mx-auto p-6">
          {/* 🔥 OVERVIEW */}
          {tab === "overview" && (
            <div className="bg-white p-6 rounded-xl shadow-sm">
              <h2 className="font-semibold mb-4">About {hospital.name}</h2>

              <p className="text-gray-600 mb-4">
                {hospital.name} is one of the best hospitals in {hospital.city}.
                It provides top-quality healthcare services.
              </p>

              <h3 className="font-semibold mb-2">Specializations</h3>
              <div className="flex flex-wrap gap-2">
                {hospital.specializations?.map((s, i) => (
                  <span
                    key={i}
                    className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-sm"
                  >
                    {s}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* 🔥 DOCTORS */}
          {tab === "doctors" && (
            <div className="grid md:grid-cols-2 gap-5">
              {doctors.map((doc) => (
                <div
                  key={doc.id}
                  className="bg-white p-5 rounded-xl shadow-sm flex justify-between"
                >
                  <div>
                    <h3 className="text-blue-600 font-semibold text-lg">
                      {doc.name}
                    </h3>
                    <p className="text-gray-600 text-sm">
                      {doc.specialization}
                    </p>
                    <p className="text-gray-500 text-sm">
                      {doc.experience} yrs exp
                    </p>
                  </div>

                  <button className="bg-blue-600 text-white px-4 py-2 rounded-lg h-fit">
                    Book
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* 🔥 SERVICES */}
          {tab === "services" && (
            <div className="bg-white p-6 rounded-xl shadow-sm">
              <h2 className="font-semibold mb-4">Services</h2>

              <div className="grid md:grid-cols-3 gap-4">
                {hospital.services?.map((s, i) => (
                  <div
                    key={i}
                    className="bg-gray-50 border p-3 rounded-lg text-gray-700"
                  >
                    • {s}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default HospitalDetails;
