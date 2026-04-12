package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.RegistrationRequest;
import ba.unsa.etf.suds.model.Stanica;
import ba.unsa.etf.suds.repository.StanicaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StanicaService {
    private final StanicaRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public StanicaService(StanicaRepository repository, BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Stanica> getAll() {
        return repository.findAll();
    }

    public Stanica getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stanica nije pronađena u bazi!"));
    }

    public void registerStanica(RegistrationRequest req) {
        String hashedPw = passwordEncoder.encode(req.getPassword());
        req.setPassword(hashedPw);
        
        Long roleIdSef = 100L; 

        repository.registerStanicaITips(req, roleIdSef);
    }
}