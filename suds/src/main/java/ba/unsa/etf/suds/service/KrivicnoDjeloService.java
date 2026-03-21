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
}