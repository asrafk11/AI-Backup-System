import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Dashboard() {

  const [logs, setLogs] = useState([]);
  const [actionMessage, setActionMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  // 🔹 Logout
  const handleLogout = () => {
    navigate("/", { replace: true });
  };

  // 🔹 Backup
  const handleBackup = async () => {
    try {
      setLoading(true);

      const res = await fetch("http://localhost:8080/backup");
      const data = await res.text();

      setActionMessage(data);
      fetchLogs();

    } catch (err) {
      setActionMessage("Backup Failed ❌");
    }

    setLoading(false);
  };

  // 🔹 Restore
  const handleRestore = async () => {
    try {
      setLoading(true);

      const res = await fetch("http://localhost:8080/restore?file=latest");
      const data = await res.text();

      setActionMessage(data);
      fetchLogs();

    } catch (err) {
      setActionMessage("Restore Failed ❌");
    }

    setLoading(false);
  };

  // 🔹 Fetch Logs
  const fetchLogs = async () => {
    try {
      const res = await fetch("http://localhost:8080/logs");
      const data = await res.json();

      // sort latest first (important)
      const sorted = data.sort((a, b) =>
        new Date(b.timestamp) - new Date(a.timestamp)
      );

      setLogs(sorted);

    } catch (err) {
      console.log("Error fetching logs");
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-black text-white p-6">

      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">🚀 AI Backup Dashboard</h1>

        <div className="flex items-center gap-4">
          <span className="text-green-400">● Online</span>

          <button
            onClick={handleLogout}
            className="bg-red-600 hover:bg-red-700 px-4 py-1 rounded"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

        {/* DB Status */}
        <div className="bg-gray-800/70 p-6 rounded-2xl shadow hover:scale-105 transition">
          <h2 className="text-lg mb-2">Database Status</h2>
          <p className="text-green-400 text-xl font-bold">Connected ✅</p>
        </div>

        {/* Last Backup */}
        <div className="bg-gray-800/70 p-6 rounded-2xl shadow hover:scale-105 transition">
          <h2 className="text-lg mb-2">Last Backup</h2>
          <p className="text-sm text-gray-300">
            {logs.length > 0
              ? logs[0].message.substring(0, 60) + "..."
              : "No Backup Yet"}
          </p>
        </div>

        {/* Total Logs */}
        <div className="bg-gray-800/70 p-6 rounded-2xl shadow hover:scale-105 transition">
          <h2 className="text-lg mb-2">Total Logs</h2>
          <p
            onClick={() => navigate("/logs")}
            className="text-blue-400 text-xl font-bold cursor-pointer hover:underline"
          >
            {logs.length}
          </p>
        </div>

      </div>

      {/* Actions */}
      <div className="mt-10">
        <h2 className="text-xl mb-4 font-semibold">Actions</h2>

        <div className="flex gap-4">
          <button
            onClick={handleBackup}
            disabled={loading}
            className="bg-blue-600 hover:bg-blue-700 px-6 py-2 rounded-xl disabled:opacity-50"
          >
            {loading ? "Processing..." : "💾 Backup Now"}
          </button>

          <button
            onClick={handleRestore}
            disabled={loading}
            className="bg-green-600 hover:bg-green-700 px-6 py-2 rounded-xl disabled:opacity-50"
          >
            {loading ? "Processing..." : "♻️ Restore Latest"}
          </button>
        </div>

        {/* Action Message */}
        {actionMessage && (
          <p className={`mt-4 ${
            actionMessage.includes("Failed")
              ? "text-red-400"
              : "text-green-400"
          }`}>
            {actionMessage}
          </p>
        )}
      </div>

      {/* Recent Activity */}
      <div className="mt-10">

        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">Recent Activity</h2>

          <button
            onClick={() => navigate("/logs")}
            className="text-blue-400 hover:underline text-sm"
          >
            View All Logs →
          </button>
        </div>

        <div className="bg-gray-800/70 p-5 rounded-2xl shadow space-y-4">

          {logs.length === 0 ? (
            <p className="text-gray-400">No activity yet 🚀</p>
          ) : (
            logs.slice(0, 3).map((log, index) => (
              <div
                key={index}
                className="flex justify-between items-center border-b border-gray-700 pb-3 last:border-none"
              >
                {/* Left */}
                <div className="flex flex-col">
                  <span className="text-sm font-medium text-white">
                    {log.message}
                  </span>

                  <span className="text-xs text-gray-400 mt-1">
                    {log.timestamp
                      ? new Date(log.timestamp).toLocaleString()
                      : "Just now"}
                  </span>
                </div>

                {/* Status */}
                <span
                  className={`text-xs font-semibold px-3 py-1 rounded-full
                  ${
                    log.status === "SUCCESS"
                      ? "bg-green-500/20 text-green-400"
                      : "bg-red-500/20 text-red-400"
                  }`}
                >
                  {log.status}
                </span>
              </div>
            ))
          )}

        </div>
      </div>

    </div>
  );
}

export default Dashboard;