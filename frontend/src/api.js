import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

// Public endpoints that should never carry a stale Authorization header.
// Sending Bearer tokens to these triggers JwtFilter (blacklist lookup, etc.)
// on requests that are intentionally unauthenticated, which can cause
// confusing 401/403 responses and stuck-token loops in the browser.
const PUBLIC_PATHS = ['/auth/login', '/stanice/register'];

api.interceptors.request.use(
  (config) => {
    const isPublic = PUBLIC_PATHS.some((p) => (config.url || '').startsWith(p));
    if (!isPublic) {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
    }
    return Promise.reject(error);
  }
);

// --- API za Autentifikaciju ---
const authApi = {
  registerStation: async (podaci) => {
    return api.post('/stanice/register', podaci);
  },

  login: async (email, lozinka, brojZnacke) => {
    const response = await api.post('/auth/login', { 
      email, 
      password: lozinka, 
      brojZnacke 
    });
    
    if (response.data && response.data.token) {
      localStorage.setItem('token', response.data.token);
    }
    return response;
  },

  logout: async () => {
    try {
      await api.post('/auth/logout');
    } finally {
      localStorage.removeItem('token');
    }
  },

  getCurrentUser: async () => {
    return api.get('/dashboard/me');
  },

  promijeniLozinku: async (staraLozinka, novaLozinka) => {
    return api.put('/auth/promijeni-lozinku', {
      staraLozinka,
      novaLozinka,
    });
  },
};

// --- API za Slučajeve (Cases) ---
const caseApi = {
  getAll: async () => api.get('/slucajevi'),
  getMyCases: async () => api.get('/slucajevi/moji'),
  getById: async (caseId) => api.get(`/slucajevi/${caseId}`),
  create: async (brojSlucaja, opis) => api.post('/slucajevi', { brojSlucaja, opis }),
  updateStatus: async (caseId, newStatus) => api.patch(`/slucajevi/${caseId}/status`, { status: newStatus }),
  getReport: async (caseId) => api.get(`/slucajevi/${caseId}/izvjestaj`),
};

// --- API za Uposlenike (Employees) ---
const employeeApi = {
  register: async (podaci) => {
    return api.post('/uposlenici', podaci);
  },

  getAll: async () => api.get('/uposlenici'),
  
  getById: async (uposlenikId) => api.get(`/uposlenici/${uposlenikId}`),

  update: async (uposlenikId, podaci) => {
    return api.put(`/uposlenici/${uposlenikId}`, podaci);
  },

  updateStatus: async (uposlenikId, noviStatus) => {
    return api.put(`/uposlenici/${uposlenikId}/status`, { status: noviStatus });
  },
  promijeniLozinku: async (userId, novaLozinka) => {
    return api.put(`/uposlenici/${userId}/password-reset`, { novaLozinka });
  },
};

// --- API za Dokaze (Evidence) ---
const evidenceApi = {
  getByCaseId: async (caseId) => api.get(`/slucajevi/${caseId}/dokazi`),
  create: async (caseId, opis, lokacija, tipDokaza) => 
    api.post(`/slucajevi/${caseId}/dokazi`, { opis, lokacija_pronalaska: lokacija, tip_dokaza: tipDokaza }),
  getStanjeDokaza: async (dokazId) => api.get(`/dokazi/${dokazId}/stanje`),
  getLanacNadzora: async (dokazId) => api.get(`/dokazi/${dokazId}/lanac`),
  updateStatus: async (dokazId, status) => api.patch(`/dokazi/${dokazId}/status`, { status }),
};

// --- API za Forenzičke Izvještaje ---
const forensicApi = {
  create: async (data) => 
    api.post('/forenzicki-izvjestaji', data),

  update: async (id, data) => 
    api.put(`/forenzicki-izvjestaji/${id}`, data),

  getByEvidenceId: async (dokazId) => 
    api.get(`/forenzicki-izvjestaji/dokaz/${dokazId}`),
};

// --- API za Lanac Nadzora (Chain of Custody) ---
const chainOfCustodyApi = {
  getForEvidence: async (dokazId) => api.get(`/dokazi/${dokazId}/lanac`),
  createEntry: async (dokazId, preuzeo_uposlenik_id, svrha) => 
    api.post(`/dokazi/${dokazId}/primopredaja`, { preuzeo_uposlenik_id, svrha }),
  evidentirajPrimopredaju: async (dokazId, data) =>
    api.post(`/dokazi/${dokazId}/primopredaja`, data),
  getZahtjeviZaPotvrdu: async () => api.get('/primopredaje/ceka-potvrdu'),
  getMojaSlanjaCekaPotvrdu: async () => api.get('/primopredaje/moja-slanja'),
  potvrdiPrimopredaju: async (unosId, status, napomena) => 
    api.patch(`/lanac-nadzora/${unosId}/potvrda`, { status, napomena }),
  ponistiSlanje: async (unosId, razlog) =>
    api.delete(`/primopredaje/${unosId}/ponisti`, { data: { razlog } }),
};

