package ba.unsa.etf.suds.ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.Svjedok;
import ba.unsa.etf.suds.repository.SvjedokRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SvjedokService {
    private final SvjedokRepository repository;

    public SvjedokService(SvjedokRepository repository) {
        this.repository = repository;
    }

    public List<Svjedok> getAllSvjedoci() {
        return repository.findAll();
    }
}