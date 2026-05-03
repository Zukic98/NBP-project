package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DokazFotografijaDTO;
import ba.unsa.etf.suds.repository.DokazFotografijaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Servis za upravljanje fotografijama dokaza.
 *
 * <p>Fotografije dokaza su INSERT-only (bez izmjene i brisanja) kako bi se
 * očuvao integritet lanca nadzora — svaka fotografija je nepromjenjivi
 * digitalni trag vezan za dokaz. Servis orkestrira
 * {@link DokazFotografijaRepository}.
 *
 * <p>Ograničenja:
 * <ul>
 *   <li>Maksimalno <b>10 fotografija</b> po dokazu.</li>
 *   <li>Maksimalna veličina fajla: <b>10 MB</b>.</li>
 *   <li>Dozvoljeni tipovi: JPEG, PNG, GIF, WebP.</li>
 * </ul>
 */
@Service
public class DokazFotografijaService {

    private final DokazFotografijaRepository dokazFotografijaRepository;

    /** Konstruktorska injekcija repozitorija fotografija dokaza. */
    public DokazFotografijaService(DokazFotografijaRepository dokazFotografijaRepository) {
        this.dokazFotografijaRepository = dokazFotografijaRepository;
    }

    /**
     * Dobavlja sve fotografije za zadani dokaz.
     *
     * @param dokazId identifikator dokaza
     * @return lista {@link DokazFotografijaDTO} objekata za dati dokaz
     */
    public List<DokazFotografijaDTO> getFotografijeByDokazId(Long dokazId) {
        return dokazFotografijaRepository.findByDokazId(dokazId);
    }

    /**
     * Uploaduje novu fotografiju za zadani dokaz.
     *
     * <p>Primjenjuje sljedeća poslovna pravila:
     * <ul>
     *   <li>Maksimalno 10 fotografija po dokazu — baca {@link RuntimeException} ako je limit dostignut.</li>
     *   <li>Maksimalna veličina fajla je 10 MB.</li>
     *   <li>Dozvoljeni MIME tipovi: {@code image/jpeg}, {@code image/png}, {@code image/gif}, {@code image/webp}.</li>
     *   <li>Ako {@code redniBroj} nije proslijeđen, automatski se dodjeljuje sljedeći slobodni redni broj.</li>
     * </ul>
     *
     * @param dokazId   identifikator dokaza
     * @param file      multipart fajl koji se uploaduje
     * @param redniBroj željeni redni broj fotografije (1–10); ako je {@code null}, automatski se određuje
     * @param userId    identifikator uposlenika koji uploaduje fotografiju
     * @param opis      tekstualni opis fotografije (može biti {@code null})
     * @throws RuntimeException ako je dostignut maksimum od 10 fotografija, fajl je prevelik
     *                          ili je tip fajla nedozvoljen
     * @throws IOException      ako dođe do greške pri čitanju sadržaja fajla
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
     * Provjerava da li je dostignut maksimalni broj fotografija (10) za zadani dokaz.
     *
     * @param dokazId identifikator dokaza
     * @return {@code true} ako dokaz već ima 10 fotografija, inače {@code false}
     */
    public boolean isMaxFotografijaDostignut(Long dokazId) {
        return dokazFotografijaRepository.countByDokazId(dokazId) >= 10;
    }

    /**
     * Vraća broj preostalih slobodnih mjesta za fotografije dokaza.
     *
     * @param dokazId identifikator dokaza
     * @return broj preostalih mjesta (0–10)
     */
    public int getPreostaloMjesta(Long dokazId) {
        return 10 - dokazFotografijaRepository.countByDokazId(dokazId);
    }
}