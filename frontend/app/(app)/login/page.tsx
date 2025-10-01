"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    // Simple hardcoded login
    if (email === "ta@example.com" && password === "data2025") {
      localStorage.setItem("loggedIn", "true");
      router.push("/"); // redirect immediately
    } else {
      setError("Invalid credentials ❌");
      setPassword("");
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-white-200">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-6 rounded-2xl shadow-md w-80"
      >
         <img
        src="images/logo.png" 
        alt="App Logo" 
        className="mt-6 w-70 h-70 object-cover rounded-full mx-auto"
        />
        <h1 className="text-3xl font-bold mb-6 text-center">Log In</h1>

        {error && (
          <p className="mb-4 text-red-600 text-center font-semibold">{error}</p>
        )}

        <input
          type="email"
          placeholder="Email"
          className="w-full p-2 mb-4 border rounded"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          disabled={loading}
        />

        <input
          type="password"
          placeholder="Password"
          className="w-full p-2 mb-6 border rounded"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          disabled={loading}
        />

        <button
          type="submit"
          disabled={loading}
          className="w-full py-2 rounded text-white bg-[#7621FF] hover:bg-[#5a18cc] disabled:opacity-50 transition-colors duration-200"
        >
          {loading ? "Logging in..." : "Log In"}
        </button>
      </form>
     
    </div>
  );
}
