import { useEffect, useState } from "react";
import api from "../api/axios";
import Navbar from "../components/Navbar";

const QrAudit = () => {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    fetchLogs();
  }, []);

  const fetchLogs = async () => {
    try {
      const res = await api.get("/patient/qr-audit");
      setLogs(res.data.data);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <>
      <Navbar />

      <div className="p-6 max-w-3xl mx-auto">
        <h1 className="text-2xl font-bold mb-6">QR Audit Logs</h1>

        {logs.length === 0 ? (
          <p className="text-gray-500">No activity yet</p>
        ) : (
          logs.map((log, index) => (
            <div
              key={index}
              className="bg-white p-4 mb-3 rounded-lg shadow hover:shadow-md transition"
            >
              <p><span className="font-semibold">Doctor:</span> {log.doctorName}</p>
              <p><span className="font-semibold">Action:</span> {log.action}</p>
              <p className="text-sm text-gray-500">
                {new Date(log.timestamp).toLocaleString()}
              </p>
            </div>
          ))
        )}
      </div>
    </>
  );
};

export default QrAudit;