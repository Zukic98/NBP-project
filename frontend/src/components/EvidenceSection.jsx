import React, { useState, useEffect } from 'react';
import { evidenceApi, formatStatusDokaza } from '../api.js';
import ChainOfCustodyModal from './ChainOfCustodyModal.jsx';

// --- Pomoćne (UI) Komponente ---
function InputPolje({ type, name, placeholder, value, onChange, required = true }) {
  return (
    <input
      type={type}
      name={name}
      placeholder={placeholder}
      required={required}
      className="p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
      value={value}
      onChange={onChange}
    />
  );
}

function AkcijaDugme({ isLoading, tekst, tip = 'submit', onClick, disabled = false }) {
  return (
    <button
      type={tip}
      onClick={onClick}
      disabled={isLoading || disabled}
      className="py-2 px-4 font-semibold text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50 transition duration-150"
    >
      {isLoading ? (
        <div className="flex items-center justify-center">
          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
          Radim...
        </div>
      ) : (
        tekst
      )}
    </button>
  );
}

function PorukaGreske({ message }) {
  if (!message) return null;
  return (
    <div className="text-red-400 bg-red-900/30 p-4 rounded-lg mb-4 border border-red-500">
      <div className="flex items-center">
        <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
        </svg>
        <span>{message}</span>
      </div>
    </div>
  );
}

function PorukaUspjeha({ message }) {
  if (!message) return null;
  return (
    <div className="text-green-800 text-center bg-green-100 p-3 rounded mb-4 border border-green-400">
      {message}
    </div>
  );
}

function LoaderPoruka({ message }) {
  return <p className="text-center text-gray-400 p-4">{message}</p>;
}

function StatusBadge({ status, text }) {
  const klase = {
    'kod_vas': 'bg-green-900/30 text-green-400 border-green-500',
    'kod_drugog': 'bg-yellow-900/30 text-yellow-400 border-yellow-500',
    'default': 'bg-gray-800 text-gray-300 border-gray-600'
  };
  
  const klasa = klase[status] || klase.default;
  
  return (
    <span className={`px-2 py-1 text-xs rounded-full border ${klasa}`}>
      {text}
    </span>
  );
}

function Sekcija({ naslov, children }) {
  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8 border border-gray-700">
      <h2 className="text-2xl font-bold mb-5 text-white border-b border-gray-700 pb-3">{naslov}</h2>
      {children}
    </div>
  );
}

