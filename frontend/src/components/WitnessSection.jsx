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
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  const [svjedoci, setSvjedoci] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const [showCreateForm, setShowCreateForm] = useState(false);
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
  const [selectedSvjedok, setSelectedSvjedok] = useState(null);

  useEffect(() => {
    const fetchWitnesses = async () => {
      try {
        setIsLoading(true);
        setError('');
        const response = await witnessApi.getByCaseId(caseId);
        setSvjedoci(response.data);
      } catch (err) {
        setError('Greška pri dobavljanju svjedoka.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchWitnesses();
  }, [caseId]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setCreateError('');
    setCreateSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsAdding(true);
    try {
      const response = await witnessApi.create(caseId, formData);
      setSvjedoci([response.data, ...svjedoci]);
      setFormData({ ime_prezime: '', jmbg: '', adresa: '', kontakt_telefon: '', biljeska: '' });
      setShowCreateForm(false);
      setCreateSuccess('Svjedok uspješno evidentiran.');
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Greška pri dodavanju.');
    } finally {
      setIsAdding(false);
    }
  };

  const dozvolaDodavanja = ['Administrator', 'Inspektor', 'SEF_STANICE', 'Šef stanice'].includes(auth.user.uloga) && !isReadOnly;

  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8">
      {/* ZAGLAVLJE SA DUGMETOM NA DESNOJ STRANI */}
      <div className="flex justify-between items-center mb-5 border-b border-gray-700 pb-3">
        <h2 className="text-2xl font-bold text-white">Svjedoci</h2>
        {dozvolaDodavanja && (
          <button 
            onClick={() => {
              setShowCreateForm(!showCreateForm);
              setCreateError('');
              setCreateSuccess('');
            }}
            className={`py-2 px-4 rounded font-semibold transition ${showCreateForm ? 'bg-gray-600 hover:bg-gray-700' : 'bg-blue-600 hover:bg-blue-700'} text-white`}
          >
            {showCreateForm ? "Zatvori" : "+ Dodaj Svjedoka"}
          </button>
        )}
      </div>

      {error && <PorukaGreske message={error} />}
      {createSuccess && <PorukaUspjeha message={createSuccess} />}

      {isReadOnly && (
        <div className="mb-6 p-4 bg-yellow-900/20 border border-yellow-500 rounded-lg">
          <p className="text-yellow-300 text-sm italic">
            ⚠️ Slučaj je u statusu "{caseStatus}". Izmjene su onemogućene.
          </p>
        </div>
      )}

      {/* FORMA ZA DODAVANJE (Prikazuje se ispod zaglavlja) */}
      {showCreateForm && (
        <form onSubmit={handleSubmit} className="mb-8 p-4 bg-gray-700/50 rounded-lg border border-gray-600 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="md:col-span-2">
            {createError && <PorukaGreske message={createError} />}
          </div>
          <InputPolje type="text" name="ime_prezime" placeholder="Ime i Prezime" value={formData.ime_prezime} onChange={handleChange} />
          <InputPolje type="text" name="kontakt_telefon" placeholder="Kontakt (tel/email)" value={formData.kontakt_telefon} onChange={handleChange} />
          <InputPolje type="text" name="jmbg" placeholder="JMBG (Opcionalno)" value={formData.jmbg} onChange={handleChange} required={false} />
          <InputPolje type="text" name="adresa" placeholder="Adresa (Opcionalno)" value={formData.adresa} onChange={handleChange} required={false} />
          <div className="md:col-span-2">
            <TextareaPolje name="biljeska" placeholder="Početna bilješka (Opcionalno)" value={formData.biljeska} onChange={handleChange} />
          </div>
          <div className="md:col-span-2">
            <AkcijaDugme isLoading={isAdding} tekst="Potvrdi i Spremi Svjedoka" />
          </div>
        </form>
      )}

      {/* TABELA SVJEDOKA */}
      <div className="overflow-x-auto bg-gray-700 rounded-lg shadow">
        <table className="min-w-full divide-y divide-gray-600">
          <thead className="bg-gray-600/50">
            <tr>
              <Th>Ime i Prezime</Th>
              <Th>Kontakt</Th>
              <Th>Adresa</Th>
              <Th>JMBG</Th>
              <Th>Bilješka</Th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-600">
            {isLoading ? (
              <tr><td colSpan="5" className="p-4 text-center text-gray-400 italic">Učitavam...</td></tr>
            ) : svjedoci.length === 0 ? (
              <tr><td colSpan="5" className="p-6 text-center text-gray-500">Nema evidentiranih svjedoka.</td></tr>
            ) : (
              svjedoci.map((svjedok) => (
                <tr key={svjedok.svjedok_id} className="hover:bg-gray-600 transition">
                  <Td>{svjedok.ime_prezime}</Td>
                  <Td>{svjedok.kontakt_telefon || '-'}</Td>
                  <Td>{svjedok.adresa || '-'}</Td>
                  <Td>{svjedok.jmbg || '-'}</Td>
                  <Td>
                    <button
                      onClick={() => setSelectedSvjedok(svjedok)}
                      className="text-blue-400 hover:underline text-xs font-bold"
                    >
                      {svjedok.biljeska ? "VIDI BILJEŠKU" : "-"}
                    </button>
                  </Td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* MODAL */}
      <Modal 
        isOpen={!!selectedSvjedok}
        title={selectedSvjedok ? `Detalji: ${selectedSvjedok.ime_prezime}` : ''}
        onClose={() => setSelectedSvjedok(null)}
        size="lg"
      >
        {selectedSvjedok && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
                <div><h4 className="text-xs text-gray-400">JMBG</h4><p className="text-white">{selectedSvjedok.jmbg || '-'}</p></div>
                <div><h4 className="text-xs text-gray-400">Kontakt</h4><p className="text-white">{selectedSvjedok.kontakt_telefon || '-'}</p></div>
            </div>
            <div>
              <h4 className="text-xs text-gray-400">Bilješka</h4>
              <p className="text-gray-300 whitespace-pre-wrap p-4 bg-gray-900 rounded-md mt-1 border border-gray-700">
                {selectedSvjedok.biljeska || 'Nema bilješke.'}
              </p>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
} // --- KRAJ WitnessSection KOMPONENTE ---