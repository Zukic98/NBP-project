package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.*;
import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.model.Slucaj;
import ba.unsa.etf.suds.model.Svjedok;
import ba.unsa.etf.suds.service.OsumnjiceniService;
import ba.unsa.etf.suds.service.SlucajService;
import ba.unsa.etf.suds.service.SvjedokService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slucajevi")
@Tag(name = "Slučajevi", description = "Upravljanje slučajevima")
public class SlucajController {

    private final SlucajService slucajService;
    private final SvjedokService svjedokService;
    private final OsumnjiceniService osumnjiceniService;

    public SlucajController(SlucajService slucajService,
                            SvjedokService svjedokService,
                            OsumnjiceniService osumnjiceniService) {
        this.slucajService = slucajService;
        this.svjedokService = svjedokService;
        this.osumnjiceniService = osumnjiceniService;
    }

    @GetMapping("/broj/{brojSlucaja}")
    @Operation(summary = "Dohvati detalje slučaja po broju")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slučaj pronađen"),
            @ApiResponse(responseCode = "404", description = "Slučaj nije pronađen")
    })
    public ResponseEntity<SlucajDetaljiDTO> getSlucajDetalji(@PathVariable String brojSlucaja) {
        SlucajDetaljiDTO detalji = slucajService.getSlucajDetalji(brojSlucaja);

        if (detalji.getBrojSlucaja() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(detalji);
    }

    @GetMapping
    @Operation(summary = "Dohvati sve slučajeve (filtriran po roli)")
    @ApiResponse(responseCode = "200", description = "Lista slučajeva vraćena")
    public ResponseEntity<List<SlucajListDTO>> getSlucajevi() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = (String) authentication.getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        String roleName = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .orElse("");

        List<SlucajListDTO> slucajevi = slucajService.getSlucajeviFiltered(userId, roleName);
        return ResponseEntity.ok(slucajevi);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dohvati slučaj po ID-u")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slučaj pronađen"),
            @ApiResponse(responseCode = "404", description = "Slučaj nije pronađen")
    })
    public ResponseEntity<SlucajListDTO> getSlucajById(@PathVariable Long id) {
        return slucajService.getSlucajById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ažuriraj status slučaja")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status uspješno ažuriran"),
            @ApiResponse(responseCode = "400", description = "Neispravan status"),
            @ApiResponse(responseCode = "404", description = "Slučaj nije pronađen")
    })
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            boolean updated = slucajService.updateSlucajStatus(id, request.getStatus());
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/izvjestaj")
    @Operation(summary = "Dohvati izvještaj za slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Izvještaj pronađen"),
            @ApiResponse(responseCode = "404", description = "Izvještaj nije pronađen")
    })
    public ResponseEntity<IzvjestajDTO> getIzvjestaj(@PathVariable Long id) {
        return slucajService.getIzvjestaj(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ŠEF', 'INSPEKTOR')")
    @PostMapping
    @Operation(summary = "Kreiraj novi slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slučaj kreiran"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<Slucaj> kreirajSlucaj(@RequestBody KreirajSlucajRequest request) {
        try {
            String userIdStr = (String) org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userIdStr);
            Slucaj slucaj = slucajService.kreirajSlucaj(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(slucaj);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/moji")
    @Operation(summary = "Dohvati moje slučajeve")
    @ApiResponse(responseCode = "200", description = "Lista mojih slučajeva vraćena")
    public ResponseEntity<List<MojSlucajDTO>> getMojiSlucajevi() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = (String) authentication.getPrincipal();
        Long userId = Long.parseLong(userIdStr);
        String roleName = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .orElse("");
        List<MojSlucajDTO> slucajevi = slucajService.getMojiSlucajevi(
                userId, roleName);
        return ResponseEntity.ok(slucajevi);
    }

    @PostMapping("/{slucajId}/svjedoci")
    @Operation(summary = "Dodaj svjedoka na slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Svjedok dodan"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<SvjedokDTO> dodajSvjedoka(@PathVariable Long slucajId,
                                                     @RequestBody DodajSvjedokaRequest request) {
        try {
            Svjedok svjedok = svjedokService.dodajSvjedoka(slucajId, request);
            SvjedokDTO dto = new SvjedokDTO(
                    svjedok.getSvjedokId(),
                    svjedok.getImePrezime(),
                    svjedok.getKontaktTelefon(),
                    request.getAdresa() != null ? request.getAdresa() : request.getUlicaIBroj(),
                    svjedok.getJmbg(),
                    svjedok.getBiljeska()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{slucajId}/osumnjiceni")
    @Operation(summary = "Dodaj osumnjičenog na slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Osumnjičeni dodan"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<Osumnjiceni> dodajOsumnjicenog(@PathVariable Long slucajId,
                                                          @RequestBody DodajOsumnjicenogRequest request) {
        try {
            Osumnjiceni osumnjiceni = osumnjiceniService.dodajOsumnjicenog(slucajId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(osumnjiceni);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
