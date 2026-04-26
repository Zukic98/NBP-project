import React, { useState, useEffect } from 'react';
import { chainOfCustodyApi } from '../api.js';

export default function HandoverApproval({ auth, onRefresh, onPrimopredajaProcessed }) {
  const [zahtjevi, setZahtjevi] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedUnos, setSelectedUnos] = useState(null);
  const [potvrdaForm, setPotvrdaForm] = useState({
    status: 'Potvrđeno',
    napomena: ''
  });

  useEffect(() => {
    ucitajZahtjeve();
  }, []);

  // Eksponiraj funkciju za osvježavanje preko callback-a
  useEffect(() => {
    if (onRefresh) {
      onRefresh(() => ucitajZahtjeve());
    }
  }, [onRefresh]);

  const ucitajZahtjeve = async () => {
    try {
        setIsLoading(true);
        setError('');
        const response = await chainOfCustodyApi.getZahtjeviZaPotvrdu();
        
        // Debug log
        
        setZahtjevi(response.data);
    } catch (err) {
        console.error('Greška pri učitavanju zahtjeva:', err);
        setError(err.response?.data?.message || 'Greška pri učitavanju zahtjeva');
    } finally {
        setIsLoading(false);
    }
  };  

  const handlePotvrdi = async (unosId) => {
    try {
      setError('');
      setSuccess('');
      
      await chainOfCustodyApi.potvrdiPrimopredaju(
        unosId, 
        potvrdaForm.status, 
        potvrdaForm.napomena
      );
      
      setSuccess(`Primopredaja uspješno ${potvrdaForm.status.toLowerCase()}`);
      setSelectedUnos(null);
      setPotvrdaForm({ status: 'Potvrđeno', napomena: '' });

      ucitajZahtjeve();

      if (onPrimopredajaProcessed) {
        onPrimopredajaProcessed(potvrdaForm.status);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri potvrdi');
    }
  };

  if (isLoading) {
    return (
      <div className="p-4 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-500 mx-auto"></div>
        <p className="mt-2 text-gray-400">Učitavam zahtjeve za potvrdu...</p>
      </div>
    );
  }

  return (
    <div className="bg-gray-800 p-6 rounded-lg">
      <h2 className="text-2xl font-bold text-white mb-6 border-b border-gray-700 pb-3">
        ⏳ Zahtjevi za potvrdu primopredaje
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
      
      {zahtjevi.length === 0 ? (
        <div className="text-center p-8 text-gray-400">
          <svg className="w-16 h-16 mx-auto mb-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-lg">Nema zahtjeva za potvrdu</p>
          <p className="text-sm mt-1">Svi dokazi su potvrđeni ili nema novih predaja za vas</p>
        </div>
      ) : (
        <div className="space-y-4">
          {zahtjevi.map((zahtjev) => (
            <div key={zahtjev.unos_id} className="bg-gray-700 p-4 rounded-lg border border-gray-600">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center mb-2">
                    <span className="px-2 py-1 bg-yellow-900/30 text-yellow-400 text-xs rounded-full border border-yellow-500">
                      ⏳ Čeka potvrdu
                    </span>
                    <span className="ml-3 text-sm text-gray-400">
                      {new Date(zahtjev.datum_primopredaje).toLocaleString()}
                    </span>
                  </div>
                  
                  <h4 className="text-lg font-semibold text-white mb-1">
                    {zahtjev.dokaz_opis}
                  </h4>
                  
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div>
                      <span className="text-gray-400">Tip:</span>{' '}
                      <span className="text-white">{zahtjev.tip_dokaza}</span>
                    </div>
                    <div>
                      <span className="text-gray-400">Predao:</span>{' '}
                      <span className="text-white">{zahtjev.predao_ime}</span>
                    </div>
                    <div>
                      <span className="text-gray-400">Svrha:</span>{' '}
                      <span className="text-white">{zahtjev.svrha_primopredaje}</span>
                    </div>
                    <div>
                      <span className="text-gray-400">Dokaz ID:</span>{' '}
                      <span className="text-white font-mono">#{zahtjev.dokaz_id}</span>
                    </div>
                  </div>
                </div>
                
                <div className="flex space-x-2 ml-4">
                  <button
                    onClick={() => {
                      setSelectedUnos(zahtjev.unos_id);
                      setPotvrdaForm({ status: 'Potvrđeno', napomena: '' });
                    }}
                    className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-md text-sm"
                  >
                    ✅ Potvrdi
                  </button>
                  <button
                    onClick={() => {
                      setSelectedUnos(zahtjev.unos_id);
                      setPotvrdaForm({ status: 'Odbijeno', napomena: '' });
                    }}
                    className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-md text-sm"
                  >
                    ❌ Odbij
                  </button>
                </div>
              </div>
              
              {/* Forma za potvrdu/odbijanje */}
              {selectedUnos === zahtjev.unos_id && (
                <div className="mt-4 pt-4 border-t border-gray-600">
                  <div className="space-y-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">
                        Status
                      </label>
                      <select
                        value={potvrdaForm.status}
                        onChange={(e) => setPotvrdaForm({...potvrdaForm, status: e.target.value})}
                        className="w-full p-2 bg-gray-600 border border-gray-500 rounded text-white"
                      >
                        <option value="Potvrđeno">✅ Potvrdi primopredaju</option>
                        <option value="Odbijeno">❌ Odbij primopredaju</option>
                      </select>
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-1">
                        Napomena {potvrdaForm.status === 'Odbijeno' && '(obavezno)'}
                      </label>
                      <textarea
                        value={potvrdaForm.napomena}
                        onChange={(e) => setPotvrdaForm({...potvrdaForm, napomena: e.target.value})}
                        className="w-full p-2 bg-gray-600 border border-gray-500 rounded text-white"
                        rows="2"
                        placeholder={potvrdaForm.status === 'Odbijeno' 
                          ? 'Obrazložite zašto odbijate primopredaju...' 
                          : 'Dodatna napomena (opcionalno)...'}
                      />
                    </div>
                    
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handlePotvrdi(zahtjev.unos_id)}
                        disabled={potvrdaForm.status === 'Odbijeno' && !potvrdaForm.napomena.trim()}
                        className={`px-4 py-2 rounded-md text-white ${
                          potvrdaForm.status === 'Potvrđeno' 
                            ? 'bg-green-600 hover:bg-green-700' 
                            : 'bg-red-600 hover:bg-red-700'
                        } disabled:opacity-50`}
                      >
                        {potvrdaForm.status === 'Potvrđeno' ? 'Potvrdi' : 'Odbij'}
                      </button>
                      <button
                        onClick={() => setSelectedUnos(null)}
                        className="px-4 py-2 bg-gray-600 hover:bg-gray-500 rounded-md text-white"
                      >
                        Otkaži
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
      
      <div className="mt-6 text-sm text-gray-400">
        <p>💡 Napomena: Dok god primopredaja čeka potvrdu, dokaz je zaključan i ne može se predati dalje.</p>
        <p className="mt-1">Ako odbijete primopredaju, dokaz će biti automatski vraćen predavaocu.</p>
      </div>
    </div>
  );
}