package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import ba.unsa.etf.suds.repository.ForenzickiIzvjestajRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForenzickiIzvjestajService {
    private final ForenzickiIzvjestajRepository repository;

    public ForenzickiIzvjestajService(ForenzickiIzvjestajRepository repository) {
        this.repository = repository;
    }


}