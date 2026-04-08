import React, { useState, useEffect } from 'react';
import { Shield, Users, Database, AlertTriangle, Plus, Edit, X, Check, Search, User, Lock } from 'lucide-react';
import { employeeApi, validatePassword } from '../api.js';
import AdminChangePasswordModal from './AdminChangePasswordModal.jsx';

// --- Pomoćne (UI) Komponente ---
export function InputPolje({ type, name, placeholder, value, onChange, required = true, disabled = false }) {
  return (
    <input
      type={type}
      name={name}
      placeholder={placeholder}
      required={required}
      disabled={disabled}
      className={`p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600 ${disabled ? 'opacity-60 cursor-not-allowed' : ''}`}
      value={value}
      onChange={onChange}
    />
  );
}

export function LoaderPoruka({ message }) {
  return (
    <div className="flex flex-col items-center justify-center p-4">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-500 mb-2"></div>
      <p className="text-gray-400 text-center">{message}</p>
    </div>
  );
}

export function AkcijaDugme({ isLoading, tekst, tip = 'submit', onClick, disabled = false, variant = 'primary' }) {
  const variants = {
    primary: 'bg-blue-600 hover:bg-blue-700',
    success: 'bg-green-600 hover:bg-green-700',
    danger: 'bg-red-600 hover:bg-red-700',
    warning: 'bg-yellow-600 hover:bg-yellow-700',
  };
  
  return (
    <button
      type={tip}
      onClick={onClick}
      disabled={isLoading || disabled}
      className={`py-2 px-4 font-semibold text-white rounded-md disabled:opacity-50 transition duration-150 ${variants[variant]}`}
    >
      {isLoading ? 'Radim...' : tekst}
    </button>
  );
}

export function PorukaGreske({ message }) {
  if (!message) return null;
  return (
    <div className="text-red-500 text-center bg-red-100 p-3 rounded mb-4 border border-red-400">
      {message}
    </div>
  );
}

export function PorukaUspjeha({ message }) {
  if (!message) return null;
  return (
    <div className="text-green-800 text-center bg-green-100 p-3 rounded mb-4 border border-green-400">
      {message}
    </div>
  );
}

export function Th({ children }) {
  return <th className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">{children}</th>;
}

export function Td({ children, className = '' }) {
  return <td className={`px-6 py-4 whitespace-nowrap text-sm text-gray-200 ${className}`}>{children}</td>;
}

export function Sekcija({ naslov, children }) {
  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8">
      <h2 className="text-2xl font-bold mb-5 text-white border-b border-gray-700 pb-3">{naslov}</h2>
      {children}
    </div>
  );
}

