package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.KreirajSlucajRequest;
import ba.unsa.etf.suds.model.Slucaj;
import ba.unsa.etf.suds.security.JwtUtil;
import ba.unsa.etf.suds.service.OsumnjiceniService;
import ba.unsa.etf.suds.service.SlucajService;
import ba.unsa.etf.suds.service.SvjedokService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SlucajControllerRbacTest {

    @Mock
    private SlucajService slucajService;

    @Mock
    private SvjedokService svjedokService;

    @Mock
    private OsumnjiceniService osumnjiceniService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest httpServletRequest;

    private SlucajController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new SlucajController(slucajService, svjedokService, osumnjiceniService, jwtUtil);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "1",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SEF_STANICE"))
                )
        );
    }

    @Test
    void kreirajSlucaj_SefRole_ReturnsCreated() {
        KreirajSlucajRequest request = new KreirajSlucajRequest();
        request.setStanicaId(1L);
        request.setBrojSlucaja("SLU-2026-001");
        request.setOpis("Test slučaj za šefa");
        request.setUlicaIBroj("Testna 1");
        request.setGrad("Sarajevo");
        request.setPostanskiBroj("71000");
        request.setDrzava("BiH");
        request.setTim(List.of());

        Slucaj expectedSlucaj = new Slucaj();
        expectedSlucaj.setSlucajId(1L);
        expectedSlucaj.setBrojSlucaja("SLU-2026-001");

        when(slucajService.kreirajSlucaj(any(KreirajSlucajRequest.class), eq(1L)))
                .thenReturn(expectedSlucaj);

        ResponseEntity<?> response = controller.kreirajSlucaj(request, httpServletRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(slucajService).kreirajSlucaj(any(), eq(1L));
    }

    @Test
    void kreirajSlucaj_ServiceSucceeds_Returns201() {
        KreirajSlucajRequest request = new KreirajSlucajRequest();
        request.setStanicaId(1L);
        request.setBrojSlucaja("SLU-2026-002");

        Slucaj slucaj = new Slucaj();
        slucaj.setSlucajId(2L);
        when(slucajService.kreirajSlucaj(any(), any())).thenReturn(slucaj);

        ResponseEntity<?> response = controller.kreirajSlucaj(request, httpServletRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void kreirajSlucaj_ServiceThrowsException_Returns500() {
        KreirajSlucajRequest request = new KreirajSlucajRequest();
        request.setStanicaId(1L);

        when(slucajService.kreirajSlucaj(any(), any())).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = controller.kreirajSlucaj(request, httpServletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
