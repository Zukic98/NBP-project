package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.MojaPrimopredajaDTO;
import ba.unsa.etf.suds.dto.PonistiRequest;
import ba.unsa.etf.suds.dto.PrimopredajaRequest;
import ba.unsa.etf.suds.dto.PrimopredajaZaPotvrduDTO;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.service.LanacNadzoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Primopredaje", description = "Primopredaja dokaza")
public class PrimopredajaController {

    private final LanacNadzoraService lanacNadzoraService;

    public PrimopredajaController(LanacNadzoraService lanacNadzoraService) {
        this.lanacNadzoraService = lanacNadzoraService;
    }

    @PostMapping("/api/dokazi/{dokazId}/primopredaja")
    @Operation(summary = "Kreiraj primopredaju dokaza")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Primopredaja kreirana"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<LanacNadzora> kreirajPrimopredaju(@PathVariable Long dokazId,
                                                            @RequestBody PrimopredajaRequest request) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        LanacNadzora lanac = lanacNadzoraService.kreirajPrimopredaju(dokazId, request, userId);
        return ResponseEntity.ok(lanac);
    }

    @GetMapping("/api/primopredaje/ceka-potvrdu")
    @Operation(summary = "Dohvati primopredaje koje čekaju potvrdu")
    @ApiResponse(responseCode = "200", description = "Lista primopredaja vraćena")
    public ResponseEntity<List<PrimopredajaZaPotvrduDTO>> getCekaPotvrdu() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(lanacNadzoraService.getCekaPotvrduZaMene(userId));
    }

    @GetMapping("/api/primopredaje/moja-slanja")
    @Operation(summary = "Dohvati moja slanja na potvrdi")
    @ApiResponse(responseCode = "200", description = "Lista mojih slanja vraćena")
    public ResponseEntity<List<MojaPrimopredajaDTO>> getMojaSlanja() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        return ResponseEntity.ok(lanacNadzoraService.getMojaSlanjaNaPotvrdi(userId));
    }

    @DeleteMapping("/api/primopredaje/{unosId}/ponisti")
    @Operation(summary = "Poništi primopredaju")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Primopredaja poništena"),
            @ApiResponse(responseCode = "404", description = "Unos nije pronađen")
    })
    public ResponseEntity<Void> ponisti(@PathVariable Long unosId,
                                        @RequestBody PonistiRequest request) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        lanacNadzoraService.ponistiPrimopredaju(unosId, request, userId);
        return ResponseEntity.ok().build();
    }
}
