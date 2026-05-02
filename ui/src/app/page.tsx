import Link from "next/link";
import ResponsiveAppBar from "@/component/layout/ResponsiveAppBar";
export default function HomePage() {
  return (
    <>
      <ResponsiveAppBar />

      <main style={{ padding: "40px", fontFamily: "Arial" }}>
        <h1>Japp</h1>
        <p>Create AI-powered cover letters in minutes.</p>

        <div style={{ marginTop: "20px" }}>
          <Link href="/login">login</Link>
          <span style={{ margin: "0 10px" }}>|</span>
          <Link href="/register">Register</Link>
        </div>

        <section style={{ marginTop: "40px" }}>
          <h2>How it works</h2>
          <ol>
            <li>Upload your CV</li>
            <li>Paste job description</li>
            <li>Generate cover letter</li>
          </ol>
        </section>
      </main>
    </>
  );
}