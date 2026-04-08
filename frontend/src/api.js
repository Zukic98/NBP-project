import axios from 'axios';

// Osnovna instanca (wrapper) za Axios
// U produkciji koristi relativni URL '/api' - nginx će proxirati na backend
// U development-u koristi REACT_APP_API_URL ili fallback na localhost:5000
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  withCredentials: true, // Omogući slanje cookies-a (potrebno za httpOnly cookies)
});

// Napomena: Token se sada čuva u httpOnly cookie-u, ne u localStorage
// Browser automatski šalje cookie sa svakim zahtjevom
// Ne trebamo više interceptor za dodavanje tokena u header

// Helper funkcija za dobavljanje CSRF tokena iz cookie-a
const getCsrfToken = () => {
  // CSRF token se čuva u cookie-u kao XSRF-TOKEN (httpOnly: false)
  const name = 'XSRF-TOKEN';
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
  return null;
};

// Interceptor za dodavanje CSRF tokena u sve POST/PUT/PATCH/DELETE zahtjeve
api.interceptors.request.use(
  (config) => {
    // Dodaj CSRF token samo za mutacijske metode
    if (['post', 'put', 'patch', 'delete'].includes(config.method?.toLowerCase())) {
      const csrfToken = getCsrfToken();
      if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken;
      } else {
        console.warn('CSRF token nije pronađen u cookie-u. Zahtjev će možda neuspjeti.');
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor za response - CSRF token se automatski postavlja u cookie od strane backend-a
// Nije potrebno čitati token iz response data jer je već u cookie-u
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    // Ako je greška 403 zbog CSRF tokena, pokušaj dobiti novi token i ponovi zahtjev
    if (error.response?.status === 403 && error.response?.data?.message?.includes('CSRF')) {
      console.warn('CSRF token greška - pokušavam dobiti novi token');

      try {
        // Pokušaj dobiti novi CSRF token (token će biti postavljen u cookie od strane backend-a)
        const tokenResponse = await axios.get(
          `${import.meta.env.VITE_API_URL || '/api'}/auth/me`,
          { withCredentials: true }
        );

        // Token je sada u cookie-u, pročitaj ga i ponovi zahtjev
        const newToken = getCsrfToken();
        if (newToken) {

          // Ponovi originalni zahtjev sa novim tokenom
          const originalRequest = error.config;
          originalRequest.headers['X-CSRF-Token'] = newToken;
          return api(originalRequest);
        }
      } catch (tokenError) {
        console.error('Greška pri dobavljanju CSRF tokena:', tokenError);
      }
    }
    return Promise.reject(error);
  }
);

// --- API za Autentifikaciju ---
const authApi = {
  // POST /api/stanice/registracija
  registerStation: async (imeStanice, adminIme, adminEmail, adminLozinka, adminBrojZnacke) => {
    return api.post('/stanice/registracija', {
      imeStanice,
      adminIme,
      adminEmail,
      adminLozinka,
      adminBrojZnacke,
    });
  },

  // POST /api/login
  login: async (email, lozinka, brojZnacke) => {
    // Koristimo axios sa withCredentials da pošaljemo cookie
    const apiUrl = import.meta.env.VITE_API_URL || '/api';
    return axios.post(
      `${apiUrl}/login`,
      { email, lozinka, brojZnacke },
      { withCredentials: true }
    );
  },

  // POST /api/logout
  logout: async () => {
    return api.post('/logout');
  },

  // GET /api/auth/me - Provjeri trenutnog korisnika
  getCurrentUser: async () => {
    return api.get('/auth/me');
  },

  // PATCH /api/auth/promijeni-lozinku - Promijeni vlastitu lozinku
  promijeniLozinku: async (staraLozinka, novaLozinka) => {
    return api.patch('/auth/promijeni-lozinku', {
      staraLozinka,
      novaLozinka,
    });
  },
};

// --- API za Slučajeve (Cases) ---
const caseApi = {
  // GET /api/slucajevi
  getAll: async () => {
    return api.get('/slucajevi');
  },

  // GET /api/slucajevi/:id
  getById: async (caseId) => {
    return api.get(`/slucajevi/${caseId}`);
  },

  // POST /api/slucajevi
  create: async (brojSlucaja, opis) => {
    return api.post('/slucajevi', { brojSlucaja, opis });
  },

  updateStatus: async (caseId, newStatus) => {
    return api.patch(`/slucajevi/${caseId}/status`, { status: newStatus });
  },

  // GET /api/slucajevi/:id/izvjestaj
  getReport: async (caseId) => {
    return api.get(`/slucajevi/${caseId}/izvjestaj`);
  },
};

// --- API za Uposlenike (Employees) ---
const employeeApi = {
  // POST /api/uposlenici/registracija (Samo za Admina)
  register: async (ime, email, lozinka, uloga, brojZnacke) => {
    return api.post('/uposlenici/registracija', {
      ime,
      email,
      lozinka,
      uloga,
      brojZnacke,
    });
  },

  // GET /api/uposlenici (Samo za Admina)
  getAll: async () => {
    return api.get('/uposlenici');
  },

  // NOVO: Dobavi detalje o jednom uposleniku
  getById: async (uposlenikId) => {
    return api.get(`/uposlenici/${uposlenikId}`);
  },

  // NOVO: Ažuriraj podatke o uposleniku
  update: async (uposlenikId, ime_prezime, email, uloga, status) => {
    return api.put(`/uposlenici/${uposlenikId}`, {
      ime_prezime,
      email,
      uloga,
      status
    });
  },

  // NOVO: Promijeni lozinku uposlenika (samo za Admina)
  promijeniLozinku: async (uposlenikId, novaLozinka) => {
    return api.patch(`/uposlenici/${uposlenikId}/promijeni-lozinku`, {
      novaLozinka,
    });
  },
};

// --- API za Dokaze (Evidence) ---
const evidenceApi = {
  // GET /api/slucajevi/:id/dokazi
  getByCaseId: async (caseId) => {
    return api.get(`/slucajevi/${caseId}/dokazi`);
  },

  // POST /api/slucajevi/:id/dokazi
  create: async (caseId, opis, lokacija, tipDokaza) => {
    return api.post(`/slucajevi/${caseId}/dokazi`, {
      opis,
      lokacija,
      tipDokaza
    });
  },

  // GET /api/dokazi/:id/stanje
  getStanjeDokaza: async (dokazId) => {
    return api.get(`/dokazi/${dokazId}/stanje`);
  },

  // GET /api/dokazi/:id/lanac (poboljšano)
  getLanacNadzora: async (dokazId) => {
    return api.get(`/dokazi/${dokazId}/lanac`);
  },

  // GET /api/dokazi/:id/trenutni-nosilac
  getTrenutniNosilac: async (dokazId) => {
    return api.get(`/dokazi/${dokazId}/stanje`)
      .then(response => response.data.trenutni_nosilac)
      .catch(error => {
        console.error('Greška pri dobavljanju nosioca:', error);
        throw error;
      });
  },

  // PATCH /api/dokazi/:id/status
  updateStatus: async (dokazId, status) => {
    return api.patch(`/dokazi/${dokazId}/status`, { status });
  },
};

// --- API za Lanac Nadzora (Chain of Custody) ---
const chainOfCustodyApi = {
  // GET /api/dokazi/:id/lanac
  getForEvidence: async (dokazId) => {
    return api.get(`/dokazi/${dokazId}/lanac`);
  },

  // POST /api/dokazi/:id/primopredaja
  createEntry: async (dokazId, preuzeo_uposlenik_id, svrha) => {
    return api.post(`/dokazi/${dokazId}/primopredaja`, {
      preuzeo_uposlenik_id,
      svrha
    });
  },

  // Provjera da li korisnik može predati dokaz
  provjeriMoguPredati: async (dokazId) => {
    try {
      const response = await api.get(`/dokazi/${dokazId}/stanje`);
      return response.data;
    } catch (error) {
      console.error('Greška pri provjeri stanja dokaza:', error);
      throw error;
    }
  },

  // Kreiranje primopredaje sa provjerama
  evidentirajPrimopredaju: async (dokazId, data) => {
    // Prvo provjeri da li može predati
    const stanje = await chainOfCustodyApi.provjeriMoguPredati(dokazId);

    if (!stanje.moze_predati) {
      // Create a proper Error object with the response data
      const error = new Error(`Samo ${stanje.trenutni_nosilac.trenutni_nosilac_ime} može predati ovaj dokaz.`);
      error.response = {
        status: 403,
        data: {
          message: `Samo ${stanje.trenutni_nosilac.trenutni_nosilac_ime} može predati ovaj dokaz.`
        }
      };
      throw error;
    }

    // Ako može, evidentiraj primopredaju
    return api.post(`/dokazi/${dokazId}/primopredaja`, {
      preuzeo_uposlenik_id: data.preuzeo_uposlenik_id,
      svrha: data.svrha
    });
  },

  // NOVO: Dobavi zahtjeve za potvrdu - ažurirano
  getZahtjeviZaPotvrdu: async () => {
    return api.get('/primopredaje/ceka-potvrdu');
  },

  // NOVO: Potvrdi ili odbij primopredaju - ažurirano
  potvrdiPrimopredaju: async (unosId, status, napomena) => {
    return api.patch(`/lanac-nadzora/${unosId}/potvrda`, {
      status,
      napomena
    });
  },

  // NOVO: Provjeri detaljno stanje dokaza (sa potvrdama)
  getStanjeDokazaDetailed: async (dokazId) => {
    return api.get(`/dokazi/${dokazId}/stanje-detailed`);
  },

  // NOVO: Dobavi moja slanja koja čekaju potvrdu
  getMojaSlanjaCekaPotvrdu: async () => {
    return api.get('/primopredaje/moja-sljanja-ceka-potvrdu');
  },

  // NOVO: Poništi slanje (za pošiljaoca)
  ponistiSlanje: async (unosId, razlog) => {
    return api.put(`/lanac-nadzora/${unosId}/ponisti`, {
      razlog
    });
  },
};

// --- API za Tim na Slučaju (Team) ---
const teamApi = {
  // GET /api/slucajevi/:id/tim
  getByCaseId: async (caseId) => {
    return api.get(`/slucajevi/${caseId}/tim`);
  },

  // POST /api/slucajevi/:id/tim
  addMember: async (caseId, uposlenik_id, uloga_na_slucaju) => {
    return api.post(`/slucajevi/${caseId}/tim`, {
      uposlenik_id,
      uloga_na_slucaju
    });
  },

  removeMember: async (dodjelaId) => {
    return api.delete(`/tim/${dodjelaId}/ukloni`);
  },
};

// --- API za Svjedoke (Witness) ---
const witnessApi = {
  // GET /api/slucajevi/:id/svjedoci
  getByCaseId: async (caseId) => {
    return api.get(`/slucajevi/${caseId}/svjedoci`);
  },

  // POST /api/slucajevi/:id/svjedoci
  create: async (caseId, formData) => {
    return api.post(`/slucajevi/${caseId}/svjedoci`, formData);
  },
};

// ===============================================
// == POMOĆNE FUNKCIJE ==
// ===============================================

// Validacija lozinke
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
  return null; // Sve je u redu
};

// Helper za prikazivanje statusa dokaza
const formatStatusDokaza = (dokaz, trenutniNosilac, korisnikId) => {
  if (!trenutniNosilac) {
    return { tekst: 'Status: Nepoznat', boja: 'text-gray-400' };
  }

  const jeTrenutniNosilac = trenutniNosilac.trenutni_nosilac_id === korisnikId;

  if (jeTrenutniNosilac) {
    return {
      tekst: 'Trenutno kod: Vas',
      boja: 'text-green-400',
      ikona: '🟢'
    };
  } else {
    return {
      tekst: `Trenutno kod: ${trenutniNosilac.trenutni_nosilac_ime}`,
      boja: 'text-yellow-400',
      ikona: '🟡'
    };
  }
};

// ===============================================
// == FINALNI EXPORT SVIH API SERVISA ==
// ===============================================

export {
  authApi,
  caseApi,
  employeeApi,
  evidenceApi,
  chainOfCustodyApi,
  teamApi,
  witnessApi,
  validatePassword,
  formatStatusDokaza,
};