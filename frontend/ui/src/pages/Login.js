import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

function Login() {

  const [dbUrl, setDbUrl] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    setDbUrl("");
    setUsername("");
    setPassword("");
    setMessage("");
  }, []);

  const handleConnect = async () => {

    if (!dbUrl || !username || !password) {
      setMessage("❌ Please fill all fields");
      return;
    }

    setLoading(true);
    setMessage("");

    try {
      // 🔹 STEP 1: TEST CONNECTION
      const res = await fetch("http://localhost:8080/api/connect", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          url: dbUrl,
          username,
          password
        })
      });

      const data = await res.text();
      console.log("API RESPONSE:", data);

      if (data.includes("SUCCESS")) {

        // 🔥 STEP 2: SAFE PARSE
        const cleanUrl = dbUrl.replace("jdbc:", "");
        const url = new URL(cleanUrl);

        const host = url.hostname;
        const port = url.port;
        const dbName = url.pathname.replace("/", "");
        const dbType = dbUrl.includes("postgresql") ? "postgres" : "mysql";

        // 🔥 FIXED USER ID (IMPORTANT)
        let userId = localStorage.getItem("userId");

        if (!userId) {
          userId = crypto.randomUUID(); // ✅ correct UUID
          localStorage.setItem("userId", userId);
        }

        console.log("PARSED:", { host, port, dbName, dbType, userId });

        // 🔥 STEP 3: SAVE TO BACKEND
        const saveRes = await fetch("http://localhost:8080/api/add-db", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            dbName,
            dbType,
            host,
            port,
            username,
            password,
            userId
          })
        });

        console.log("SAVE RESPONSE:", await saveRes.text());

        // 🔥 STEP 4: SAVE LOCALLY
        localStorage.setItem("db", JSON.stringify({
          url: dbUrl,
          username,
          password
        }));

        setMessage("✅ Connected & Saved Successfully");

        setTimeout(() => {
          navigate("/dashboard");
        }, 800);

      } else {
        setMessage("❌ Connection Failed");
      }

    } catch (err) {
      console.log(err);
      setMessage("❌ Server Error");
    }

    setLoading(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900 text-white">

      <div className="bg-gray-800 p-8 rounded-2xl shadow-lg w-96">

        <h2 className="text-2xl font-bold mb-2 text-center">
          🔗 Connect Database
        </h2>

        <p className="text-gray-400 text-sm text-center mb-6">
          Enter your database credentials
        </p>

        <input
          type="text"
          placeholder="DB URL (jdbc:postgresql://localhost:5432/dbname)"
          value={dbUrl}
          onChange={(e) => setDbUrl(e.target.value)}
          className="w-full p-3 mb-4 rounded bg-gray-700 focus:outline-none"
        />

        <input
          type="text"
          placeholder="Database Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="w-full p-3 mb-4 rounded bg-gray-700 focus:outline-none"
        />

        <input
          type="password"
          placeholder="Database Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full p-3 mb-4 rounded bg-gray-700 focus:outline-none"
        />

        <button
          onClick={handleConnect}
          disabled={loading}
          className="w-full bg-blue-600 hover:bg-blue-700 p-3 rounded mt-2 disabled:opacity-50"
        >
          {loading ? "Connecting..." : "Connect Database"}
        </button>

        {message && (
          <p className={`mt-4 text-center text-sm ${
            message.includes("❌") ? "text-red-400" : "text-green-400"
          }`}>
            {message}
          </p>
        )}

      </div>

    </div>
  );
}

export default Login;