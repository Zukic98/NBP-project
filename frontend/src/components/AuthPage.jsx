import React, { useState } from 'react';
import { authApi } from '../api.js';

// --- POMOĆNA FUNKCIJA ZA VALIDACIJU LOZINKE ---
const validatePassword = (password) => {
  const minLength = 8;
  const hasUppercase = /[A-Z]/.test(password);
  const hasLowercase = /[a-z]/.test(password);
  const hasNumber = /[0-9]/.test(password);
  const hasSpecialChar = /[\W_]/.test(password);

  if (password.length < minLength) {
    return `Lozinka mora imati najmanje ${minLength} karaktera.`;
  }
  if (!hasUppercase) {
    return "Lozinka mora sadržavati najmanje jedno veliko slovo.";
  }
  if (!hasLowercase) {
    return "Lozinka mora sadržavati najmanje jedno malo slovo.";
  }
  if (!hasNumber) {
    return "Lozinka mora sadržavati najmanje jedan broj.";
  }
  if (!hasSpecialChar) {
    return "Lozinka mora sadržavati najmanje jedan specijalni karakter (npr. !, #, ?, @).";
  }
  return null;
};

// --- UI KOMPONENTE ---

function InputPolje({ label, type, id, name, vrijednost, onChange, autoComplete = 'off' }) {
  return (
    <div className="mb-4">
      <label htmlFor={id} className="block text-sm font-medium text-gray-300 mb-1">
        {label}
      </label>
      <input
        type={type}
        id={id}
        name={name || id}
        value={vrijednost}
        onChange={onChange}
        required
        autoComplete={autoComplete}
        className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
      />
    </div>
  );
}

function AkcijaDugme({ isLoading, tekst }) {
  return (
    <button
      type="submit"
      disabled={isLoading}
      className={`w-full font-bold py-3 px-4 rounded-lg transition duration-200 flex items-center justify-center ${
        isLoading
          ? 'bg-gray-600 cursor-not-allowed'
          : 'bg-blue-600 hover:bg-blue-700 text-white'
      }`}
    >
      {isLoading ? (
        <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      ) : (
        tekst
      )}
    </button>
  );
}

export function PorukaGreske({ message }) {
  return (
    <div className="bg-red-800 border border-red-600 text-red-100 px-4 py-3 rounded-lg mb-4" role="alert">
      <span className="block sm:inline">{message}</span>
    </div>
  );
}

export function PorukaUspjeha({ message }) {
  return (
    <div className="bg-green-800 border border-green-600 text-green-100 px-4 py-3 rounded-lg mb-4" role="alert">
      <span className="block sm:inline">{message}</span>
    </div>
  );
}

// ---------- GLAVNA KOMPONENTA STRANICE (AUTH) ----------
export default function AuthPage({ onLogin }) {
  const [prikaziLogin, setPrikaziLogin] = useState(true);

  const handleRegisterSuccess = () => {
    setPrikaziLogin(true); 
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-4">
      <div className="w-full max-w-md">
        {prikaziLogin ? (
          <>
            <LoginForm onLogin={onLogin} />
            <p className="mt-4 text-center text-gray-400">
              Nova policijska stanica?{' '}
              <button
                onClick={() => setPrikaziLogin(false)}
                className="font-medium text-blue-400 hover:text-blue-300"
              >
                Registrujte svoju stanicu
              </button>
            </p>
          </>
        ) : (
          <>
            <RegisterStationForm onRegisterSuccess={handleRegisterSuccess} />
            <p className="mt-4 text-center text-gray-400">
              Već imate nalog?{' '}
              <button
                onClick={() => setPrikaziLogin(true)}
                className="font-medium text-blue-400 hover:text-blue-300"
              >
                Prijavite se
              </button>
            </p>
          </>
        )}
      </div>
    </div>
  );
}

