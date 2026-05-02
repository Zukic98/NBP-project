package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DodajUposlenikaRequest;
import ba.unsa.etf.suds.dto.PromijeniStatusRequest;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servis za upravljanje uposlenicima policijskih stanica.
 *
 * <p>Orkestrira {@link UposlenikRepository} i {@link BCryptPasswordEncoder} kako bi
 * podržao kompletan životni ciklus uposlenika: dodavanje, pregled, promjenu statusa,
 * ažuriranje podataka i upravljanje lozinkama. Sve operacije koje mijenjaju podatke
 * uposlenika iz druge stanice bacaju {@link SecurityException}.
 */
@Service
public class UposlenikService {

    private final UposlenikRepository uposlenikRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /** Konstruktorska injekcija repozitorija uposlenika i enkodera lozinki. */
    public UposlenikService(UposlenikRepository uposlenikRepository, BCryptPasswordEncoder passwordEncoder) {
        this.uposlenikRepository = uposlenikRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Dohvata listu svih uposlenika u sistemu.
     *
     * @return lista {@link UposlenikDTO} objekata za sve uposlenike
     */
    public List<UposlenikDTO> getAllUposlenici() {
        return uposlenikRepository.findAllUposlenici();
    }

    /**
     * Dohvata uposlenika prema ID-u korisnika.
     *
     * @param id identifikator korisnika ({@code NBP_USER.ID})
     * @return {@link UposlenikDTO} s podacima o uposleniku
     * @throws RuntimeException ako uposlenik s datim ID-om nije pronađen
     */
    public UposlenikDTO getUposlenikById(Long id) {
        return uposlenikRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Uposlenik sa ID " + id + " nije pronađen!"));
    }

    /**
     * Dohvata listu uposlenika koji pripadaju određenoj stanici.
     *
     * @param stanicaId identifikator policijske stanice
     * @return lista {@link UposlenikDTO} objekata za datu stanicu
     */
    public List<UposlenikDTO> getUposleniciPoStanici(Long stanicaId) {
        return uposlenikRepository.findAllByStanicaId(stanicaId);
    }

    /**
     * Dodaje novog uposlenika u stanicu.
     *
     * <p>Provjerava jedinstvenost emaila, korisničkog imena i broja značke.
     * Lozinka se enkodira BCrypt algoritmom prije pohrane.
     *
     * @param request   podaci o novom uposleniku (uključujući lozinku u čistom tekstu)
     * @param stanicaId identifikator stanice kojoj uposlenik pripada
     * @throws RuntimeException ako email, korisničko ime ili broj značke već postoje
     */
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

    

    /**
     * Mijenja status uposlenika (npr. aktivacija, penzionisanje, otpuštanje).
     *
     * <p>Sigurnosna pravila:
     * <ul>
     *   <li>Uposlenik mora pripadati istoj stanici kao trenutni korisnik.</li>
     *   <li>Zadnji aktivni {@code SEF_STANICE} ne može biti otpušten ni penzionisan.</li>
     * </ul>
     *
     * @param userId           identifikator uposlenika čiji se status mijenja
     * @param request          zahtjev s novim statusom
     * @param currentStanicaId identifikator stanice trenutno prijavljenog korisnika
     * @param currentUserId    identifikator trenutno prijavljenog korisnika
     * @throws SecurityException ako uposlenik pripada drugoj stanici ili bi
     *                           otpuštanje/penzionisanje ostavilo stanicu bez šefa
     */
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

   /**
    * Ažurira osnovne podatke uposlenika (ime, prezime, email).
    *
    * <p>Uposlenik mora pripadati istoj stanici kao trenutni korisnik.
    * Provjerava se jedinstvenost emaila — novi email ne smije biti zauzet
    * od strane drugog korisnika.
    *
    * @param targetUserId     identifikator uposlenika čiji se podaci ažuriraju
    * @param request          DTO s novim podacima (ime, prezime, email)
    * @param currentStanicaId identifikator stanice trenutno prijavljenog korisnika
    * @throws SecurityException ako uposlenik pripada drugoj stanici
    * @throws RuntimeException  ako je novi email već zauzet
    */
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
    /**
     * Resetuje lozinku uposlenika (administrativna akcija šefa stanice).
     *
     * <p>Uposlenik mora pripadati istoj stanici kao trenutni korisnik.
     * Nova lozinka se enkodira BCrypt algoritmom. Pristup ovoj metodi
     * ograničen je na {@code SEF_STANICE} ulogom na nivou kontrolera.
     *
     * @param targetUserId     identifikator uposlenika čija se lozinka resetuje
     * @param novaLozinka      nova lozinka u čistom tekstu
     * @param currentStanicaId identifikator stanice trenutno prijavljenog korisnika
     * @throws SecurityException ako uposlenik pripada drugoj stanici
     */
    public void resetujLozinku(Long targetUserId, String novaLozinka, Long currentStanicaId) {
        if (!uposlenikRepository.isUserInStanica(targetUserId, currentStanicaId)) {
            throw new SecurityException("Nemate pravo mijenjati lozinku uposlenika iz druge stanice!");
        }
        String encoded = passwordEncoder.encode(novaLozinka);
        uposlenikRepository.updatePassword(targetUserId, encoded);
    }

    /**
     * Omogućava korisniku da sam promijeni svoju lozinku.
     *
     * <p>Stara lozinka mora odgovarati trenutno pohranjenoj hash vrijednosti.
     * Nova lozinka se enkodira BCrypt algoritmom. Svaki prijavljeni korisnik
     * može koristiti ovu metodu za vlastiti nalog.
     *
     * @param userId identifikator korisnika koji mijenja lozinku
     * @param stara  stara lozinka u čistom tekstu (za verifikaciju)
     * @param nova   nova lozinka u čistom tekstu
     * @throws RuntimeException ako stara lozinka nije ispravna
     */
    public void promijeniLicnuLozinku(Long userId, String stara, String nova) {
        String trenutnaLozinkaHash = uposlenikRepository.getPasswordByUserId(userId);
        
        if (!passwordEncoder.matches(stara, trenutnaLozinkaHash)) {
            throw new RuntimeException("Stara lozinka nije ispravna!");
        }
        
        uposlenikRepository.updatePassword(userId, passwordEncoder.encode(nova));
    }
}