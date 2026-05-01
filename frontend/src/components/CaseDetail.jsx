import React, { useState, useEffect, useRef } from 'react';
import { caseApi } from '../api.js';
import { Download } from 'lucide-react';
import { LoaderPoruka, PorukaGreske, Sekcija } from './AdminPanel.jsx';

// Uvozimo nove, odvojene komponente
// Moramo dodati ekstenzije za create-react-app
import EvidenceSection from './EvidenceSection.jsx';
import TeamSection from './TeamSection.jsx';
import WitnessSection from './WitnessSection.jsx';
import SuspectSection from './SuspectSection.jsx';
import HandoverApproval from './HandoverApproval.jsx';
import MyPendingHandovers from './MyPendingHandovers.jsx';
import CriminalOffensesSection from './CriminalOffensesSection.jsx';


// Glavna komponenta
export default function CaseDetail({ caseId, onBackToList, auth }) {
  const [slucaj, setSlucaj] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [statusError, setStatusError] = useState('');

  const [isGeneratingReport, setIsGeneratingReport] = useState(false);

  // Ref za funkciju osvježavanja MyPendingHandovers
  const refreshPendingHandoversRef = useRef(null);
  // Ref za funkciju osvježavanja HandoverApproval
  const refreshHandoverApprovalRef = useRef(null);
  // Ref za funkciju osvježavanja statusa dokaza u EvidenceSection (issue #19).
  // Bez ovoga, nakon što inspektor potvrdi primopredaju, panel "Dokazi"
  // ne mijenja prikaz "Trenutno kod" na "Vas" sve dok korisnik manuelno ne klikne refresh.
  const refreshEvidenceRef = useRef(null);

  // Dobavljamo osnovne podatke o slučaju
  useEffect(() => {
    const fetchCaseDetails = async () => {
      try {
        setIsLoading(true);
        const response = await caseApi.getById(caseId);
        setSlucaj(response.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Greška pri dobavljanju detalja slučaja.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchCaseDetails();
  }, [caseId]);

  const handleStatusChange = async (e) => {
    const noviStatus = e.target.value;
    setIsUpdatingStatus(true);
    setStatusError('');

    try {
      await caseApi.updateStatus(caseId, noviStatus);
      // Ažuriraj lokalno stanje slučaja da se odmah vidi promjena
      setSlucaj(prev => ({ ...prev, status: noviStatus }));
    } catch (err) {
      setStatusError(err.response?.data?.message || 'Greška pri ažuriranju statusa.');
      // Vrati <select> na staru vrijednost
      e.target.value = slucaj.status;
    } finally {
      setIsUpdatingStatus(false);
    }
  };

    const handleGenerateReport = async () => {
        setIsGeneratingReport(true);
        try {
            const response = await caseApi.generateReport(caseId);

            // Kreiraj download link za PDF
            const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
            const link = document.createElement('a');
            link.href = url;

            // Izvuci ime fajla iz headera ili generiši default
            const contentDisposition = response.headers['content-disposition'];
            let filename = `Izvjestaj_Slucaj_${slucaj.broj_slucaja}_${new Date().getTime()}.pdf`;
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename="?(.+)"?/);
                if (filenameMatch) filename = filenameMatch[1];
            }

            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);

        } catch (err) {
            alert('Greška pri generisanju izvještaja: ' + (err.response?.data?.message || err.message));
        } finally {
            setIsGeneratingReport(false);
        }
    };

  // Prikaz stanja
  if (isLoading) return <LoaderPoruka message="Učitavam detalje slučaja..." />;
  if (error) return <PorukaGreske message={error} />;
  if (!slucaj) return <PorukaGreske message="Slučaj nije pronađen." />;

  // Glavni Prikaz
  return (
    <div className="space-y-6">
      <button
        onClick={onBackToList}
        className="text-sm text-blue-400 hover:underline"
      >
        &larr; Nazad na sve slučajeve
      </button>

      {/* Zaglavlje Slučaja */}
      <Sekcija naslov={`Detalji Slučaja: ${slucaj.broj_slucaja}`}>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <h4 className="text-sm font-medium text-gray-400">Opis</h4>
            <p className="text-lg text-white">{slucaj.opis}</p>
          </div>
          <div>
            <h4 className="text-sm font-medium text-gray-400">Status</h4>

            {/* NOVO: Padajući meni za promjenu statusa */}
            <select
              value={slucaj.status}
              onChange={handleStatusChange}
              // Onemogući ako korisnik nije Admin ili Inspektor, ili ako se status ažurira
              disabled={!['Administrator', 'Inspektor'].includes(auth.user.uloga) || isUpdatingStatus}
              className={`text-lg p-1 rounded ${isUpdatingStatus ? 'text-gray-400' : 'text-white'
                } bg-gray-700 border border-gray-600 focus:ring-blue-500 focus:border-blue-500`}
            >
              <option value="Otvoren">Otvoren</option>
              <option value="Zatvoren">Zatvoren</option>
              <option value="Arhiviran">Arhiviran</option>
            </select>
            {isUpdatingStatus && <p className="text-xs text-yellow-400">Ažuriram...</p>}
            {statusError && <PorukaGreske message={statusError} />}
          </div>
          <div>
            <h4 className="text-sm font-medium text-gray-400">Voditelj Slučaja</h4>
            <p className="text-lg text-white">{slucaj.voditelj_slucaja || 'Nije dodijeljen'}</p>
          </div>
        </div>

        {/* Dugme za generisanje izvještaja */}
        <div className="mt-6 pt-4 border-t border-gray-700">
          <button
            onClick={handleGenerateReport}
            disabled={isGeneratingReport || !['Administrator', 'Inspektor'].includes(auth.user.uloga)}
            className="flex items-center space-x-2 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-gray-500 rounded-lg text-white font-medium transition duration-150"
            title={!['Administrator', 'Inspektor'].includes(auth.user.uloga) ? 'Samo Administrator ili Inspektor mogu generisati izvještaj' : ''}
          >
            <Download className="w-5 h-5" />
            <span>{isGeneratingReport ? 'Generišem...' : 'Preuzmi Izvještaj'}</span>
          </button>
        </div>
      </Sekcija>

      {/* Ostale sekcije (Dokazi, Tim, Svjedoci) */}

      <div className="lg:col-span-2">
        <EvidenceSection 
          caseId={caseId} 
          auth={auth} 
          caseStatus={slucaj.status}
          onRefresh={(refreshFn) => {
            refreshEvidenceRef.current = refreshFn;
          }}
          onPrimopredajaCreated={() => {
            if (refreshPendingHandoversRef.current) {
              refreshPendingHandoversRef.current();
            }
          }}
          onDokazStatusChanged={() => {
            // Osvježi obje liste kada se dokaz poništi
            if (refreshPendingHandoversRef.current) {
              refreshPendingHandoversRef.current();
            }
            if (refreshHandoverApprovalRef.current) {
              refreshHandoverApprovalRef.current();
            }
          }}
        />
      </div>
      {/* Prikazujemo Potvrde Primopredaje za Inspektore, Forenzičare i Administratore */}
      <div>
        {['Inspektor', 'Forenzičar', 'Administrator'].includes(auth.user.uloga) && (
          <HandoverApproval 
            auth={auth} 
            onRefresh={(refreshFn) => {
              refreshHandoverApprovalRef.current = refreshFn;
            }}
            onPrimopredajaProcessed={() => {
              // Issue #19: nakon Potvrđeno/Odbijeno trenutni nosilac dokaza se mijenja
              // u bazi, pa moramo osvježiti i panel "Dokazi" i poslana primopredaja-listu.
              if (refreshEvidenceRef.current) {
                refreshEvidenceRef.current();
              }
              if (refreshPendingHandoversRef.current) {
                refreshPendingHandoversRef.current();
              }
            }}
          />
        )}
      </div>
      {/* MyPendingHandovers - za poništavanje slanja */}
      <div>
        {['Inspektor', 'Forenzičar', 'Administrator'].includes(auth.user.uloga) && (
          <MyPendingHandovers 
            auth={auth} 
            onRefresh={(refreshFn) => {
              refreshPendingHandoversRef.current = refreshFn;
            }}
          />
        )}
      </div>
      <div>
        <TeamSection caseId={caseId} auth={auth} caseStatus={slucaj.status} />
      </div>
      <div>
        {/* NOVO: Prikazujemo WitnessSection SAMO ako NIJE Forenzičar */}
        {auth.user.uloga !== 'Forenzičar' && (
          <WitnessSection caseId={caseId} auth={auth} caseStatus={slucaj.status} />
        )}
      </div>

      <div>
        {auth.user.uloga !== 'Forenzičar' && (
          <SuspectSection caseId={caseId} auth={auth} caseStatus={slucaj.status} />
        )}
      </div>

      {/* Panel za krivična djela */}
      <div>
        {auth.user.uloga !== 'Forenzičar' && (
          <CriminalOffensesSection caseId={caseId} auth={auth} caseStatus={slucaj.status} />
        )}
      </div>
    </div>
  );
}