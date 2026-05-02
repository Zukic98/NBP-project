# FRONTEND KNOWLEDGE BASE

**Generated:** 2026-05-02 **Stack:** React 19 + Vite 5 + Tailwind 3 + axios

## OVERVIEW

Single-page React app for the SUDS backend. **No router** — navigation is local
component state (`view === 'list' | 'detail'`). **No global store** — auth state
lives in `App.jsx`, drilled via props. Migrated from CRA → Vite, but
`reportWebVitals` and `setupTests.js` artifacts remain.

## STRUCTURE

```
frontend/
├── src/
│   ├── api.js           # All HTTP calls. ONE axios instance + 10 grouped APIs
│   ├── App.jsx          # Auth gate + Dashboard/AuthPage switch
│   ├── index.jsx        # ReactDOM root
│   ├── components/      # 18 flat .jsx files (no subdirs)
│   ├── utils/sanitize.js # String sanitization + email/integer validators
│   └── App.test.js      # ONLY test (CRA leftover, no test runner wired)
├── public/              # Static assets (CRA leftovers: manifest, robots)
├── index.html           # Vite entry
├── tailwind.config.js   # Default theme, no extensions
├── Dockerfile           # STALE — uses REACT_APP_* (CRA), npm ci into /app/build
└── nginx.conf           # Used by Dockerfile stage 2
```

## WHERE TO LOOK

| Task                  | Location                                   | Notes                                                        |
| --------------------- | ------------------------------------------ | ------------------------------------------------------------ |
| Add backend call      | `src/api.js`                               | Append to existing grouped `xxxApi` object                   |
| Add screen / modal    | `src/components/*.jsx`                     | Flat dir, PascalCase filename                                |
| Wire role-based UI    | `auth.user.nazivUloge` string compare      | Uppercase enum: `SEF_STANICE`, `INSPEKTOR`, etc.             |
| Auth state            | `src/App.jsx`                              | `localStorage.getItem('token')` + `authApi.getCurrentUser()` |
| Sanitize user input   | `src/utils/sanitize.js`                    | Strips `<>`, escapes HTML entities                           |
| Configure API URL     | `.env` (key: `VITE_API_URL`)               | `.env.example` ships wrong key — **see root NOTES**          |
| PDF generation (case) | `caseApi.generateReport()` in `src/api.js` | `responseType: 'blob'` — server emits iText 7 PDF            |
| Client-side PDF       | `html2pdf.js` / `jspdf` (deps installed)   | Used in forensic report component                            |

## CONVENTIONS

- **JSX, not TSX.** No TypeScript. No `.d.ts` files. Don't introduce TS without
  approval — would force renames across all 18 components.
- **Tailwind utility classes inline.** No CSS modules, no styled-components.
  Theme is **hardcoded dark** (`bg-gray-900 text-white`).
- **Bosnian variable names** in component logic (`provjeriAutentifikaciju`,
  `lozinka`, `staraLozinka`). UI strings in Bosnian.
- **Axios via single instance** (`src/api.js`). Request interceptor injects
  `Bearer ${token}` from `localStorage` for all paths NOT in `PUBLIC_PATHS`. 401
  response → auto-clears token (no redirect — caller handles).
- **Endpoints grouped per domain**: `authApi`, `caseApi`, `employeeApi`,
  `evidenceApi`, `forensicApi`, `chainOfCustodyApi`, `teamApi`, `witnessApi`,
  `suspectApi`. Add new methods inside the matching object — don't create
  parallel free functions.
- **Snake_case payload keys** when backend expects them (`uposlenik_id`,
  `lokacija_pronalaska`). Don't camelCase — backend DTOs use snake_case via
  Jackson.
- **Lucide-react** for icons (already installed). Don't add another icon lib.

## ANTI-PATTERNS

- **NEVER add a router** (`react-router`, etc.) without architectural discussion
  — current state machine is `App.view` + `Dashboard.view`. Adding a router
  changes auth-gating and breaks `localStorage` token flow.
- **NEVER store anything in `localStorage` besides `token`.** No user object, no
  preferences. The token is the source of truth, decoded server-side.
- **NEVER trust `.env.example`** — it defines `VITE_API_BASE_URL` but `api.js`
  reads `import.meta.env.VITE_API_URL`. Renaming requires touching both.
- **NEVER hand-roll fetch calls** — go through the `api` axios instance so the
  JWT interceptor and 401-eviction work.
- **NEVER use the `Dockerfile`** as-is. It's broken (CRA env-var name, expects
  `/app/build` but Vite emits `/app/dist`). Treat it as a stub.

## TESTING

Dependencies present (`@testing-library/react`, `@testing-library/jest-dom`,
`setupTests.js`), but **no test runner configured**: `package.json` has no
`test` script, no `vitest.config.js`, no `jest.config.js`. Adding a test today
requires wiring vitest first. Only `App.test.js` exists (CRA boilerplate).

## NOTES

- `frontend/dist/` is committed (build output) — odd, but present. Don't
  re-commit after `npm run build`.
- `App.test.js` extension is `.js` (not `.jsx`) — leftover from CRA naming.
- `reportWebVitals.js` and `setupTests.js` are CRA artifacts; they don't break
  Vite but are dead code.
- `index.css` and `App.css` exist alongside Tailwind — minimal global rules
  only; prefer Tailwind utilities in new code.
