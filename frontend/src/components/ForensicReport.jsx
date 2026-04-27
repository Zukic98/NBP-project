import React, { useState, useEffect } from 'react';
import { forensicApi } from '../api.js';

export default function ForensicReport({ dokazId, auth, isReadOnly }) {
  const [izvjestaj, setIzvjestaj] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({ sadrzaj: '', zakljucak: '' });

  const isForenzicar = auth.user.uloga === 'Forenzičar';

  useEffect(() => {
    fetchReport();
  }, [dokazId]);

 const fetchReport = async () => {
  try {
    const res = await forensicApi.getByEvidenceId(dokazId);
    
    if (res.status === 200 && res.data) {
      setIzvjestaj(res.data);
    } 
    else if (res.status === 204) {
      setIzvjestaj(null);
    }
  } catch (err) {
    console.error("Greška pri dohvaćanju:", err);
    setIzvjestaj(null);
  }
};
  const handleEditClick = () => {
    setFormData({
      sadrzaj: izvjestaj.sadrzaj,
      zakljucak: izvjestaj.zakljucak
    });
    setIsEditing(true);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.sadrzaj || !formData.zakljucak) return alert("Popunite sva polja!");

    setLoading(true);
    try {
      const payload = {
        dokazId: dokazId,
        sadrzaj: formData.sadrzaj,
        zakljucak: formData.zakljucak
      };

      if (isEditing && izvjestaj?.izvjestajId) {
        await forensicApi.update(izvjestaj.izvjestajId, payload);
      } else {
        await forensicApi.create(payload);
      }

      setShowForm(false);
      setIsEditing(false);
      setFormData({ sadrzaj: '', zakljucak: '' });
      fetchReport();
    } catch (err) {
      alert("Greška pri spašavanju izvještaja.");
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setIsEditing(false);
    setFormData({ sadrzaj: '', zakljucak: '' });
  };


  if (izvjestaj && !showForm) {
    return (
      <>
        <div className="flex items-center gap-3 mt-2">
          <button
            onClick={() => setShowViewModal(true)}
            className="flex items-center text-xs font-semibold text-blue-400 hover:text-blue-300 bg-blue-400/10 px-2 py-1 rounded border border-blue-400/20 transition"
          >
            <span className="mr-1.5">📄</span> Pogledaj izvještaj
          </button>

          {isForenzicar && !isReadOnly && (
            <button
              onClick={handleEditClick}
              className="text-[10px] font-bold text-gray-400 hover:text-purple-400 uppercase tracking-tighter transition"
            >
              ✏️ Izmijeni
            </button>
          )}
        </div>

        {showViewModal && (
          <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
            <div className="bg-gray-800 border border-gray-700 rounded-xl max-w-lg w-full shadow-2xl overflow-hidden">
              <div className="p-5 border-b border-gray-700 flex justify-between items-center bg-gray-800/50">
                <h3 className="text-lg font-bold text-white">Forenzički Izvještaj</h3>
                <button onClick={() => setShowViewModal(false)} className="text-gray-400 hover:text-white text-xl">✕</button>
              </div>
              <div className="p-6 space-y-5">
                <div>
                  <label className="text-[10px] text-gray-500 uppercase tracking-widest font-bold">Detaljna analiza</label>
                  <div className="text-gray-200 mt-1.5 bg-gray-900/50 p-4 rounded-lg border border-gray-700 text-sm leading-relaxed max-h-60 overflow-y-auto">
                    {izvjestaj.sadrzaj}
                  </div>
                </div>
                <div>
                  <label className="text-[10px] text-blue-400 uppercase tracking-widest font-bold">Konačni Zaključak</label>
                  <p className="text-white font-medium mt-1.5 bg-blue-500/10 p-3 rounded-lg border border-blue-500/30">
                    {izvjestaj.zakljucak}
                  </p>
                </div>
                <div className="pt-2 flex justify-between items-center text-[10px] text-gray-500">
                  <span>ID: #{izvjestaj.izvjestajId}</span>
                  <span>Datum: {izvjestaj.datumKreiranja ? new Date(izvjestaj.datumKreiranja).toLocaleString() : 'N/A'}</span>
                </div>
              </div>
              <div className="p-4 bg-gray-900/50 text-right">
                <button onClick={() => setShowViewModal(false)} className="px-5 py-2 bg-gray-700 text-white text-sm rounded-lg hover:bg-gray-600 transition">
                  Zatvori
                </button>
              </div>
            </div>
          </div>
        )}
      </>
    );
  }

  if (isForenzicar && !isReadOnly) {
    return (
      <div className="mt-2">
        {!showForm ? (
          <button
            onClick={() => setShowForm(true)}
            className="flex items-center text-xs bg-purple-600/20 hover:bg-purple-600 text-purple-400 hover:text-white border border-purple-500/40 px-3 py-1.5 rounded-lg transition-all font-medium"
          >
            <span className="mr-1.5">+</span> Dodaj forenzički izvještaj
          </button>
        ) : (
          <div className="mt-3 p-4 bg-gray-900/80 rounded-xl border border-purple-500/30 shadow-xl">
            <h4 className="text-[10px] font-bold text-purple-400 mb-3 uppercase tracking-wider">
              {isEditing ? 'Uređivanje izvještaja' : 'Unos novog izvještaja'}
            </h4>
            <textarea
              placeholder="Opišite postupak analize..."
              className="w-full bg-gray-800 text-sm p-3 rounded-lg text-white border border-gray-700 mb-3 focus:border-purple-500 outline-none transition-all"
              rows="4"
              value={formData.sadrzaj}
              onChange={(e) => setFormData({ ...formData, sadrzaj: e.target.value })}
            />
            <input
              type="text"
              placeholder="Konačni zaključak..."
              className="w-full bg-gray-800 text-sm p-3 rounded-lg text-white border border-gray-700 mb-4 focus:border-purple-500 outline-none transition-all"
              value={formData.zakljucak}
              onChange={(e) => setFormData({ ...formData, zakljucak: e.target.value })}
            />
            <div className="flex gap-2">
              <button
                onClick={handleSubmit}
                disabled={loading}
                className="flex-1 text-xs bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-700 disabled:opacity-50 transition font-bold"
              >
                {loading ? 'Spašavam...' : isEditing ? 'Spasi izmjene' : 'Objavi izvještaj'}
              </button>
              <button
                onClick={handleCancel}
                className="px-4 text-xs bg-gray-700 text-gray-300 py-2 rounded-lg hover:bg-gray-600 transition"
              >
                Otkaži
              </button>
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="mt-2 inline-flex items-center px-2 py-1 rounded bg-gray-800 border border-gray-700">
      <span className="text-[10px] text-gray-500 uppercase font-bold tracking-tight">
        ⏳ Čeka se analiza
      </span>
    </div>
  );
}