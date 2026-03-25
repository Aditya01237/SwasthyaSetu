import React, { useEffect, useState } from "react";
import api from "../api/axios";
import Navbar from "../components/Navbar";
import { useNavigate } from "react-router-dom";

const Dashboard = () => {
  const [hospitals, setHospitals] = useState([]);
  const [search, setSearch] = useState("");
  const [city, setCity] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    fetchHospitals();
  }, []);

  const fetchHospitals = async () => {
    try {
      const res = await api.get("/hospital/list", {
        params: {
          name: search,
          city: city,
        },
      });
      setHospitals(res.data.data);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <>
      <Navbar />
      <div className="p-6 max-w-6xl mx-auto">
        {/* 🔍 Filters */}
        <div className="flex flex-wrap gap-4 mb-6">
          <input
            type="text"
            placeholder="Search hospital..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="p-3 border rounded-lg w-full md:w-1/3"
          />

          <input
            type="text"
            placeholder="City..."
            value={city}
            onChange={(e) => setCity(e.target.value)}
            className="p-3 border rounded-lg w-full md:w-1/3"
          />

          <button
            onClick={fetchHospitals}
            className="bg-blue-600 text-white px-6 rounded-lg"
          >
            Search
          </button>
        </div>

        {/* 🏥 Hospitals Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {hospitals.map((h) => (
            <div
              key={h.id}
              onClick={() => navigate(`/hospital/${h.id}`)}
              className="bg-white rounded-xl shadow hover:shadow-lg transition overflow-hidden cursor-pointer"
            >
              {/* Image */}
              <img
                src={h.imageUrls?.[0] || "https://via.placeholder.com/400"}
                alt={h.name}
                className="w-full h-40 object-cover"
              />

              <div className="p-4">
                <h2 className="text-lg font-semibold">{h.name}</h2>

                <p className="text-gray-500 text-sm">{h.city}</p>

                <p className="text-sm mt-1">{h.address}</p>

                <p className="text-yellow-500 mt-2">⭐ {h.rating || 4.5}</p>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    navigate(`/hospital/${h.id}`);
                  }}
                  className="mt-3 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
                >
                  View Details
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};

export default Dashboard;
