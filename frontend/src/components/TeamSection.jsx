import React, { useState, useEffect } from 'react';
import { teamApi, employeeApi } from '../api.js'; 

// --- Pomoćne (UI) Komponente ---
function LoaderPoruka({ message }) {
  return (
    <div className="flex flex-col items-center justify-center p-4">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-500 mb-2"></div>
      <p className="text-gray-400 text-center">{message}</p>
    </div>
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

function Sekcija({ naslov, children }) {
  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8 border border-gray-700">
      <h2 className="text-2xl font-bold mb-5 text-white border-b border-gray-700 pb-3">{naslov}</h2>
      {children}
    </div>
  );
}

function Th({ children }) {
  return <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider border-b border-gray-600">{children}</th>;
}

function Td({ children, className = '' }) {
  return <td className={`px-6 py-4 whitespace-nowrap text-sm text-gray-200 ${className}`}>{children}</td>;
}

// --- Glavna Komponenta ---
export default function TeamSection({ caseId, auth, caseStatus }) {
  // Provjeri da li je slučaj u read-only statusu
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  // Stanje za listu članova koji su VEĆ na timu
  const [team, setTeam] = useState([]);
  // Stanje za listu SVIH uposlenika u stanici (za popunjavanje dropdown-a)
  const [allUsers, setAllUsers] = useState([]);
  
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Stanje za formu za dodavanje novog člana
  const [selectedUserId, setSelectedUserId] = useState('');
  const [ulogaNaSlucaju, setUlogaNaSlucaju] = useState('Član tima');
  const [addError, setAddError] = useState('');
  const [addSuccess, setAddSuccess] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  // Provjera permisija
  const isAdmin = auth.user.uloga === 'Administrator';
  const dozvolaDodavanja = ['Administrator', 'Inspektor'].includes(auth.user.uloga) && !isReadOnly;
  // Dobavljanje tima I (eventualno) svih uposlenika
  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        setError('');
        
        // 1. Dobavi tim koji je VEĆ na slučaju (ovo mogu svi)
        const teamResponse = await teamApi.getByCaseId(caseId);
        setTeam(teamResponse.data);

        // 2. Dobavi SVE uposlenike u stanici - SAMO ako je Administrator
        if (dozvolaDodavanja) {
          try {
            const usersResponse = await employeeApi.getAll();
            setAllUsers(usersResponse.data);
          } catch (err) {
            // Ako nije admin, nećemo ni pokušavati dohvatiti sve uposlenike
            setAllUsers([]);
          }
        } else {
          // Za ne-administratore, postavi praznu listu
          setAllUsers([]);
        }

      } catch (err) {
        // Ako greška nije vezana za dohvat svih uposlenika (što je očekivano za ne-administratore)
        if (!dozvolaDodavanja && err.response?.data?.message?.includes('uposlenike')) {
          // Ovo je očekivano - samo ignoriši grešku za ne-administratore
        } else {
          const poruka = err.response?.data?.message || 'Greška pri učitavanju tima.';
          console.error("Greška u TeamSection:", err);
          setError(poruka);
        }
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, [caseId, dozvolaDodavanja]);

  // Izračunaj dostupne uposlenike (oni koji NISU u timu i koji su AKTIVNI)
  const getFilteredAvailableUsers = () => {
    const currentTeamIds = team.map(member => member.uposlenik_id);
    return allUsers.filter(user => 
      !currentTeamIds.includes(user.uposlenik_id) && 
      user.status === 'Aktivan'
    );
  };

  const filteredAvailableUsers = getFilteredAvailableUsers();

  // Postavi default vrijednost za 'select' (prvi slobodan korisnik)
  useEffect(() => {
    if (dozvolaDodavanja) {
      const availableUsers = getFilteredAvailableUsers();
      if (availableUsers.length > 0 && !selectedUserId) {
        setSelectedUserId(availableUsers[0].uposlenik_id);
      } else if (availableUsers.length === 0) {
        setSelectedUserId('');
      }
    }
  }, [team, allUsers, selectedUserId, dozvolaDodavanja]);

  const handleAddMember = async (e) => {
    e.preventDefault();
    setAddError('');
    setAddSuccess('');
    
    // Validacija na frontendu
    if (!selectedUserId || selectedUserId === "") {
      setAddError('Molimo odaberite uposlenika za dodavanje.');
      return;
    }
    
    setIsAdding(true);

    try {
      // Koristimo ispravnu funkciju iz api.js
      const response = await teamApi.addMember(caseId, selectedUserId, ulogaNaSlucaju);
      
      // 1. Dodaj novog člana u lokalnu listu tima
      setTeam(prevTeam => [...prevTeam, response.data]);
      
      // 2. Resetuj formu
      setAddSuccess(`Uposlenik "${response.data.ime_prezime}" uspješno dodan timu.`);
      setUlogaNaSlucaju('Član tima');
      
      // 3. Osvježi podatke
      setTimeout(async () => {
        try {
          const teamResponse = await teamApi.getByCaseId(caseId);
          setTeam(teamResponse.data);
          
          // Ako je admin, osvježi i listu svih uposlenika
          if (dozvolaDodavanja) {
            const usersResponse = await employeeApi.getAll();
            setAllUsers(usersResponse.data);
          }
          
          // 4. Odaberi novog korisnika ako postoji (samo ako ima dozvolu za dodavanje)
          if (dozvolaDodavanja) {
            const updatedCurrentTeamIds = teamResponse.data.map(member => member.uposlenik_id);
            const updatedFilteredUsers = allUsers.filter(user => 
              !updatedCurrentTeamIds.includes(user.uposlenik_id)
            );
            
            if (updatedFilteredUsers.length > 0) {
              setSelectedUserId(updatedFilteredUsers[0].uposlenik_id);
            } else {
              setSelectedUserId('');
            }
          }
        } catch (refreshErr) {
          console.error('Greška pri osvježavanju:', refreshErr);
        }
      }, 100);

    } catch (err) {
      setAddError(err.response?.data?.message || 'Greška pri dodavanju člana tima.');
    } finally {
      setIsAdding(false);
    }
  };

  const handleRemoveMember = async (dodjelaId, memberName) => {
    if (!window.confirm(`Da li ste sigurni da želite ukloniti ${memberName} iz tima?\n\nNapomena: Ovaj uposlenik će izgubiti pristup slučaju.`)) {
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      setAddSuccess('');
      
      await teamApi.removeMember(dodjelaId);
      
      // Osvježi podatke
      const teamResponse = await teamApi.getByCaseId(caseId);
      setTeam(teamResponse.data);
      
      // Samo administratori osvježavaju full listu
      if (dozvolaDodavanja) {
        const usersResponse = await employeeApi.getAll();
        setAllUsers(usersResponse.data);
      }
      
      setAddSuccess(`Uposlenik "${memberName}" uspješno uklonjen iz tima.`);
    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri uklanjanju člana tima.');
    } finally {
      setIsLoading(false);
    }
  };

  // Izračunaj broj dostupnih i ukupnih
  const availableCount = filteredAvailableUsers.length;
  const totalCount = allUsers.length;

  // --- HTML/JSX Prikaz ---
  return (
    <Sekcija naslov="👥 Tim na Slučaju">
      {error && <PorukaGreske message={error} />}
      {addSuccess && <PorukaUspjeha message={addSuccess} />}

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

      {/* Forma za dodavanje člana - SAMO za one koji imaju dozvolu */}
      {dozvolaDodavanja && (
        <div className="mb-6 border-b border-gray-700 pb-6">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h4 className="text-lg font-semibold text-white mb-1">Dodaj Novog Člana Tima</h4>
              <p className="text-sm text-gray-400">Odaberite uposlenika koji nije već u timu</p>
            </div>
            <div className="text-sm text-gray-400">
              {availableCount} dostupno 
              {dozvolaDodavanja && ` / ${totalCount} ukupno`}
            </div>
          </div>
          
          <form onSubmit={handleAddMember} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Odabir Uposlenika */}
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Uposlenik</label>
                {availableCount === 0 ? (
                  <div className="p-3 bg-yellow-900/20 border border-yellow-500 rounded text-yellow-300">
                    <p className="font-medium">⚠️ {dozvolaDodavanja ? 'Svi uposlenici su već u timu' : 'Nema dostupnih uposlenika'}</p>
                    <p className="text-sm mt-1">
                      {dozvolaDodavanja 
                        ? 'Nema dostupnih uposlenika za dodavanje.' 
                        : 'Samo administratori mogu vidjeti listu svih uposlenika.'}
                    </p>
                  </div>
                ) : (
                  <select
                    value={selectedUserId}
                    onChange={(e) => setSelectedUserId(e.target.value)}
                    required
                    className="p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
                  >
                    <option value="" disabled>-- Odaberi uposlenika --</option>
                    {filteredAvailableUsers.map(user => (
                      <option key={user.uposlenik_id} value={user.uposlenik_id}>
                        {user.ime_prezime} • {user.naziv_uloge} • Značka: {user.broj_znacke}
                      </option>
                    ))}
                  </select>
                )}
              </div>

              {/* Unos Uloge na Slučaju */}
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Uloga na slučaju</label>
                <input
                  type="text"
                  value={ulogaNaSlucaju}
                  onChange={(e) => setUlogaNaSlucaju(e.target.value)}
                  required
                  placeholder="npr. Glavni istražitelj, Tehnička podrška..."
                  className="p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
                />
                <p className="text-xs text-gray-500 mt-1">
                  Specifična uloga na ovom slučaju
                </p>
              </div>

              {/* Dugme za Slanje */}
              <div className="flex items-end align items-center pt-2">
                <AkcijaDugme 
                  isLoading={isAdding} 
                  tekst="Dodaj u Tim"
                  disabled={availableCount === 0}
                  boja="green"
                  className="w-full"
                />
              </div>
            </div>

            {addError && <PorukaGreske message={addError} />}
            
            {/* Suggestion za uloge */}
            <div className="text-sm text-gray-400">
              <p className="font-medium mb-1">💡 Prijedlozi uloga:</p>
              <div className="flex flex-wrap gap-2 mt-1">
                {['Glavni istražitelj', 'Pomoćni istražitelj', 'Forenzičar DNK', 'Forenzičar tragova', 'IT podrška', 'Koordinator', 'Dokumentalista'].map(uloga => (
                  <button
                    key={uloga}
                    type="button"
                    onClick={() => setUlogaNaSlucaju(uloga)}
                    className="px-2 py-1 bg-gray-700 hover:bg-gray-600 text-gray-300 text-xs rounded border border-gray-600 transition"
                  >
                    {uloga}
                  </button>
                ))}
              </div>
            </div>
          </form>
        </div>
      )}

      {/* Lista članova tima - OVO MOGU SVI VIDJETI */}
      <div>
        <div className="flex justify-between items-center mb-4">
          <div>
            <h4 className="text-lg font-semibold text-white mb-1">Postojeći Članovi Tima</h4>
            <p className="text-sm text-gray-400">{team.length} članova u timu</p>
          </div>
          <button
            onClick={async () => {
              setIsLoading(true);
              try {
                const teamResponse = await teamApi.getByCaseId(caseId);
                setTeam(teamResponse.data);
                
                // Samo administratori osvježavaju full listu
                if (dozvolaDodavanja) {
                  const usersResponse = await employeeApi.getAll();
                  setAllUsers(usersResponse.data);
                }
              } catch (err) {
                console.error('Greška pri osvježavanju:', err);
              } finally {
                setIsLoading(false);
              }
            }}
            className="text-sm text-gray-400 hover:text-gray-300 flex items-center"
          >
            <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Osvježi
          </button>
        </div>
        
        {isLoading ? (
          <LoaderPoruka message="Učitavam tim..." />
        ) : (
          <div className="overflow-x-auto bg-gray-750 rounded-lg shadow border border-gray-700">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-900">
                <tr>
                  <Th>Ime i Prezime</Th>
                  <Th>Uloga (u stanici)</Th>
                  <Th>Uloga (na slučaju)</Th>
                  <Th>Značka</Th>
                  {/* Prikazuj kolonu Akcije SAMO za Administratora */}
                  {isAdmin && <Th>Akcije</Th>}
                </tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {team.length === 0 ? (
                  <tr>
                    <Td colSpan={isAdmin ? "5" : "4"} className="text-center text-gray-500 py-8">
                      <svg className="w-12 h-12 mx-auto text-gray-600 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5 2.5a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                      </svg>
                      <p className="text-lg mb-2">Tim još nije formiran</p>
                      <p className="text-sm text-gray-400">
                        {dozvolaDodavanja 
                          ? 'Dodajte prve članove tima pomoću gornje forme' 
                          : 'Tim će biti formiran od strane inspektora ili administratora'}
                      </p>
                    </Td>
                  </tr>
                ) : (
                  team.map((member) => (
                    <tr key={member.dodjela_id || member.uposlenik_id} className="hover:bg-gray-750 transition-colors">
                      <Td className="font-medium">{member.ime_prezime}</Td>
                      <Td>
                        <span className={`px-2 py-1 text-xs rounded-full ${
                          member.naziv_uloge === 'Administrator' ? 'bg-red-900/30 text-red-400 border border-red-500' : 
                          member.naziv_uloge === 'Inspektor' ? 'bg-blue-900/30 text-blue-400 border border-blue-500' : 
                          'bg-yellow-900/30 text-yellow-400 border border-yellow-500'
                        }`}>
                          {member.naziv_uloge}
                        </span>
                      </Td>
                      <Td>
                        <div className="font-semibold text-white">
                          {member.uloga_na_slucaju}
                        </div>
                      </Td>
                      <Td className="font-mono text-sm text-gray-400">{member.broj_znacke || 'N/A'}</Td>
                      {/* Prikazuj ćeliju za akcije SAMO za Administratora */}
                      {isAdmin && (
                        <Td>
                          {member.dodjela_id && member.uloga_na_slucaju !== 'Voditelj slučaja' && !isReadOnly && (
                            <button
                              className="text-red-400 hover:text-red-300 text-sm font-medium"
                              title="Ukloni iz tima"
                              onClick={() => handleRemoveMember(member.dodjela_id, member.ime_prezime)}
                            >
                              Ukloni
                            </button>
                          )}
                          {member.uloga_na_slucaju === 'Voditelj slučaja' && (
                            <span className="text-purple-400 text-sm italic">
                              Voditelj
                            </span>
                          )}
                          {isReadOnly && member.uloga_na_slucaju !== 'Voditelj slučaja' && (
                            <span className="text-gray-500 text-sm italic">
                              Read-only
                            </span>
                          )}
                        </Td>
                      )}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Informativna poruka */}
      <div className="mt-6 pt-4 border-t border-gray-700">
        <div className="flex items-start">
          <span className="text-blue-400 mr-2">ℹ️</span>
          <div>
            <p className="text-sm text-gray-300">Korisnici koji su u timu na slučaju mogu:</p>
            <ul className="text-sm text-gray-400 list-disc list-inside mt-1 space-y-1">
              <li>Pregledati sve dokaze na slučaju</li>
              <li>Dodavati nove dokaze i primopredaje</li>
              <li>Potvrđivati primopredaje (ako su primalac)</li>
              <li>Pregledati i dodavati svjedoke (osim forenzičara)</li>
            </ul>
            {isAdmin && (
              <p className="text-sm text-red-400 mt-2">
                ⚠️ Samo Administrator može uklanjati članove tima. Voditelja slučaja ne možete ukloniti.
              </p>
            )}
            {!dozvolaDodavanja && (
              <p className="text-sm text-yellow-400 mt-2">
                ℹ️ Dodavanje novih članova u tim mogu samo Administratori i Inspektori.
              </p>
            )}
          </div>
        </div>
      </div>
    </Sekcija>
  );
}