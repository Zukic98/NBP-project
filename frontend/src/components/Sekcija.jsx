import React from 'react';

/**
 * Višekratna komponenta za zamatanje (wrapping) sekcija sadržaja
 * s naslovom i konzistentnim stilizovanjem.
 * * @param {object} props
 * @param {string} props.title - Naslov sekcije.
 * @param {React.ReactNode} props.children - Sadržaj unutar sekcije.
 */
export default function Sekcija({ title, children }) {
  return (
    <div className="bg-gray-800 p-6 rounded-xl shadow-lg mb-8 border border-gray-700">
      {/* Naslov sekcije - stilizovan sa donjom linijom za vizualno razdvajanje */}
      <h2 className="text-2xl font-semibold text-indigo-400 mb-4 border-b border-indigo-700 pb-2">
        {title}
      </h2>
      {/* Sadržaj sekcije */}
      <div>
        {children}
      </div>
    </div>
  );
}