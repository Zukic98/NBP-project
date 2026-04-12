import React, { useState, useEffect } from 'react';
import AuthPage from './components/AuthPage.jsx';
import Dashboard from './components/Dashboard.jsx';
import { authApi } from './api.js';

export default function App() {
  const [auth, setAuth] = useState({
    user: null,
    token: localStorage.getItem('token')
  });
  
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const provjeriAutentifikaciju = async () => {
      const token = localStorage.getItem('token');
      
      if (!token) {
        setAuth({ user: null, token: null });
        setIsLoading(false);
        return;
      }

      try {
        const response = await authApi.getCurrentUser();
        if (response.data) {
          setAuth({ user: response.data, token: token });
        }
      } catch (error) {
        console.error("Token više nije validan:", error);
        localStorage.removeItem('token');
        setAuth({ user: null, token: null });
      } finally {
        setIsLoading(false);
      }
    };

    provjeriAutentifikaciju();
  }, []);

  const handleLogin = async (data) => {
    const token = data.token;
    localStorage.setItem('token', token);

    try {
      const userResponse = await authApi.getCurrentUser();
      setAuth({
        user: userResponse.data,
        token: token
      });
    } catch (error) {
      console.error("Greška pri dobavljanju podataka o korisniku:", error);
    }
  };

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch (error) {
      console.error('Greška pri odjavi na serveru:', error);
    } finally {
      localStorage.removeItem('token');
      setAuth({ user: null, token: null });
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center text-xl font-semibold text-teal-400">
        Učitavam aplikaciju i provjeravam token...
      </div>
    );
  }

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