// --- Glavna Komponenta ---
export default function EvidenceSection({ caseId, auth, caseStatus, onPrimopredajaCreated, onDokazStatusChanged }) {
  // Provjeri da li je slučaj u read-only statusu
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  const isAdmin = auth.user.uloga === 'Administrator';
  
  // Funkcija za promjenu statusa dokaza
  const handleStatusChange = async (dokazId, noviStatus) => {
    try {
      const response = await evidenceApi.updateStatus(dokazId, noviStatus);
      
      // Ažuriraj lokalnu listu
      setDokazi(prev => prev.map(d => 
        d.dokaz_id === dokazId 
          ? { ...d, status: noviStatus }
          : d
      ));
      
      // Ako je dokaz poništen, osvježi liste zahtjeva
      if (noviStatus === 'Poništen') {
        if (onDokazStatusChanged) {
          onDokazStatusChanged();
        }
      }
      
      const message = response.data?.message || `Status dokaza je ažuriran na "${noviStatus}".`;
      setCreateSuccess(message);
      setTimeout(() => setCreateSuccess(''), 5000);
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Greška pri promjeni statusa dokaza.');
      setTimeout(() => setCreateError(''), 5000);
    }
  };
  
  const [dokazi, setDokazi] = useState([]);
  const [trenutniNosioci, setTrenutniNosioci] = useState({}); // Map dokaz_id -> trenutniNosilac
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingNosioci, setIsLoadingNosioci] = useState(false);
  const [error, setError] = useState('');
  
  // Stanje za formu
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState({ opis: '', lokacija: '', tipDokaza: 'Biološki' });
  const [createError, setCreateError] = useState('');
  const [createSuccess, setCreateSuccess] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  // Stanje za Modal
  const [selectedDokaz, setSelectedDokaz] = useState(null);

  // Dobavljanje dokaza
  useEffect(() => {
    const fetchEvidence = async () => {
      try {
        setIsLoading(true);
        const response = await evidenceApi.getByCaseId(caseId);
        const dokazi = response.data;
        setDokazi(dokazi);
        
        // Dobavi trenutne nosioce za svaki dokaz
        if (dokazi.length > 0) {
          await ucitajTrenutneNosioce(dokazi);
        }
        
      } catch (err) {
        setError(err.response?.data?.message || 'Greška pri dobavljanju dokaza.');
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchEvidence();
  }, [caseId]);

  // Funkcija za učitavanje trenutnih nosilaca
  const ucitajTrenutneNosioce = async (dokaziLista) => {
    setIsLoadingNosioci(true);
    try {
      const noviNosioci = {};
      
      for (const dokaz of dokaziLista) {
        try {
          const stanje = await evidenceApi.getStanjeDokaza(dokaz.dokaz_id);
          noviNosioci[dokaz.dokaz_id] = stanje.data.trenutni_nosilac;
        } catch (err) {
          console.error(`Greška pri dobavljanju nosioca za dokaz ${dokaz.dokaz_id}:`, err);
          noviNosioci[dokaz.dokaz_id] = null;
        }
      }
      
      setTrenutniNosioci(noviNosioci);
    } catch (err) {
      console.error('Greška pri učitavanju nosilaca:', err);
    } finally {
      setIsLoadingNosioci(false);
    }
  };

  // Osvježi trenutne nosioce
  const osvjeziTrenutneNosioce = () => {
    if (dokazi.length > 0) {
      ucitajTrenutneNosioce(dokazi);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setCreateError('');
    setCreateSuccess('');
    setIsAdding(true);
    
    try {
      const response = await evidenceApi.create(caseId, formData.opis, formData.lokacija, formData.tipDokaza);
      const noviDokaz = response.data;
      
      // Dobavi trenutnog nosioca za novi dokaz
      try {
        const stanje = await evidenceApi.getStanjeDokaza(noviDokaz.dokaz_id);
        setTrenutniNosioci(prev => ({
          ...prev,
          [noviDokaz.dokaz_id]: stanje.data.trenutni_nosilac
        }));
      } catch (err) {
        console.error('Greška pri dobavljanju nosioca za novi dokaz:', err);
      }
      
      setDokazi([noviDokaz, ...dokazi]); // Dodaj na vrh liste
      setFormData({ opis: '', lokacija: '', tipDokaza: 'Biološki' });
      setShowCreateForm(false);
      setCreateSuccess('Dokaz uspješno evidentiran.');
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Greška pri dodavanju dokaza.');
    } finally {
      setIsAdding(false);
    }
  };

  const dozvolaDodavanja = ['Administrator', 'Inspektor', 'Forenzičar'].includes(auth.user.uloga) && !isReadOnly;

  // Prikaz statusa za svaki dokaz
  const renderStatus = (dokaz) => {
    const trenutniNosilac = trenutniNosioci[dokaz.dokaz_id];
    
    if (isLoadingNosioci && !trenutniNosilac) {
      return (
        <div className="flex items-center">
          <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-teal-500 mr-2"></div>
          <span className="text-xs text-gray-400">Učitavam...</span>
        </div>
      );
    }
    
    if (!trenutniNosilac) {
      return <StatusBadge status="default" text="Status: Nepoznat" />;
    }
    
    const statusInfo = formatStatusDokaza(dokaz, trenutniNosilac, auth.user.uposlenik_id);
    
    return (
      <div className="flex items-center space-x-2">
        <StatusBadge 
          status={statusInfo.tekst.includes('Vas') ? 'kod_vas' : 'kod_drugog'} 
          text={statusInfo.ikona ? `${statusInfo.ikona} ${statusInfo.tekst}` : statusInfo.tekst}
        />
        <button
          onClick={osvjeziTrenutneNosioce}
          className="text-xs text-gray-400 hover:text-gray-300 transition"
          title="Osvježi status"
        >
          ↻
        </button>
      </div>
    );
  };

  return (
    <Sekcija naslov="📦 Dokazi i Lanac Nadzora">
      {error && <PorukaGreske message={error} />}
      {createSuccess && <PorukaUspjeha message={createSuccess} />}

      {/* Poruka ako je slučaj u read-only statusu */}
      {isReadOnly && (
        <div className="mb-6 p-4 bg-yellow-900/20 border border-yellow-500 rounded-lg">
          <div className="flex items-center">
            <span className="text-yellow-400 mr-2">⚠️</span>
            <p className="text-yellow-300">
              Slučaj je u statusu "{caseStatus}". Samo pregled podataka je dozvoljen, izmjene nisu moguće.
            </p>
          </div>
        </div>
      )}

      {/* Forma za dodavanje dokaza */}
      {dozvolaDodavanja && (
        <div className="mb-8">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="text-lg font-semibold text-white">Upravljanje dokazima</h3>
              <p className="text-sm text-gray-400">Dodajte nove dokaze na slučaj</p>
            </div>
            <div className="flex gap-2">
              <AkcijaDugme 
                tip="button" 
                isLoading={isLoadingNosioci}
                tekst="Osvježi status"
                onClick={osvjeziTrenutneNosioce}
                boja="gray"
              />
              <AkcijaDugme 
                tip="button" 
                isLoading={false} 
                tekst={showCreateForm ? "✕ Zatvori" : "+ Dodaj Dokaz"}
                onClick={() => setShowCreateForm(!showCreateForm)}
                boja={showCreateForm ? "gray" : "teal"}
              />
            </div>
          </div>
          
          {showCreateForm && (
            <form onSubmit={handleSubmit} className="mt-4 p-6 bg-gray-750 rounded-lg border border-gray-700 space-y-4">
              {createError && <PorukaGreske message={createError} />}
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    Opis dokaza
                  </label>
                  <InputPolje 
                    type="text" 
                    name="opis" 
                    placeholder="npr. Čahura 9mm, Otisak prsta, USB memorija..." 
                    value={formData.opis} 
                    onChange={handleChange} 
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    Lokacija pronalaska
                  </label>
                  <InputPolje 
                    type="text" 
                    name="lokacija" 
                    placeholder="npr. Soba 203, Automobil, Džep..." 
                    value={formData.lokacija} 
                    onChange={handleChange} 
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    Tip dokaza
                  </label>
                  <select
                    name="tipDokaza"
                    value={formData.tipDokaza}
                    onChange={handleChange}
                    className="p-3 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
                  >
                    <option value="Biološki">Biološki (krv, DNK, dlake)</option>
                    <option value="Fizički">Fizički (oružje, alat, odjeća)</option>
                    <option value="Digitalni">Digitalni (telefon, laptop, USB)</option>
                    <option value="Tragovi">Tragovi (otisci, šina, tragovi guma)</option>
                    <option value="Hemijski">Hemijski (droga, eksploziv, otrov)</option>
                    <option value="Ostalo">Ostalo</option>
                  </select>
                </div>
              </div>

              <div className="flex gap-2 pt-2">
                <AkcijaDugme isLoading={isAdding} tekst="Spremi Dokaz" boja="green" />
                <AkcijaDugme 
                  tip="button"
                  isLoading={false} 
                  tekst="Poništi"
                  onClick={() => setShowCreateForm(false)}
                  boja="gray"
                />
              </div>
            </form>
          )}
        </div>
      )}

      {/* Lista dokaza */}
      <div className="mt-8">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h4 className="text-lg font-semibold text-white">Evidentirani Dokazi</h4>
            <p className="text-sm text-gray-400">
              {dokazi.length} dokaza • Kliknite na "Lanac Nadzora" za detalje
            </p>
          </div>
          {dokazi.length > 0 && (
            <div className="flex items-center text-sm text-gray-400">
              <div className="flex items-center mr-4">
                <div className="w-3 h-3 rounded-full bg-green-500 mr-2"></div>
                <span>Kod vas</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 rounded-full bg-yellow-500 mr-2"></div>
                <span>Kod drugog</span>
              </div>
            </div>
          )}
        </div>
        
        {isLoading ? (
          <LoaderPoruka message="Učitavam dokaze..." />
        ) : (
          <div className="overflow-x-auto bg-gray-750 rounded-lg shadow border border-gray-700">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-800">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-300 uppercase tracking-wider border-r border-gray-700">
                    ID
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Dokaz
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-300 uppercase tracking-wider border-r border-gray-700">
                    Status Lanca
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Status Dokaza
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Detalji
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Akcije
                  </th>
                </tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {dokazi.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                      <svg className="w-16 h-16 mx-auto text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                      <p className="text-lg mb-2">Nema dokaza na ovom slučaju</p>
                      <p className="text-sm text-gray-400">Dodajte prvi dokaz pomoću gornje forme</p>
                    </td>
                  </tr>
                ) : (
                  dokazi.map((dokaz) => (
                    <tr key={dokaz.dokaz_id} className="hover:bg-gray-750 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap border-r border-gray-700">
                        <div className="font-mono text-sm bg-gray-900 rounded px-2 py-1 inline-block">
                          #{dokaz.dokaz_id}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-start space-x-3">
                          <div className="flex-shrink-0">
                            <div className="w-10 h-10 rounded bg-gray-700 flex items-center justify-center">
                              <span className="text-lg">
                                {dokaz.tip_dokaza === 'Biološki' ? '🧬' :
                                 dokaz.tip_dokaza === 'Fizički' ? '🔧' :
                                 dokaz.tip_dokaza === 'Digitalni' ? '💻' :
                                 dokaz.tip_dokaza === 'Tragovi' ? '👣' : '📦'}
                              </span>
                            </div>
                          </div>
                          <div>
                            <div className="font-medium text-white mb-1">{dokaz.opis}</div>
                            <div className="text-sm text-gray-400">
                              {dokaz.lokacija_pronalaska} • {dokaz.tip_dokaza}
                            </div>
                            <div className="text-xs text-gray-500 mt-1">
                              Prikupio: {dokaz.prikupio_ime} • {new Date(dokaz.datum_prikupa).toLocaleDateString()}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap border-r border-gray-700">
                        {renderStatus(dokaz)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap border-r border-gray-700">
                        <div className="flex flex-col items-start space-y-2">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                            (dokaz.status || 'Odobren') === 'Odobren' 
                              ? 'bg-green-900/30 text-green-400 border border-green-500'
                              : 'bg-red-900/30 text-red-400 border border-red-500'
                          }`}>
                            {dokaz.status || 'Odobren'}
                          </span>
                          {isAdmin && !isReadOnly && (
                            <select
                              value={dokaz.status || 'Odobren'}
                              onChange={(e) => handleStatusChange(dokaz.dokaz_id, e.target.value)}
                              className="text-xs bg-gray-700 border border-gray-600 rounded px-2 py-1 text-white"
                            >
                              <option value="Odobren">Odobren</option>
                              <option value="Poništen">Poništen</option>
                            </select>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="space-y-1">
                          <div className="text-sm">
                            <span className="text-gray-400">Tip:</span>{' '}
                            <span className="text-white">{dokaz.tip_dokaza}</span>
                          </div>
                          <div className="text-sm">
                            <span className="text-gray-400">Lokacija:</span>{' '}
                            <span className="text-white">{dokaz.lokacija_pronalaska}</span>
                          </div>
                          <div className="text-sm">
                            <span className="text-gray-400">Prikupio:</span>{' '}
                            <span className="text-white">{dokaz.prikupio_ime}</span>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <button 
                          onClick={() => setSelectedDokaz(dokaz)}
                          disabled={isReadOnly || (dokaz.status || 'Odobren') === 'Poništen'}
                          className={`inline-flex items-center px-4 py-2 text-white text-sm font-medium rounded-md transition duration-150 ${
                            isReadOnly || (dokaz.status || 'Odobren') === 'Poništen'
                              ? 'bg-gray-600 cursor-not-allowed opacity-50' 
                              : 'bg-blue-600 hover:bg-blue-700'
                          }`}
                          title={
                            isReadOnly 
                              ? 'Slučaj je u read-only statusu' 
                              : (dokaz.status || 'Odobren') === 'Poništen'
                              ? 'Dokaz je poništen. Lanac nadzora je onemogućen.'
                              : 'Pregledaj lanac nadzora'
                          }
                        >
                          <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                          </svg>
                          Lanac Nadzora
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
      
      {/* Modal za Lanac Nadzora */}
      {selectedDokaz && (
        <ChainOfCustodyModal
          dokaz={selectedDokaz}
          auth={auth}
          caseStatus={caseStatus}
          onPrimopredajaCreated={onPrimopredajaCreated}
          onClose={() => {
            setSelectedDokaz(null);
            // Osvježi podatke nakon zatvaranja modala
            setTimeout(() => {
              if (dokazi.length > 0) {
                ucitajTrenutneNosioce(dokazi);
              }
            }, 500);
          }}
        />
      )}
    </Sekcija>
  );
}