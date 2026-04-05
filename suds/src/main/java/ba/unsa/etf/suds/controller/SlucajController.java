package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.*;
import ba.unsa.etf.suds.model.Osumnjiceni;
import ba.unsa.etf.suds.model.Slucaj;
import ba.unsa.etf.suds.model.Svjedok;
import ba.unsa.etf.suds.security.CustomUserDetails;
import ba.unsa.etf.suds.service.OsumnjiceniService;
import ba.unsa.etf.suds.service.SlucajService;
import ba.unsa.etf.suds.service.SvjedokService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slucajevi")
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

    @GetMapping("/{brojSlucaja}")
    public ResponseEntity<SlucajDetaljiDTO> getSlucajDetalji(@PathVariable String brojSlucaja) {
        SlucajDetaljiDTO detalji = slucajService.getSlucajDetalji(brojSlucaja);

        if (detalji.getBrojSlucaja() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(detalji);
    }

    @PreAuthorize("hasAnyRole('ŠEF', 'INSPEKTOR')")
    @PostMapping
    public ResponseEntity<Slucaj> kreirajSlucaj(@RequestBody KreirajSlucajRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails user) {
        try {
            Slucaj slucaj = slucajService.kreirajSlucaj(request, user.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(slucaj);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/moji")
    public ResponseEntity<List<MojSlucajDTO>> getMojiSlucajevi(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<MojSlucajDTO> slucajevi = slucajService.getMojiSlucajevi(
                user.getUserId(), user.getRoleName());
        return ResponseEntity.ok(slucajevi);
    }

    @PostMapping("/{slucajId}/svjedoci")
    public ResponseEntity<Svjedok> dodajSvjedoka(@PathVariable Long slucajId,
                                                  @RequestBody DodajSvjedokaRequest request) {
        try {
            Svjedok svjedok = svjedokService.dodajSvjedoka(slucajId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(svjedok);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{slucajId}/osumnjiceni")
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
