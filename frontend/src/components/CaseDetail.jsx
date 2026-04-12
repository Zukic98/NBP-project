import React, { useState, useEffect, useRef } from 'react';
import { caseApi } from '../api.js';
import { Download } from 'lucide-react';
import html2pdf from 'html2pdf.js';
import { LoaderPoruka, PorukaGreske, Sekcija } from './AdminPanel.jsx';

// Uvozimo nove, odvojene komponente
// Moramo dodati ekstenzije za create-react-app
import EvidenceSection from './EvidenceSection.jsx';
import TeamSection from './TeamSection.jsx';
import WitnessSection from './WitnessSection.jsx';
import HandoverApproval from './HandoverApproval.jsx';
import MyPendingHandovers from './MyPendingHandovers.jsx';


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
      const response = await caseApi.getReport(caseId);
      const izvjestaj = response.data;

      // Kreiraj HTML sadržaj za PDF
      let htmlContent = `
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          body {
            font-family: Arial, sans-serif;
            color: #333;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            background-color: white;
          }
          .header {
            text-align: center;
            margin-bottom: 30px;
            border-bottom: 2px solid #ddd;
            padding-bottom: 15px;
          }
          .header h1 {
            color: #1f2937;
            font-size: 24px;
            margin: 0;
          }
          .header h2 {
            color: #374151;
            font-size: 16px;
            margin: 10px 0 0 0;
          }
          .section {
            margin: 20px 0;
            page-break-inside: avoid;
          }
          .section h3 {
            color: #334155;
            font-size: 14px;
            font-weight: bold;
            margin: 15px 0 10px 0;
            padding-bottom: 5px;
            border-bottom: 1px solid #ddd;
          }
          .section-content {
            margin-left: 10px;
          }
          .item {
            margin: 8px 0;
            font-size: 11px;
          }
          .item-label {
            font-weight: bold;
            color: #555;
          }
          .item-value {
            color: #374151;
          }
          .sub-item {
            margin-left: 15px;
            margin-top: 4px;
            font-size: 11px;
          }
          .footer {
            margin-top: 40px;
            padding: 20px 0 15px 0;
            border-top: 1px solid #ddd;
            font-size: 10px;
            color: #666;
            text-align: center;
            page-break-inside: avoid;
          }
          .footer p {
            margin: 5px 0;
          }
          .no-data {
            color: #999;
            font-style: italic;
            font-size: 11px;
          }
        </style>

        <div class="header">
          <h1>IZVJEŠTAJ O SLUČAJU</h1>
          <h2>Broj slučaja: ${izvjestaj.slucaj.broj_slucaja}</h2>
        </div>

        <div class="section">
          <h3>OSNOVNI PODACI</h3>
          <div class="section-content">
            <div class="item">
              <span class="item-label">Broj slučaja:</span>
              <span class="item-value">${izvjestaj.slucaj.broj_slucaja}</span>
            </div>
            <div class="item">
              <span class="item-label">Status:</span>
              <span class="item-value">${izvjestaj.slucaj.status}</span>
            </div>
            <div class="item">
              <span class="item-label">Opis:</span>
              <span class="item-value">${izvjestaj.slucaj.opis}</span>
            </div>
            <div class="item">
              <span class="item-label">Voditelj slučaja:</span>
              <span class="item-value">${izvjestaj.slucaj.voditelj_slucaja || 'Nije dodijeljen'}</span>
            </div>
            <div class="item">
              <span class="item-label">Datum kreiranja:</span>
              <span class="item-value">${new Date(izvjestaj.slucaj.datum_kreiranja).toLocaleDateString('bs-BA')}</span>
            </div>
          </div>
        </div>

        <div class="section">
          <h3>DOKAZI</h3>
          <div class="section-content">
            ${izvjestaj.dokazi && izvjestaj.dokazi.length > 0
          ? izvjestaj.dokazi.map((dokaz, index) => `
                <div style="margin-bottom: 12px;">
                  <div class="item"><strong>${index + 1}. ${dokaz.opis}</strong></div>
                  <div class="sub-item"><span class="item-label">Tip:</span> <span class="item-value">${dokaz.tip_dokaza}</span></div>
                  <div class="sub-item"><span class="item-label">Lokacija:</span> <span class="item-value">${dokaz.lokacija_pronalaska}</span></div>
                  <div class="sub-item"><span class="item-label">Prikupio:</span> <span class="item-value">${dokaz.prikupio_ime || 'N/A'}</span></div>
                  <div class="sub-item"><span class="item-label">Datum:</span> <span class="item-value">${new Date(dokaz.datum_prikupa).toLocaleDateString('bs-BA')}</span></div>
                </div>
              `).join('')
          : '<div class="no-data">Nema evidentiranih dokaza.</div>'
        }
          </div>
        </div>

        <div class="section">
          <h3>LANAC NADZORA</h3>
          <div class="section-content">
            ${izvjestaj.lanacNadzora && izvjestaj.lanacNadzora.length > 0
          ? izvjestaj.lanacNadzora.map((unos, index) => `
                <div style="margin-bottom: 12px;">
                  <div class="item"><strong>${index + 1}. ${unos.dokaz_opis || 'Primopredaja'}</strong></div>
                  <div class="sub-item"><span class="item-label">Predao:</span> <span class="item-value">${unos.predao_ime || 'N/A'}</span></div>
                  <div class="sub-item"><span class="item-label">Preuzeo:</span> <span class="item-value">${unos.preuzeo_ime || 'N/A'}</span></div>
                  <div class="sub-item"><span class="item-label">Datum:</span> <span class="item-value">${new Date(unos.datum_primopredaje).toLocaleDateString('bs-BA')}</span></div>
                  <div class="sub-item"><span class="item-label">Status potvrde:</span> <span class="item-value">${unos.potvrda_status}</span></div>
                </div>
              `).join('')
          : '<div class="no-data">Nema evidentiranog lanca nadzora.</div>'
        }
          </div>
        </div>

        <div class="section">
          <h3>TIM NA SLUČAJU</h3>
          <div class="section-content">
            ${izvjestaj.tim && izvjestaj.tim.length > 0
          ? izvjestaj.tim.map((clan, index) => `
                <div style="margin-bottom: 10px;">
                  <div class="item"><strong>${index + 1}. ${clan.ime_prezime}</strong></div>
                  ${clan.naziv_uloge ? `<div class="sub-item"><span class="item-label">Uloga:</span> <span class="item-value">${clan.naziv_uloge}</span></div>` : ''}
                  <div class="sub-item"><span class="item-label">Uloga na slučaju:</span> <span class="item-value">${clan.uloga_na_slucaju}</span></div>
                  ${clan.email ? `<div class="sub-item"><span class="item-label">Email:</span> <span class="item-value">${clan.email}</span></div>` : ''}
                </div>
              `).join('')
          : '<div class="no-data">Nema članova tima na slučaju.</div>'
        }
          </div>
        </div>

        <div class="section">
          <h3>SVJEDOCI</h3>
          <div class="section-content">
            ${izvjestaj.svjedoci && izvjestaj.svjedoci.length > 0
          ? izvjestaj.svjedoci.map((svjedok, index) => `
                <div style="margin-bottom: 12px;">
                  <div class="item"><strong>${index + 1}. ${svjedok.ime_prezime}</strong></div>
                  ${svjedok.jmbg ? `<div class="sub-item"><span class="item-label">JMBG:</span> <span class="item-value">${svjedok.jmbg}</span></div>` : ''}
                  ${svjedok.adresa ? `<div class="sub-item"><span class="item-label">Adresa:</span> <span class="item-value">${svjedok.adresa}</span></div>` : ''}
                  ${svjedok.kontakt_telefon ? `<div class="sub-item"><span class="item-label">Telefon:</span> <span class="item-value">${svjedok.kontakt_telefon}</span></div>` : ''}
                  ${svjedok.biljeska ? `<div class="sub-item"><span class="item-label">Bilješka:</span> <span class="item-value">${svjedok.biljeska}</span></div>` : ''}
                </div>
              `).join('')
          : '<div class="no-data">Nema evidentiranih svjedoka.</div>'
        }
          </div>
        </div>

        <div class="footer">
          <p>Izvještaj generisan: ${new Date().toLocaleString('bs-BA')}</p>
          <p>Generisao: ${auth?.user?.ime_prezime || 'Nepoznat korisnik'} (${auth?.user?.uloga || 'N/A'})</p>
        </div>
      `;

      // Konfiguriraj html2pdf
      // Napomena: innerHTML se koristi jer html2pdf zahtijeva DOM element sa HTML sadržajem
      // HTML se generiše na frontendu iz podataka iz baze, tako da je relativno siguran
      // Za dodatnu sigurnost, sanitizujemo potencijalno opasne elemente
      const element = document.createElement('div');
      // Sanitizuj HTML prije postavljanja - ukloni potencijalno opasne elemente
      const safeHtmlContent = htmlContent
        .replace(/javascript:/gi, '') // Ukloni javascript: protokole
        .replace(/on\w+\s*=/gi, '') // Ukloni event handlere (onclick, onload, itd.)
        .replace(/<script[\s\S]*?<\/script>/gi, '') // Ukloni script tagove
        .replace(/<iframe[\s\S]*?<\/iframe>/gi, ''); // Ukloni iframe tagove
      element.innerHTML = safeHtmlContent;

      const options = {
        margin: [10, 10, 10, 10],
        filename: `Izvjestaj_Slucaj_${izvjestaj.slucaj.broj_slucaja}_${new Date().getTime()}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2 },
        jsPDF: { orientation: 'portrait', unit: 'mm', format: 'a4' },
        pagebreak: { mode: 'avoid-all' },
      };

      html2pdf().set(options).from(element).save();

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
    <div>
      <button
        onClick={onBackToList}
        className="mb-6 text-sm text-blue-400 hover:underline"
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
    </div>
  );
}