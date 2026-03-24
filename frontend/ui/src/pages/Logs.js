import { useEffect, useState } from "react";

function Logs() {

  const [logs, setLogs] = useState([]);

  const fetchLogs = async () => {
    try {
      const res = await fetch("http://localhost:8080/logs");
      const data = await res.json();
      setLogs(data);
    } catch (err) {
      console.log("Error fetching logs");
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  return (
    <div className="min-h-screen bg-gray-900 text-white p-6">

      <h1 className="text-2xl font-bold mb-6">📜 All Backup Logs</h1>

      <div className="bg-gray-800 p-4 rounded-xl">

        {logs.length === 0 ? (
          <p>No logs available</p>
        ) : (
          logs.map((log, index) => (
            <div
              key={index}
              className="border-b border-gray-600 py-2 flex justify-between"
            >
              <span>{log.message}</span>
              <span className={log.status === "SUCCESS" ? "text-green-400" : "text-red-400"}>
                {log.status}
              </span>
            </div>
          ))
        )}

      </div>

    </div>
  );
}

export default Logs;