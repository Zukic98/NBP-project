package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.OsumnjiceniFotografijaDTO;
import ba.unsa.etf.suds.repository.OsumnjiceniFotografijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Servis za upravljanje fotografijama osumnjičenih.
 *
 * <p>Za razliku od fotografija dokaza, fotografije osumnjičenih podržavaju
 * izmjenu i brisanje. Orkestrira {@link OsumnjiceniFotografijaRepository}.
 *
 * <p>Ograničenja:
 * <ul>
 *   <li>Maksimalno <b>3 fotografije</b> po osumnjičenom.</li>
 *   <li>Maksimalna veličina fajla: <b>5 MB</b>.</li>
 *   <li>Dozvoljeni tipovi: JPEG, PNG, GIF, WebP.</li>
 * </ul>
 */
@Service
public class OsumnjiceniFotografijaService {

    private final OsumnjiceniFotografijaRepository osumnjiceniFotografijaRepository;

    /** Konstruktorska injekcija repozitorija fotografija osumnjičenih. */
    public OsumnjiceniFotografijaService(OsumnjiceniFotografijaRepository osumnjiceniFotografijaRepository) {
        this.osumnjiceniFotografijaRepository = osumnjiceniFotografijaRepository;
    }

    /**
     * Dobavlja sve fotografije za zadanog osumnjičenog.
     *
     * @param osumnjiceniId identifikator osumnjičenog
     * @return lista {@link OsumnjiceniFotografijaDTO} objekata za datog osumnjičenog
     */
    public List<OsumnjiceniFotografijaDTO> getFotografijeByOsumnjiceniId(Long osumnjiceniId) {
        return osumnjiceniFotografijaRepository.findByOsumnjiceniId(osumnjiceniId);
    }

    /**
     * Uploaduje novu fotografiju za zadanog osumnjičenog.
     *
     * <p>Primjenjuje sljedeća poslovna pravila:
     * <ul>
     *   <li>Maksimalno 3 fotografije po osumnjičenom — baca {@link RuntimeException} ako je limit dostignut.</li>
     *   <li>Maksimalna veličina fajla je 5 MB.</li>
     *   <li>Dozvoljeni MIME tipovi: {@code image/jpeg}, {@code image/png}, {@code image/gif}, {@code image/webp}.</li>
     *   <li>Ako {@code redniBroj} nije proslijeđen, automatski se dodjeljuje sljedeći slobodni redni broj.</li>
     * </ul>
     *
     * @param osumnjiceniId identifikator osumnjičenog
     * @param file          multipart fajl koji se uploaduje
     * @param redniBroj     željeni redni broj fotografije (1–3); ako je {@code null}, automatski se određuje
     * @param userId        identifikator uposlenika koji uploaduje fotografiju
     * @param opis          tekstualni opis fotografije (može biti {@code null})
     * @throws RuntimeException ako je dostignut maksimum od 3 fotografije, fajl je prevelik
     *                          ili je tip fajla nedozvoljen
     * @throws IOException      ako dođe do greške pri čitanju sadržaja fajla
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
     * Ažurira postojeću fotografiju osumnjičenog.
     *
     * <p>Validira veličinu (max 5 MB) i tip fajla (JPEG, PNG, GIF, WebP).
     *
     * @param fotografijaId identifikator fotografije koja se ažurira
     * @param file          novi multipart fajl
     * @param userId        identifikator uposlenika koji vrši izmjenu
     * @param opis          novi tekstualni opis fotografije (može biti {@code null})
     * @throws RuntimeException ako je fajl prevelik ili je tip fajla nedozvoljen
     * @throws IOException      ako dođe do greške pri čitanju sadržaja fajla
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
     * Briše fotografiju osumnjičenog.
     *
     * @param fotografijaId identifikator fotografije koja se briše
     */
    public void obrisiFotografiju(Long fotografijaId) {
        osumnjiceniFotografijaRepository.delete(fotografijaId);
    }

    /**
     * Provjerava da li je dostignut maksimalni broj fotografija (3) za zadanog osumnjičenog.
     *
     * @param osumnjiceniId identifikator osumnjičenog
     * @return {@code true} ako osumnjičeni već ima 3 fotografije, inače {@code false}
     */
    public boolean isMaxFotografijaDostignut(Long osumnjiceniId) {
        return osumnjiceniFotografijaRepository.countByOsumnjiceniId(osumnjiceniId) >= 3;
    }

    /**
     * Vraća broj preostalih slobodnih mjesta za fotografije osumnjičenog.
     *
     * @param osumnjiceniId identifikator osumnjičenog
     * @return broj preostalih mjesta (0–3)
     */
    public int getPreostaloMjesta(Long osumnjiceniId) {
        return 3 - osumnjiceniFotografijaRepository.countByOsumnjiceniId(osumnjiceniId);
    }
}