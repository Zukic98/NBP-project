package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.OsumnjiceniFotografijaDTO;
import ba.unsa.etf.suds.service.OsumnjiceniFotografijaService;
import ba.unsa.etf.suds.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/osumnjiceni")
public class OsumnjiceniFotografijaController {

    private final OsumnjiceniFotografijaService osumnjiceniFotografijaService;
    private final JwtUtil jwtUtil;

    public OsumnjiceniFotografijaController(OsumnjiceniFotografijaService osumnjiceniFotografijaService,
                                            JwtUtil jwtUtil) {
        this.osumnjiceniFotografijaService = osumnjiceniFotografijaService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * GET /api/osumnjiceni/{osumnjiceniId}/fotografije
     * Dobavljanje svih fotografija za osumnjičenog
     */
    @GetMapping("/{osumnjiceniId}/fotografije")
    public ResponseEntity<List<OsumnjiceniFotografijaDTO>> getFotografije(@PathVariable Long osumnjiceniId) {
        try {
            List<OsumnjiceniFotografijaDTO> fotografije = osumnjiceniFotografijaService.getFotografijeByOsumnjiceniId(osumnjiceniId);
            return ResponseEntity.ok(fotografije);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/osumnjiceni/{osumnjiceniId}/fotografije
     * Upload fotografije za osumnjičenog
     * Max 3 fotografije
     */
    @PostMapping("/{osumnjiceniId}/fotografije")
    public ResponseEntity<?> uploadFotografiju(@PathVariable Long osumnjiceniId,
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

            // Validacija veličine (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Fotografija ne smije biti veća od 5MB");
                return ResponseEntity.badRequest().body(error);
            }

            // Ekstrakcija user ID iz tokena
            String userIdStr = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            Long userId = Long.parseLong(userIdStr);

            // Delegiranje servisu
            osumnjiceniFotografijaService.uploadFotografiju(osumnjiceniId, file, redniBroj, userId, opis);

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

    /**
     * PUT /api/osumnjiceni/{osumnjiceniId}/fotografije/{fotografijaId}
     * Ažuriranje fotografije osumnjičenog
     */
    @PutMapping("/{osumnjiceniId}/fotografije/{fotografijaId}")
    public ResponseEntity<?> azurirajFotografiju(@PathVariable Long osumnjiceniId,
                                                 @PathVariable Long fotografijaId,
                                                 @RequestParam("fotografija") MultipartFile file,
                                                 @RequestParam(value = "opis", required = false) String opis,
                                                 @RequestHeader("Authorization") String token) {
        try {
            String userIdStr = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            Long userId = Long.parseLong(userIdStr);

            osumnjiceniFotografijaService.azurirajFotografiju(fotografijaId, file, userId, opis);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Fotografija uspješno ažurirana");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Greška pri ažuriranju fotografije: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * DELETE /api/osumnjiceni/{osumnjiceniId}/fotografije/{fotografijaId}
     * Brisanje fotografije osumnjičenog
     */
    @DeleteMapping("/{osumnjiceniId}/fotografije/{fotografijaId}")
    public ResponseEntity<?> obrisiFotografiju(@PathVariable Long osumnjiceniId,
                                               @PathVariable Long fotografijaId,
                                               @RequestHeader("Authorization") String token) {
        try {
            osumnjiceniFotografijaService.obrisiFotografiju(fotografijaId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Fotografija uspješno obrisana");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Greška pri brisanju fotografije: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}