import React, { useEffect, useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

const Dashboard = () => {
  const [data, setData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = localStorage.getItem("token");

        // ❌ If not logged in
        if (!token) {
          navigate("/");
          return;
        }

        // ✅ NO UHID needed now
        const res = await api.get("/patient/history");

        setData(res.data.data);

      } catch (err) {
        console.error(err);

        // 🔥 Invalid token → logout
        if (err.response?.status === 400 || err.response?.status === 401) {
          localStorage.clear();
          navigate("/");
        }
      }
    };

    fetchData();
  }, [navigate]);

  if (!data)
    return <p className="text-center mt-10 text-lg">Loading...</p>;

  return (
    <div className="min-h-screen bg-gray-100 p-6">

      {/* Navbar */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Dashboard</h1>

        <button
          onClick={() => {
            localStorage.clear();
            navigate("/");
          }}
          className="bg-red-500 text-white px-4 py-2 rounded-lg"
        >
          Logout
        </button>
      </div>

      {/* Patient */}
      <div className="bg-white shadow-md rounded-xl p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Patient Details</h2>
        <p><b>Name:</b> {data.patient.name}</p>
        <p><b>Age:</b> {data.patient.age}</p>
        <p><b>Gender:</b> {data.patient.gender}</p>
      </div>

      {/* Records */}
      <div className="bg-white shadow-md rounded-xl p-6">
        <h2 className="text-xl font-semibold mb-4">Medical Records</h2>

        {data.medicalRecord.length === 0 ? (
          <p>No records found</p>
        ) : (
          data.medicalRecord.map((record, index) => (
            <div key={index} className="border p-4 mb-2 rounded-lg">
              <p><b>Diagnosis:</b> {record.diagnosis}</p>
              <p><b>Prescription:</b> {record.prescription}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default Dashboard;