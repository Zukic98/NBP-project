package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.model.LanacNadzora;
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
        dokazRepository.updateStatus(request.getDokazId(), "Čeka potvrdu");

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
        dokazRepository.updateStatus(lanac.getDokazId(), "U posjedu");
    }
}