// --- Edit Modal Komponenta ---
function EditModal({ isOpen, onClose, uposlenik, onSave, isSaving, error, success, currentUser }) {
  const [formData, setFormData] = useState({
    ime_prezime: '',
    email: '',
    uloga: 'Forenzičar',
  });

  useEffect(() => {
    if (uposlenik) {
      setFormData({
        ime_prezime: uposlenik.ime_prezime || '',
        email: uposlenik.email || '',
        uloga: uposlenik.naziv_uloge || 'Forenzičar',
      });
    }
  }, [uposlenik]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await onSave(uposlenik.uposlenik_id, formData);
  };

  // Provjera da li je trenutni korisnik pokušava mijenjati samog sebe
  const isEditingSelf = currentUser && uposlenik && currentUser.uposlenik_id === uposlenik.uposlenik_id;
  const isAdmin = uposlenik?.naziv_uloge === 'Administrator';

  if (!isOpen || !uposlenik) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-gray-800 rounded-lg w-full max-w-md">
        <div className="flex justify-between items-center p-6 border-b border-gray-700">
          <h3 className="text-xl font-bold text-white">Uredi uposlenika</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white transition"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <PorukaGreske message={error} />}
          {success && <PorukaUspjeha message={success} />}

          {/* Informacija ako je administrator uredjuje samog sebe */}
          {isEditingSelf && (
            <div className="bg-blue-900/30 border border-blue-500 rounded-lg p-3 mb-2">
              <div className="flex items-center space-x-2 text-blue-300">
                <User className="w-4 h-4" />
                <span className="text-sm font-medium">
                  Uređujete vlastiti profil. Ne možete promijeniti svoju ulogu.
                </span>
              </div>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Ime i prezime
            </label>
            <InputPolje
              type="text"
              name="ime_prezime"
              placeholder="Ime i prezime"
              value={formData.ime_prezime}
              onChange={handleChange}
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Email
            </label>
            <InputPolje
              type="email"
              name="email"
              placeholder="Email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Uloga
            </label>
            <select
              name="uloga"
              value={formData.uloga}
              onChange={handleChange}
              disabled={isEditingSelf && isAdmin}
              className={`p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600 ${
                isEditingSelf && isAdmin ? 'opacity-60 cursor-not-allowed' : ''
              }`}
              required
            >
              <option value="Forenzičar">Forenzičar</option>
              <option value="Inspektor">Inspektor</option>
              {!isEditingSelf && (
                <option value="Administrator">Administrator</option>
              )}
            </select>
            {isEditingSelf && isAdmin && (
              <p className="text-xs text-gray-400 mt-1">
                Administrator ne može promijeniti vlastitu ulogu
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Broj značke
            </label>
            <InputPolje
              type="text"
              name="broj_znacke"
              placeholder="Broj značke"
              value={uposlenik.broj_znacke}
              onChange={handleChange}
              disabled
            />
            <p className="text-xs text-gray-400 mt-1">Broj značke se ne može mijenjati</p>
          </div>

          {/* Status polje - samo ako nije editing self */}
          {!isEditingSelf && (
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">
                Status
              </label>
              <select
                name="status"
                value={formData.status || uposlenik.status || 'Aktivan'}
                onChange={handleChange}
                className="p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
                required
              >
                <option value="Aktivan">Aktivan</option>
                <option value="Otpušten">Otpušten</option>
                <option value="Penzionisan">Penzionisan</option>
              </select>
              <p className="text-xs text-gray-400 mt-1">
                Neaktivni uposlenici ne mogu se logirati u sistem
              </p>
            </div>
          )}

          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-700">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-300 hover:text-white transition"
            >
              Otkaži
            </button>
            <AkcijaDugme
              isLoading={isSaving}
              tekst="Spremi promjene"
              tip="submit"
              variant="success"
            />
          </div>
        </form>
      </div>
    </div>
  );
}

// --- Glavna Komponenta ---
export default function AdminPanel({ auth }) {
  // Stanje (state) za formu za registraciju novog uposlenika
  const [formData, setFormData] = useState({
    ime: '',
    email: '',
    lozinka: '',
    uloga: 'Forenzičar',
    brojZnacke: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Stanje (state) za listu postojećih uposlenika
  const [uposlenici, setUposlenici] = useState([]);
  const [isLoadingList, setIsLoadingList] = useState(true);
  const [errorList, setErrorList] = useState('');

  // Stanje za search
  const [searchTerm, setSearchTerm] = useState('');

  // Stanje za edit modal
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [selectedUposlenik, setSelectedUposlenik] = useState(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [updateError, setUpdateError] = useState('');
  const [updateSuccess, setUpdateSuccess] = useState('');
  const [editingId, setEditingId] = useState(null);

  // Stanje za promjenu lozinke modal
  const [changePasswordModalOpen, setChangePasswordModalOpen] = useState(false);
  const [selectedUposlenikForPassword, setSelectedUposlenikForPassword] = useState(null);

  // Trenutno prijavljeni korisnik (admin)
  const currentUser = auth.user;

  // useEffect kuka: Pokreće se samo jednom, kada se komponenta prvi put učita.
  useEffect(() => {
    const fetchUposlenici = async () => {
      try {
        const response = await employeeApi.getAll();
        setUposlenici(response.data);
      } catch (err) {
        setErrorList(err.response?.data?.message || 'Interna greška servera pri dobavljanju uposlenika.');
      } finally {
        setIsLoadingList(false);
      }
    };

    fetchUposlenici();
  }, []);

  // --- Logika za upravljanje formom ---
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);

    const passError = validatePassword(formData.lozinka);
    if (passError) {
      setError(passError);
      setIsLoading(false);
      return;
    }

    try {
      const newUser = await employeeApi.register(
        formData.ime,
        formData.email,
        formData.lozinka,
        formData.uloga,
        formData.brojZnacke
      );

      setSuccess(`Uposlenik "${newUser.data.ime_prezime}" uspješno kreiran.`);
      setUposlenici(trenutniUposlenici => [newUser.data, ...trenutniUposlenici]);

      setFormData({ ime: '', email: '', lozinka: '', uloga: 'Forenzičar', brojZnacke: '' });

    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri registraciji uposlenika.');
    } finally {
      setIsLoading(false);
    }
  };

  // --- Edit funkcionalnost ---
  const handleEditClick = (uposlenik) => {
    setSelectedUposlenik(uposlenik);
    setEditingId(uposlenik.uposlenik_id);
    setEditModalOpen(true);
    setUpdateError('');
    setUpdateSuccess('');
  };

  const handleUpdateUposlenik = async (id, data) => {
    setIsUpdating(true);
    setUpdateError('');
    setUpdateSuccess('');
    
    // Provjeri da li pokušava deaktivirati samog sebe
    if (data.status && data.status !== 'Aktivan' && id === currentUser.uposlenik_id) {
      setUpdateError('Ne možete deaktivirati samog sebe.');
      setIsUpdating(false);
      return;
    }
    try {
      // Provjera da li administrator pokušava promijeniti svoju ulogu
      const isEditingSelf = currentUser.uposlenik_id === id;
      const originalUposlenik = uposlenici.find(u => u.uposlenik_id === id);
      
      if (isEditingSelf && originalUposlenik?.naziv_uloge === 'Administrator' && data.uloga !== 'Administrator') {
        setUpdateError('Ne možete promijeniti vlastitu ulogu administratora.');
        setIsUpdating(false);
        return;
      }

      await employeeApi.update(
        id,
        data.ime_prezime,
        data.email,
        data.uloga,
        data.status
      );
      
      // Ažuriraj listu uposlenika
      setUposlenici(prev => prev.map(uposlenik => {
        if (uposlenik.uposlenik_id === id) {
          return {
            ...uposlenik,
            ime_prezime: data.ime_prezime,
            email: data.email,
            naziv_uloge: data.uloga,
            status: data.status || uposlenik.status || 'Aktivan'
          };
        }
        return uposlenik;
      }));

      setUpdateSuccess('Uposlenik uspješno ažuriran!');
      
      // Auto zatvori modal nakon 1.5 sekunde
      setTimeout(() => {
        setEditModalOpen(false);
        setSelectedUposlenik(null);
        setEditingId(null);
      }, 1500);
      
    } catch (err) {
      const poruka = err.response?.data?.message || 'Greška pri ažuriranju uposlenika.';
      setUpdateError(poruka);
    } finally {
      setIsUpdating(false);
    }
  };

  // Filter uposlenika po search termu
  const filteredUposlenici = uposlenici.filter(uposlenik => 
    searchTerm === '' ||
    uposlenik.ime_prezime.toLowerCase().includes(searchTerm.toLowerCase()) ||
    uposlenik.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    uposlenik.broj_znacke.includes(searchTerm) ||
    uposlenik.naziv_uloge.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Funkcija za dobijanje boje uloge
  const getRoleColor = (uloga) => {
    switch (uloga) {
      case 'Administrator': return 'bg-red-900/30 text-red-400 border border-red-500';
      case 'Inspektor': return 'bg-blue-900/30 text-blue-400 border border-blue-500';
      case 'Forenzičar': return 'bg-yellow-900/30 text-yellow-400 border border-yellow-500';
      default: return 'bg-gray-900/30 text-gray-400 border border-gray-500';
    }
  };

  return (
    <Sekcija naslov="👨‍💼 Administratorski Panel">

      {/* Forma za registraciju novog uposlenika */}
      <div className="mb-8">
        <h3 className="text-xl font-bold mb-4 text-white">Registruj Novog Uposlenika</h3>
        {error && <PorukaGreske message={error} />}
        {success && <PorukaUspjeha message={success} />}

        <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <InputPolje type="text" name="ime" placeholder="Ime i Prezime" value={formData.ime} onChange={handleChange} />
          <InputPolje type="email" name="email" placeholder="Email" value={formData.email} onChange={handleChange} />
          <InputPolje type="password" name="lozinka" placeholder="Privremena Lozinka" value={formData.lozinka} onChange={handleChange} />
          <InputPolje type="text" name="brojZnacke" placeholder="Broj Značke" value={formData.brojZnacke} onChange={handleChange} />

          <select
            name="uloga"
            value={formData.uloga}
            onChange={handleChange}
            className="p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600"
          >
            <option value="Forenzičar">Forenzičar</option>
            <option value="Inspektor">Inspektor</option>
            <option value="Administrator">Administrator</option>
          </select>

          <AkcijaDugme isLoading={isLoading} tekst="Registruj Uposlenika" variant="primary" />
        </form>
      </div>

      {/* --- Tabela sa Listom Uposlenika --- */}
      <div>
        <div className="flex flex-col md:flex-row md:items-center justify-between mb-4 gap-4">
          <h3 className="text-xl font-bold text-white">
            Postojeći Uposlenici ({uposlenici.length})
          </h3>
          
          {/* Search input */}
          <div className="relative w-full md:w-64">
            <input
              type="text"
              placeholder="Pretraži uposlenike..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full p-2 pl-10 rounded-lg bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
            />
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
          </div>
        </div>

        {searchTerm && (
          <p className="text-sm text-gray-400 mb-2">
            Pronađeno: {filteredUposlenici.length} uposlenika
          </p>
        )}

        {/* Prikaz stanja učitavanja liste */}
        {isLoadingList ? (
          <LoaderPoruka message="Učitavam uposlenike..." />
        ) : errorList ? (
          <PorukaGreske message={errorList} />
        ) : (
          <div className="overflow-x-auto bg-gray-750 rounded-lg shadow border border-gray-700">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-900">
                <tr>
                  <Th>ID</Th>
                  <Th>Ime i Prezime</Th>
                  <Th>Email</Th>
                  <Th>Broj Značke</Th>
                  <Th>Uloga</Th>
                  <Th>Status</Th>
                  <Th>Akcije</Th>
                </tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {filteredUposlenici.length === 0 ? (
                  <tr>
                    <Td colSpan="7" className="text-center text-gray-500 py-8">
                      <svg className="w-12 h-12 mx-auto text-gray-600 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5 2.5a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                      </svg>
                      <p>{searchTerm ? 'Nema rezultata za pretragu' : 'Nema registrovanih uposlenika.'}</p>
                    </Td>
                  </tr>
                ) : (
                  filteredUposlenici.map((uposlenik) => (
                    <tr 
                      key={uposlenik.uposlenik_id} 
                      className={`hover:bg-gray-750 transition-colors ${
                        uposlenik.uposlenik_id === currentUser.uposlenik_id ? 'bg-gray-900/50' : ''
                      }`}
                    >
                      <Td className="font-mono text-xs bg-gray-900 rounded px-2 py-1">
                        {uposlenik.uposlenik_id}
                      </Td>
                      <Td className="font-medium">
                        <div className="flex flex-col">
                          <span>{uposlenik.ime_prezime}</span>
                        </div>
                      </Td>
                      <Td className="text-blue-400">{uposlenik.email}</Td>
                      <Td className="font-mono text-sm">{uposlenik.broj_znacke}</Td>
                      <Td className="px-4 py-3">
                        <div className="flex items-center space-x-2">
                          <span className={`px-3 py-1 text-xs leading-5 font-semibold rounded-full whitespace-nowrap ${getRoleColor(uposlenik.naziv_uloge)}`}>
                            {uposlenik.naziv_uloge}
                          </span>
                          {uposlenik.uposlenik_id === currentUser.uposlenik_id && (
                            <div className="flex items-center" title="Vi ste ovaj korisnik">
                              <User className="w-3 h-3 text-green-400" />
                            </div>
                          )}
                        </div>
                      </Td>
                      <Td>
                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                          (uposlenik.status || 'Aktivan') === 'Aktivan' 
                            ? 'bg-green-900/30 text-green-400 border border-green-500'
                            : uposlenik.status === 'Otpušten'
                            ? 'bg-red-900/30 text-red-400 border border-red-500'
                            : 'bg-yellow-900/30 text-yellow-400 border border-yellow-500'
                        }`}>
                          {uposlenik.status || 'Aktivan'}
                        </span>
                      </Td>
                      <Td>
                        <div className="flex items-center space-x-2">
                          <button
                            onClick={() => handleEditClick(uposlenik)}
                            disabled={editingId === uposlenik.uposlenik_id}
                            className={`flex items-center space-x-1 px-3 py-1 rounded transition ${
                              editingId === uposlenik.uposlenik_id 
                                ? 'bg-gray-700 text-gray-400 cursor-not-allowed' 
                                : 'bg-blue-900/30 text-blue-400 hover:bg-blue-900/50 border border-blue-500'
                            }`}
                            title="Uredi uposlenika"
                          >
                            {editingId === uposlenik.uposlenik_id ? (
                              <>
                                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                <span>Uređivanje...</span>
                              </>
                            ) : (
                              <>
                                <Edit className="w-4 h-4" />
                                <span>Uredi</span>
                              </>
                            )}
                          </button>
                          <button
                            onClick={() => {
                              setSelectedUposlenikForPassword(uposlenik);
                              setChangePasswordModalOpen(true);
                            }}
                            className="flex items-center space-x-1 px-3 py-1 rounded transition bg-yellow-900/30 text-yellow-400 hover:bg-yellow-900/50 border border-yellow-500"
                            title="Promijeni lozinku"
                          >
                            <Lock className="w-4 h-4" />
                            <span>Lozinka</span>
                          </button>
                        </div>
                      </Td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Edit Modal */}
      <EditModal
        isOpen={editModalOpen}
        onClose={() => {
          setEditModalOpen(false);
          setSelectedUposlenik(null);
          setEditingId(null);
          setUpdateError('');
          setUpdateSuccess('');
        }}
        uposlenik={selectedUposlenik}
        onSave={handleUpdateUposlenik}
        isSaving={isUpdating}
        error={updateError}
        success={updateSuccess}
        currentUser={currentUser}
      />

      {/* Change Password Modal */}
      <AdminChangePasswordModal
        isOpen={changePasswordModalOpen}
        onClose={() => {
          setChangePasswordModalOpen(false);
          setSelectedUposlenikForPassword(null);
        }}
        uposlenik={selectedUposlenikForPassword}
      />
    </Sekcija>
  );
} // --- KRAJ AdminPanel KOMPONENTE ---