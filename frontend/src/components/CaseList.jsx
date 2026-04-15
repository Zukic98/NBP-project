import React, { useState, useEffect } from 'react';
import { caseApi } from '../api.js';
import { Th, Td, PorukaGreske, PorukaUspjeha, AkcijaDugme, InputPolje, LoaderPoruka, Sekcija } from './AdminPanel.jsx';

/**
 * Komponenta za prikaz brojačkih kartica (stats)
 */
function StatKartica({ naslov, vrijednost, ikona, boja = 'blue', trend }) {
  const boje = {
    blue: 'bg-blue-900/20 border-blue-500',
    green: 'bg-green-900/20 border-green-500',
    red: 'bg-red-900/20 border-red-500',
    yellow: 'bg-yellow-900/20 border-yellow-500',
    purple: 'bg-purple-900/20 border-purple-500'
  };

  return (
    <div className={`p-4 rounded-lg border ${boje[boja]} flex items-center justify-between`}>
      <div>
        <p className="text-sm text-gray-400">{naslov}</p>
        <p className="text-2xl font-bold text-white mt-1">{vrijednost}</p>
        {trend && (
          <p className={`text-xs mt-1 ${trend.includes('+') ? 'text-green-400' : 'text-red-400'}`}>
            {trend}
          </p>
        )}
      </div>
      <div className="text-3xl opacity-50">
        {ikona}
      </div>
    </div>
  );
}


