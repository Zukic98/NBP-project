package ba.unsa.etf.suds.ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DokazDosijeDTO;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.ForenzickiIzvjestajRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

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
}