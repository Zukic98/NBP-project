import React, { useState, useEffect } from 'react';
import { Shield, Users, Plus, Edit, X, Search, Lock } from 'lucide-react';
import { employeeApi, validatePassword } from '../api.js';
import AdminChangePasswordModal from './AdminChangePasswordModal.jsx';

export function InputPolje({ type = 'text', name, placeholder, value, onChange, required = true, disabled = false }) {
  return (
    <input
      type={type} name={name} placeholder={placeholder} required={required} disabled={disabled}
      className={`p-2 rounded bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 w-full border border-gray-600 ${disabled ? 'opacity-60 cursor-not-allowed' : ''}`}
      value={value} onChange={onChange}
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
      type={tip} onClick={onClick} disabled={isLoading || disabled}
      className={`py-2 px-4 font-semibold text-white rounded-md disabled:opacity-50 transition duration-150 ${variants[variant]}`}
    >
      {isLoading ? 'Radim...' : tekst}
    </button>
  );
}

export function PorukaGreske({ message }) {
  if (!message) return null;
  return (
    <div className="text-red-500 text-center bg-red-100 p-3 rounded mb-4 border border-red-400 text-sm font-medium">{message}</div>
  );
}

export function PorukaUspjeha({ message }) {
  if (!message) return null;
  return (
    <div className="text-green-800 text-center bg-green-100 p-3 rounded mb-4 border border-green-400 text-sm font-medium">{message}</div>
  );
}

export function Th({ children }) { return <th className="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">{children}</th>; }
export function Td({ children, className = '' }) { return <td className={`px-6 py-4 whitespace-nowrap text-sm text-gray-200 ${className}`}>{children}</td>; }

export function Sekcija({ naslov, children }) {
  return (
    <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8 border border-gray-700">
      <h2 className="text-2xl font-bold mb-5 text-white border-b border-gray-700 pb-3">{naslov}</h2>
      {children}
    </div>
  );
}

function EditModal({ isOpen, onClose, uposlenik, onSave, isSaving, error, success }) {
  const [formData, setFormData] = useState({ ime: '', prezime: '', email: '', status: 'Aktivan' });

  useEffect(() => {
    if (uposlenik) {
      setFormData({
        ime: uposlenik.ime || '',
        prezime: uposlenik.prezime || '',
        email: uposlenik.email || '',
        status: uposlenik.status || 'Aktivan',
      });
    }
  }, [uposlenik]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await onSave(uposlenik.userId, formData);
  };

  if (!isOpen || !uposlenik) return null;

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
      <div className="bg-gray-800 rounded-lg w-full max-w-md border border-gray-700 shadow-2xl">
        <div className="flex justify-between items-center p-6 border-b border-gray-700">
          <h3 className="text-xl font-bold text-white flex items-center gap-2">
            <Edit className="w-5 h-5 text-blue-400" /> Uredi Podatke Uposlenika
          </h3>
          <button onClick={onClose} className="text-gray-400 hover:text-white"><X /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <PorukaGreske message={error} />}
          {success && <PorukaUspjeha message={success} />}
          
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-gray-400 mb-1">Ime</label>
              <InputPolje name="ime" value={formData.ime} onChange={handleChange} />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-400 mb-1">Prezime</label>
              <InputPolje name="prezime" value={formData.prezime} onChange={handleChange} />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-400 mb-1">Email</label>
            <InputPolje type="email" name="email" value={formData.email} onChange={handleChange} />
          </div>

          <div>
            <label className="block text-xs font-medium text-gray-400 mb-1">Status Radnog Odnosa</label>
            <select
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="p-2 rounded bg-gray-700 text-white w-full border border-gray-600 focus:ring-2 focus:ring-blue-500"
            >
              <option value="Aktivan">Aktivan</option>
              <option value="Otpušten">Otpušten</option>
              <option value="Penzionisan">Penzionisan</option>
            </select>
          </div>

          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-700">
            <button type="button" onClick={onClose} className="px-4 py-2 text-gray-300 hover:text-white">Otkaži</button>
            <AkcijaDugme isLoading={isSaving} tekst="Sačuvaj Promjene" variant="success" />
          </div>
        </form>
      </div>
    </div>
  );
}

