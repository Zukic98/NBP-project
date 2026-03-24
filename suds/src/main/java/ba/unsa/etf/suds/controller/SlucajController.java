package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.SlucajDetaljiDTO;
import ba.unsa.etf.suds.service.SlucajService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/slucajevi")
public class SlucajController {

    private final SlucajService slucajService;

    public SlucajController(SlucajService slucajService) {
        this.slucajService = slucajService;
    }

        /**
         * Endpoint: GET /api/slucajevi/{brojSlucaja}
         * Vraća kompletnu sliku slučaja: opis, inspektora, osumnjičene i djela.
         */
    @GetMapping("/{brojSlucaja}")
    public ResponseEntity<SlucajDetaljiDTO> getSlucajDetalji(@PathVariable String brojSlucaja) {
        SlucajDetaljiDTO detalji = slucajService.getSlucajDetalji(brojSlucaja);
        
        if (detalji.getBrojSlucaja() == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(detalji);
    }
}