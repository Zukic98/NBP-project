package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DodajUposlenikaRequest;
import ba.unsa.etf.suds.dto.PromijeniStatusRequest;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UposlenikService {

    private final UposlenikRepository uposlenikRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UposlenikService(UposlenikRepository uposlenikRepository, BCryptPasswordEncoder passwordEncoder) {
        this.uposlenikRepository = uposlenikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UposlenikDTO> getAllUposlenici() {
        return uposlenikRepository.findAllUposlenici();
    }

    public UposlenikDTO getUposlenikById(Long id) {
        return uposlenikRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Uposlenik sa ID " + id + " nije pronađen!"));
    }

    //Dodavanje uposlenika
    public void dodajUposlenika(DodajUposlenikaRequest request, Long stanicaId) {
        // Provjera da li već postoji
        if (uposlenikRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' već postoji!");
        }
        if (uposlenikRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username '" + request.getUsername() + "' već postoji!");
        }
        if (uposlenikRepository.existsByBrojZnacke(request.getBrojZnacke())) {
            throw new RuntimeException("Broj značke '" + request.getBrojZnacke() + "' već postoji!");
        }

        // Enkodiranje lozinke
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Čuvanje u bazu
        uposlenikRepository.insertUposlenik(request, stanicaId, encodedPassword);
    }

    //Promjena statusa (sa sigurnosnom bravom)
    public void promijeniStatus(Long userId, PromijeniStatusRequest request, Long currentStanicaId, Long currentUserId) {
        // Provjera da li uposlenik pripada istoj stanici
        if (!uposlenikRepository.isUserInStanica(userId, currentStanicaId)) {
            throw new SecurityException("Nemate pravo mijenjati status uposlenika iz druge stanice!");
        }

        String noviStatus = request.getStatus();

        // SIGURNOSNA BRAVA: Ako se otpušta ili penzioniše šef
        if ("Penzionisan".equalsIgnoreCase(noviStatus) || "Otpušten".equalsIgnoreCase(noviStatus)) {
            String ulogaTarget = uposlenikRepository.getUserRoleName(userId);
            
            if ("SEF_STANICE".equals(ulogaTarget)) {
                int brojAktivnihSefova = uposlenikRepository.countActiveSefovaPoStanici(currentStanicaId);
                if (brojAktivnihSefova <= 1) {
                    throw new SecurityException("Ne možete otpustiti/penzionisati zadnjeg aktivnog šefa stanice!");
                }
            }
        }

        // Ažuriranje statusa
        uposlenikRepository.updateStatus(userId, noviStatus);
    }
}