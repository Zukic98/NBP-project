package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.*;
import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.model.Svjedok;
import ba.unsa.etf.suds.security.JwtUtil;
import ba.unsa.etf.suds.service.OsumnjiceniService;
import ba.unsa.etf.suds.service.SlucajService;
import ba.unsa.etf.suds.service.SvjedokService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST kontroler za upravljanje krivičnim slučajevima.
 *
 * <p>Bazna putanja: {@code /api/slucajevi}. Pruža operacije za dohvat, kreiranje,
 * ažuriranje statusa slučajeva, upravljanje svjedocima i generisanje PDF izvještaja.
 * Kreiranje slučaja zahtijeva ulogu {@code SEF_STANICE} ili {@code INSPEKTOR}.
 * Identitet korisnika se čita iz {@code SecurityContextHolder}; stanicaId se
 * izvlači iz JWT tokena putem {@code JwtUtil.extractStanicaId} kada nije proslijeđen
 * u tijelu zahtjeva. Delegira operacije servisima {@code SlucajService},
 * {@code SvjedokService} i {@code OsumnjiceniService}.
 */
@RestController
@RequestMapping("/api/slucajevi")
@Tag(name = "Slučajevi", description = "Upravljanje slučajevima")
public class SlucajController {

    private final SlucajService slucajService;
    private final SvjedokService svjedokService;
    private final OsumnjiceniService osumnjiceniService;
    private final JwtUtil jwtUtil;

    /** Konstruktorska injekcija servisa za slučajeve, svjedoke, osumnjičene i JWT pomoćnih metoda. */
    public SlucajController(SlucajService slucajService,
                            SvjedokService svjedokService,
                            OsumnjiceniService osumnjiceniService,
                            JwtUtil jwtUtil) {
        this.slucajService = slucajService;
        this.svjedokService = svjedokService;
        this.osumnjiceniService = osumnjiceniService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * GET /api/slucajevi/broj/{brojSlucaja} - dohvata detalje slučaja po broju slučaja.
     *
     * @param brojSlucaja broj slučaja (alfanumerički identifikator)
     * @return 200 + {@link SlucajDetaljiDTO} sa svim detaljima slučaja,
     *         404 ako slučaj nije pronađen
     */
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

    /**
     * GET /api/slucajevi - dohvata sve slučajeve filtrirane prema ulozi prijavljenog korisnika.
     *
     * <p>Identitet i uloga korisnika se čitaju iz {@code SecurityContextHolder}.
     * Filtriranje po ulozi vrši {@code SlucajService.getSlucajeviFiltered}.
     *
     * @return 200 + lista {@link SlucajListDTO} filtrirana prema ulozi korisnika
     */
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

    /**
     * GET /api/slucajevi/{id} - dohvata slučaj po internom ID-u.
     *
     * @param id interni identifikator slučaja
     * @return 200 + {@link SlucajListDTO},
     *         404 ako slučaj nije pronađen
     */
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

    /**
     * PATCH /api/slucajevi/{id}/status - ažurira status slučaja.
     *
     * @param id      interni identifikator slučaja
     * @param request tijelo zahtjeva sa novim statusom
     * @return 200 ako je status uspješno ažuriran,
     *         400 ako je status nevalidan,
     *         404 ako slučaj nije pronađen
     */
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

    /**
     * GET /api/slucajevi/{id}/izvjestaj - dohvata izvještaj za slučaj.
     *
     * @param id interni identifikator slučaja
     * @return 200 + {@link IzvjestajDTO},
     *         404 ako izvještaj nije pronađen
     */
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

    /**
     * POST /api/slucajevi - kreira novi krivični slučaj.
     *
     * <p>Samo {@code SEF_STANICE} i {@code INSPEKTOR} mogu kreirati slučajeve
     * ({@code @PreAuthorize("hasAnyRole('SEF_STANICE', 'INSPEKTOR')")}).
     * Identitet korisnika se čita iz {@code SecurityContextHolder}. Ako {@code stanicaId}
     * nije proslijeđen u tijelu zahtjeva, izvlači se iz JWT tokena putem
     * {@code JwtUtil.extractStanicaId}.
     *
     * @param request     tijelo zahtjeva sa podacima novog slučaja
     * @param httpRequest HTTP zahtjev iz kojeg se izvlači JWT token za stanicaId
     * @return 201 + kreirani {@link SlucajListDTO},
     *         403 ako korisnik nema odgovarajuću ulogu,
     *         500 pri grešci na serveru
     */
    @PreAuthorize("hasAnyRole('SEF_STANICE', 'INSPEKTOR')")
    @PostMapping
    @Operation(summary = "Kreiraj novi slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Slučaj kreiran"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "500", description = "Greška na serveru")
    })
    public ResponseEntity<?> kreirajSlucaj(@RequestBody KreirajSlucajRequest request,
                                              HttpServletRequest httpRequest) {
        try {
            String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = Long.parseLong(userIdStr);

            if (request.getStanicaId() == null) {
                String token = httpRequest.getHeader("Authorization").substring(7);
                Long stanicaId = jwtUtil.extractStanicaId(token);
                request.setStanicaId(stanicaId);
            }

            SlucajListDTO slucaj = slucajService.kreirajSlucaj(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(slucaj);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Greška pri kreiranju slučaja: " + e.getMessage());
        }
    }

    /**
     * GET /api/slucajevi/moji - dohvata slučajeve prijavljenog korisnika.
     *
     * <p>Identitet i uloga korisnika se čitaju iz {@code SecurityContextHolder}.
     * Filtriranje po ulozi vrši {@code SlucajService.getMojiSlucajevi}.
     *
     * @return 200 + lista {@link MojSlucajDTO} slučajeva vezanih za prijavljenog korisnika
     */
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

    /**
     * POST /api/slucajevi/{slucajId}/svjedoci - dodaje svjedoka na slučaj.
     *
     * @param slucajId identifikator slučaja
     * @param request  tijelo zahtjeva sa podacima svjedoka
     * @return 201 + {@link SvjedokDTO} kreiranog svjedoka,
     *         500 pri grešci na serveru
     */
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

    /*@PostMapping("/{slucajId}/osumnjiceni")
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
    }*/

    /**
     * GET /api/slucajevi/{id}/generate-report - generiše i preuzima PDF izvještaj za slučaj.
     *
     * <p>Vraća binarni PDF fajl kao {@code application/pdf} sa {@code Content-Disposition: attachment}.
     * Identitet korisnika se izvlači iz JWT tokena putem {@code JwtUtil.extractUserId}.
     *
     * @param id      interni identifikator slučaja
     * @param request HTTP zahtjev iz kojeg se izvlači JWT token
     * @return 200 + PDF bajt-niz sa odgovarajućim headerima za preuzimanje,
     *         404 ako slučaj nije pronađen,
     *         500 pri grešci pri generisanju PDF-a
     */
    @GetMapping("/{id}/generate-report")
    @Operation(summary = "Generiši i preuzmi PDF izvještaj za slučaj")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF izvještaj generisan"),
            @ApiResponse(responseCode = "404", description = "Slučaj nije pronađen"),
            @ApiResponse(responseCode = "500", description = "Greška pri generisanju PDF-a")
    })
    public ResponseEntity<byte[]> generatePdfReport(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String userIdStr = jwtUtil.extractUserId(token);
            Long userId = Long.parseLong(userIdStr);

            byte[] pdfBytes = slucajService.generatePdfReport(id, userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("Izvjestaj_Slucaj_" + id + ".pdf")
                    .build());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
