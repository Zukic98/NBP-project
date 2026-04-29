import React, { useState, useEffect } from 'react';
import { suspectApi } from '../api.js';

export default function SuspectSection({ caseId, auth, caseStatus }) {
  const isReadOnly = caseStatus === 'Zatvoren' || caseStatus === 'Arhiviran';
  const [osumnjiceni, setOsumnjiceni] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const today = new Date().toISOString().split('T')[0];

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState({
    imePrezime: '',
    jmbg: '',
    datumRodjenja: '',
    ulicaIBroj: '',
    grad: '',
    postanskiBroj: '',
    drzava: 'Bosna i Hercegovina'
  });

  const [createError, setCreateError] = useState('');
  const [createSuccess, setCreateSuccess] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  // --- STANJA ZA FOTOGRAFIJE ---
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [photoPreviews, setPhotoPreviews] = useState([]);
  const [showPhotoModal, setShowPhotoModal] = useState(false);
  const [selectedOsumnjiceniPhotos, setSelectedOsumnjiceniPhotos] = useState([]);
  const [selectedOsumnjiceniName, setSelectedOsumnjiceniName] = useState('');
  const [osumnjiceniPhotos, setOsumnjiceniPhotos] = useState({});
  const [isUploadingPhotos, setIsUploadingPhotos] = useState(false);

  useEffect(() => {
    const fetchSuspects = async () => {
      try {
        setIsLoading(true);
        const response = await suspectApi.getByCaseId(caseId);
        const suspects = response.data;
        setOsumnjiceni(suspects);

        if (suspects.length > 0) {
          await loadPhotosForSuspects(suspects);
        }
      } catch (err) {
        setError('Greška pri dobavljanju osumnjičenih.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchSuspects();
  }, [caseId]);

  const loadPhotosForSuspects = async (suspects) => {
    const photosMap = {};
    for (const suspect of suspects) {
      try {
        const response = await suspectApi.getPhotos(suspect.osumnjiceniId);
        photosMap[suspect.osumnjiceniId] = response.data;
      } catch (err) {
        console.error(`Greška pri učitavanju fotografija za osumnjičenog ${suspect.osumnjiceniId}:`, err);
        photosMap[suspect.osumnjiceniId] = [];
      }
    }
    setOsumnjiceniPhotos(photosMap);
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setCreateError('');
    setCreateSuccess('');
  };

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);

    if (selectedFiles.length + files.length > 3) {
      setCreateError('Maksimalno 3 fotografije po osumnjičenom.');
      return;
    }

    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    const invalidFiles = files.filter(file => !allowedTypes.includes(file.type));
    if (invalidFiles.length > 0) {
      setCreateError('Dozvoljeni formati: JPEG, PNG, GIF, WebP');
      return;
    }

    const maxSize = 5 * 1024 * 1024;
    const oversizedFiles = files.filter(file => file.size > maxSize);
    if (oversizedFiles.length > 0) {
      setCreateError('Svaka fotografija mora biti manja od 5MB.');
      return;
    }

    setSelectedFiles(prev => [...prev, ...files]);
    const newPreviews = files.map(file => URL.createObjectURL(file));
    setPhotoPreviews(prev => [...prev, ...newPreviews]);
    setCreateError('');
  };

  const removeSelectedFile = (index) => {
    URL.revokeObjectURL(photoPreviews[index]);
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
    setPhotoPreviews(prev => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsAdding(true);

    if (new Date(formData.datumRodjenja) > new Date()) {
      setCreateError("Datum rođenja ne može biti u budućnosti.");
      setIsAdding(false);
      return;
    }

    try {
      const response = await suspectApi.create(caseId, formData);
      const noviOsumnjiceni = response.data;

      if (selectedFiles.length > 0) {
        setIsUploadingPhotos(true);
        try {
          const uploadPromises = selectedFiles.map((file, index) => {
            const formDataPhoto = new FormData();
            formDataPhoto.append('fotografija', file);
            formDataPhoto.append('nazivFajla', file.name);
            formDataPhoto.append('redniBroj', index + 1);
            formDataPhoto.append('opis', `Fotografija ${index + 1} za osumnjičenog ${formData.imePrezime}`);
            return suspectApi.uploadPhoto(noviOsumnjiceni.osumnjiceniId, formDataPhoto);
          });
          await Promise.all(uploadPromises);
        } catch (uploadErr) {
          console.error('Greška pri uploadu fotografija:', uploadErr);
        } finally {
          setIsUploadingPhotos(false);
        }
      }

      const refresh = await suspectApi.getByCaseId(caseId);
      setOsumnjiceni(refresh.data);
      await loadPhotosForSuspects(refresh.data);

      setFormData({
        imePrezime: '', jmbg: '', datumRodjenja: '',
        ulicaIBroj: '', grad: '', postanskiBroj: '', drzava: 'Bosna i Hercegovina'
      });
      setSelectedFiles([]);
      setPhotoPreviews([]);
      setShowCreateForm(false);
      setCreateSuccess('Osumnjičeni uspješno dodan sa fotografijama.');
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Greška pri dodavanju.');
    } finally {
      setIsAdding(false);
    }
  };

  const openPhotoModal = async (osumnjiceni) => {
    setSelectedOsumnjiceniName(osumnjiceni.imePrezime);
    setShowPhotoModal(true);

    if (!osumnjiceniPhotos[osumnjiceni.osumnjiceniId]) {
      try {
        const response = await suspectApi.getPhotos(osumnjiceni.osumnjiceniId);
        setOsumnjiceniPhotos(prev => ({
          ...prev,
          [osumnjiceni.osumnjiceniId]: response.data
        }));
        setSelectedOsumnjiceniPhotos(response.data);
      } catch (err) {
        console.error('Greška pri učitavanju fotografija:', err);
        setSelectedOsumnjiceniPhotos([]);
      }
    } else {
      setSelectedOsumnjiceniPhotos(osumnjiceniPhotos[osumnjiceni.osumnjiceniId]);
    }
  };

  const closePhotoModal = () => {
    setShowPhotoModal(false);
    setSelectedOsumnjiceniPhotos([]);
    setSelectedOsumnjiceniName('');
  };

  const handleAdditionalPhotoUpload = async (osumnjiceniId, files) => {
    const currentPhotos = osumnjiceniPhotos[osumnjiceniId] || [];

    if (currentPhotos.length + files.length > 3) {
      alert(`Maksimalno 3 fotografije. Trenutno ima ${currentPhotos.length}, može se dodati još ${3 - currentPhotos.length}.`);
      return;
    }

    setIsUploadingPhotos(true);
    try {
      const uploadPromises = files.map((file, index) => {
        const formDataPhoto = new FormData();
        formDataPhoto.append('fotografija', file);
        formDataPhoto.append('nazivFajla', file.name);
        formDataPhoto.append('redniBroj', currentPhotos.length + index + 1);
        return suspectApi.uploadPhoto(osumnjiceniId, formDataPhoto);
      });
      await Promise.all(uploadPromises);

      const response = await suspectApi.getPhotos(osumnjiceniId);
      setOsumnjiceniPhotos(prev => ({
        ...prev,
        [osumnjiceniId]: response.data
      }));

      if (showPhotoModal) {
        setSelectedOsumnjiceniPhotos(response.data);
      }
    } catch (err) {
      alert('Greška pri uploadu fotografija: ' + (err.response?.data?.message || err.message));
    } finally {
      setIsUploadingPhotos(false);
    }
  };

  const dozvolaDodavanja = [
    'SEF_STANICE', 'INSPEKTOR', 'Šef stanice', 'Inspektor',
    'Administrator', 'ADMINISTRATOR', 'ROLE_SEF_STANICE', 'ROLE_INSPEKTOR'
  ].includes(auth.user.uloga) && !isReadOnly;

  if (isLoading && osumnjiceni.length === 0) return <div className="text-gray-400 p-4 text-center">Učitavam osumnjičene...</div>;

  // 🌟 SAMO JEDAN RETURN - KOMPLETAN JSX
  return (
      <div className="bg-gray-800 p-6 rounded-lg shadow-lg mb-8">
        {/* HEADER SA DUGMETOM */}
        <div className="flex justify-between items-center mb-5 border-b border-gray-700 pb-3">
          <h2 className="text-2xl font-bold text-white">Osumnjičeni</h2>
          {dozvolaDodavanja && (
              <button
                  onClick={() => setShowCreateForm(!showCreateForm)}
                  className={`py-2 px-4 rounded font-semibold transition ${showCreateForm ? 'bg-gray-600 hover:bg-gray-700' : 'bg-red-600 hover:bg-red-700'} text-white`}
              >
                {showCreateForm ? "Odustani" : "+ Dodaj Osumnjičenog"}
              </button>
          )}
        </div>

        {/* PORUKE */}
        {error && <div className="text-red-500 mb-4">{error}</div>}
        {createError && <div className="text-red-400 mb-4 bg-red-900/20 p-2 rounded border border-red-800">{createError}</div>}
        {createSuccess && <div className="text-green-500 mb-4 bg-green-100/10 p-2 rounded border border-green-800/30">{createSuccess}</div>}

        {/* FORMA ZA DODAVANJE */}
        {showCreateForm && (
            <form onSubmit={handleSubmit} className="mb-8 p-4 bg-gray-700/50 rounded-lg border border-gray-600 grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* ... polja forme ... */}
              <div className="flex flex-col">
                <label className="text-xs text-gray-400 mb-1 ml-1">Ime i Prezime</label>
                <input type="text" name="imePrezime" placeholder="npr. Ivan Ivić" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.imePrezime} onChange={handleChange} required />
              </div>
              <div className="flex flex-col">
                <label className="text-xs text-gray-400 mb-1 ml-1">JMBG</label>
                <input type="text" name="jmbg" placeholder="1234567890123" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.jmbg} onChange={handleChange} required />
              </div>
              <div className="flex flex-col">
                <label className="text-xs text-gray-400 mb-1 ml-1">Datum rođenja</label>
                <input type="date" name="datumRodjenja" max={today} className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.datumRodjenja} onChange={handleChange} required />
              </div>
              <div className="flex flex-col">
                <label className="text-xs text-gray-400 mb-1 ml-1">Ulica i broj</label>
                <input type="text" name="ulicaIBroj" placeholder="npr. Maršala Tita 1" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.ulicaIBroj} onChange={handleChange} required />
              </div>
              <div className="flex flex-col">
                <label className="text-xs text-gray-400 mb-1 ml-1">Grad</label>
                <input type="text" name="grad" placeholder="npr. Sarajevo" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.grad} onChange={handleChange} required />
              </div>
              <div className="flex flex-col">
                <label className="text-xs text-gray-400 mb-1 ml-1">Poštanski broj</label>
                <input type="text" name="postanskiBroj" placeholder="npr. 71000" className="p-2 rounded bg-gray-600 text-white border border-gray-500 focus:border-blue-500 outline-none" value={formData.postanskiBroj} onChange={handleChange} required />
              </div>

              {/* SEKCJA ZA FOTOGRAFIJE */}
              <div className="md:col-span-2 mt-2">
                <label className="text-xs text-gray-400 mb-2 block">📸 Fotografije osumnjičenog (max 3, opcionalno)</label>
                <div className="flex items-center gap-3 mb-3">
                  <label className="cursor-pointer bg-gray-600 hover:bg-gray-500 text-white px-4 py-2 rounded transition">
                    <span>📁 Odaberi fotografije</span>
                    <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple onChange={handleFileSelect} className="hidden" disabled={selectedFiles.length >= 3} />
                  </label>
                  <span className="text-xs text-gray-400">{selectedFiles.length}/3 odabrano • JPEG, PNG, GIF, WebP • Max 5MB</span>
                </div>
                {photoPreviews.length > 0 && (
                    <div className="grid grid-cols-3 gap-3 mb-3">
                      {photoPreviews.map((preview, index) => (
                          <div key={`preview-${index}`} className="relative group">
                            <img src={preview} alt={`Preview ${index + 1}`} className="w-full h-32 object-cover rounded border border-gray-500" />
                            <button type="button" onClick={() => removeSelectedFile(index)} className="absolute top-1 right-1 bg-red-600 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition" title="Ukloni fotografiju">✕</button>
                            <span className="absolute bottom-1 left-1 bg-black/70 text-white text-xs px-1 rounded">{index + 1}</span>
                          </div>
                      ))}
                    </div>
                )}
              </div>

              <div className="md:col-span-2 mt-2">
                <button type="submit" disabled={isAdding || isUploadingPhotos} className="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white rounded font-bold disabled:bg-gray-500 transition">
                  {isUploadingPhotos ? "Upload fotografija u toku..." : isAdding ? "Spremanje u bazu..." : "Potvrdi i Evidentiraj"}
                </button>
              </div>
            </form>
        )}

        {/* TABELA */}
        <div className="overflow-x-auto">
          <table className="min-w-full bg-gray-700 rounded-lg overflow-hidden">
            <thead>
            <tr className="text-left text-gray-400 border-b border-gray-600 bg-gray-700/50">
              <th className="p-3">Ime i Prezime</th>
              <th className="p-3">JMBG</th>
              <th className="p-3">Datum rođenja</th>
              <th className="p-3">Adresa</th>
              <th className="p-3">Fotografije</th>
            </tr>
            </thead>
            <tbody>
            {osumnjiceni.length === 0 ? (
                <tr><td colSpan="5" className="p-10 text-center text-gray-500 italic">Trenutno nema evidentiranih osumnjičenih lica za ovaj slučaj.</td></tr>
            ) : (
                osumnjiceni.map(o => {
                  const photos = osumnjiceniPhotos[o.osumnjiceniId] || [];
                  return (
                      <tr key={o.osumnjiceniId} className="border-b border-gray-600 hover:bg-gray-600/50 transition duration-150">
                        <td className="p-3 text-white font-medium">{o.imePrezime}</td>
                        <td className="p-3 text-gray-300 font-mono text-sm">{o.jmbg}</td>
                        <td className="p-3 text-gray-300">{o.datumRodjenja ? new Date(o.datumRodjenja).toLocaleDateString('bs-BA') : 'N/A'}</td>
                        <td className="p-3 text-gray-300 text-sm">{o.adresa}</td>
                        <td className="p-3">
                          <button onClick={() => openPhotoModal(o)} className="flex items-center gap-2 text-blue-400 hover:text-blue-300 transition">
                            <span>📷</span>
                            <span className="text-sm">{photos.length > 0 ? `${photos.length} fotografije` : 'Dodaj fotografije'}</span>
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                            </svg>
                          </button>
                        </td>
                      </tr>
                  );
                })
            )}
            </tbody>
          </table>
        </div>

        {/* MODAL ZA FOTOGRAFIJE */}
        {showPhotoModal && (
            <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
              <div className="bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto border border-gray-600">
                <div className="flex justify-between items-center p-4 border-b border-gray-700">
                  <h3 className="text-xl font-bold text-white">📸 Fotografije: {selectedOsumnjiceniName}</h3>
                  <button onClick={closePhotoModal} className="text-gray-400 hover:text-white transition">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <div className="p-4">
                  {selectedOsumnjiceniPhotos.length === 0 ? (
                      <div className="text-center py-8">
                        <p className="text-gray-400 mb-4">Nema evidentiranih fotografija za ovog osumnjičenog.</p>
                        <label className="cursor-pointer inline-block bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded transition">
                          <span>📁 Dodaj prve fotografije</span>
                          <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple onChange={(e) => {
                            const files = Array.from(e.target.files);
                            const osumnjiceniObj = osumnjiceni.find(o => o.imePrezime === selectedOsumnjiceniName);
                            if (osumnjiceniObj) handleAdditionalPhotoUpload(osumnjiceniObj.osumnjiceniId, files);
                          }} className="hidden" />
                        </label>
                      </div>
                  ) : (
                      <>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
                          {selectedOsumnjiceniPhotos.map((photo) => {
                            const base64Data = photo.fotografijaBase64 || photo.fotografija_base64 || '';
                            const mimeType = photo.mimeType || photo.mime_type || 'image/jpeg';
                            const fileName = photo.nazivFajla || photo.naziv_fajla || 'Nepoznat fajl';
                            const fileSize = photo.velicinaFajla || photo.velicina_fajla;
                            const opis = photo.opisFotografije || photo.opis_fotografije;
                            const datum = photo.datumDodavanja || photo.datum_dodavanja;
                            return (
                                <div key={photo.fotografijaId || photo.fotografija_id} className="bg-gray-700 rounded-lg overflow-hidden border border-gray-600">
                                  {base64Data ? (
                                      <img src={`data:${mimeType};base64,${base64Data}`} alt={fileName} className="w-full h-48 object-cover" onError={(e) => { e.target.style.display = 'none'; }} />
                                  ) : (
                                      <div className="w-full h-48 bg-gray-600 flex items-center justify-center"><p className="text-gray-400 text-sm">Slika nije dostupna</p></div>
                                  )}
                                  <div className="p-3">
                                    <p className="text-sm text-gray-300 truncate" title={fileName}>{fileName}</p>
                                    {fileSize && <p className="text-xs text-gray-400 mt-1">{(fileSize / 1048576).toFixed(2)} MB</p>}
                                    {opis && <p className="text-xs text-gray-500 mt-1 italic">{opis}</p>}
                                    {datum && <p className="text-xs text-gray-500 mt-1">Dodano: {new Date(datum).toLocaleDateString('bs-BA')}</p>}
                                  </div>
                                </div>
                            );
                          })}
                        </div>
                        {selectedOsumnjiceniPhotos.length < 3 && (
                            <div className="text-center pt-4 border-t border-gray-700">
                              <label className="cursor-pointer inline-block bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded transition">
                                <span>📁 Dodaj još fotografija ({3 - selectedOsumnjiceniPhotos.length} preostalo)</span>
                                <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple onChange={(e) => {
                                  const files = Array.from(e.target.files);
                                  const osumnjiceniObj = osumnjiceni.find(o => o.imePrezime === selectedOsumnjiceniName);
                                  if (osumnjiceniObj) handleAdditionalPhotoUpload(osumnjiceniObj.osumnjiceniId, files);
                                }} className="hidden" />
                              </label>
                            </div>
                        )}
                      </>
                  )}
                </div>
              </div>
            </div>
        )}
      </div>
  );
}