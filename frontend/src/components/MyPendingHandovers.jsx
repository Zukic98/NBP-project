import React, { useState, useEffect } from 'react';
import { chainOfCustodyApi } from '../api.js';

export default function MyPendingHandovers({ auth, onRefresh }) {
  const [slanja, setSlanja] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedUnos, setSelectedUnos] = useState(null);
  const [razlog, setRazlog] = useState('');

  const ucitajSlanja = async () => {
    try {
      setIsLoading(true);
      setError('');
      const response = await chainOfCustodyApi.getMojaSlanjaCekaPotvrdu();
      
      setSlanja(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri učitavanju slanja');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    ucitajSlanja();
  }, []);

  // Eksponiraj funkciju za osvježavanje preko callback-a
  useEffect(() => {
    if (onRefresh) {
      onRefresh(() => ucitajSlanja());
    }
  }, [onRefresh]);

  const handlePonisti = async (unosId) => {
    if (!razlog.trim() && !window.confirm('Da li ste sigurni da želite poništiti slanje bez navođenja razloga?')) {
      return;
    }

    try {
      setError('');
      setSuccess('');
      
      await chainOfCustodyApi.ponistiSlanje(unosId, razlog);
      
      setSuccess('Slanje uspješno poništeno. Dokaz je vraćen vama.');
      setSelectedUnos(null);
      setRazlog('');
      
      // Osvježi listu
      ucitajSlanja();
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri poništavanju');
    }
  };

  const formatirajVrijeme = (sekunde) => {
    if (!sekunde) return '';
    
    const sati = Math.floor(sekunde / 3600);
    const minute = Math.floor((sekunde % 3600) / 60);
    
    if (sati > 0) {
      return `${sati}h ${minute}m`;
    } else {
      return `${minute} min`;
    }
  };

  if (isLoading) {
    return (
      <div className="p-4 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
        <p className="mt-2 text-gray-400">Učitavam moja slanja...</p>
      </div>
    );
  }

  return (
    <div className="bg-gray-800 p-6 rounded-lg mt-6">
      <h2 className="text-2xl font-bold text-white mb-6 border-b border-gray-700 pb-3">
        📤 Moja slanja koja čekaju potvrdu
      </h2>
      
      {error && (
        <div className="bg-red-900/30 text-red-400 p-4 rounded-lg mb-4 border border-red-500">
          {error}
        </div>
      )}
      
      {success && (
        <div className="bg-green-900/30 text-green-400 p-4 rounded-lg mb-4 border border-green-500">
          {success}
        </div>
      )}
      
      {slanja.length === 0 ? (
        <div className="text-center p-8 text-gray-400">
          <svg className="w-16 h-16 mx-auto mb-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-lg">Nemate slanja koja čekaju potvrdu</p>
          <p className="text-sm mt-1">Sva vaša slanja su potvrđena ili odbijena</p>
        </div>
      ) : (
        <div className="space-y-4">
          {slanja.map((slanje) => (
            <div key={slanje.unos_id} className="bg-gray-700 p-4 rounded-lg border border-gray-600">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center mb-2">
                    <span className="px-2 py-1 bg-yellow-900/30 text-yellow-400 text-xs rounded-full border border-yellow-500">
                      ⏳ Čeka potvrdu primaoca
                    </span>
                    <span className="ml-3 text-sm text-gray-400">
                      {new Date(slanje.datum_primopredaje).toLocaleString()}
                    </span>
                    <span className="ml-3 text-sm text-blue-400">
                      Čeka se: {formatirajVrijeme(slanje.proteklo_sekundi)}
                    </span>
                  </div>
                  
                  <h4 className="text-lg font-semibold text-white mb-1">
                    {slanje.dokaz_opis}
                  </h4>
                  
                  <div className="grid grid-cols-2 gap-2 text-sm mb-3">
                    <div>
                      <span className="text-gray-400">Tip:</span>{' '}
                      <span className="text-white">{slanje.tip_dokaza}</span>
                    </div>
                    <div>
                      <span className="text-gray-400">Prima:</span>{' '}
                      <span className="text-white">{slanje.preuzeo_ime}</span>
                    </div>
                    <div>
                      <span className="text-gray-400">Svrha:</span>{' '}
                      <span className="text-white">{slanje.svrha_primopredaje}</span>
                    </div>
                    <div>
                      <span className="text-gray-400">Dokaz ID:</span>{' '}
                      <span className="text-white font-mono">#{slanje.dokaz_id}</span>
                    </div>
                  </div>
                  
                  <div className="text-sm text-gray-300 bg-gray-800/50 p-3 rounded">
                    <p>💡 <span className="font-medium">Napomena:</span> Dokaz trenutno čeka da {slanje.preuzeo_ime} potvrdi primopredaju.</p>
                    <p className="mt-1">Možete poništiti slanje ako se predomislite ili ako je došlo do greške.</p>
                  </div>
                </div>
                
                <div className="ml-4">
                  <button
                    onClick={() => {
                      setSelectedUnos(slanje.unos_id);
                      setRazlog('');
                    }}
                    className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-md text-sm flex items-center"
                  >
                    <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                    Poništi slanje
                  </button>
                </div>
              </div>
              
              {/* Forma za poništavanje */}
              {selectedUnos === slanje.unos_id && (
                <div className="mt-4 pt-4 border-t border-gray-600">
                  <div className="space-y-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">
                        Razlog poništavanja (opcionalno)
                      </label>
                      <textarea
                        value={razlog}
                        onChange={(e) => setRazlog(e.target.value)}
                        className="w-full p-2 bg-gray-600 border border-gray-500 rounded text-white"
                        rows="2"
                        placeholder="Obrazložite zašto poništavate slanje (opcionalno)..."
                      />
                    </div>
                    
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handlePonisti(slanje.unos_id)}
                        className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-md"
                      >
                        Potvrdi poništavanje
                      </button>
                      <button
                        onClick={() => {
                          setSelectedUnos(null);
                          setRazlog('');
                        }}
                        className="px-4 py-2 bg-gray-600 hover:bg-gray-500 rounded-md text-white"
                      >
                        Odustani
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}