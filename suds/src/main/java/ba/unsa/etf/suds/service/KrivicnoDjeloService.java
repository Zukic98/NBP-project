package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.KrivicnoDjelo;
import ba.unsa.etf.suds.repository.KrivicnoDjeloRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KrivicnoDjeloService {
    private final KrivicnoDjeloRepository repository;

    public KrivicnoDjeloService(KrivicnoDjeloRepository repository) {
        this.repository = repository;
    }

    public List<KrivicnoDjelo> getAll() {
        return repository.findAll();
    }

    public KrivicnoDjelo getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Krivično djelo nije pronađeno!"));
    }


    public KrivicnoDjelo create(KrivicnoDjelo djelo) {
        // Validacije
        if (djelo.getNaziv() == null || djelo.getNaziv().trim().isEmpty()) {
            throw new IllegalArgumentException("Naziv krivičnog djela je obavezan.");
        }
        if (djelo.getKazneniZakonClan() == null || djelo.getKazneniZakonClan().trim().isEmpty()) {
            throw new IllegalArgumentException("Član kaznenog zakona je obavezan.");
        }

        if (repository.postojiDuplikat(djelo.getNaziv(), djelo.getKazneniZakonClan())) {
        throw new IllegalArgumentException("Krivično djelo '" + djelo.getNaziv() + "' sa članom '" + djelo.getKazneniZakonClan() + "' već postoji u bazi!");
    }
        
        return repository.save(djelo);
    }

    public KrivicnoDjelo update(Long id, KrivicnoDjelo djelo) {
        // Provjeri da li postoji
        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Krivično djelo nije pronađeno!"));
        
        djelo.setId(id);
        
        // Validacije
        if (djelo.getNaziv() == null || djelo.getNaziv().trim().isEmpty()) {
            throw new IllegalArgumentException("Naziv krivičnog djela je obavezan.");
        }
        if (djelo.getKazneniZakonClan() == null || djelo.getKazneniZakonClan().trim().isEmpty()) {
            throw new IllegalArgumentException("Član kaznenog zakona je obavezan.");
        }
        
        return repository.update(djelo);
    }

    public void delete(Long id) {
        // Provjeri da li postoji
        repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Krivično djelo nije pronađeno!"));
        
        repository.deleteById(id);
    }
}