import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Dashboard() {

  const [logs, setLogs] = useState([]);
  const [backupFiles, setBackupFiles] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  const [actionMessage, setActionMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const [showModal, setShowModal] = useState(false);
  const [time, setTime] = useState("02:00");
  const [days, setDays] = useState(5);
  const [scheduleText, setScheduleText] = useState("No schedule applied ❌");

  const navigate = useNavigate();

  const db = JSON.parse(localStorage.getItem("db"));

  const dbName = db?.url
    ? db.url.substring(db.url.lastIndexOf("/") + 1)
    : "Not Connected";

  // 🔹 Logout
  const handleLogout = () => {
    localStorage.removeItem("db");
    navigate("/", { replace: true });
  };

  // 🔹 Backup
  const handleBackup = async () => {
    try {
      setLoading(true);

      if (!db) {
        alert("No database connected ❌");
        return;
      }

      const res = await fetch("http://localhost:8080/api/backup", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(db)
      });

      const data = await res.text();

      setActionMessage(
        data.trim() === "SUCCESS"
          ? "Backup Created Successfully 🚀"
          : "Backup Failed ❌"
      );

      fetchLogs();
      fetchBackupFiles();

    } catch {
      setActionMessage("Server Error ❌");
    }

    setLoading(false);
  };

  // 🔹 Restore
  const handleRestore = async () => {
    try {
      setLoading(true);

      const res = await fetch("http://localhost:8080/api/restore", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(db)
      });

      const data = await res.text();

      setActionMessage(
        data.trim() === "SUCCESS"
          ? "Restore Successful 🚀"
          : data.trim() === "NO_BACKUP"
          ? "No backup found ❌"
          : "Restore Failed ❌"
      );

      fetchLogs();

    } catch {
      setActionMessage("Server Error ❌");
    }

    setLoading(false);
  };

  // 🔹 Fetch Logs
  const fetchLogs = async () => {
    try {
      const res = await fetch("http://localhost:8080/logs");
      const data = await res.json();

      const sorted = data.sort(
        (a, b) => new Date(b.timestamp) - new Date(a.timestamp)
      );

      setLogs(sorted);
    } catch {
      console.log("Error fetching logs");
    }
  };

  // 🔹 Fetch Backup Files
  const fetchBackupFiles = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/backups");
      const data = await res.json();
      setBackupFiles(data);
    } catch {
      console.log("Error fetching files");
    }
  };

  // 🔹 Schedule (UPDATED 🔥)
  const handleSchedule = async () => {
    if (days < 1) {
      setActionMessage("Days must be at least 1 ❌");
      return;
    }

    const [hour, minute] = time.split(":");
    const cron = `0 ${minute} ${hour} */${days} * ?`;

    try {
      await fetch("http://localhost:8080/schedule", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: cron
      });

      const formattedTime = new Date(`1970-01-01T${time}`)
        .toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

      setScheduleText(`Every ${days} days at ${formattedTime} ✅`);
      setActionMessage("Schedule updated successfully ");
      setShowModal(false);

    } catch {
      setActionMessage("Schedule failed ");
    }
  };

  useEffect(() => {
    fetchLogs();
    fetchBackupFiles();
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-black text-white p-6">

      {/* HEADER */}
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">🚀 AI Backup Dashboard</h1>

        <div className="flex items-center gap-4">
          <button onClick={() => setShowHistory(true)} className="bg-blue-600 px-4 py-1 rounded">
            📜 History
          </button>

          <button onClick={handleLogout} className="bg-red-600 px-4 py-1 rounded">
            Logout
          </button>
        </div>
      </div>

      {/* CARDS */}
      <div className="grid md:grid-cols-3 gap-6">

        <div className="bg-gray-800/70 p-6 rounded-2xl">
          <h2 className="text-lg mb-2">Database</h2>
          <p className="text-green-400 text-xl font-bold">Connected ✅</p>
          <p className="text-sm text-gray-400 mt-2">DB: {dbName}</p>
        </div>

        <div className="bg-gray-800/70 p-6 rounded-2xl">
          <h2 className="text-lg mb-2">Last Backup</h2>
          <p className="text-sm text-gray-300">
            {logs.length ? logs[0].message : "No backup yet"}
          </p>
        </div>

        <div className="bg-gray-800/70 p-6 rounded-2xl">
          <h2 className="text-lg mb-2">Total Logs</h2>
          <p onClick={() => navigate("/logs")} className="text-blue-400 cursor-pointer">
            {logs.length}
          </p>
        </div>

      </div>

      {/* 🔥 SCHEDULE STATUS */}
      <div className="mt-6 bg-gray-800/70 p-4 rounded-xl">
        <h2 className="text-lg mb-2">⏰ Schedule Status</h2>
        <p className="text-yellow-400">{scheduleText}</p>
      </div>

      {/* ACTIONS */}
      <div className="mt-10">
        <h2 className="text-xl mb-4">Actions</h2>

        <div className="flex gap-4">
          <button onClick={handleBackup} className="bg-blue-600 px-6 py-2 rounded-xl">
            💾 Backup
          </button>

          <button onClick={handleRestore} className="bg-green-600 px-6 py-2 rounded-xl">
            ♻ Restore
          </button>

          <button onClick={() => setShowModal(true)} className="bg-yellow-500 px-6 py-2 rounded-xl">
            ⏰ Schedule
          </button>
        </div>
      </div>

      {/* MESSAGE */}
      {actionMessage && (
        <div className="mt-6 bg-gray-800 p-3 rounded text-center">
          {actionMessage}
        </div>
      )}

      {/* BACKUP FILES */}
      <div className="mt-10">
        <h2 className="text-xl mb-4">Backup Files</h2>

        <div className="bg-gray-800 p-5 rounded-2xl">
          {backupFiles.length === 0 ? (
            <p>No backups yet. Click Backup 🚀</p>
          ) : (
            backupFiles.map((file, i) => (
              <div key={i} className="flex justify-between border-b py-2">
                <span>{file}</span>
                <span className="text-green-400 text-xs">Available</span>
              </div>
            ))
          )}
        </div>
      </div>

      {/* 🔥 SCHEDULE MODAL (IMPROVED UI) */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60">
          <div className="bg-gray-900 p-6 rounded-xl w-80">

            <h2 className="mb-4 text-lg">⏰ Schedule Backup</h2>

            <label className="text-sm text-gray-400">Select Time</label>
            <input
              type="time"
              value={time}
              onChange={(e) => setTime(e.target.value)}
              className="w-full p-2 bg-gray-800 mb-3 rounded"
            />

            <label className="text-sm text-gray-400">Repeat Every (Days)</label>
            <input
              type="number"
              min="1"
              value={days}
              onChange={(e) => setDays(e.target.value)}
              className="w-full p-2 bg-gray-800 rounded"
            />

            <p className="text-sm text-gray-400 mt-3">
              Every {days} days at {time}
            </p>

            <div className="flex justify-end gap-2 mt-4">
              <button onClick={() => setShowModal(false)}>Cancel</button>
              <button onClick={handleSchedule} className="bg-yellow-500 px-3 py-1 rounded">
                Save
              </button>
            </div>

          </div>
        </div>
      )}

      {/* 🔥 HISTORY POPUP */}
      {showHistory && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60 z-50">
          <div className="bg-gray-900 p-6 rounded-xl w-[500px] max-h-[400px] overflow-y-auto">

            <div className="flex justify-between mb-4">
              <h2 className="text-lg">📜 Backup History</h2>
              <button onClick={() => setShowHistory(false)}>❌</button>
            </div>

            {logs.length === 0 ? (
              <p>No history available</p>
            ) : (
              logs.map((log, i) => (
                <div key={i} className="border-b py-2">
                  <p>{log.message}</p>
                  <p className="text-xs text-gray-400">
                    {new Date(log.timestamp).toLocaleString()}
                  </p>
                  <span className="text-green-400 text-xs">{log.status}</span>
                </div>
              ))
            )}

          </div>
        </div>
      )}

    </div>
  );
}

export default Dashboard;