// --- API za Tim na Slučaju (Team) ---
const teamApi = {
  getByCaseId: async (caseId) => api.get(`/slucajevi/${caseId}/tim`),
  addMember: async (caseId, uposlenik_id, uloga_na_slucaju) => 
    api.post(`/slucajevi/${caseId}/tim`, { uposlenik_id, uloga_na_slucaju }),
  removeMember: async (dodjelaId) => api.delete(`/tim/${dodjelaId}/ukloni`),
};

// --- API za Svjedoke (Witness) ---
const witnessApi = {
  getByCaseId: async (caseId) => api.get(`/slucajevi/${caseId}/svjedoci`),
  create: async (caseId, formData) => api.post(`/slucajevi/${caseId}/svjedoci`, formData),
};

// --- API za Osumnjičene (Suspects) ---
const suspectApi = {
  getByCaseId: async (caseId) => api.get(`/slucajevi/${caseId}/osumnjiceni`),
  create: async (caseId, formData) => api.post(`/slucajevi/${caseId}/osumnjiceni`, formData),
};

// --- API za Krivična Djela (Criminal Offenses) ---
const krivicnoDjeloApi = {
  getAll: async () => api.get('/krivicna-djela'),
  getById: async (id) => api.get(`/krivicna-djela/${id}`),
  create: async (data) => api.post('/krivicna-djela', data),
  update: async (id, data) => api.put(`/krivicna-djela/${id}`, data),
  delete: async (id) => api.delete(`/krivicna-djela/${id}`),
};

// --- API za Krivična djela na slučaju ---
const slucajKrivicnoDjeloApi = {
  // Dohvati sva krivična djela za slučaj
  getBySlucajId: async (slucajId) => api.get(`/slucajevi/${slucajId}/krivicna-djela`),
  
  // Dodaj jedno krivično djelo na slučaj
  dodajDjelo: async (slucajId, djeloId) => 
    api.post(`/slucajevi/${slucajId}/krivicna-djela`, { djeloId }),
  
  // Dodaj više krivičnih djela na slučaj odjednom
  dodajViseDjela: async (slucajId, djeloIds) => 
    api.post(`/slucajevi/${slucajId}/krivicna-djela/batch`, { djeloIds }),
  
  // Ukloni krivično djelo sa slučaja
  ukloniDjelo: async (slucajId, vezaId) => 
    api.delete(`/slucajevi/${slucajId}/krivicna-djela/${vezaId}`),
};


// ===============================================
// == POMOĆNE FUNKCIJE ==
// ===============================================

const validatePassword = (password) => {
  const minLength = 8;
  if (password.length < minLength) return `Lozinka mora imati najmanje ${minLength} karaktera.`;
  if (!/[A-Z]/.test(password)) return "Lozinka mora sadržavati veliko slovo.";
  if (!/[a-z]/.test(password)) return "Lozinka mora sadržavati malo slovo.";
  if (!/[0-9]/.test(password)) return "Lozinka mora sadržavati broj.";
  if (!/[\W_]/.test(password)) return "Lozinka mora sadržavati specijalni karakter.";
  return null;
};

const formatStatusDokaza = (dokaz, trenutniNosilac, korisnikId) => {
  if (!trenutniNosilac) return { tekst: 'Status: Nepoznat', boja: 'text-gray-400' };
  const jeTrenutniNosilac = trenutniNosilac.trenutni_nosilac_id === korisnikId;
  return jeTrenutniNosilac 
    ? { tekst: 'Trenutno kod: Vas', boja: 'text-green-400', ikona: '🟢' }
    : { tekst: `Trenutno kod: ${trenutniNosilac.trenutni_nosilac_ime}`, boja: 'text-yellow-400', ikona: '🟡' };
};

export {
  authApi,
  caseApi,
  employeeApi,
  evidenceApi,
  chainOfCustodyApi,
  teamApi,
  suspectApi,
  witnessApi,
  krivicnoDjeloApi,
  slucajKrivicnoDjeloApi,
  validatePassword,
  formatStatusDokaza,
  forensicApi,
};