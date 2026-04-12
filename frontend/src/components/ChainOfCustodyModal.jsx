import React, { useState, useEffect } from 'react';
import { chainOfCustodyApi, evidenceApi, formatStatusDokaza, teamApi } from '../api.js'; 

// --- Pomoćne (UI) Komponente ---
function AkcijaDugme({ isLoading, tekst, tip = 'submit', onClick, disabled = false, boja = 'blue' }) {
  const boje = {
    blue: 'bg-blue-600 hover:bg-blue-700',
    green: 'bg-green-600 hover:bg-green-700',
    red: 'bg-red-600 hover:bg-red-700',
    gray: 'bg-gray-600 hover:bg-gray-700',
    yellow: 'bg-yellow-600 hover:bg-yellow-700'
  };
  
  return (
    <button
      type={tip}
      onClick={onClick}
      disabled={isLoading || disabled}
      className={`py-2 px-4 font-semibold text-white rounded-md disabled:opacity-50 transition duration-150 ${boje[boja]}`}
    >
      {isLoading ? 'Radim...' : tekst}
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
    <div className="text-green-400 bg-green-900/30 p-4 rounded-lg mb-4 border border-green-500">
      <div className="flex items-center">
        <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
        </svg>
        <span>{message}</span>
      </div>
    </div>
  );
}

function InfoPoruka({ message, boja = 'blue' }) {
  if (!message) return null;
  const boje = {
    blue: 'text-blue-300 bg-blue-900/20 border-blue-500',
    yellow: 'text-yellow-300 bg-yellow-900/20 border-yellow-500',
    gray: 'text-gray-300 bg-gray-800 border-gray-600',
    red: 'text-red-300 bg-red-900/20 border-red-500',
    green: 'text-green-300 bg-green-900/20 border-green-500'
  };
  
  return (
    <div className={`p-3 rounded-lg mb-4 border ${boje[boja]}`}>
      <div className="flex items-center">
        <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
        </svg>
        <span>{message}</span>
      </div>
    </div>
  );
}

function LoaderPoruka({ message }) {
  return (
    <div className="flex flex-col items-center justify-center p-8">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-500 mb-4"></div>
      <p className="text-gray-400 text-center">{message}</p>
    </div>
  );
}

function StatusBadge({ status }) {
  const statusi = {
    'Čeka potvrdu': { boja: 'bg-yellow-900/30 text-yellow-400 border-yellow-500', ikona: '⏳', tekst: 'Čeka potvrdu' },
    'Potvrđeno': { boja: 'bg-green-900/30 text-green-400 border-green-500', ikona: '✅', tekst: 'Potvrđeno' },
    'Odbijeno': { boja: 'bg-red-900/30 text-red-400 border-red-500', ikona: '❌', tekst: 'Odbijeno' }
  };
  
  const info = statusi[status] || { boja: 'bg-gray-800 text-gray-300 border-gray-600', ikona: '❓', tekst: status };
  
  return (
    <span className={`px-2 py-1 text-xs rounded-full border flex items-center ${info.boja}`}>
      <span className="mr-1">{info.ikona}</span>
      {info.tekst}
    </span>
  );
}

function Th({ children }) {
  return <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider border-b border-gray-600">{children}</th>;
}

function Td({ children, className = '' }) {
  return <td className={`px-4 py-3 text-sm ${className}`}>{children}</td>;
}

