import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Dashboard() {

  const [logs, setLogs] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [showAll, setShowAll] = useState(false);
  const [backupFiles, setBackupFiles] = useState([]);
  const [dbUrl, setDbUrl] = useState("");
  const [dbUsername, setDbUsername] = useState("");
  const [dbPassword, setDbPassword] = useState("");
  const [toast, setToast] = useState("");
  const [showHistory, setShowHistory] = useState(false);
  const [actionMessage, setActionMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [deleteId, setDeleteId] = useState(null);

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

  const saveConfig = async () => {

    if (!db || !db.url || !db.username || !db.password) {
      alert("DB details missing ❌");
      return;
    }

    const [hour, minute] = time.split(":");
    const cron = `0 ${minute} ${hour} */${days} * ?`;

    const data = {
      url: db.url,
      username: db.username,
      password: db.password,
      cronExpression: cron
    };

    try {
      const res = await fetch("http://localhost:8080/api/save-config", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
      });

      console.log(await res.text());
      alert("Config Saved ✅");

    } catch (err) {
      console.error(err);
      alert("Error ❌");
    }
  };

  // 🔹 Backup (UPDATED WITH TOAST 🔥)
  const handleBackup = async () => {
    try {
      setToast("⏳ Backup Started...");

      const res = await fetch("http://localhost:8080/api/backup", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(db)
      });

      const data = await res.text();

      if (res.ok) {
        setToast("✅ Backup Completed");
      } else {
        setToast("❌ Backup Failed");
      }

      fetchLogs();
      fetchBackupFiles();

    } catch {
      setToast("❌ Server Error");
    }

    setTimeout(() => setToast(""), 3000);
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

  // 🔹 Fetch Schedules 🔥
  const fetchSchedules = async () => {
    try {
      const res = await fetch("http://localhost:8080/schedules");
      const data = await res.json();
      setSchedules(data);
    } catch {
      console.log("Error fetching schedules");
    }
  };

  // 🔹 Schedule
  const handleSchedule = async () => {
   console.log("STEP 1: Schedule clicked");
    if (days < 1) {
      setActionMessage("Days must be at least 1 ❌");
      return;
    }

    const [hour, minute] = time.split(":");
    const cron = `0 ${minute} ${hour} */${days} * ?`;

    try {
      // 🔥 DON'T BLOCK HERE
      await fetch("http://localhost:8080/schedule", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          dbUrl,
          dbUsername,
          dbPassword,
          cronExpression: cron
        })
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
    fetchSchedules();
  }, []);

  const latestSchedule =
    schedules.length > 0 ? schedules[schedules.length - 1] : null;


    const formatCron = (cron) => {
      try {
        const parts = cron.split(" ");

        const minute = parts[1];
        const hour = parts[2];
        const days = parts[3].replace("*/", "");

        const time = new Date(`1970-01-01T${hour}:${minute}`)
          .toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit"
          });

        return `Every ${days} days at ${time}`;
      } catch {
        return cron;
      }
    };
    // 🟢 Click Cancel → open popup only
    const deleteSchedule = (id) => {
      setDeleteId(id);
    };

    // 🟢 Confirm Delete (safe + debug)
    const confirmDelete = async () => {
      if (!deleteId) {
        alert("Invalid schedule ❌");
        return;
      }

      try {
        console.log("Deleting ID:", deleteId); // debug

        const res = await fetch(`http://localhost:8080/schedule/${deleteId}`, {
          method: "DELETE"
        });

        if (res.status !== 200) {
          throw new Error("Delete failed");
        }

        setDeleteId(null); // close popup
        fetchSchedules(); // refresh UI

      } catch (err) {
        console.error(err);
        alert("Delete failed ❌");
      }
    };

    // 🟢 Cancel Delete (close popup)
    const cancelDelete = () => {
      setDeleteId(null);
    };

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

      {/* SCHEDULE STATUS */}
      <div className="mt-6 bg-gray-800/70 p-4 rounded-xl">
        <h2 className="text-lg mb-2">⏰ Schedule Status</h2>
        <p className="text-yellow-400">
          {latestSchedule
            ? formatCron(latestSchedule.cronExpression)
            : "No schedule applied ❌"}
        </p>

        <button
          onClick={() => setShowAll(!showAll)}
          className="bg-blue-600 px-4 py-1 rounded mt-2"
        >
          {showAll ? "Hide Schedules" : "Show All Schedules"}
        </button>

        {showAll && (
          <div className="mt-3">
            {schedules.map((s) => (
              <div key={s.id} className="bg-gray-700 p-3 rounded mb-2">
                <p>🕒 {formatCron(s.cronExpression)}</p>
                <p>Status: {s.active ? "🟢 ON" : "🔴 OFF"}</p>
                <button
                  onClick={() => deleteSchedule(s.id)}
                  className="bg-red-500 px-2 py-1 rounded mt-2"
                >
                  ❌ Cancel
                </button>
              </div>
            ))}
          </div>
        )}
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

        <p className="text-sm text-gray-400 mt-2">
          💾 Backup will be saved in <span className="text-yellow-400">C:/backup</span>
        </p>
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

      {/* SCHEDULE MODAL */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60">
          <div className="bg-gray-900 p-6 rounded-xl w-80">

            <h2 className="mb-4 text-lg">⏰ Schedule Backup</h2>

            <input
              type="time"
              value={time}
              onChange={(e) => setTime(e.target.value)}
              className="w-full p-2 bg-gray-800 mb-3 rounded"
            />

            <input
              type="number"
              min="1"
              value={days}
              onChange={(e) => setDays(e.target.value)}
              className="w-full p-2 bg-gray-800 rounded"
            />

            <div className="flex justify-end gap-2 mt-4">
              <button onClick={() => setShowModal(false)}>Cancel</button>
              <button onClick={handleSchedule} className="bg-yellow-500 px-3 py-1 rounded">
                Save
              </button>
            </div>

          </div>
        </div>
      )}

      {/* HISTORY MODAL */}
      {showHistory && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/70 z-50">

          <div className="bg-gray-900 rounded-2xl w-[800px] h-[500px] shadow-lg flex flex-col">

            <div className="flex justify-between items-center p-4 border-b border-gray-700">
              <h2 className="text-xl font-semibold">📜 Backup History</h2>
              <button onClick={() => setShowHistory(false)} className="text-red-400 text-lg">✖</button>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {logs.map((log, i) => (
                <div key={i} className="bg-gray-800 p-4 rounded-xl border border-gray-700">
                  <p>{log.message}</p>
                  <p className="text-sm text-gray-400">
                    {log.timestamp ? new Date(log.timestamp).toLocaleString() : "⏳ Old Record"}
                  </p>
                </div>
              ))}
            </div>

          </div>

        </div>
      )}

      {/* 🔥 TOAST (FINAL POSITION) */}
      {toast && (
        <div className={`fixed bottom-5 right-5 px-4 py-2 rounded-lg shadow-lg text-sm
          ${toast.includes("Completed") ? "bg-green-600" :
            toast.includes("Failed") ? "bg-red-600" :
            "bg-gray-800"}
        `}>
          {toast}
        </div>
      )}

      {deleteId && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60 z-50">

          <div className="bg-gray-900 p-6 rounded-xl w-80 text-center shadow-lg">

            <h2 className="text-lg mb-4">⚠ Delete Schedule?</h2>
            <p className="text-sm text-gray-400 mb-5">
              Are you sure you want to delete this schedule?
            </p>

            <div className="flex justify-center gap-4">
              <button
                onClick={confirmDelete}
                className="bg-red-500 px-4 py-1 rounded"
              >
                Yes
              </button>

              <button
                onClick={cancelDelete}
                className="bg-gray-600 px-4 py-1 rounded"
              >
                No
              </button>
            </div>

          </div>

        </div>
      )}

    </div>
  );
}

export default Dashboard;