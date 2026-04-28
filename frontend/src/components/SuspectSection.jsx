import React, { useState, useEffect } from 'react';
import { suspectApi } from '../api.js'; 

export default function SuspectSection({ caseId, auth, caseStatus }) {
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  const [osumnjiceni, setOsumnjiceni] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const today = new Date().toISOString().split('T')[0];

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState({
    imePrezime: '',
    jmbg: '',
    datumRodjenja: '',
    ulicaIBroj: '',
    grad: '',
    postanskiBroj: '',
    drzava: 'Bosna i Hercegovina'
  });

  const [createError, setCreateError] = useState('');
  const [createSuccess, setCreateSuccess] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  useEffect(() => {
    const fetchSuspects = async () => {
      try {
        setIsLoading(true);
        const response = await suspectApi.getByCaseId(caseId);
        setOsumnjiceni(response.data);
      } catch (err) {
        setError('Greška pri dobavljanju osumnjičenih.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchSuspects();
  }, [caseId]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setCreateError('');
    setCreateSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsAdding(true);
    
    if (new Date(formData.datumRodjenja) > new Date()) {
      setCreateError("Datum rođenja ne može biti u budućnosti.");
      setIsAdding(false);
      return;
    }

    try {
      await suspectApi.create(caseId, formData);
      
      const refresh = await suspectApi.getByCaseId(caseId);
      setOsumnjiceni(refresh.data);
      
      setFormData({ 
        imePrezime: '', jmbg: '', datumRodjenja: '', 
        ulicaIBroj: '', grad: '', postanskiBroj: '', drzava: 'Bosna i Hercegovina' 
      });
      setShowCreateForm(false);
      setCreateSuccess('Osumnjičeni uspješno dodan u sistem.');
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Greška pri dodavanju.');
    } finally {
      setIsAdding(false);
    }
  };

  const dozvolaDodavanja = [
    'SEF_STANICE', 'INSPEKTOR', 'Šef stanice', 'Inspektor', 
    'Administrator', 'ADMINISTRATOR', 'ROLE_SEF_STANICE', 'ROLE_INSPEKTOR'
  ].includes(auth.user.uloga) && !isReadOnly;

  if (isLoading && osumnjiceni.length === 0) return <div className="text-gray-400 p-4 text-center">Učitavam osumnjičene...</div>;

  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8">
      <div className="flex justify-between items-center mb-5 border-b border-gray-700 pb-3">
        <h2 className="text-2xl font-bold text-white">Osumnjičeni</h2>
        {dozvolaDodavanja && (
          <button 
            onClick={() => setShowCreateForm(!showCreateForm)}
            className={`py-2 px-4 rounded font-semibold transition ${showCreateForm ? 'bg-gray-600 hover:bg-gray-700' : 'bg-red-600 hover:bg-red-700'} text-white`}
          >
            {showCreateForm ? "Odustani" : "+ Dodaj Osumnjičenog"}
          </button>
        )}
      </div>
      
      {error && <div className="text-red-500 mb-4">{error}</div>}
      {createError && <div className="text-red-400 mb-4 bg-red-900/20 p-2 rounded border border-red-800">{createError}</div>}
      {createSuccess && <div className="text-green-500 mb-4 bg-green-100/10 p-2 rounded border border-green-800/30">{createSuccess}</div>}

      {showCreateForm && (
        <form onSubmit={handleSubmit} className="mb-8 p-4 bg-gray-700/50 rounded-lg border border-gray-600 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="flex flex-col">
            <label className="text-xs text-gray-400 mb-1 ml-1">Ime i Prezime</label>
            <input type="text" name="imePrezime" placeholder="npr. Ivan Ivić" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.imePrezime} onChange={handleChange} required />
          </div>

          <div className="flex flex-col">
            <label className="text-xs text-gray-400 mb-1 ml-1">JMBG</label>
            <input type="text" name="jmbg" placeholder="1234567890123" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.jmbg} onChange={handleChange} required />
          </div>
          
          <div className="flex flex-col">
            <label className="text-xs text-gray-400 mb-1 ml-1">Datum rođenja</label>
            <input 
              type="date" 
              name="datumRodjenja" 
              max={today} 
              className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" 
              value={formData.datumRodjenja} 
              onChange={handleChange} 
              required 
            />
          </div>

          <div className="flex flex-col">
            <label className="text-xs text-gray-400 mb-1 ml-1">Ulica i broj</label>
            <input type="text" name="ulicaIBroj" placeholder="npr. Maršala Tita 1" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.ulicaIBroj} onChange={handleChange} required />
          </div>

          <div className="flex flex-col">
            <label className="text-xs text-gray-400 mb-1 ml-1">Grad</label>
            <input type="text" name="grad" placeholder="npr. Sarajevo" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.grad} onChange={handleChange} required />
          </div>

          <div className="flex flex-col">
            <label className="text-xs text-gray-400 mb-1 ml-1">Poštanski broj</label>
            <input type="text" name="postanskiBroj" placeholder="npr. 71000" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.postanskiBroj} onChange={handleChange} required />
          </div>
          
          <div className="md:col-span-2 mt-2">
            <button 
              type="submit" 
              disabled={isAdding}
              className="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white rounded font-bold disabled:bg-gray-500 transition"
            >
              {isAdding ? "Spremanje u bazu..." : "Potvrdi i Evidentiraj"}
            </button>
          </div>
        </form>
      )}

      <div className="overflow-x-auto">
        <table className="min-w-full bg-gray-700 rounded-lg overflow-hidden">
          <thead>
            <tr className="text-left text-gray-400 border-b border-gray-600 bg-gray-700/50">
              <th className="p-3">Ime i Prezime</th>
              <th className="p-3">JMBG</th>
              <th className="p-3">Datum rođenja</th>
              <th className="p-3">Adresa</th>
            </tr>
          </thead>
          <tbody>
            {osumnjiceni.length === 0 ? (
              <tr><td colSpan="4" className="p-10 text-center text-gray-500 italic">Trenutno nema evidentiranih osumnjičenih lica za ovaj slučaj.</td></tr>
            ) : (
              osumnjiceni.map(o => (
                <tr key={o.osumnjiceniId} className="border-b border-gray-600 hover:bg-gray-600/50 transition duration-150">
                  <td className="p-3 text-white font-medium">{o.imePrezime}</td>
                  <td className="p-3 text-gray-300 font-mono text-sm">{o.jmbg}</td>
                  <td className="p-3 text-gray-300">
                    {o.datumRodjenja ? new Date(o.datumRodjenja).toLocaleDateString('bs-BA') : 'N/A'}
                  </td>
                  <td className="p-3 text-gray-300 text-sm">{o.adresa}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}