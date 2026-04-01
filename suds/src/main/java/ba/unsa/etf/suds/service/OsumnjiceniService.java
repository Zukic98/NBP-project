package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.repository.OsumnjiceniRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OsumnjiceniService {
    private final OsumnjiceniRepository repository;

    public OsumnjiceniService(OsumnjiceniRepository repository) {
        this.repository = repository;
    }

    public List<Osumnjiceni> getAllOsumnjiceni() {
        return repository.findAll(); // Pretpostavljam da imate ovu metodu u repozitoriju
    }
}