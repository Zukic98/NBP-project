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

    public List<UposlenikDTO> getUposleniciPoStanici(Long stanicaId) {
        return uposlenikRepository.findAllByStanicaId(stanicaId);
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

   public void azurirajPodatke(Long targetUserId, UposlenikDTO request, Long currentStanicaId) {
    if (!uposlenikRepository.isUserInStanica(targetUserId, currentStanicaId)) {
        throw new SecurityException("Nemate pravo mijenjati podatke uposlenika iz druge stanice!");
    }
    if (uposlenikRepository.existsByEmailAndNotUserId(request.getEmail(), targetUserId)) {
        throw new RuntimeException("Email '" + request.getEmail() + "' je već zauzet!");
    }

    uposlenikRepository.updateBasicInfo(
        targetUserId, 
        request.getIme(), 
        request.getPrezime(), 
        request.getEmail()
    );
}
    public void resetujLozinku(Long targetUserId, String novaLozinka, Long currentStanicaId) {
        if (!uposlenikRepository.isUserInStanica(targetUserId, currentStanicaId)) {
            throw new SecurityException("Nemate pravo mijenjati lozinku uposlenika iz druge stanice!");
        }
        String encoded = passwordEncoder.encode(novaLozinka);
        uposlenikRepository.updatePassword(targetUserId, encoded);
    }

    public void promijeniLicnuLozinku(Long userId, String stara, String nova) {
        String trenutnaLozinkaHash = uposlenikRepository.getPasswordByUserId(userId);
        
        if (!passwordEncoder.matches(stara, trenutnaLozinkaHash)) {
            throw new RuntimeException("Stara lozinka nije ispravna!");
        }
        
        uposlenikRepository.updatePassword(userId, passwordEncoder.encode(nova));
    }
}