// --- 2. Komponenta: Lista Svih Slučajeva ---
export default function CaseList({ auth, onSelectCase }) {
  // Stanje (state) za listu slučajeva
  const [slucajevi, setSlucajevi] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Stanje (state) za formu za kreiranje novog slučaja
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [caseFormData, setCaseFormData] = useState({ brojSlucaja: '', opis: '' });
  const [createError, setCreateError] = useState('');
  const [createSuccess, setCreateSuccess] = useState('');
  const [isCreating, setIsCreating] = useState(false);

  // Stanje za statistiku - sada se računa iz slučajeva
  const [stats, setStats] = useState({
    ukupno: 0,
    otvoreno: 0,
    zatvoreno: 0,
    arhivirano: 0
  });

  // Funkcija za izračunavanje statistike iz liste slučajeva
  const izracunajStatistiku = (slucajeviLista) => {
    if (!slucajeviLista || slucajeviLista.length === 0) {
      return {
        ukupno: 0,
        otvoreno: 0,
        zatvoreno: 0,
        arhivirano: 0
      };
    }

    const otvoreno = slucajeviLista.filter(s => s.status === 'Otvoren').length;
    const zatvoreno = slucajeviLista.filter(s => s.status === 'Zatvoren').length;
    const arhivirano = slucajeviLista.filter(s => s.status === 'Arhiviran').length;

    return {
      ukupno: slucajeviLista.length,
      otvoreno,
      zatvoreno,
      arhivirano
    };
  };

  // useEffect kuka: Pokreće se kada se komponenta prvi put učita ili kada se promijeni auth.user
  useEffect(() => {
    const fetchCases = async () => {
      setIsLoading(true);
      setError('');
      
      try {
        let response;
        
        // Ako je korisnik Administrator, uzmi sve slučajeve
        if (auth.user.uloga === 'Administrator') {
          response = await caseApi.getAll();
        } 
        // Inače, uzmi samo slučajeve dodijeljene ovom korisniku
        else {
          response = await caseApi.getMyCases();
        }
        
        const slucajevi = response.data;
        setSlucajevi(slucajevi);
        
        // Izračunaj statistiku
        const novaStatistika = izracunajStatistiku(slucajevi);
        setStats(novaStatistika);
      } catch (err) {
        setError(err.response?.data?.message || 'Interna greška servera pri dobavljanju slučajeva.');
        console.error('Greška pri dobavljanju slučajeva:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCases();
  }, [auth.user]); // Ovisi o auth.user da se osvježi kada se korisnik promijeni

  // Funkcija koja ažurira i listu slučajeva i statistiku
  const azurirajSlucajIOStatistiku = (caseId, newStatus) => {
    setSlucajevi(trenutniSlucajevi => {
      const azuriraniSlucajevi = trenutniSlucajevi.map(slucaj =>
        slucaj.slucaj_id === caseId
          ? { ...slucaj, status: newStatus }
          : slucaj
      );
      
      // Automatski izračunaj novu statistiku iz ažurirane liste
      const novaStatistika = izracunajStatistiku(azuriraniSlucajevi);
      setStats(novaStatistika);
      
      return azuriraniSlucajevi;
    });
  };

  // --- Logika za upravljanje formom za kreiranje slučaja ---

  const handleCaseFormChange = (e) => {
    setCaseFormData({ ...caseFormData, [e.target.name]: e.target.value });
    setCreateError('');
    setCreateSuccess('');
  };

  const handleCreateCase = async (e) => {
    e.preventDefault();
    setCreateError('');
    setCreateSuccess('');
    setIsCreating(true);

    try {
      const response = await caseApi.create(caseFormData.brojSlucaja, caseFormData.opis);
      const noviSlucaj = response.data;

      setCreateSuccess(`Slučaj "${noviSlucaj.broj_slucaja}" je uspješno kreiran.`);
      
      // Dodaj novi slučaj na početak liste
      setSlucajevi(trenutniSlucajevi => {
        // Ako korisnik nije administrator, dodaj samo ako je taj korisnik dodijeljen slučaju
        if (auth.user.uloga !== 'Administrator') {
          // Provjeri je li novi slučaj dodijeljen ovom korisniku
          const jeDodijeljenMeni = noviSlucaj.voditelj_slucaja === auth.user.ime;
          
          if (jeDodijeljenMeni) {
            return [noviSlucaj, ...trenutniSlucajevi];
          } else {
            return trenutniSlucajevi; // Ne dodavaj ako nije dodijeljen korisniku
          }
        }
        return [noviSlucaj, ...trenutniSlucajevi];
      });
      
      // Ažuriraj statistiku - samo ako je novi slučaj dodan u listu
      setStats(prev => ({
        ukupno: prev.ukupno + 1,
        otvoreno: prev.otvoreno + 1,
        zatvoreno: prev.zatvoreno,
        arhivirano: prev.arhivirano
      }));

      setCaseFormData({ brojSlucaja: '', opis: '' });
      setShowCreateForm(false);

    } catch (err) {
      setCreateError(err.response?.data?.message || 'Greška pri kreiranju slučaja.');
    } finally {
      setIsCreating(false);
    }
  };

  // Funkcija za ažuriranje statusa slučaja
  const handleUpdateStatus = async (caseId, newStatus) => {
    try {
      await caseApi.updateStatus(caseId, newStatus);
      
      // Koristimo novu funkciju koja ažurira i listu i statistiku
      azurirajSlucajIOStatistiku(caseId, newStatus);

    } catch (error) {
      console.error('Greška pri ažuriranju statusa:', error);
      setError('Greška pri ažuriranju statusa slučaja.');
    }
  };

  // --- HTML/JSX Prikaz ---
  return (
    <div>
      {/* Naslov i informacija o korisničkom pristupu */}
      <div className="mb-4">
        <h2 className="text-2xl font-bold text-white">
          📋 Slučajevi
          <span className="text-sm font-normal ml-3 px-3 py-1 bg-blue-900/30 text-blue-300 rounded-full">
            {auth.user.uloga === 'Administrator' ? 'Prikaz svih slučajeva' : 'Prikaz slučajeva dodijeljenih meni'}
          </span>
        </h2>
      </div>

      {/* Statistika - sada se automatski ažurira */}
      <div className="mb-8">
        <h2 className="text-2xl font-bold mb-4 text-white">📊 Statistika Slučajeva</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <StatKartica
            naslov="Ukupno slučajeva"
            vrijednost={stats.ukupno}
            ikona="📋"
            boja="blue"
          />
          <StatKartica
            naslov="Otvoreni"
            vrijednost={stats.otvoreno}
            ikona="🟢"
            boja="green"
            trend={stats.ukupno > 0 ? `${((stats.otvoreno / stats.ukupno) * 100).toFixed(0)}%` : '0%'}
          />
          <StatKartica
            naslov="Zatvoreni"
            vrijednost={stats.zatvoreno}
            ikona="🔴"
            boja="red"
            trend={stats.ukupno > 0 ? `${((stats.zatvoreno / stats.ukupno) * 100).toFixed(0)}%` : '0%'}
          />
          <StatKartica
            naslov="Arhivirani"
            vrijednost={stats.arhivirano}
            ikona="📁"
            boja="purple"
            trend={stats.ukupno > 0 ? `${((stats.arhivirano / stats.ukupno) * 100).toFixed(0)}%` : '0%'}
          />
        </div>
        
        {/* Progress bar za vizualni prikaz */}
        {stats.ukupno > 0 && (
          <div className="mt-4">
            <div className="flex items-center justify-between text-xs text-gray-400 mb-1">
              <span>Raspodjela statusa:</span>
              <span>{stats.otvoreno}🟢 / {stats.zatvoreno}🔴 / {stats.arhivirano}📁</span>
            </div>
            <div className="w-full h-2 bg-gray-700 rounded-full overflow-hidden">
              <div 
                className="h-full bg-green-500 float-left"
                style={{ width: `${(stats.otvoreno / stats.ukupno) * 100}%` }}
              ></div>
              <div 
                className="h-full bg-red-500 float-left"
                style={{ width: `${(stats.zatvoreno / stats.ukupno) * 100}%` }}
              ></div>
              <div 
                className="h-full bg-purple-500 float-left"
                style={{ width: `${(stats.arhivirano / stats.ukupno) * 100}%` }}
              ></div>
            </div>
          </div>
        )}
      </div>

      {/* Sekcija sa slučajevima */}
      <Sekcija naslov={`📋 ${auth.user.uloga === 'Administrator' ? 'Svi Slučajevi' : 'Moji Slučajevi'}`}>
        {/* Prikaz stanja učitavanja liste */}
        {isLoading && <LoaderPoruka message="Učitavam slučajeve..." />}
        {error && <PorukaGreske message={error} />}

        {/* Dugme i forma za kreiranje slučaja */}
        {['Administrator', 'Inspektor'].includes(auth.user.uloga) && (
          <div className="mb-6">
            {createSuccess && <PorukaUspjeha message={createSuccess} />}

            <div className="flex justify-between items-center">
              <div>
                <h3 className="text-lg font-semibold text-white">Upravljanje slučajevima</h3>
                <p className="text-sm text-gray-400">Kreirajte i upravljajte slučajevima</p>
              </div>
              <AkcijaDugme
                isLoading={false}
                tekst={showCreateForm ? "✕ Zatvori" : "+ Kreiraj Novi Slučaj"}
                tip="button"
                onClick={() => {
                  setShowCreateForm(!showCreateForm);
                  setCreateError('');
                  setCreateSuccess('');
                }}
                boja={showCreateForm ? "gray" : "green"}
              />
            </div>

            {showCreateForm && (
              <form onSubmit={handleCreateCase} className="mt-4 p-6 bg-gray-750 rounded-lg border border-gray-700 space-y-4">
                {createError && <PorukaGreske message={createError} />}

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Broj slučaja
                    </label>
                    <InputPolje
                      type="text"
                      name="brojSlucaja"
                      placeholder="npr. 2025-001, SUDS-25-123"
                      value={caseFormData.brojSlucaja}
                      onChange={handleCaseFormChange}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Kratki opis
                    </label>
                    <InputPolje
                      type="text"
                      name="opis"
                      placeholder="Kratki opis slučaja"
                      value={caseFormData.opis}
                      onChange={handleCaseFormChange}
                    />
                  </div>
                </div>

                <div className="flex gap-2">
                  <AkcijaDugme
                    isLoading={isCreating}
                    tekst="Spremi Slučaj"
                    boja="green"
                  />
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

        {/* Tabela sa listom slučajeva */}
        {!isLoading && !error && (
          <div className="overflow-x-auto bg-gray-750 rounded-lg shadow border border-gray-700">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-900">
                <tr>
                  <Th>Broj Slučaja</Th>
                  <Th>Opis</Th>
                  <Th>Status</Th>
                  <Th>Voditelj</Th>
                  <Th>Datum Kreiranja</Th>
                  <Th>Akcije</Th>
                </tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {slucajevi.length === 0 ? (
                  <tr>
                    <Td colSpan="6" className="text-center text-gray-500 py-12">
                      <svg className="w-16 h-16 mx-auto text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                      <p className="text-lg mb-2">
                        {auth.user.uloga === 'Administrator' ? 'Nema slučajeva' : 'Nema slučajeva dodijeljenih vama'}
                      </p>
                      <p className="text-sm text-gray-400">
                        {auth.user.uloga === 'Administrator' 
                          ? 'Kreirajte prvi slučaj pomoću gornjeg dugmeta'
                          : 'Nijedan slučaj vam nije trenutno dodijeljen'}
                      </p>
                    </Td>
                  </tr>
                ) : (
                  slucajevi.map((slucaj) => (
                    <tr key={slucaj.slucaj_id} className="hover:bg-gray-750 transition-colors">
                      <Td>
                        <button
                          onClick={() => onSelectCase(slucaj.slucaj_id)}
                          className="text-blue-400 hover:text-blue-300 hover:underline font-medium text-left"
                          title={`Otvori detalje za ${slucaj.broj_slucaja}`}
                        >
                          {slucaj.broj_slucaja}
                        </button>
                      </Td>
                      <Td className="max-w-xs">
                        <div className="truncate" title={slucaj.opis}>
                          {slucaj.opis}
                        </div>
                      </Td>
                      <Td>
                        <div className="flex items-center space-x-2">
                          <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${slucaj.status === 'Otvoren' ? 'bg-green-900/30 text-green-400 border border-green-500' :
                              slucaj.status === 'Zatvoren' ? 'bg-red-900/30 text-red-400 border border-red-500' :
                                'bg-yellow-900/30 text-yellow-400 border border-yellow-500'
                            }`}>
                            {slucaj.status === 'Otvoren' ? '🟢 Otvoren' :
                              slucaj.status === 'Zatvoren' ? '🔴 Zatvoren' :
                                '📁 Arhiviran'}
                          </span>
                          {auth.user.uloga === 'Administrator' && (
                            <select
                              value={slucaj.status}
                              onChange={(e) => handleUpdateStatus(slucaj.slucaj_id, e.target.value)}
                              className="text-xs bg-gray-700 border border-gray-600 rounded px-2 py-1 text-white"
                            >
                              <option value="Otvoren">Otvoren</option>
                              <option value="Zatvoren">Zatvoren</option>
                              <option value="Arhiviran">Arhiviran</option>
                            </select>
                          )}
                        </div>
                      </Td>
                      <Td className="text-gray-300">{slucaj.voditelj_slucaja || 'Nije dodijeljen'}</Td>
                      <Td className="text-gray-400 text-sm">
                        {new Date(slucaj.datum_kreiranja).toLocaleDateString()}
                      </Td>
                      <Td>
                        <button
                          onClick={() => onSelectCase(slucaj.slucaj_id)}
                          className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-xs font-medium rounded transition duration-150"
                        >
                          Detalji
                        </button>
                      </Td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </Sekcija>
    </div>
  );
} // --- KRAJ CaseList KOMPONENTE ---