import React from 'react';

const Hero: React.FC = () => {
  return (
    <section className="hero-section px-6 py-16 bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 text-white">
      <div className="max-w-5xl mx-auto text-center">
        <p className="text-sm uppercase tracking-[0.3em] text-sky-300 mb-4">Job Application Tool</p>
        <h1 className="text-4xl sm:text-5xl font-extrabold leading-tight mb-6">
          Find the perfect job, organize your applications, and land your next role.
        </h1>
        <p className="text-lg sm:text-xl text-slate-300 max-w-3xl mx-auto mb-8">
          A clean, modern dashboard for tracking job applications, creating resumes, and staying focused during your search.
        </p>
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
          <button className="px-8 py-3 rounded-full bg-sky-500 hover:bg-sky-400 transition-colors font-semibold">
            Get Started
          </button>
          <button className="px-8 py-3 rounded-full border border-slate-500 hover:border-slate-300 hover:text-white transition-colors font-semibold text-slate-200 bg-transparent">
            Learn More
          </button>
        </div>
      </div>
    </section>
  );
};

export default Hero;
