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

@RestController
@RequestMapping("/api/dokazi")
public class DokazFotografijaController {

    private final DokazFotografijaService dokazFotografijaService;
    private final JwtUtil jwtUtil;

    public DokazFotografijaController(DokazFotografijaService dokazFotografijaService,
                                      JwtUtil jwtUtil) {
        this.dokazFotografijaService = dokazFotografijaService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * GET /api/dokazi/{dokazId}/fotografije
     * Dobavljanje svih fotografija za dokaz
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
     * POST /api/dokazi/{dokazId}/fotografije
     * Upload fotografije za dokaz
     * Samo INSERT dozvoljen - jednom dodana fotografija ostaje trajno
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