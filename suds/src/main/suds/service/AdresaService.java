package ba.unsa.etf.suds.ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.repository.AdresaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdresaService {

    private final AdresaRepository adresaRepository;

    public AdresaService(AdresaRepository adresaRepository) {
        this.adresaRepository = adresaRepository;
    }

    public List<Adresa> getAllAdrese() {
        return adresaRepository.findAll();
    }

    public Adresa createAdresa(Adresa adresa) {
        // Validation before input
        if (adresa.getUlicaIBroj() == null || adresa.getUlicaIBroj().isEmpty()) {
            throw new IllegalArgumentException("Ulica i broj su obavezni!");
        }
        return adresaRepository.save(adresa);
    }
}