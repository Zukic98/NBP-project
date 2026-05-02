package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.DokazFotografijaDTO;
import ba.unsa.etf.suds.service.DokazFotografijaService;
import ba.unsa.etf.suds.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST kontroler za upravljanje fotografijama dokaza.
 *
 * <p>Bazna putanja: {@code /api/dokazi}. Podržava dohvat i upload fotografija
 * vezanih za dokaze. Upload je multipart/form-data zahtjev; jednom dodana fotografija
 * ostaje trajno (nema brisanja). Identitet korisnika se izvlači iz JWT tokena
 * putem {@code JwtUtil.extractUserId}.
 */
@RestController
@RequestMapping("/api/dokazi")
public class DokazFotografijaController {

    private final DokazFotografijaService dokazFotografijaService;
    private final JwtUtil jwtUtil;

    /** Konstruktorska injekcija servisa za fotografije dokaza i JWT pomoćnih metoda. */
    public DokazFotografijaController(DokazFotografijaService dokazFotografijaService,
                                      JwtUtil jwtUtil) {
        this.dokazFotografijaService = dokazFotografijaService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * GET /api/dokazi/{dokazId}/fotografije - dohvata sve fotografije za određeni dokaz.
     *
     * <p>Fotografije se vraćaju kao Base64-kodirani stringovi unutar {@link DokazFotografijaDTO}.
     *
     * @param dokazId identifikator dokaza
     * @return 200 + lista {@link DokazFotografijaDTO},
     *         500 pri grešci na serveru
     */
    // U DokazFotografijaController.java
    @GetMapping("/{dokazId}/fotografije")
    public ResponseEntity<List<DokazFotografijaDTO>> getFotografije(@PathVariable Long dokazId) {
        try {
            List<DokazFotografijaDTO> fotografije = dokazFotografijaService.getFotografijeByDokazId(dokazId);
            // Debug ispis
            System.out.println("Broj fotografija za dokaz " + dokazId + ": " + fotografije.size());
            for (DokazFotografijaDTO foto : fotografije) {
                System.out.println("Foto ID: " + foto.getFotografijaId() +
                        ", Base64 length: " + (foto.getFotografijaBase64() != null ? foto.getFotografijaBase64().length() : "null"));
            }
            return ResponseEntity.ok(fotografije);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/dokazi/{dokazId}/fotografije - upload fotografije za određeni dokaz.
     *
     * <p>Multipart/form-data zahtjev. Dozvoljeni formati: JPEG, PNG, GIF, WebP.
     * Maksimalna veličina fajla: 10 MB. Identitet korisnika se izvlači iz JWT tokena
     * putem {@code JwtUtil.extractUserId}. Jednom dodana fotografija ostaje trajno.
     *
     * @param dokazId   identifikator dokaza
     * @param file      fotografija kao multipart fajl
     * @param redniBroj opcionalni redni broj fotografije
     * @param opis      opcionalni opis fotografije
     * @param token     Authorization header u obliku {@code "Bearer <jwt>"}
     * @return 201 + poruka o uspješnom uploadu,
     *         400 ako je format ili veličina fajla neispravna,
     *         500 pri grešci na serveru
     */
    @PostMapping("/{dokazId}/fotografije")
    public ResponseEntity<?> uploadFotografiju(@PathVariable Long dokazId,
                                               @RequestParam("fotografija") MultipartFile file,
                                               @RequestParam(value = "redniBroj", required = false) Integer redniBroj,
                                               @RequestParam(value = "opis", required = false) String opis,
                                               @RequestHeader("Authorization") String token) {
        try {
            // Validacija tipa fajla
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.equals("image/jpeg") &&
                            !contentType.equals("image/png") &&
                            !contentType.equals("image/gif") &&
                            !contentType.equals("image/webp"))) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Dozvoljeni su samo slikovni fajlovi (JPEG, PNG, GIF, WebP)");
                return ResponseEntity.badRequest().body(error);
            }

            // Validacija veličine (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Fotografija ne smije biti veća od 10MB");
                return ResponseEntity.badRequest().body(error);
            }

            // Ekstrakcija user ID iz tokena
            String userIdStr = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            Long userId = Long.parseLong(userIdStr);

            // Delegiranje servisu
            dokazFotografijaService.uploadFotografiju(dokazId, file, redniBroj, userId, opis);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fotografija uspješno dodana");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Greška pri uploadu fotografije: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}