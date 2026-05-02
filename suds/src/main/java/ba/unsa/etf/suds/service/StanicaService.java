package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.RegistrationRequest;
import ba.unsa.etf.suds.model.Stanica;
import ba.unsa.etf.suds.repository.StanicaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servis za upravljanje policijskim stanicama.
 *
 * <p>Orkestrira {@link StanicaRepository} i {@link BCryptPasswordEncoder} kako bi
 * podržao pregled i registraciju stanica. Registracija stanice je atomična operacija
 * koja kreira adresu, stanicu, NBP korisnika i profil uposlenika s ulogom
 * {@code SEF_STANICE} (hardkodirani ID uloge: 100).
 */
@Service
public class StanicaService {
    private final StanicaRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    /** Konstruktorska injekcija repozitorija stanica i enkodera lozinki. */
    public StanicaService(StanicaRepository repository, BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Dohvata listu svih policijskih stanica.
     *
     * @return lista svih {@link Stanica} zapisa iz baze
     */
    public List<Stanica> getAll() {
        return repository.findAll();
    }

    /**
     * Dohvata stanicu prema ID-u.
     *
     * @param id identifikator stanice
     * @return {@link Stanica} objekat
     * @throws RuntimeException ako stanica s datim ID-om ne postoji
     */
    public Stanica getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stanica nije pronađena u bazi!"));
    }

    /**
     * Registruje novu policijsku stanicu zajedno s inicijalnim šefom stanice.
     *
     * <p>Lozinka se enkodira BCrypt algoritmom prije pohrane. Delegira atomičnu
     * operaciju metodi {@code repository.registerStanicaITips} koja kreira:
     * adresu, stanicu, NBP korisnika i profil uposlenika s ulogom
     * {@code SEF_STANICE} (ID uloge: 100).
     *
     * @param req podaci za registraciju stanice i šefa (uključujući lozinku u čistom tekstu)
     */
    public void registerStanica(RegistrationRequest req) {
        String hashedPw = passwordEncoder.encode(req.getPassword());
        req.setPassword(hashedPw);
        
        Long roleIdSef = 100L; 

        repository.registerStanicaITips(req, roleIdSef);
    }
}