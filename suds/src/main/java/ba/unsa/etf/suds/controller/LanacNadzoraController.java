package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.security.CustomUserDetails;
import ba.unsa.etf.suds.service.LanacNadzoraService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lanac-nadzora")
public class LanacNadzoraController {
    private final LanacNadzoraService service;

    public LanacNadzoraController(LanacNadzoraService service) {
        this.service = service;
    }

    @PostMapping("/posalji")
    public ResponseEntity<LanacNadzora> posaljiDokaz(
            @RequestBody PosaljiDokazRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        LanacNadzora lanac = service.posaljiDokaz(request, user.getUserId());
        return ResponseEntity.ok(lanac);
    }

    @GetMapping("/moji-zahtjevi")
    public ResponseEntity<List<LanacNadzora>> getMojiZahtjevi(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<LanacNadzora> zahtjevi = service.getMojiZahtjevi(user.getUserId());
        return ResponseEntity.ok(zahtjevi);
    }

    @PutMapping("/prihvati/{id}")
    public ResponseEntity<Void> prihvatiDokaz(
            @PathVariable("id") Long unosId,
            @AuthenticationPrincipal CustomUserDetails user) {
        service.prihvatiDokaz(unosId, user.getUserId());
        return ResponseEntity.ok().build();
    }
}