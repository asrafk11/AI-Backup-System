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

      // 🔥 IMPORTANT FIX (trim)
      if (data.includes("SUCCESS")) {

        const dbData = {
          url: dbUrl,
          username,
          password
        };

        // 🔥 SAVE DB
        localStorage.setItem("db", JSON.stringify(dbData));

        console.log("Saved DB:", localStorage.getItem("db"));

        setMessage("✅ Connected Successfully");

        // smooth navigation
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

        <button
          onClick={() => {
            localStorage.setItem("db", JSON.stringify({
              url: "testdb",
              username: "test",
              password: "test"
            }));
            console.log("MANUAL SAVE DONE");
          }}
          className="w-full bg-gray-600 mt-2 p-2 rounded"
        >
          TEST SAVE
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