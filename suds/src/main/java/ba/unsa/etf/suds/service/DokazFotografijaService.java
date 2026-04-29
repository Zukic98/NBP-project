package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DokazFotografijaDTO;
import ba.unsa.etf.suds.repository.DokazFotografijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DokazFotografijaService {

    private final DokazFotografijaRepository dokazFotografijaRepository;

    public DokazFotografijaService(DokazFotografijaRepository dokazFotografijaRepository) {
        this.dokazFotografijaRepository = dokazFotografijaRepository;
    }

    /**
     * Dobavljanje svih fotografija za dokaz
     */
    public List<DokazFotografijaDTO> getFotografijeByDokazId(Long dokazId) {
        return dokazFotografijaRepository.findByDokazId(dokazId);
    }

    /**
     * Upload fotografije za dokaz
     * @throws RuntimeException ako je dostignut maksimum od 10 fotografija
     */
    public void uploadFotografiju(Long dokazId, MultipartFile file, Integer redniBroj,
                                  Long userId, String opis) throws IOException {
        // Provjera broja postojećih fotografija
        int trenutniBroj = dokazFotografijaRepository.countByDokazId(dokazId);
        if (trenutniBroj >= 10) {
            throw new RuntimeException("Dokaz već ima maksimalnih 10 fotografija. Nije moguće dodati novu.");
        }

        // Validacija veličine fajla (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("Fotografija ne smije biti veća od 10MB");
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
        if (redniBroj < 1 || redniBroj > 10) {
            throw new RuntimeException("Redni broj mora biti između 1 i 10");
        }

        dokazFotografijaRepository.save(dokazId, file, redniBroj, userId, opis);
    }

    /**
     * Provjera da li dokaz ima maksimalan broj fotografija
     */
    public boolean isMaxFotografijaDostignut(Long dokazId) {
        return dokazFotografijaRepository.countByDokazId(dokazId) >= 10;
    }

    /**
     * Broj preostalih mjesta za fotografije
     */
    public int getPreostaloMjesta(Long dokazId) {
        return 10 - dokazFotografijaRepository.countByDokazId(dokazId);
    }
}