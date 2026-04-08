import React from 'react';

/**
 * Generička komponenta modala.
 * Prikazuje se preko cijelog ekrana s tamnom pozadinom.
 * * @param {object} props
 * @param {boolean} props.isOpen - Da li je modal otvoren.
 * @param {function} props.onClose - Funkcija za zatvaranje modala.
 * @param {string} props.title - Naslov modala.
 * @param {React.ReactNode} props.children - Sadržaj modala.
 * @param {string} [props.size='md'] - Veličina modala ('sm', 'md', 'lg').
 */
export default function Modal({ isOpen, onClose, title, children, size = 'md' }) {
  if (!isOpen) return null;

  let widthClass;
  switch (size) {
    case 'sm':
      widthClass = 'max-w-md';
      break;
    case 'lg':
      widthClass = 'max-w-3xl';
      break;
    case 'md':
    default:
      widthClass = 'max-w-xl';
      break;
  }

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-75 transition-opacity"
      onClick={onClose} // Zatvori klikom na pozadinu
    >
        <div 
          className={`bg-gray-800 rounded-xl shadow-2xl transform transition-all w-full ${widthClass} max-h-[90vh] flex flex-col`}
          onClick={(e) => e.stopPropagation()} 
        >
        {/* Header */}
        <div className="flex justify-between items-center border-b border-gray-700 pb-3 mb-4 p-6 flex-shrink-0">
          <h3 className="text-xl font-bold text-white">{title}</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white transition-colors"
            aria-label="Zatvori modal"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        {/* Body */}
        <div className="text-gray-200 overflow-y-auto p-6">
          {children}
        </div>
      </div>
    </div>
  );
}