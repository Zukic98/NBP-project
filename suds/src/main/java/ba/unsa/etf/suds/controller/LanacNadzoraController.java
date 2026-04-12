package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.dto.PotvrdaRequest;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.service.LanacNadzoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lanac-nadzora")
@Tag(name = "Lanac nadzora", description = "Lanac nadzora dokaza")
public class LanacNadzoraController {
    private final LanacNadzoraService service;

    public LanacNadzoraController(LanacNadzoraService service) {
        this.service = service;
    }

    @PostMapping("/posalji")
    @Operation(summary = "Pošalji dokaz na primopredaju")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zahtjev za slanje kreiran"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<LanacNadzora> posaljiDokaz(
            @RequestBody PosaljiDokazRequest request) {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        LanacNadzora lanac = service.posaljiDokaz(request, userId);
        return ResponseEntity.ok(lanac);
    }

    @GetMapping("/moji-zahtjevi")
    @Operation(summary = "Dohvati moje zahtjeve u lancu nadzora")
    @ApiResponse(responseCode = "200", description = "Lista zahtjeva vraćena")
    public ResponseEntity<List<LanacNadzora>> getMojiZahtjevi() {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        List<LanacNadzora> zahtjevi = service.getMojiZahtjevi(userId);
        return ResponseEntity.ok(zahtjevi);
    }

    @PutMapping("/prihvati/{id}")
    @Operation(summary = "Prihvati zaprimljeni dokaz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dokaz prihvaćen"),
            @ApiResponse(responseCode = "404", description = "Unos nije pronađen")
    })
    public ResponseEntity<Void> prihvatiDokaz(@PathVariable("id") Long unosId) {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        service.prihvatiDokaz(unosId, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{unosId}/potvrda")
    @Operation(summary = "Potvrdi ili odbij primopredaju")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status potvrde ažuriran"),
            @ApiResponse(responseCode = "404", description = "Unos nije pronađen")
    })
    public ResponseEntity<Void> potvrdiIliOdbij(@PathVariable Long unosId,
                                                @RequestBody PotvrdaRequest request) {
        String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        service.potvrdiIliOdbij(unosId, request.getStatus(), request.getNapomena(), userId);
        return ResponseEntity.ok().build();
    }
}
