package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UposlenikService {
    private final UposlenikRepository repository;

    public UposlenikService(UposlenikRepository repository) {
        this.repository = repository;
    }

    public List<UposlenikDTO> getAllUposlenici() {
        return repository.findAllUposlenici();
    }

    public UposlenikDTO getUposlenikById(Long id) {
        return repository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Uposlenik sa ID " + id + " nije pronađen!"));
    }
}