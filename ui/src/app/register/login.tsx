"use client";

import { useState } from "react";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    console.log("Register:", name, email, password);
  }

  return (
    <main style={{ padding: "40px", fontFamily: "Arial" }}>
      <h1>Register</h1>

      <form onSubmit={handleRegister}>
        <div style={{ marginBottom: "15px" }}>
          <input
            type="text"
            placeholder="Full name"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

        <div style={{ marginBottom: "15px" }}>
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div style={{ marginBottom: "15px" }}>
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button type="submit">Create Account</button>
      </form>
    </main>
  );
}