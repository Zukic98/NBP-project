package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DokazDosijeDTO;
import ba.unsa.etf.suds.dto.DokazListDTO;
import ba.unsa.etf.suds.dto.DokazStanjeDTO;
import ba.unsa.etf.suds.dto.KreirajDokazRequest;
import ba.unsa.etf.suds.dto.LanacDetaljiDTO;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.ForenzickiIzvjestajRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class DokazService {

    private final DokazRepository dokazRepository;
    private final LanacNadzoraRepository lanacNadzoraRepository;
    private final ForenzickiIzvjestajRepository izvjestajRepository;

    // Spring automatski injekta repozitorije
    public DokazService(DokazRepository dokazRepository,
                        LanacNadzoraRepository lanacNadzoraRepository,
                        ForenzickiIzvjestajRepository izvjestajRepository) {
        this.dokazRepository = dokazRepository;
        this.lanacNadzoraRepository = lanacNadzoraRepository;
        this.izvjestajRepository = izvjestajRepository;
    }

    // Unos novog dokaza
    public Dokaz kreirajDokaz(Dokaz dokaz) {
        // Ako datum prikupa nije poslan, postavi na trenutno vrijeme
        if (dokaz.getDatumPrikupa() == null) {
            dokaz.setDatumPrikupa(new Timestamp(System.currentTimeMillis()));
        }
        return dokazRepository.save(dokaz);
    }

    // Dodavanje novog koraka u lanac nadzora
    public LanacNadzora dodajULanacNadzora(Long dokazId, LanacNadzora lanac) {
        // Obavezno uvezujemo proslijeđeni ID dokaza iz URL-a sa objektom
        lanac.setDokazId(dokazId);
        return lanacNadzoraRepository.save(lanac);
    }

    // Sklapanje DTO objekta (traženo u zadatku)
    public DokazDosijeDTO getDokazDosije(Long dokazId) {
        Dokaz dokaz = dokazRepository.findById(dokazId);
        if (dokaz == null) {
            throw new IllegalArgumentException("Dokaz sa ID-em " + dokazId + " nije pronađen!");
        }

        DokazDosijeDTO dosije = new DokazDosijeDTO();
        dosije.setDokaz(dokaz);
        dosije.setLanacNadzora(lanacNadzoraRepository.findByDokazId(dokazId));
        dosije.setForenzickiZakljucak(izvjestajRepository.findZakljucakByDokazId(dokazId));

        return dosije;
    }

    public List<DokazListDTO> getBySlucajId(Long slucajId) {
        return dokazRepository.findBySlucajId(slucajId);
    }

    public DokazListDTO kreirajZaSlucaj(Long slucajId, KreirajDokazRequest request, Long userId) {
        Long stanicaId = dokazRepository.findStanicaIdByUserId(userId);
        if (stanicaId == null) {
            throw new IllegalArgumentException("Employee profile not found for user ID: " + userId);
        }

        Dokaz dokaz = new Dokaz();
        dokaz.setSlucajId(slucajId);
        dokaz.setStanicaId(stanicaId);
        dokaz.setOpis(request.getOpis());
        dokaz.setLokacijaPronalaska(request.getLokacijaPronalaska());
        dokaz.setTipDokaza(request.getTipDokaza());
        dokaz.setStatus("U posjedu");
        dokaz.setDatumPrikupa(new Timestamp(System.currentTimeMillis()));
        dokaz.setPrikupioUserId(userId);

        Dokaz sacuvan = dokazRepository.save(dokaz);

        return dokazRepository.findBySlucajId(slucajId).stream()
                .filter(dto -> dto.getDokazId().equals(sacuvan.getDokazId()))
                .findFirst()
                .orElseGet(() -> {
                    DokazListDTO fallback = new DokazListDTO();
                    fallback.setDokazId(sacuvan.getDokazId());
                    fallback.setOpis(sacuvan.getOpis());
                    fallback.setLokacijaPronalaska(sacuvan.getLokacijaPronalaska());
                    fallback.setTipDokaza(sacuvan.getTipDokaza());
                    fallback.setStatus(sacuvan.getStatus());
                    fallback.setDatumPrikupa(sacuvan.getDatumPrikupa());
                    fallback.setSlucajId(sacuvan.getSlucajId());
                    return fallback;
                });
    }

    public DokazStanjeDTO getStanje(Long dokazId, Long userId) {
        DokazRepository.DokazStanjeInfo stanje = dokazRepository.findStanje(dokazId)
                .orElseThrow(() -> new IllegalArgumentException("Dokaz sa ID-em " + dokazId + " nije pronađen!"));

        DokazStanjeDTO dto = new DokazStanjeDTO();
        DokazStanjeDTO.TrenutniNosilacInfo nosilac = new DokazStanjeDTO.TrenutniNosilacInfo();
        nosilac.setTrenutniNosilacId(stanje.trenutniNosilacId());
        nosilac.setTrenutniNosilacIme(stanje.trenutniNosilacIme());
        nosilac.setStatus(stanje.status());
        nosilac.setZadnjaPrimopredaja(stanje.zadnjaPrimopredaja());

        dto.setTrenutniNosilac(nosilac);
        dto.setMozePredati(!stanje.imaCekajucuPotvrdu()
                && stanje.trenutniNosilacId() != null
                && stanje.trenutniNosilacId().equals(userId));

        return dto;
    }

    public List<LanacDetaljiDTO> getLanacWithNames(Long dokazId) {
        return dokazRepository.findLanacWithNames(dokazId);
    }

    public void azurirajStatus(Long dokazId, String status) {
        boolean updated = dokazRepository.updateStatus(dokazId, status);
        if (!updated) {
            throw new IllegalArgumentException("Dokaz sa ID-em " + dokazId + " nije pronađen!");
        }
    }
}
