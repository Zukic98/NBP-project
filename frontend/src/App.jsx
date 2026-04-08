import React, { useState, useEffect } from 'react';
import AuthPage from './components/AuthPage.jsx'; // Importiramo našu stranicu za prijavu/registraciju
import Dashboard from './components/Dashboard.jsx'; // Importiramo glavni Dashboard
import { authApi } from './api.js'; // Importiramo API za autentifikaciju

// Glavna komponenta aplikacije koja upravlja stanjem autentifikacije
export default function App() {
  // Glavno stanje autentifikacije za cijelu aplikaciju
  // Token se sada čuva u httpOnly cookie-u, ne u localStorage
  const [auth, setAuth] = useState({
    user: null, // Korisnik se dobiva iz API-ja
  });
  
  // Stanje za provjeru da li se token još uvijek provjerava pri učitavanju
  const [isLoading, setIsLoading] = useState(true);

  // useEffect kuka se pokreće SAMO JEDNOM, kada se App komponenta prvi put učita
  useEffect(() => {
    // Funkcija koja provjerava da li je korisnik prijavljen (cookie se automatski šalje)
    const provjeriAutentifikaciju = async () => {
      try {
        // Pokušaj dobiti trenutnog korisnika (cookie se automatski šalje)
        const response = await authApi.getCurrentUser();
        if (response.data && response.data.user) {
          setAuth({ user: response.data.user });
        }
      } catch (error) {
        // Ako zahtjev ne uspije (npr. nema cookie-a ili je istekao), korisnik nije prijavljen
        setAuth({ user: null });
      } finally {
        setIsLoading(false);
      }
    };

    provjeriAutentifikaciju();
  }, []); // Prazan niz [] znači da se ovo pokreće samo jednom (kao componentDidMount)

  // Funkcija koja se poziva iz LoginForm komponente nakon uspješne prijave
  const handleLogin = async (data) => {
    
    // Token se sada automatski postavlja u httpOnly cookie na backend-u
    // Ne trebamo ga više čuvati u localStorage
    const { user } = data;

    // Ažuriraj glavno stanje aplikacije
    setAuth({ user });
  };

  // Funkcija za odjavu (Logout)
  const handleLogout = async () => {
    try {
      // Pozovi logout endpoint koji briše cookie
      await authApi.logout();
    } catch (error) {
      console.error('Greška pri odjavi:', error);
    } finally {
      // Resetuj glavno stanje aplikacije
      setAuth({ user: null });
    }
  };

  // Ako još uvijek provjeravamo token, prikaži poruku o učitavanju
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center text-xl font-semibold text-teal-400">
        Učitavam aplikaciju i provjeravam autentifikaciju...
      </div>
    );
  }

  // Glavni return:
  // Prikazujemo Dashboard ako imamo korisnika (prijavljeni smo)
  // Inače, prikazujemo AuthPage (stranicu za prijavu/registraciju)
  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {auth.user ? (
        <Dashboard auth={auth} onLogout={handleLogout} />
      ) : (
        <AuthPage onLogin={handleLogin} />
      )}
    </div>
  );
}