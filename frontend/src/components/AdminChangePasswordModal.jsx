import React, { useState } from 'react';
import { X, Lock, User } from 'lucide-react';
import { employeeApi, validatePassword } from '../api.js';

// Pomoćne komponente
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

export default function AdminChangePasswordModal({ isOpen, onClose, uposlenik }) {
  const [formData, setFormData] = useState({
    novaLozinka: '',
    potvrdaLozinke: '',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    // Resetuj poruke kada korisnik počne tipkati
    if (error) setError('');
    if (success) setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);

    // Validacija
    if (!formData.novaLozinka) {
      setError('Nova lozinka je obavezna.');
      setIsLoading(false);
      return;
    }

    // Validacija jačine nove lozinke
    const passError = validatePassword(formData.novaLozinka);
    if (passError) {
      setError(passError);
      setIsLoading(false);
      return;
    }

    // Provjeri da li se nova lozinka i potvrda podudaraju
    if (formData.novaLozinka !== formData.potvrdaLozinke) {
      setError('Nova lozinka i potvrda lozinke se ne podudaraju.');
      setIsLoading(false);
      return;
    }

    try {
      await employeeApi.promijeniLozinku(uposlenik.uposlenik_id, formData.novaLozinka);
      
      setSuccess(`Lozinka za ${uposlenik.ime_prezime} je uspješno promijenjena!`);
      
      // Resetuj formu nakon 2 sekunde i zatvori modal
      setTimeout(() => {
        setFormData({
          novaLozinka: '',
          potvrdaLozinke: '',
        });
        setSuccess('');
        onClose();
      }, 2000);

    } catch (err) {
      setError(err.response?.data?.message || 'Greška pri promjeni lozinke.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      novaLozinka: '',
      potvrdaLozinke: '',
    });
    setError('');
    setSuccess('');
    onClose();
  };

  if (!isOpen || !uposlenik) return null;

  return (
    <div 
      className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center p-4 z-50"
      onClick={handleClose}
    >
      <div 
        className="bg-gray-800 rounded-lg w-full max-w-md"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b border-gray-700">
          <div className="flex items-center">
            <Lock className="w-6 h-6 text-blue-500 mr-2" />
            <div>
              <h3 className="text-xl font-bold text-white">Promijeni Lozinku</h3>
              <p className="text-sm text-gray-400 mt-1">
                Za: {uposlenik.ime_prezime} ({uposlenik.email})
              </p>
            </div>
          </div>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-white transition"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && <PorukaGreske message={error} />}
          {success && <PorukaUspjeha message={success} />}

          <div className="bg-blue-900/30 border border-blue-500 rounded-lg p-3 mb-2">
            <div className="flex items-center space-x-2 text-blue-300">
              <User className="w-4 h-4" />
              <span className="text-sm font-medium">
                Kao administrator, možete promijeniti lozinku bilo kog uposlenika bez potrebe za starom lozinkom.
              </span>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Nova Lozinka
            </label>
            <InputPolje
              type="password"
              name="novaLozinka"
              placeholder="Unesite novu lozinku"
              value={formData.novaLozinka}
              onChange={handleChange}
            />
            <p className="text-xs text-gray-400 mt-1">
              Lozinka mora imati najmanje 8 karaktera, jedno veliko slovo, jedno malo slovo, jedan broj i jedan specijalni karakter.
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Potvrdi Novu Lozinku
            </label>
            <InputPolje
              type="password"
              name="potvrdaLozinke"
              placeholder="Potvrdite novu lozinku"
              value={formData.potvrdaLozinke}
              onChange={handleChange}
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={handleClose}
              className="py-2 px-4 font-semibold text-white bg-gray-600 hover:bg-gray-700 rounded-md transition duration-150"
              disabled={isLoading}
            >
              Otkaži
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="py-2 px-4 font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-md disabled:opacity-50 transition duration-150"
            >
              {isLoading ? 'Radim...' : 'Promijeni Lozinku'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