// ---------- FORMA ZA PRIJAVU (LOGIN) ----------
function LoginForm({ onLogin }) {
  const [email, setEmail] = useState('');
  const [lozinka, setLozinka] = useState('');
  const [brojZnacke, setBrojZnacke] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      // API poziv za login
      const response = await authApi.login(email, lozinka, brojZnacke);
      // Uspješan login - prosljeđujemo podatke roditeljskoj komponenti
      onLogin(response.data);
    } catch (err) {
      // Prikaz greške korisniku, ali bez logovanja detalja u konzolu
      const poruka = err.response?.data?.message || 'Greška pri prijavi';
      setError(poruka);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-gray-800 p-8 rounded-lg shadow-2xl">
      <h2 className="text-3xl font-bold mb-6 text-center text-white">Prijava</h2>
      {error && <PorukaGreske message={error} />}
      
      <InputPolje
        label="Email"
        type="email"
        id="login-email"
        vrijednost={email}
        onChange={(e) => setEmail(e.target.value)}
        autoComplete="email"
      />
      <InputPolje
        label="Lozinka"
        type="password"
        id="login-lozinka"
        vrijednost={lozinka}
        onChange={(e) => setLozinka(e.target.value)}
        autoComplete="current-password"
      />
      <InputPolje
        label="Broj Značke"
        type="text"
        id="login-broj-znacke"
        vrijednost={brojZnacke}
        onChange={(e) => setBrojZnacke(e.target.value)}
        autoComplete="off"
      />
      
      <div className="mt-6">
        <AkcijaDugme isLoading={isLoading} tekst="Prijavi se" />
      </div>
    </form>
  );
}

// ---------- FORMA ZA REGISTRACIJU STANICE ----------
function RegisterStationForm({ onRegisterSuccess }) {
  const [formData, setFormData] = useState({
    imeStanice: '',
    adminIme: '',
    adminEmail: '',
    adminLozinka: '',
    adminBrojZnacke: ''
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    const fieldName = e.target.name;
    setFormData({ ...formData, [fieldName]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);

    // Validacija lozinke na frontendu
    const passwordError = validatePassword(formData.adminLozinka);
    if (passwordError) {
      setError(passwordError);
      setIsLoading(false);
      return;
    }

    try {
      await authApi.registerStation(
        formData.imeStanice,
        formData.adminIme,
        formData.adminEmail,
        formData.adminLozinka,
        formData.adminBrojZnacke
      );
      
      setSuccess('Stanica uspješno registrovana! Prebacujem na prijavu...');
      
      setTimeout(() => {
        onRegisterSuccess();
      }, 2000);

    } catch (err) {
      const poruka = err.response?.data?.message || 'Greška pri registraciji';
      setError(poruka);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-gray-800 p-8 rounded-lg shadow-2xl">
      <h2 className="text-3xl font-bold mb-6 text-center text-white">Registracija Stanice</h2>
      <p className="text-center text-gray-400 mb-6 text-sm">Kreirajte nalog za svoju stanicu. Vi ćete automatski postati Administrator.</p>
      
      {error && <PorukaGreske message={error} />}
      {success && <PorukaUspjeha message={success} />}
      
      <h3 className="text-lg font-semibold text-blue-300 mb-2">Podaci o Stanici</h3>
      <InputPolje
        label="Ime Stanice"
        type="text"
        id="imeStanice"
        name="imeStanice"
        vrijednost={formData.imeStanice}
        onChange={handleChange}
      />

      <InputPolje
        label="Vaše Ime i Prezime"
        type="text"
        id="adminIme"
        name="adminIme"
        vrijednost={formData.adminIme}
        onChange={handleChange}
      />

      <InputPolje
        label="Vaš Email"
        type="email"
        id="adminEmail"
        name="adminEmail"
        vrijednost={formData.adminEmail}
        onChange={handleChange}
        autoComplete="email"
      />

      <InputPolje
        label="Vaš Broj Značke"
        type="text"
        id="adminBrojZnacke"
        name="adminBrojZnacke"
        vrijednost={formData.adminBrojZnacke}
        onChange={handleChange}
      />

      <InputPolje
        label="Lozinka"
        type="password"
        id="adminLozinka"
        name="adminLozinka"
        vrijednost={formData.adminLozinka}
        onChange={handleChange}
        autoComplete="new-password"
      />
      
      <div className="mt-6">
        <AkcijaDugme isLoading={isLoading} tekst="Registruj Stanicu" />
      </div>
    </form>
  );
}