package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.OsumnjiceniFotografijaDTO;
import ba.unsa.etf.suds.repository.OsumnjiceniFotografijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class OsumnjiceniFotografijaService {

    private final OsumnjiceniFotografijaRepository osumnjiceniFotografijaRepository;

    public OsumnjiceniFotografijaService(OsumnjiceniFotografijaRepository osumnjiceniFotografijaRepository) {
        this.osumnjiceniFotografijaRepository = osumnjiceniFotografijaRepository;
    }

    /**
     * Dobavljanje svih fotografija za osumnjičenog
     */
    public List<OsumnjiceniFotografijaDTO> getFotografijeByOsumnjiceniId(Long osumnjiceniId) {
        return osumnjiceniFotografijaRepository.findByOsumnjiceniId(osumnjiceniId);
    }

    /**
     * Upload fotografije za osumnjičenog
     * @throws RuntimeException ako je dostignut maksimum od 3 fotografije
     */
    public void uploadFotografiju(Long osumnjiceniId, MultipartFile file, Integer redniBroj,
                                  Long userId, String opis) throws IOException {
        // Provjera broja postojećih fotografija
        int trenutniBroj = osumnjiceniFotografijaRepository.countByOsumnjiceniId(osumnjiceniId);
        if (trenutniBroj >= 3) {
            throw new RuntimeException("Osumnjičeni već ima maksimalne 3 fotografije. Nije moguće dodati novu.");
        }

        // Validacija veličine fajla (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Fotografija ne smije biti veća od 5MB");
        }

        // Validacija tipa fajla
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/gif") &&
                        !contentType.equals("image/webp"))) {
            throw new RuntimeException("Dozvoljeni su samo slikovni fajlovi (JPEG, PNG, GIF, WebP)");
        }

        // Ako redni broj nije postavljen, koristi sljedeći dostupni
        if (redniBroj == null) {
            redniBroj = trenutniBroj + 1;
        }

        // Validacija rednog broja
        if (redniBroj < 1 || redniBroj > 3) {
            throw new RuntimeException("Redni broj mora biti između 1 i 3");
        }

        osumnjiceniFotografijaRepository.save(osumnjiceniId, file, redniBroj, userId, opis);
    }

    /**
     * Ažuriranje postojeće fotografije osumnjičenog
     */
    public void azurirajFotografiju(Long fotografijaId, MultipartFile file,
                                    Long userId, String opis) throws IOException {
        // Validacija veličine fajla (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Fotografija ne smije biti veća od 5MB");
        }

        // Validacija tipa fajla
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/gif") &&
                        !contentType.equals("image/webp"))) {
            throw new RuntimeException("Dozvoljeni su samo slikovni fajlovi (JPEG, PNG, GIF, WebP)");
        }

        osumnjiceniFotografijaRepository.update(fotografijaId, file, userId, opis);
    }

    /**
     * Brisanje fotografije osumnjičenog
     */
    public void obrisiFotografiju(Long fotografijaId) {
        osumnjiceniFotografijaRepository.delete(fotografijaId);
    }

    /**
     * Provjera da li osumnjičeni ima maksimalan broj fotografija
     */
    public boolean isMaxFotografijaDostignut(Long osumnjiceniId) {
        return osumnjiceniFotografijaRepository.countByOsumnjiceniId(osumnjiceniId) >= 3;
    }

    /**
     * Broj preostalih mjesta za fotografije
     */
    public int getPreostaloMjesta(Long osumnjiceniId) {
        return 3 - osumnjiceniFotografijaRepository.countByOsumnjiceniId(osumnjiceniId);
    }
}