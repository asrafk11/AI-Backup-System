import { useState } from "react";
import { useNavigate } from "react-router-dom";

function App() {
  const [dbUrl, setDbUrl] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            url: dbUrl,
            username: username,
            password: password,
          }),
        });

        const data = await res.text();

        if (data === "SUCCESS") {
          setMessage("✅ Connected Successfully");
          navigate("/dashboard");
        } else {
          setMessage("❌ Connection Failed");
        }

        } catch (err) {
          setMessage("❌ Server Error");
        }

        setLoading(false);
    };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900 text-white">

      <div className="bg-gray-800 p-8 rounded-2xl shadow-lg w-96">

        <h2 className="text-2xl font-bold mb-6 text-center">
          DB Connection Login
        </h2>

        <input
          type="text"
          placeholder="Database URL"
          value={dbUrl}
          onChange={(e) => setDbUrl(e.target.value)}
          className="w-full p-2 mb-4 rounded bg-gray-700 focus:outline-none"
        />

        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="w-full p-2 mb-4 rounded bg-gray-700 focus:outline-none"
        />

        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full p-2 mb-4 rounded bg-gray-700 focus:outline-none"
        />

        <button
          onClick={handleConnect}
          className="w-full bg-blue-600 hover:bg-blue-700 p-2 rounded mt-2"
        >
          Connect
        </button>

        {message && (
          <p className="mt-4 text-center text-sm">{message}</p>
        )}

      </div>

    </div>
  );
}

export default App;