// --- Glavna Komponenta ---
export default function ChainOfCustodyModal({ dokaz, auth, onClose, caseStatus, onPrimopredajaCreated }) {
  // Provjeri da li je slučaj u read-only statusu
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  // Provjeri da li je dokaz poništen
  const isDokazPonisten = (dokaz?.status || 'Odobren') === 'Poništen';
  // Stanje lanca nadzora (historija)
  const [chain, setChain] = useState([]);
  const [trenutniNosilac, setTrenutniNosilac] = useState(null);
  const [mozePredati, setMozePredati] = useState(false);
  const [cekaPotvrdu, setCekaPotvrdu] = useState(false);
  
  // Stanje svih uposlenika (za dropdown)
  const [users, setUsers] = useState([]);
  
  // Stanje za formu
  const [formData, setFormData] = useState({ 
    preuzeo_uposlenik_id: '',
    svrha: ''
  });

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
  const fetchData = async () => {
    setError('');
    setIsLoading(true);
    
    try {      
      // Dobijte teamId iz slučaja dokaza
      const teamId = dokaz.slucaj?.id || dokaz.slucaj?.slucaj_id || dokaz.slucaj_id;
      
      if (!teamId) {
        setError('Dokaz nema povezan slučaj.');
        setIsLoading(false);
        return;
      }
            
      // 1. Provjeri stanje dokaza
      const stanje = await evidenceApi.getStanjeDokaza(dokaz.dokaz_id);      
      setMozePredati(stanje.data.moze_predati);
      setTrenutniNosilac(stanje.data.trenutni_nosilac);
      const statusCekaPotvrdu = stanje.data.trenutni_nosilac?.status === 'Čeka potvrdu';

      // 2. Dobavi lanac nadzora
      const chainResponse = await evidenceApi.getLanacNadzora(dokaz.dokaz_id);
      
      setChain(chainResponse.data.lanac || chainResponse.data);

      // 3. Dobavi listu uposlenika (samo ako dokaz ne čeka potvrdu)
      if (!statusCekaPotvrdu && stanje.data.moze_predati) {
        const usersResponse = await teamApi.getByCaseId(teamId);

        if (!usersResponse.data || usersResponse.data.length === 0) {
          console.warn('⚠️ API vratio praznu listu uposlenika!');
          setUsers([]);
          if (stanje.data.moze_predati) {
            setError('Nema drugih uposlenika u timu ovog slučaja.');
          }
        } else {
          // Filtriraj - isključi trenutnog korisnika
          const filtriraniUposlenici = usersResponse.data.filter(
            u => u.uposlenik_id !== auth.user.uposlenik_id
          );
                  
          setUsers(filtriraniUposlenici);

          // Postavi default vrijednost
          if (filtriraniUposlenici.length > 0 && stanje.data.moze_predati) {
            setFormData(prev => ({ 
              ...prev, 
              preuzeo_uposlenik_id: filtriraniUposlenici[0].uposlenik_id 
            }));
          } else if (filtriraniUposlenici.length === 0 && stanje.data.moze_predati) {
            setError('Nema drugih uposlenika u timu kojima možete predati dokaz.');
          }
        }
      }

    } catch (err) {      
      if (err.response?.status === 403) {
        setError(err.response.data?.message || 'Nemate dozvolu za predaju dokaza.');
      } else if (err.response?.status === 404) {
        setError('Dokaz ili uposlenici nisu pronađeni.');
      } else if (err.message === 'Network Error') {
        setError('Greška u mrežnoj vezi. Provjerite da li je server pokrenut.');
      } else {
        setError(err.response?.data?.message || 'Greška pri učitavanju podataka: ' + err.message);
      }
    } finally {
      setIsLoading(false);
    }
  };
  
  if (dokaz?.dokaz_id) {
    fetchData();
  } else {
    console.error('Dokaz nema dokaz_id!', dokaz);
    setError('Dokaz nije validan.');
    setIsLoading(false);
  }
  
}, [dokaz?.dokaz_id, dokaz?.slucaj?.id, dokaz, auth.user.uposlenik_id]); // Dodajte slucaj.id u dependency array

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
    setSuccess('');
  };

  const handleSubmitPrimopredaju = async (e) => {
    e.preventDefault();
    setSuccess('');
    setError('');
    
    // Validacija na frontendu
    if (!formData.preuzeo_uposlenik_id) {
      setError('Morate odabrati primaoca.');
      return;
    }
    
    if (!formData.svrha.trim()) {
      setError('Morate unijeti svrhu primopredaje.');
      return;
    }

    setIsSubmitting(true);

    try {
      // Koristimo novu funkciju koja automatski provjerava da li korisnik može predati
      await chainOfCustodyApi.evidentirajPrimopredaju(dokaz.dokaz_id, {
        preuzeo_uposlenik_id: formData.preuzeo_uposlenik_id,
        svrha: formData.svrha
      });
      
      setSuccess('Primopredaja uspješno evidentirana! Čeka se potvrda primaoca.');
      setFormData(prev => ({ ...prev, svrha: '' })); // Resetuj samo svrhu

      // Ponovo učitaj podatke
      const stanje = await evidenceApi.getStanjeDokaza(dokaz.dokaz_id);
      setMozePredati(stanje.data.moze_predati);
      setTrenutniNosilac(stanje.data.trenutni_nosilac);
      setCekaPotvrdu(true);

      const chainResponse = await evidenceApi.getLanacNadzora(dokaz.dokaz_id);
      setChain(chainResponse.data.lanac || chainResponse.data);
      
      // Očisti listu uposlenika jer dokaz sada čeka potvrdu
      setUsers([]);

      // Osvježi listu "Moja slanja koja čekaju potvrdu" ako je callback dostupan
      if (onPrimopredajaCreated) {
        setTimeout(() => {
          onPrimopredajaCreated();
        }, 500);
      }

    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri evidentiranju primopredaje.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const dozvolaPrimopredaje = ['Administrator', 'Inspektor', 'Forenzičar'].includes(auth.user.uloga);
  
  // Formatiraj status za prikaz
  const statusInfo = trenutniNosilac ? 
    formatStatusDokaza(dokaz, trenutniNosilac, auth.user.uposlenik_id) : 
    { tekst: 'Učitavam...', boja: 'text-gray-400' };

  // --- HTML/JSX Prikaz ---
  return (
    <div 
      className="fixed inset-0 bg-black bg-opacity-70 z-50 flex justify-center items-center p-4"
      onClick={onClose}
    >
      <div 
        className="bg-gray-800 rounded-xl shadow-2xl w-full max-w-6xl max-h-[90vh] flex flex-col transform transition-all duration-300"
        onClick={e => e.stopPropagation()}
      >
        {/* Zaglavlje modala */}
        <div className="p-6 border-b border-gray-700 flex justify-between items-center sticky top-0 bg-gray-800 rounded-t-xl z-10">
          <div className="flex-1">
            <h3 className="text-2xl font-bold text-white">
              Lanac Nadzora za Dokaz #{dokaz.dokaz_id}
            </h3>
            <p className="text-gray-400 mt-1">
              {dokaz.opis} ({dokaz.tip_dokaza}) • {dokaz.lokacija_pronalaska}
            </p>
          </div>
          <button 
            onClick={onClose} 
            className="text-gray-400 hover:text-white transition rounded-full p-2 hover:bg-gray-700"
            title="Zatvori"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Tijelo Modala */}
        <div className="p-6 overflow-y-auto flex-1">
          {isLoading ? (
            <LoaderPoruka message="Učitavam podatke o lancu nadzora..." />
          ) : error ? (
            <PorukaGreske message={error} />
          ) : null}
          
          {success && <PorukaUspjeha message={success} />}

          {/* Status informacije */}
          <div className={`mb-6 p-4 rounded-lg border ${statusInfo.boja} border-opacity-30`}>
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-lg font-semibold mb-1">Status dokaza</h4>
                <p className="text-sm">
                  {statusInfo.ikona && <span className="mr-2">{statusInfo.ikona}</span>}
                  <span className={statusInfo.boja}>{statusInfo.tekst}</span>
                </p>
                {cekaPotvrdu && (
                  <p className="text-sm text-yellow-400 mt-1">
                    ⏳ Dokaz čeka potvrdu od {trenutniNosilac?.trenutni_nosilac_ime || 'primaoca'}
                  </p>
                )}
              </div>
              {trenutniNosilac && (
                <div className="text-right">
                  <p className="text-sm text-gray-400">Zadnja primopredaja:</p>
                  <p className="text-sm">
                    {trenutniNosilac.zadnja_primopredaja ? 
                      new Date(trenutniNosilac.zadnja_primopredaja).toLocaleString() : 
                      'Nema evidencije'}
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Poruka ako dokaz čeka potvrdu */}
          {cekaPotvrdu && (
            <InfoPoruka 
              message={`⏳ Ovaj dokaz čeka potvrdu od ${trenutniNosilac?.trenutni_nosilac_ime || 'primaoca'}. Ne možete predati dokaz dok se ne potvrdi trenutna primopredaja.`}
              boja="yellow"
            />
          )}

          {/* Historija lanca nadzora */}
          <div className="mb-8">
            <h4 className="text-lg font-semibold text-white mb-4 pb-2 border-b border-gray-700">
              Historija Primopredaje ({chain.length} unosa)
            </h4>
            
            <div className="overflow-x-auto rounded-lg shadow max-h-80 overflow-y-auto border border-gray-700">
              <table className="min-w-full divide-y divide-gray-700">
                <thead className="bg-gray-900 sticky top-0">
                  <tr>
                    <Th>Datum</Th>
                    <Th>Predao</Th>
                    <Th>Preuzeo</Th>
                    <Th>Svrha</Th>
                    <Th>Status potvrde</Th> {/* 👈 Dodajemo novu kolonu */}
                  </tr>
                </thead>
                <tbody className="bg-gray-800 divide-y divide-gray-700">
                  {chain.length === 0 ? (
                    <tr>
                      <Td colSpan="5" className="text-center text-gray-500 py-8">
                        <svg className="w-12 h-12 mx-auto text-gray-600 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <p>Nema historije nadzora za ovaj dokaz.</p>
                      </Td>
                    </tr>
                  ) : (
                    chain.map((unos) => (
                      <tr key={unos.unos_id} className="hover:bg-gray-750 transition-colors">
                        <Td className="font-mono text-xs">
                          {new Date(unos.datum_primopredaje).toLocaleString()}
                        </Td>
                        <Td>
                          <div className="flex items-center">
                            {unos.predao_ime ? (
                              <>
                                <div className="w-2 h-2 rounded-full bg-blue-500 mr-2"></div>
                                <span>{unos.predao_ime}</span>
                              </>
                            ) : (
                              <span className="text-gray-500 italic">Početno prikupljanje</span>
                            )}
                          </div>
                        </Td>
                        <Td>
                          <div className="flex items-center">
                            <div className="w-2 h-2 rounded-full bg-green-500 mr-2"></div>
                            <span>{unos.preuzeo_ime}</span>
                          </div>
                        </Td>
                        <Td className="max-w-xs">
                          <div className="truncate" title={unos.svrha_primopredaje}>
                            {unos.svrha_primopredaje || '-'}
                          </div>
                        </Td>
                        <Td>
                          <div className="flex flex-col space-y-1">
                            <StatusBadge status={unos.potvrda_status || 'Potvrđeno'} />
                            {unos.potvrda_napomena && (
                              <span className="text-xs text-gray-400 truncate" title={unos.potvrda_napomena}>
                                {unos.potvrda_napomena}
                              </span>
                            )}
                            {unos.potvrda_datum && unos.potvrda_status !== 'Čeka potvrdu' && (
                              <span className="text-xs text-gray-500">
                                {new Date(unos.potvrda_datum).toLocaleDateString()}
                              </span>
                            )}
                            {unos.potvrdio_ime && unos.potvrda_status !== 'Čeka potvrdu' && (
                              <span className="text-xs text-gray-500">
                                Potvrdio: {unos.potvrdio_ime}
                              </span>
                            )}
                          </div>
                        </Td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Forma za Primopredaju */}
          {dozvolaPrimopredaje && !isLoading && !cekaPotvrdu && (
            <div className="mt-8 pt-6 border-t border-gray-700">
              <div className="flex justify-between items-center mb-6">
                <h4 className="text-lg font-semibold text-white">Evidentiraj Novu Primopredaju</h4>
                {!mozePredati && trenutniNosilac && (
                  <div className="text-sm bg-yellow-900/30 text-yellow-300 px-3 py-1 rounded-full border border-yellow-500">
                    <span className="mr-1">⚠️</span> Samo {trenutniNosilac.trenutni_nosilac_ime} može predati
                  </div>
                )}
              </div>

              {/* Poruka ako je slučaj u read-only statusu ili dokaz poništen */}
              {(isReadOnly || isDokazPonisten) && (
                <div className={`mb-6 p-4 border rounded-lg ${
                  isDokazPonisten 
                    ? 'bg-red-900/20 border-red-500' 
                    : 'bg-yellow-900/20 border-yellow-500'
                }`}>
                  <div className="flex items-center">
                    <span className={`mr-2 ${isDokazPonisten ? 'text-red-400' : 'text-yellow-400'}`}>
                      {isDokazPonisten ? '🚫' : '⚠️'}
                    </span>
                    <p className={isDokazPonisten ? 'text-red-300' : 'text-yellow-300'}>
                      {isDokazPonisten 
                        ? 'Dokaz je poništen. Lanac nadzora je onemogućen.'
                        : `Slučaj je u statusu "${caseStatus}". Samo pregled podataka je dozvoljen, izmjene nisu moguće.`
                      }
                    </p>
                  </div>
                </div>
              )}
              
              {mozePredati && !isReadOnly && !isDokazPonisten ? (
                <form onSubmit={handleSubmitPrimopredaju} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-2">
                        Novi Primalac Dokaza
                      </label>
                      {users.length === 0 ? (
                        <div className="p-3 bg-yellow-900/20 border border-yellow-500 rounded text-yellow-300">
                          <p className="font-medium">⚠️ Nema dostupnih uposlenika</p>
                          <p className="text-sm mt-1">Dodajte nove uposlenike preko Administratorskog panela.</p>
                        </div>
                      ) : (
                        <>
                          <select
                            name="preuzeo_uposlenik_id"
                            value={formData.preuzeo_uposlenik_id}
                            onChange={handleChange}
                            required
                            className="p-3 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
                          >
                            <option value="" disabled>-- Odaberi Uposlenika --</option>
                            {users.map(user => (
                              <option key={user.uposlenik_id} value={user.uposlenik_id}>
                                {user.ime_prezime} • {user.naziv_uloge} • Značka: {user.broj_znacke}
                              </option>
                            ))}
                          </select>
                          <p className="text-xs text-gray-500 mt-1">
                            Ova osoba će morati potvrditi primopredaju
                          </p>
                        </>
                      )}
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-2">
                        Svrha (razlog) primopredaje
                      </label>
                      <input
                        type="text"
                        name="svrha"
                        placeholder="npr. Predaja forenzičaru za analizu, Pohrana u trezor..."
                        value={formData.svrha}
                        onChange={handleChange}
                        required
                        className="p-3 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
                      />
                      <p className="text-xs text-gray-500 mt-1">
                        Jasno opišite razlog predaje
                      </p>
                    </div>
                  </div>

                  <div className="bg-blue-900/20 border border-blue-500 p-4 rounded-lg">
                    <div className="flex items-start">
                      <span className="text-blue-400 mr-2">ℹ️</span>
                      <div>
                        <p className="text-blue-300 font-medium">Važna napomena:</p>
                        <p className="text-blue-200 text-sm mt-1">
                          Nakon predaje, dokaz će biti zaključan dok {(formData.preuzeo_uposlenik_id && users.find(u => u.uposlenik_id === formData.preuzeo_uposlenik_id)?.ime_prezime) || 'primalac'} ne potvrdi primopredaju.
                          Ako primalac odbije, dokaz će biti automatski vraćen vama.
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="flex gap-3 pt-4 border-t border-gray-700">
                    <AkcijaDugme 
                      isLoading={isSubmitting} 
                      tekst="Evidentiraj Primopredaju"
                      boja="green"
                      disabled={users.length === 0 || isReadOnly}
                    />
                    <AkcijaDugme 
                      tip="button"
                      isLoading={false} 
                      tekst="Poništi"
                      onClick={onClose}
                      boja="gray"
                    />
                  </div>
                </form>
              ) : isReadOnly ? (
                <InfoPoruka 
                  message={`Slučaj je u statusu "${caseStatus}". Samo pregled podataka je dozvoljen.`}
                  boja="yellow"
                />
              ) : (
                <InfoPoruka 
                  message={`Samo ${trenutniNosilac?.trenutni_nosilac_ime || 'trenutni nosilac'} može predati ovaj dokaz.`}
                  boja="yellow"
                />
              )}
            </div>
          )}

          {/* Ako dokaz čeka potvrdu, prikaži informaciju */}
          {cekaPotvrdu && dozvolaPrimopredaje && (
            <div className="mt-8 pt-6 border-t border-gray-700">
              <h4 className="text-lg font-semibold text-white mb-4">Status predaje</h4>
              <div className="bg-yellow-900/20 border border-yellow-500 p-6 rounded-lg">
                <div className="flex items-start">
                  <span className="text-yellow-400 text-2xl mr-3">⏳</span>
                  <div>
                    <h5 className="text-yellow-300 font-semibold text-lg">Dokaz čeka potvrdu</h5>
                    <p className="text-yellow-200 mt-2">
                      Trenutno ne možete predati ovaj dokaz jer čeka potvrdu od <span className="font-semibold">{trenutniNosilac?.trenutni_nosilac_ime || 'primaoca'}</span>.
                    </p>
                    <p className="text-yellow-200 mt-1">
                      Kada primalac potvrdi primopredaju, dokaz će biti dostupan za dalje predaje.
                    </p>
                    <p className="text-yellow-200 mt-3 text-sm">
                      <span className="font-semibold">Šta se dešava ako primalac odbije?</span><br/>
                      Ako primalac odbije primopredaju, dokaz će biti automatski vraćen vama i možete ga ponovo predati.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}