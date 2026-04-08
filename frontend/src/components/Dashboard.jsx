import React, { useState, useEffect } from 'react';
import { caseApi, employeeApi, validatePassword } from '../api.js';
import CaseDetail from './CaseDetail.jsx'; // Uvozimo detalje
import AdminPanel from './AdminPanel.jsx';
import CaseList from './CaseList.jsx';
import ChangePasswordModal from './ChangePasswordModal.jsx';

// --- Pomoćne (UI) Komponente ---
// Ove komponente su definisane ovdje da bi ovaj fajl bio samostalan



// --- 3. Glavna Komponenta (Dashboard) ---
export default function Dashboard({ auth, onLogout }) {
  const [view, setView] = useState('list'); // 'list' ili 'detail'
  const [selectedCaseId, setSelectedCaseId] = useState(null);
  const [isChangePasswordModalOpen, setIsChangePasswordModalOpen] = useState(false);

  // Funkcija koju poziva CaseList kada korisnik klikne na broj slučaja
  const handleSelectCase = (id) => {
    setSelectedCaseId(id);
    setView('detail');
  };

  // Funkcija koju poziva CaseDetail kada korisnik klikne "Nazad"
  const handleBackToList = () => {
    setSelectedCaseId(null);
    setView('list');
  };

  return (
    <div className="p-6 md:p-10 max-w-7xl mx-auto">
      {/* Zaglavlje Dashboarda */}
      <header className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 p-6 bg-gray-800 rounded-lg border border-gray-700">
        <div className="mb-4 md:mb-0">
          <h1 className="text-3xl font-bold text-white">🚔 SUDS Dashboard</h1>
          <div className="flex flex-wrap items-center gap-4 mt-2">
            <div className="flex items-center">
              <div className="w-3 h-3 rounded-full bg-green-500 mr-2"></div>
              <span className="text-gray-400">{auth.user.ime_stanice}</span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 rounded-full bg-blue-500 mr-2"></div>
              <span className="text-gray-400">Prijavljeni ste kao: <span className="text-white font-medium">{auth.user.ime_prezime}</span></span>
            </div>
            <div className="flex items-center">
              <div className="w-3 h-3 rounded-full bg-yellow-500 mr-2"></div>
              <span className="text-gray-400">Uloga: <span className={`font-medium ${auth.user.uloga === 'Administrator' ? 'text-red-400' :
                  auth.user.uloga === 'Inspektor' ? 'text-blue-400' :
                    'text-yellow-400'
                }`}>{auth.user.uloga}</span></span>
            </div>
          </div>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setIsChangePasswordModalOpen(true)}
            className="py-2 px-4 font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-md transition duration-150 flex items-center"
            title="Promijeni lozinku"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
            </svg>
            Promijeni Lozinku
          </button>
          <button
            onClick={onLogout}
            className="py-2 px-4 font-semibold text-white bg-red-600 hover:bg-red-700 rounded-md transition duration-150 flex items-center"
          >
            <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            Odjavi se
          </button>
        </div>
      </header>

      {/* Modal za promjenu lozinke */}
      <ChangePasswordModal 
        isOpen={isChangePasswordModalOpen}
        onClose={() => setIsChangePasswordModalOpen(false)}
      />

      <main className="space-y-8">
        {/* Prikazujemo Admin Panel samo ako je korisnik Administrator i ako smo u list view */}
        {auth.user.uloga === 'Administrator' && view === 'list' && <AdminPanel auth={auth} />}

        {/* Glavni sadržaj (Lista ili Detalji) */}
        {view === 'list' ? (
          <CaseList auth={auth} onSelectCase={handleSelectCase} />
        ) : (
          <CaseDetail
            caseId={selectedCaseId}
            onBackToList={handleBackToList}
            auth={auth}
          />
        )}

        {/* Footer informacije */}
        <footer className="mt-12 pt-8 border-t border-gray-700 text-center text-gray-500 text-sm">
          <p>🚨 SUDS - Sistem za Upravljanje Dokazima i Slučajevima</p>
          <p className="mt-1">Verzija 2.0 • Lanac nadzora sa potvrdama • {new Date().getFullYear()}</p>
          <div className="mt-4 flex justify-center space-x-4 text-xs">
            <span className="flex items-center">
              <div className="w-2 h-2 rounded-full bg-green-500 mr-1"></div>
              Otvoreni slučajevi
            </span>
            <span className="flex items-center">
              <div className="w-2 h-2 rounded-full bg-red-500 mr-1"></div>
              Zatvoreni slučajevi
            </span>
            <span className="flex items-center">
              <div className="w-2 h-2 rounded-full bg-yellow-500 mr-1"></div>
              Dokaz čeka potvrdu
            </span>
          </div>
        </footer>
      </main>
    </div>
  );
}