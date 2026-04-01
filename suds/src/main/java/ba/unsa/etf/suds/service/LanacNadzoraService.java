package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanacNadzoraService {
    private final LanacNadzoraRepository repository;

    public LanacNadzoraService(LanacNadzoraRepository repository) {
        this.repository = repository;
    }

}