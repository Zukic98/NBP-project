package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.Stanica;
import ba.unsa.etf.suds.repository.StanicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StanicaService {
    private final StanicaRepository repository;

    public StanicaService(StanicaRepository repository) {
        this.repository = repository;
    }

    public List<Stanica> getAll() {
        return repository.findAll();
    }

    public Stanica getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stanica nije pronađena u bazi!"));
    }
}