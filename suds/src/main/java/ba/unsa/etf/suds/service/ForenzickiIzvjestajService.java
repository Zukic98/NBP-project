package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import ba.unsa.etf.suds.repository.ForenzickiIzvjestajRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ForenzickiIzvjestajService {
    private final ForenzickiIzvjestajRepository repository;

    public ForenzickiIzvjestajService(ForenzickiIzvjestajRepository repository) {
        this.repository = repository;
    }

    public ForenzickiIzvjestaj kreirajIzvjestaj(ForenzickiIzvjestaj izvjestaj) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        izvjestaj.setKreatorUserId(Long.parseLong(principal.toString()));
        
        return repository.save(izvjestaj);
    }

    public ForenzickiIzvjestaj getIzvjestajZaDokaz(Long dokazId) {
        return repository.findByDokazId(dokazId);
    }

    public ForenzickiIzvjestaj azurirajIzvjestaj(ForenzickiIzvjestaj izvjestaj) {
    return repository.update(izvjestaj);
}
}