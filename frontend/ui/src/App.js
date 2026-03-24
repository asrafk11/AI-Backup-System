import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Logs from "./pages/Logs";
import Dashboard from "./pages/Dashboard";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login key={Date.now()} />} />
        <Route path="/logs" element={<Logs />} />
        <Route path="/dashboard" element={<Dashboard />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;