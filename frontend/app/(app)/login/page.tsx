"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import axios, { AxiosError } from "axios";

const api = axios.create({
  baseURL: "http://localhost:9090",
  timeout: 10000,
  // withCredentials: true, // uncomment if your API sets cookies
  headers: { "Content-Type": "application/json" },
});

export default function LoginPage() {
  const router = useRouter();

  const [isSignUp, setIsSignUp] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");

  // login fields
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  // signup fields
  const [suUsername, setSuUsername] = useState("");
  const [suPassword, setSuPassword] = useState("");

  const extractErr = (err: unknown) => {
    const ax = err as AxiosError<any>;
    return (
      (ax.response?.data && (ax.response.data.message || ax.response.data.error)) ||
      ax.message ||
      "Request failed."
    );
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(""); setInfo(""); setLoading(true);
    try {
      const { data } = await api.post("/api/users/login", {
        username: username.trim(),
        password,
      });
      if (data?.token) localStorage.setItem("authToken", data.token);
      localStorage.setItem("loggedIn", "true");
      router.push("/home");
    } catch (err) {
      setError(extractErr(err));
      setPassword("");
    } finally {
      setLoading(false);
    }
  };

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(""); setInfo(""); setLoading(true);

    if (!suUsername.trim()) {
      setError("Please enter a username.");
      setLoading(false);
      return;
    }

    try {
      await api.post("/api/users/signup", {
        username: suUsername.trim(),
        password: suPassword,
      });

      // Go back to login and prefill
      setUsername(suUsername.trim());
      setPassword("");
      setInfo("Account created ✅ Please log in.");
      setIsSignUp(false);
    } catch (err) {
      setError(extractErr(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-white-200">
      <form
        onSubmit={isSignUp ? handleSignUp : handleLogin}
        className="bg-white p-6 rounded-2xl shadow-md w-80"
      >
        <img
          src="images/logo.png"
          alt="App Logo"
          className="mt-6 w-70 h-70 object-cover rounded-full mx-auto"
        />
        <h1 className="text-3xl font-bold mb-6 text-center">
          {isSignUp ? "Sign Up" : "Log In"}
        </h1>

        {(error || info) && (
          <p className={`mb-4 text-center font-semibold ${error ? "text-red-600" : "text-green-600"}`}>
            {error || info}
          </p>
        )}

        {isSignUp ? (
          <>
            <input
              type="text"
              placeholder="Username"
              className="w-full p-2 mb-4 border rounded"
              value={suUsername}
              onChange={(e) => setSuUsername(e.target.value)}
              required
              disabled={loading}
            />
            <input
              type="password"
              placeholder="Password (min 6 chars)"
              className="w-full p-2 mb-6 border rounded"
              value={suPassword}
              onChange={(e) => setSuPassword(e.target.value)}
              required
              disabled={loading}
            />
            <button
              type="submit"
              disabled={loading}
              className="w-full py-2 rounded text-white bg-[#7621FF] hover:bg-[#5a18cc] disabled:opacity-50 transition-colors duration-200"
            >
              {loading ? "Creating..." : "Create Account"}
            </button>
            <button
              type="button"
              onClick={() => { setIsSignUp(false); setError(""); setInfo(""); }}
              className="mt-3 w-full py-2 rounded border hover:bg-gray-50 transition-colors duration-200"
              disabled={loading}
            >
              Back to Log In
            </button>
          </>
        ) : (
          <>
            <input
              type="text"
              placeholder="Username"
              className="w-full p-2 mb-4 border rounded"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
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
            <button
              type="button"
              onClick={() => { setIsSignUp(true); setError(""); setInfo(""); setSuUsername(username); }}
              className="mt-3 w-full py-2 rounded border hover:bg-gray-50 transition-colors duration-200"
              disabled={loading}
            >
              Create an account
            </button>
          </>
        )}
      </form>
    </div>
  );
}
