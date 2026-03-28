package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.InspektorDTO;
import ba.unsa.etf.suds.model.Korisnik;
import ba.unsa.etf.suds.model.Stanica;
import ba.unsa.etf.suds.model.UposlenikProfil;
import ba.unsa.etf.suds.repository.KorisnikRepository;
import ba.unsa.etf.suds.repository.StanicaRepository;
import ba.unsa.etf.suds.repository.UposlenikProfilRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KorisnikService {

    private final KorisnikRepository korisnikRepository;
    private final UposlenikProfilRepository profilRepository;
    private final StanicaRepository stanicaRepository;

    public KorisnikService(KorisnikRepository korisnikRepository, 
                           UposlenikProfilRepository profilRepository,
                           StanicaRepository stanicaRepository) {
        this.korisnikRepository = korisnikRepository;
        this.profilRepository = profilRepository;
        this.stanicaRepository = stanicaRepository;
    }

    public List<InspektorDTO> getAllInspektori() {
        List<Korisnik> sviKorisnici = korisnikRepository.findAll();
        List<UposlenikProfil> sviProfili = profilRepository.findAll();
        
        Map<Long, String> mapeStanica = stanicaRepository.findAll().stream()
                .collect(Collectors.toMap(Stanica::getStanicaId, Stanica::getImeStanice));

        List<InspektorDTO> inspektori = new ArrayList<>();

        for (UposlenikProfil profil : sviProfili) {
            sviKorisnici.stream()
                .filter(k -> k.getId().equals(profil.getUserId()))
                .findFirst()
                .ifPresent(k -> {
                    String nazivStanice = mapeStanica.getOrDefault(profil.getStanicaId(), "Nepoznata stanica");
                    inspektori.add(new InspektorDTO(
                        k.getId(),
                        k.getFirstName(),
                        k.getLastName(),
                        k.getEmail(),
                        profil.getBrojZnacke(),
                        nazivStanice
                    ));
                });
        }
        return inspektori;
    }
}