export default function AdminPanel({ auth }) {
  const [empMsg, setEmpMsg] = useState({ error: '', success: '', loading: false });
  const [updateMsg, setUpdateMsg] = useState({ error: '', success: '', loading: false });

  const [formData, setFormData] = useState({ 
    firstName: '', 
    lastName: '', 
    email: '', 
    username: '', 
    password: '', 
    roleId: 81, 
    brojZnacke: '' 
  });
  
  const [uposlenici, setUposlenici] = useState([]);
  const [isLoadingList, setIsLoadingList] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [selectedUposlenik, setSelectedUposlenik] = useState(null);
  const [changePasswordModalOpen, setChangePasswordModalOpen] = useState(false);
  const [selectedUposlenikForPassword, setSelectedUposlenikForPassword] = useState(null);

  useEffect(() => { fetchUposlenici(); }, []);

  const fetchUposlenici = async () => {
    try {
      const response = await employeeApi.getAll();
      setUposlenici(response.data);
    } catch (err) { console.error(err); } finally { setIsLoadingList(false); }
  };

  const handleEmployeeSubmit = async (e) => {
    e.preventDefault();
    setEmpMsg({ error: '', success: '', loading: true });
    const passError = validatePassword(formData.password);
    if (passError) { setEmpMsg({ error: passError, success: '', loading: false }); return; }

    try {
      await employeeApi.register(formData);
      setEmpMsg({ error: '', success: 'Uposlenik uspješno kreiran.', loading: false });
      fetchUposlenici();
      setFormData({ firstName: '', lastName: '', email: '', username: '', password: '', roleId: 81, brojZnacke: '' });
      setTimeout(() => setEmpMsg(prev => ({ ...prev, success: '' })), 3000);
    } catch (err) {
      setEmpMsg({ error: err.response?.data || 'Greška pri registraciji.', success: '', loading: false });
    }
  };

  const handleUpdateUposlenik = async (id, data) => {
    setUpdateMsg({ error: '', success: '', loading: true });
    try {
      if (data.status !== selectedUposlenik.status) {
        await employeeApi.updateStatus(id, data.status);
      }
      await employeeApi.update(id, {
        ime: data.ime,
        prezime: data.prezime,
        email: data.email
      });
      setUpdateMsg({ error: '', success: 'Podaci uspješno ažurirani!', loading: false });
      fetchUposlenici();
      setTimeout(() => {
        setEditModalOpen(false);
        setUpdateMsg({ error: '', success: '', loading: false });
      }, 1500);
    } catch (err) {
      setUpdateMsg({ error: err.response?.data || 'Greška pri ažuriranju.', success: '', loading: false });
    }
  };

  const filteredUposlenici = uposlenici.filter(u => 
    `${u.ime} ${u.prezime}`.toLowerCase().includes(searchTerm.toLowerCase()) || 
    u.brojZnacke.includes(searchTerm)
  );

  return (
    <div className="space-y-8">
      <Sekcija naslov="👨‍💼 Upravljanje Uposlenicima">
        <div className="mb-10 bg-gray-900/30 p-5 rounded-lg border border-gray-700">
          <h3 className="text-lg font-bold mb-4 text-blue-400 flex items-center"><Plus className="mr-2 w-5 h-5" /> Dodaj Uposlenika</h3>
          <PorukaGreske message={empMsg.error} />
          <PorukaUspjeha message={empMsg.success} />
          <form onSubmit={handleEmployeeSubmit} className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <InputPolje name="firstName" placeholder="Ime" value={formData.firstName} onChange={e => setFormData({...formData, firstName: e.target.value})} />
            <InputPolje name="lastName" placeholder="Prezime" value={formData.lastName} onChange={e => setFormData({...formData, lastName: e.target.value})} />
            <InputPolje name="email" placeholder="Email" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} />
            <InputPolje type="password" name="password" placeholder="Lozinka" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} />
            <InputPolje name="username" placeholder="Korisničko ime" value={formData.username} onChange={e => setFormData({...formData, username: e.target.value})} />
            <InputPolje name="brojZnacke" placeholder="Broj Značke" value={formData.brojZnacke} onChange={e => setFormData({...formData, brojZnacke: e.target.value})} />
            
            <select 
              value={formData.roleId} 
              onChange={e => setFormData({...formData, roleId: parseInt(e.target.value)})} 
              className="p-2 rounded bg-gray-700 text-white border border-gray-600 focus:ring-2 focus:ring-blue-500"
            >
              <option value={81}>Inspektor</option>
              <option value={82}>Policijski sluzbenik</option>
              <option value={83}>Forenzicar</option>
              <option value={100}>Šef Stanice</option>

            </select>
            
            <AkcijaDugme isLoading={empMsg.loading} tekst="Dodaj" />
          </form>
        </div>

        {/* LISTA UPOSLENIKA */}
        <div className="flex justify-between items-center mb-6">
          <h3 className="text-xl font-bold text-white">Lista uposlenika</h3>
          <div className="relative">
            <input type="text" placeholder="Pretraži po imenu ili znački..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} className="p-2 pl-10 rounded-lg bg-gray-700 text-white border border-gray-600 w-64" />
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          </div>
        </div>

        {isLoadingList ? <LoaderPoruka message="Učitavam listu..." /> : (
          <div className="overflow-x-auto rounded-lg border border-gray-700">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-900/50">
                <tr><Th>Ime i Prezime</Th><Th>Značka</Th><Th>Uloga</Th><Th>Status</Th><Th>Akcije</Th></tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {filteredUposlenici.map((u) => (
                  <tr key={u.userId} className="hover:bg-gray-700/50 transition-colors">
                    <Td>{u.ime} {u.prezime}</Td>
                    <Td className="font-mono">{u.brojZnacke}</Td>
                    <Td><span className="px-2 py-1 text-xs rounded-full bg-blue-900/30 text-blue-400 border border-blue-500">{u.nazivUloge}</span></Td>
                    <Td>
                        <span className={`px-2 py-1 text-xs rounded-full border ${
                            u.status === 'Aktivan' ? 'border-green-500 text-green-400' : 
                            u.status === 'Otpušten' ? 'border-red-500 text-red-400' : 'border-yellow-500 text-yellow-400'
                        }`}>
                            {u.status || 'Aktivan'}
                        </span>
                    </Td>
                    <Td>
                      <div className="flex space-x-2">
                        <button 
                          onClick={() => { 
                            setUpdateMsg({ error: '', success: '', loading: false });
                            setSelectedUposlenik(u); 
                            setEditModalOpen(true); 
                          }} 
                          className="p-1 text-blue-400 hover:bg-blue-400/10 rounded transition-colors"
                          title="Uredi podatke"
                        >
                          <Edit className="w-4 h-4"/>
                        </button>
                        <button 
                          onClick={() => { 
                            setSelectedUposlenikForPassword(u); 
                            setChangePasswordModalOpen(true); 
                          }} 
                          className="p-1 text-yellow-400 hover:bg-yellow-400/10 rounded transition-colors"
                          title="Promijeni lozinku"
                        >
                          <Lock className="w-4 h-4"/>
                        </button>
                      </div>
                    </Td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Sekcija>

      <EditModal 
        isOpen={editModalOpen} 
        onClose={() => setEditModalOpen(false)} 
        uposlenik={selectedUposlenik} 
        onSave={handleUpdateUposlenik} 
        isSaving={updateMsg.loading}
        error={updateMsg.error}
        success={updateMsg.success}
      />

      {changePasswordModalOpen && (
        <AdminChangePasswordModal 
          isOpen={changePasswordModalOpen} 
          onClose={() => setChangePasswordModalOpen(false)} 
          uposlenik={selectedUposlenikForPassword} 
        />
      )}
    </div>
  );
}