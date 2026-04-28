import React, { useState, useEffect, useRef } from 'react';
import { krivicnoDjeloApi, slucajKrivicnoDjeloApi } from '../api.js';

export default function CriminalOffensesSection({ caseId, auth, caseStatus }) {
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  
  const [djelaNaSlucaju, setDjelaNaSlucaju] = useState([]);
  const [svaDjela, setSvaDjela] = useState([]);
  
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Dropdown sa pretragom
  const [searchTerm, setSearchTerm] = useState('');
  const [showDropdown, setShowDropdown] = useState(false);
  const [selectedDjeloId, setSelectedDjeloId] = useState(null);
  const [selectedDjeloNaziv, setSelectedDjeloNaziv] = useState('');
  const [isAddingExisting, setIsAddingExisting] = useState(false);
  const dropdownRef = useRef(null);
  const searchInputRef = useRef(null);

  // Forma za novo djelo
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState({
    naziv: '',
    kategorija: '',
    kazneniZakonClan: ''
  });
  const [formError, setFormError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const kategorije = [
    'Protiv života i tijela',
    'Protiv imovine',
    'Protiv sigurnosti javnog saobraćaja',
    'Protiv privrede',
    'Protiv službene dužnosti',
    'Protiv pravosuđa',
    'Protiv javnog reda i mira',
    'Protiv čovječnosti',
    'Ostalo'
  ];

  const dozvolaDodavanja = ['Administrator', 'Inspektor', 'SEF_STANICE'].includes(auth.user.uloga) && !isReadOnly;

  // Zatvaranje dropdown-a kad se klikne van njega
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
        // Ako nije nista selektovano, resetuj pretragu
        if (!selectedDjeloId) {
          setSearchTerm('');
        }
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [selectedDjeloId]);

  useEffect(() => {
    ucitajPodatke();
  }, [caseId]);

  const ucitajPodatke = async () => {
    try {
      setIsLoading(true);
      setError('');
      
      const [djelaResponse, svaDjelaResponse] = await Promise.all([
        slucajKrivicnoDjeloApi.getBySlucajId(caseId),
        krivicnoDjeloApi.getAll()
      ]);
      
      setDjelaNaSlucaju(djelaResponse.data);
      setSvaDjela(svaDjelaResponse.data);
    } catch (err) {
      setError('Greška pri učitavanju podataka. Pokušajte ponovo.');
    } finally {
      setIsLoading(false);
    }
  };

  // Filtriraj dostupna djela
  const dostupnaDjela = svaDjela.filter(
    djelo => !djelaNaSlucaju.some(dns => dns.djeloId === djelo.id)
  );

  // Filtriraj po pretrazi
  const filtriranaDjela = dostupnaDjela.filter(djelo =>
    searchTerm === '' ||
    djelo.naziv.toLowerCase().includes(searchTerm.toLowerCase()) ||
    djelo.kazneniZakonClan.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (djelo.kategorija && djelo.kategorija.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  // Grupisanje po kategorijama
  const filtriranaPoKategorijama = filtriranaDjela.reduce((grupe, djelo) => {
    const kat = djelo.kategorija || 'Ostalo';
    if (!grupe[kat]) grupe[kat] = [];
    grupe[kat].push(djelo);
    return grupe;
  }, {});

  // Dodavanje postojeceg djela
  const handleDodajPostojeceDjelo = async () => {
    if (!selectedDjeloId) {
      setError('Molimo odaberite krivično djelo sa liste.');
      return;
    }

    const vecPostoji = djelaNaSlucaju.some(d => d.djeloId === selectedDjeloId);
    if (vecPostoji) {
      setError('Ovo krivično djelo je već dodijeljeno slučaju.');
      return;
    }

    setIsAddingExisting(true);
    setError('');
    setSuccess('');

    try {
      await slucajKrivicnoDjeloApi.dodajDjelo(caseId, selectedDjeloId);
      
      setSuccess('Krivično djelo "' + selectedDjeloNaziv + '" je uspješno dodijeljeno slučaju.');
      
      // Resetuj dropdown
      setSelectedDjeloId(null);
      setSelectedDjeloNaziv('');
      setSearchTerm('');
      setShowDropdown(false);
      ucitajPodatke();
      
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError('Greška pri dodavanju. Pokušajte ponovo.');
    } finally {
      setIsAddingExisting(false);
    }
  };

  // Dodavanje novog djela
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setFormError('');
  };

  const handleSubmitNovoDjelo = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFormError('');
    setSuccess('');

    if (!formData.naziv.trim()) {
      setFormError('Naziv krivičnog djela je obavezan.');
      setIsSubmitting(false);
      return;
    }
    if (!formData.kazneniZakonClan.trim()) {
      setFormError('Član kaznenog zakona je obavezan.');
      setIsSubmitting(false);
      return;
    }

    const vecPostoji = svaDjela.some(
      d => d.naziv.toLowerCase() === formData.naziv.toLowerCase() && 
           d.kazneniZakonClan.toLowerCase() === formData.kazneniZakonClan.toLowerCase()
    );

    if (vecPostoji) {
      setFormError('Ovo krivično djelo već postoji u sistemu. Molimo odaberite ga sa liste.');
      setIsSubmitting(false);
      return;
    }

    try {
      const response = await krivicnoDjeloApi.create(formData);
      const novoDjeloId = response.data.id;

      await slucajKrivicnoDjeloApi.dodajDjelo(caseId, novoDjeloId);

      setSuccess('Krivično djelo "' + formData.naziv + '" je uspješno evidentirano i dodijeljeno slučaju.');
      
      setFormData({ naziv: '', kategorija: '', kazneniZakonClan: '' });
      setShowCreateForm(false);
      ucitajPodatke();
      
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setFormError('Greška pri evidentiranju. Provjerite podatke i pokušajte ponovo.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Brisanje djela sa slucaja
  const handleUkloniDjelo = async (vezaId, nazivDjela) => {
    if (!window.confirm('Da li ste sigurni da želite ukloniti "' + nazivDjela + '" sa ovog slučaja?')) {
      return;
    }

    try {
      setError('');
      setSuccess('');
      
      await slucajKrivicnoDjeloApi.ukloniDjelo(caseId, vezaId);
      setSuccess('"' + nazivDjela + '" je uklonjeno sa slučaja.');
      ucitajPodatke();
      
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError('Greška pri uklanjanju. Pokušajte ponovo.');
    }
  };

  if (isLoading) {
    return (
      <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8">
        <div className="text-gray-400 text-center p-6">Učitavanje krivičnih djela...</div>
      </div>
    );
  }

  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8">
      {/* ZAGLAVLJE */}
      <div className="flex justify-between items-center mb-5 border-b border-gray-700 pb-3">
        <div>
          <h2 className="text-2xl font-bold text-white">Krivična djela na slučaju</h2>
          <p className="text-sm text-gray-400 mt-1">
            {djelaNaSlucaju.length} {djelaNaSlucaju.length === 1 ? 'djelo' : 'djela'} dodijeljeno ovom slučaju
          </p>
        </div>
      </div>

      {/* PORUKE */}
      {error && (
        <div className="text-red-400 bg-red-900/30 p-3 rounded-lg mb-4 border border-red-500 text-sm">
          {error}
        </div>
      )}
      {success && (
        <div className="text-green-400 bg-green-900/30 p-3 rounded-lg mb-4 border border-green-500 text-sm">
          {success}
        </div>
      )}

      {/* SEKCIJA 1: PRETRAGA I DODAVANJE POSTOJECEG DJELA */}
      {dozvolaDodavanja && dostupnaDjela.length > 0 && (
        <div className="mb-4 p-4 bg-gray-700/50 rounded-lg border border-gray-600">
          <h3 className="text-sm font-semibold text-gray-200 mb-3">
            Dodaj postojece krivicno djelo ({dostupnaDjela.length} dostupno):
          </h3>
          
          <div className="flex gap-2">
            <div className="flex-1 relative" ref={dropdownRef}>
              {/* Polje za unos - klikom otvara dropdown */}
              <div 
                className="relative"
                onClick={() => {
                  setShowDropdown(true);
                  setTimeout(() => {
                    if (searchInputRef.current) {
                      searchInputRef.current.focus();
                    }
                  }, 100);
                }}
              >
                <input
                  ref={searchInputRef}
                  type="text"
                  placeholder="Kliknite ovdje da vidite sva djela ili ukucajte za pretragu..."
                  value={searchTerm}
                  onChange={(e) => {
                    setSearchTerm(e.target.value);
                    setShowDropdown(true);
                    setError('');
                    if (!e.target.value && !selectedDjeloId) {
                      setSelectedDjeloId(null);
                      setSelectedDjeloNaziv('');
                    }
                  }}
                  onFocus={() => setShowDropdown(true)}
                  className="w-full p-2.5 rounded bg-gray-600 text-white border border-gray-500 
                           focus:border-blue-500 outline-none cursor-pointer placeholder-gray-400"
                  readOnly={!!selectedDjeloId}
                />
                {/* Strelica za dropdown */}
                <svg 
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none"
                  fill="none" stroke="currentColor" viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </div>
              
              {/* Dropdown lista */}
              {showDropdown && (
                <div className="absolute z-10 w-full mt-1 bg-gray-700 border border-gray-500 
                              rounded-md shadow-lg max-h-80 overflow-hidden">
                  
                  {/* Header sa brojem rezultata */}
                  <div className="px-3 py-2 text-xs text-gray-400 bg-gray-800 border-b border-gray-600 sticky top-0">
                    {searchTerm 
                      ? 'Rezultati pretrage: ' + filtriranaDjela.length + ' djela'
                      : 'Sva dostupna djela: ' + dostupnaDjela.length
                    }
                    {selectedDjeloId && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedDjeloId(null);
                          setSelectedDjeloNaziv('');
                          setSearchTerm('');
                        }}
                        className="ml-2 text-blue-400 hover:text-blue-300"
                      >
                        (poništi odabir)
                      </button>
                    )}
                  </div>

                  <div className="overflow-y-auto max-h-64">
                    {filtriranaDjela.length === 0 ? (
                      <div className="p-4 text-center text-gray-400 text-sm">
                        {searchTerm ? (
                          <>
                            Nema rezultata za "{searchTerm}".
                            <button
                              type="button"
                              onClick={() => {
                                setShowCreateForm(true);
                                setFormData(prev => ({ ...prev, naziv: searchTerm }));
                                setShowDropdown(false);
                              }}
                              className="block mx-auto mt-2 text-blue-400 hover:text-blue-300"
                            >
                              Kliknite da unesete novo krivično djelo
                            </button>
                          </>
                        ) : (
                          'Nema dostupnih krivičnih djela.'
                        )}
                      </div>
                    ) : (
                      Object.entries(filtriranaPoKategorijama).map(([kategorija, djela]) => (
                        <div key={kategorija}>
                          <div className="px-3 py-1.5 text-xs font-semibold text-gray-400 uppercase 
                                        bg-gray-800/50 border-b border-gray-600 sticky top-0">
                            {kategorija} ({djela.length})
                          </div>
                          {djela.map(djelo => (
                            <button
                              key={djelo.id}
                              type="button"
                              onClick={() => {
                                setSelectedDjeloId(djelo.id);
                                setSelectedDjeloNaziv(djelo.naziv);
                                setSearchTerm(djelo.naziv);
                                setShowDropdown(false);
                                setError('');
                              }}
                              className={'w-full text-left px-3 py-2.5 transition border-b border-gray-600 last:border-b-0 ' +
                                (selectedDjeloId === djelo.id 
                                  ? 'bg-blue-900/40 border-l-2 border-l-blue-500' 
                                  : 'hover:bg-gray-600')
                              }
                            >
                              <div className="flex items-center justify-between">
                                <span className="text-sm text-white font-medium">{djelo.naziv}</span>
                                {selectedDjeloId === djelo.id && (
                                  <span className="text-xs text-blue-400">Odabrano</span>
                                )}
                              </div>
                              <div className="text-xs text-gray-400 mt-0.5">{djelo.kazneniZakonClan}</div>
                            </button>
                          ))}
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
            
            <button
              onClick={handleDodajPostojeceDjelo}
              disabled={!selectedDjeloId || isAddingExisting}
              className="py-2 px-5 bg-green-600 hover:bg-green-700 disabled:bg-gray-500 
                       disabled:cursor-not-allowed text-white rounded font-semibold 
                       transition whitespace-nowrap text-sm self-start"
            >
              {isAddingExisting ? 'Dodavanje...' : 'Dodaj na slucaj'}
            </button>
          </div>
          
          <div className="mt-4 text-center text-gray-500 text-sm">ili</div>
        </div>
      )}

      {/* SEKCIJA 2: DODAVANJE NOVOG DJELA */}
      {dozvolaDodavanja && (
        <div className="mb-6">
          {!showCreateForm ? (
            <button
              onClick={() => {
                setShowCreateForm(true);
                setFormError('');
                setSuccess('');
              }}
              className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white 
                       rounded font-semibold transition text-sm"
            >
              Unesi novo krivicno djelo
            </button>
          ) : (
            <form onSubmit={handleSubmitNovoDjelo} className="p-4 bg-gray-700/50 rounded-lg border border-gray-500 space-y-4">
              <h3 className="text-lg font-semibold text-blue-400">
                Unos novog krivicnog djela
              </h3>
              
              {formError && (
                <div className="text-red-400 bg-red-900/30 p-3 rounded border border-red-500 text-sm">
                  {formError}
                </div>
              )}

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs text-gray-400 mb-1 ml-1">Naziv krivicnog djela</label>
                  <input
                    type="text"
                    name="naziv"
                    placeholder="npr. Teska kradja"
                    className="p-2 rounded bg-gray-600 text-white border border-gray-500 w-full 
                             focus:border-blue-500 outline-none"
                    value={formData.naziv}
                    onChange={handleChange}
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-xs text-gray-400 mb-1 ml-1">Kategorija</label>
                  <select
                    name="kategorija"
                    className="p-2 rounded bg-gray-600 text-white border border-gray-500 w-full 
                             focus:border-blue-500 outline-none"
                    value={formData.kategorija}
                    onChange={handleChange}
                  >
                    <option value="">-- Odaberite kategoriju --</option>
                    {kategorije.map(kat => (
                      <option key={kat} value={kat}>{kat}</option>
                    ))}
                  </select>
                </div>
                
                <div>
                  <label className="block text-xs text-gray-400 mb-1 ml-1">Clan kaznenog zakona</label>
                  <input
                    type="text"
                    name="kazneniZakonClan"
                    placeholder="npr. KZ FBiH Cl. 286"
                    className="p-2 rounded bg-gray-600 text-white border border-gray-500 w-full 
                             focus:border-blue-500 outline-none"
                    value={formData.kazneniZakonClan}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="text-xs text-gray-400 bg-gray-800/50 p-2 rounded">
                Nakon unosa, krivicno djelo ce biti evidentirano i automatski dodijeljeno ovom slucaju.
              </div>

              <div className="flex gap-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="py-2 px-6 bg-green-600 hover:bg-green-700 text-white rounded 
                           font-semibold disabled:opacity-50 transition text-sm"
                >
                  {isSubmitting ? 'Spremanje...' : 'Evidentiraj i dodijeli slucaju'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateForm(false);
                    setFormData({ naziv: '', kategorija: '', kazneniZakonClan: '' });
                    setFormError('');
                  }}
                  className="py-2 px-4 bg-gray-600 hover:bg-gray-700 text-white rounded transition text-sm"
                >
                  Odustani
                </button>
              </div>
            </form>
          )}
        </div>
      )}

      {/* Poruka ako nema dostupnih djela a forma nije otvorena */}
      {dozvolaDodavanja && dostupnaDjela.length === 0 && !showCreateForm && (
        <div className="mb-6 p-4 bg-gray-700/50 rounded-lg border border-gray-600 text-center">
          <p className="text-gray-400 text-sm mb-2">Sva krivicna djela iz sistema su vec dodijeljena ovom slucaju.</p>
          <button
            onClick={() => {
              setShowCreateForm(true);
              setFormError('');
              setSuccess('');
            }}
            className="text-blue-400 hover:text-blue-300 text-sm font-medium"
          >
            Unesite novo krivicno djelo
          </button>
        </div>
      )}

      {/* TABELA */}
      <div className="overflow-x-auto bg-gray-700 rounded-lg">
        <table className="min-w-full divide-y divide-gray-600">
          <thead className="bg-gray-600/50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase w-12">Br.</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Naziv</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Kategorija</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Clan zakona</th>
              {dozvolaDodavanja && (
                <th className="px-4 py-3 text-center text-xs font-medium text-gray-300 uppercase w-20">Akcija</th>
              )}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-600">
            {djelaNaSlucaju.length === 0 ? (
              <tr>
                <td colSpan={dozvolaDodavanja ? "5" : "4"} className="p-8 text-center text-gray-500">
                  Nema dodijeljenih krivicnih djela.
                </td>
              </tr>
            ) : (
              djelaNaSlucaju.map((veza, index) => (
                <tr key={veza.vezaId} className="hover:bg-gray-600/50 transition duration-150">
                  <td className="px-4 py-3 text-gray-500 text-sm">{index + 1}.</td>
                  <td className="px-4 py-3 text-white font-medium text-sm">{veza.nazivDjela}</td>
                  <td className="px-4 py-3 text-gray-300 text-sm">
                    {veza.kategorija ? (
                      <span className="px-2 py-0.5 text-xs rounded-full bg-blue-900/30 text-blue-400 border border-blue-500">
                        {veza.kategorija}
                      </span>
                    ) : (
                      <span className="text-gray-500">-</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-gray-300 font-mono text-xs">{veza.kazneniZakonClan}</td>
                  {dozvolaDodavanja && (
                    <td className="px-4 py-3 text-center">
                      <button
                        onClick={() => handleUkloniDjelo(veza.vezaId, veza.nazivDjela)}
                        className="text-red-400 hover:text-red-300 text-sm transition"
                        title="Ukloni sa slucaja"
                      >
                        Ukloni
                      </button>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      
      <div className="mt-4 text-xs text-gray-500">
        Uklanjanjem sa slucaja, krivicno djelo ostaje u sistemu i moze se dodijeliti drugim slucajevima.
      </div>
    </div>
  );
}