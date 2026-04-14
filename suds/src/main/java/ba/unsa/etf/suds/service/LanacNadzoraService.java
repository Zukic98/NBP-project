package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.MojaPrimopredajaDTO;
import ba.unsa.etf.suds.dto.PonistiRequest;
import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.dto.PrimopredajaRequest;
import ba.unsa.etf.suds.dto.PrimopredajaZaPotvrduDTO;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class LanacNadzoraService {
    private final LanacNadzoraRepository lanacRepository;
    private final DokazRepository dokazRepository;

    public LanacNadzoraService(LanacNadzoraRepository lanacRepository, DokazRepository dokazRepository) {
        this.lanacRepository = lanacRepository;
        this.dokazRepository = dokazRepository;
    }

    public LanacNadzora posaljiDokaz(PosaljiDokazRequest request, Long predaoUserId) {
        LanacNadzora lanac = new LanacNadzora();
        lanac.setDokazId(request.getDokazId());
        lanac.setStanicaId(request.getStanicaId());
        lanac.setDatumPrimopredaje(new Timestamp(System.currentTimeMillis()));
        lanac.setPredaoUserId(predaoUserId);
        lanac.setPreuzeoUserId(request.getPrimaocUserId());
        lanac.setSvrhaPrimopredaje(request.getSvrhaPrimopredaje());
        lanac.setPotvrdaStatus("Čeka potvrdu");

        return lanacRepository.save(lanac);
    }

    public List<LanacNadzora> getMojiZahtjevi(Long userId) {
        return lanacRepository.findZahtjeviZaKorisnika(userId);
    }

    public void prihvatiDokaz(Long unosId, Long potvrdioUserId) {
        LanacNadzora lanac = lanacRepository.findById(unosId)
                .orElseThrow(() -> new IllegalStateException("Unos lanca nadzora ne postoji: " + unosId));

        if (!lanac.getPreuzeoUserId().equals(potvrdioUserId)) {
            throw new IllegalStateException("Samo primaoc može potvrditi primopredaju");
        }

        if (!"Čeka potvrdu".equals(lanac.getPotvrdaStatus())) {
            throw new IllegalStateException("Primopredaja je već obrađena");
        }

        lanacRepository.prihvati(unosId, potvrdioUserId);
    }

    public LanacNadzora kreirajPrimopredaju(Long dokazId, PrimopredajaRequest request, Long predaoUserId) {
        Dokaz dokaz = dokazRepository.findById(dokazId);
        if (dokaz == null) {
            throw new IllegalStateException("Dokaz ne postoji: " + dokazId);
        }

        LanacNadzora lanac = new LanacNadzora();
        lanac.setDokazId(dokazId);
        lanac.setStanicaId(dokaz.getStanicaId());
        lanac.setDatumPrimopredaje(new Timestamp(System.currentTimeMillis()));
        lanac.setPredaoUserId(predaoUserId);
        lanac.setPreuzeoUserId(request.getPreuzeoUposlenikId());
        lanac.setSvrhaPrimopredaje(request.getSvrha());
        lanac.setPotvrdaStatus("Čeka potvrdu");

        return lanacRepository.save(lanac);
    }

    public List<PrimopredajaZaPotvrduDTO> getCekaPotvrduZaMene(Long userId) {
        return lanacRepository.findPrimopredajeZaPotvrdu(userId);
    }

    public List<MojaPrimopredajaDTO> getMojaSlanjaNaPotvrdi(Long userId) {
        List<MojaPrimopredajaDTO> slanja = lanacRepository.findMojaSlanjaNaPotvrdi(userId);
        long sada = System.currentTimeMillis();
        for (MojaPrimopredajaDTO dto : slanja) {
            Timestamp datum = dto.getDatumPrimopredaje();
            if (datum != null) {
                dto.setProtekloSekundi((sada - datum.getTime()) / 1000);
            } else {
                dto.setProtekloSekundi(0L);
            }
        }
        return slanja;
    }

    public void potvrdiIliOdbij(Long unosId, String status, String napomena, Long potvrdioUserId) {
        if (!"Potvrđeno".equals(status) && !"Odbijeno".equals(status)) {
            throw new IllegalStateException("Neispravan status potvrde: " + status);
        }

        LanacNadzora lanac = lanacRepository.findById(unosId)
                .orElseThrow(() -> new IllegalStateException("Unos lanca nadzora ne postoji: " + unosId));

        if (!lanac.getPreuzeoUserId().equals(potvrdioUserId)) {
            throw new IllegalStateException("Samo primaoc može potvrditi ili odbiti primopredaju");
        }

        if (!"Čeka potvrdu".equals(lanac.getPotvrdaStatus())) {
            throw new IllegalStateException("Primopredaja je već obrađena");
        }

        lanacRepository.potvrdiIliOdbij(unosId, status, napomena, potvrdioUserId);
    }

    public void ponistiPrimopredaju(Long unosId, PonistiRequest request, Long userId) {
        LanacNadzora lanac = lanacRepository.findById(unosId)
                .orElseThrow(() -> new IllegalStateException("Unos lanca nadzora ne postoji: " + unosId));

        if (!lanac.getPredaoUserId().equals(userId)) {
            throw new IllegalStateException("Samo pošiljalac može poništiti primopredaju");
        }

        if (!"Čeka potvrdu".equals(lanac.getPotvrdaStatus())) {
            throw new IllegalStateException("Moguće je poništiti samo primopredaju koja čeka potvrdu");
        }

        lanacRepository.ponisti(unosId, request.getRazlog(), userId);
    }
}
