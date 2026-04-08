import React, { useState, useEffect } from 'react';
// Uvozimo ISPRAVNE API funkcije iz našeg api.js
import { witnessApi } from '../api.js'; 
// Uvozit ćemo Modal koji ćemo kreirati kasnije
import Modal from './Modal.jsx'; 

// --- Pomoćne (UI) Komponente ---
// Definišemo ih ponovo da fajl bude samostalan

function LoaderPoruka({ message }) {
  return <p className="text-center text-gray-400 p-4">{message}</p>;
}

function PorukaGreske({ message }) {
  if (!message) return null;
  return (
    <div className="text-red-500 text-center bg-red-100 p-3 rounded mb-4 border border-red-400">
      {message}
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

// Trebat će nam i Textarea za duže bilješke
function TextareaPolje({ name, placeholder, value, onChange, required = false }) {
  return (
    <textarea
      name={name}
      placeholder={placeholder}
      required={required}
      rows={3}
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
      {isLoading ? 'Radim...' : tekst}
    </button>
  );
}

function Sekcija({ naslov, children }) {
  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8">
      <h2 className="text-2xl font-bold mb-5 text-white border-b border-gray-700 pb-3">{naslov}</h2>
      {children}
    </div>
  );
}

function Th({ children }) {
  return <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">{children}</th>;
}

function Td({ children, className = '' }) {
  return <td className={`px-6 py-4 whitespace-nowrap text-sm text-gray-200 ${className}`}>{children}</td>;
}

// --- Glavna Komponenta ---

export default function WitnessSection({ caseId, auth, caseStatus }) {
  // Provjeri da li je slučaj u read-only statusu
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  // Stanje (state) za listu svjedoka
  const [svjedoci, setSvjedoci] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Stanje (state) za formu za kreiranje novog svjedoka
  const [showCreateForm, setShowCreateForm] = useState(false);
  // Koristimo ISPRAVNA imena polja koja backend očekuje
  const [formData, setFormData] = useState({
    ime_prezime: '',
    jmbg: '',
    adresa: '',
    kontakt_telefon: '',
    biljeska: ''
  });
  const [createError, setCreateError] = useState('');
  const [createSuccess, setCreateSuccess] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  // NOVO: Stanje za prikaz bilješke u modalu (tvoja ideja)
  const [selectedSvjedok, setSelectedSvjedok] = useState(null); // Čuvamo cijeli objekat

  // useEffect kuka: Dobavljanje svjedoka
  useEffect(() => {
    const fetchWitnesses = async () => {
      try {
        setIsLoading(true);
        setError(''); // Resetuj grešku pri svakom novom učitavanju
        // Koristimo ISPRAVNU API funkciju
        const response = await witnessApi.getByCaseId(caseId);
        setSvjedoci(response.data);
      } catch (err) {
        const poruka = err.response?.data?.message || 'Greška pri dobavljanju svjedoka.';
        console.error("Greška u WitnessSection:", err);
        setError(poruka);
      } finally {
        setIsLoading(false);
      }
    };
    fetchWitnesses();
  }, [caseId]); // Pokreni ponovo ako se promijeni caseId

  // Funkcija koja se poziva na promjenu u input poljima forme
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setCreateError(''); // Obriši stare greške
    setCreateSuccess(''); // Obriši stare poruke o uspjehu
  };

  // Funkcija koja se poziva prilikom slanja forme za kreiranje svjedoka
  const handleSubmit = async (e) => {
    e.preventDefault();
    setCreateError('');
    setCreateSuccess('');
    setIsAdding(true);
    
    try {
      // 1. Slanje podataka na backend (koristimo ISPRAVNU API funkciju)
      // Šaljemo cijeli formData objekat koji sadrži ispravna polja
      const response = await witnessApi.create(caseId, formData);
      
      // 2. Obrada uspješnog odgovora
      setSvjedoci([response.data, ...svjedoci]); // Dodaj na vrh liste
      // Resetuj formu na ispravna polja
      setFormData({ ime_prezime: '', jmbg: '', adresa: '', kontakt_telefon: '', biljeska: '' });
      setShowCreateForm(false); // Sakrij formu
      setCreateSuccess('Svjedok uspješno evidentiran.');

    } catch (err) {
      // 3. Obrada greške sa servera
      setCreateError(err.response?.data?.message || 'Greška pri dodavanju svjedoka.');
    } finally {
      setIsAdding(false);
    }
  };

  const dozvolaDodavanja = ['Administrator', 'Inspektor'].includes(auth.user.uloga) && !isReadOnly;

  // --- HTML/JSX Prikaz ---
  return (
    <Sekcija naslov="Svjedoci">
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

      {/* Forma za dodavanje svjedoka */}
      {dozvolaDodavanja && (
        <div className="mb-6">
          <AkcijaDugme 
            tip="button" 
            isLoading={false} 
            tekst={showCreateForm ? "Zatvori Formu" : "+ Dodaj Svjedoka"}
            onClick={() => {
              setShowCreateForm(!showCreateForm);
              setCreateError(''); // Resetuj poruke pri otvaranju/zatvaranju
              setCreateSuccess('');
            }}
          />
          {showCreateForm && (
            <form onSubmit={handleSubmit} className="mt-4 p-4 bg-gray-700 rounded grid grid-cols-1 md:grid-cols-2 gap-4">
              {createError && <PorukaGreske message={createError} />}
              
              {/* Koristimo ISPRAVNA polja */}
              <InputPolje type="text" name="ime_prezime" placeholder="Ime i Prezime" value={formData.ime_prezime} onChange={handleChange} />
              <InputPolje type="text" name="kontakt_telefon" placeholder="Kontakt (tel/email)" value={formData.kontakt_telefon} onChange={handleChange} />
              <InputPolje type="text" name="jmbg" placeholder="JMBG (Opcionalno)" value={formData.jmbg} onChange={handleChange} required={false} />
              <InputPolje type="text" name="adresa" placeholder="Adresa (Opcionalno)" value={formData.adresa} onChange={handleChange} required={false} />
              
              <div className="md:col-span-2">
                <TextareaPolje 
                  name="biljeska" 
                  placeholder="Početna bilješka (Opcionalno)" 
                  value={formData.biljeska} 
                  onChange={handleChange} 
                  required={false} 
                />
              </div>
              
              <div className="md:col-span-2 flex items-end">
                <AkcijaDugme isLoading={isAdding} tekst="Spremi Svjedoka" />
              </div>
            </form>
          )}
        </div>
      )}

      {/* Lista svjedoka */}
      <h4 className="text-lg font-semibold mb-3 text-white">Evidentirani Svjedoci</h4>
      {isLoading ? (
        <LoaderPoruka message="Učitavam svjedoke..." />
      ) : (
        <div className="overflow-x-auto bg-gray-700 rounded-lg shadow">
          <table className="min-w-full divide-y divide-gray-600">
            <thead className="bg-gray-600">
              <tr>
                <Th>Ime i Prezime</Th>
                <Th>Kontakt</Th>
                <Th>Adresa</Th>
                <Th>JMBG</Th>
                <Th>Bilješka</Th>
              </tr>
            </thead>
            <tbody className="bg-gray-700 divide-y divide-gray-600">
              {svjedoci.length === 0 ? (
                <tr>
                  <Td colSpan="5" className="text-center text-gray-400">Nema evidentiranih svjedoka na ovom slučaju.</Td>
                </tr>
              ) : (
                svjedoci.map((svjedok) => (
                  <tr key={svjedok.svjedok_id} className="hover:bg-gray-600">
                    {/* Koristimo ISPRAVNA imena polja */}
                    <Td>{svjedok.ime_prezime}</Td>
                    <Td>{svjedok.kontakt_telefon || '-'}</Td>
                    <Td>{svjedok.adresa || '-'}</Td>
                    <Td>{svjedok.jmbg || '-'}</Td>
                    <Td>
                      {/* NOVO: Implementacija tvoje ideje za modal */}
                      <button
                        onClick={() => setSelectedSvjedok(svjedok)}
                        className="text-blue-400 hover:underline text-sm font-medium"
                        disabled={!svjedok.biljeska} // Onemogući dugme ako nema bilješke
                        title={svjedok.biljeska ? "Klikni da vidiš cijelu bilješku" : "Nema bilješke"}
                      >
                        {svjedok.biljeska ? "Vidi bilješku" : "-"}
                      </button>
                    </Td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* NOVO: Modal za prikaz kompletne bilješke (Tvoja ideja) */}
            <Modal 
        isOpen={!!selectedSvjedok}
            title={selectedSvjedok ? `Detalji Svjedoka: ${selectedSvjedok.ime_prezime}` : ''} // Koristimo 'title'
        onClose={() => setSelectedSvjedok(null)} // Koristimo ispravan state setter
        size="lg" // Koristimo veći modal da stane više podataka
      >
        {/* Sadržaj modala - prikaži sve podatke */}
        {selectedSvjedok && (
          <div className="space-y-4">
            <div>
              <h4 className="text-sm font-medium text-gray-400">Ime i Prezime</h4>
              <p className="text-lg text-white">{selectedSvjedok.ime_prezime}</p>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-400">Kontakt Telefon</h4>
              <p className="text-lg text-white">{selectedSvjedok.kontakt_telefon || '-'}</p>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-400">Adresa</h4>
              <p className="text-lg text-white">{selectedSvjedok.adresa || '-'}</p>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-400">JMBG</h4>
              <p className="text-lg text-white">{selectedSvjedok.jmbg || '-'}</p>
            </div>
            <div>
              <h4 className="text-sm font-medium text-gray-400">Bilješka</h4>
              <p className="text-gray-300 whitespace-pre-wrap p-4 bg-gray-700 rounded-md">
                {selectedSvjedok.biljeska || 'Nema bilješke.'}
              </p>
            </div>
          </div>
        )}
      </Modal>
    </Sekcija>
  );
} // --- KRAJ WitnessSection KOMPONENTE ---