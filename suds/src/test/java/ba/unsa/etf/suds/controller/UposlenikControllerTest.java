package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.dto.PromijeniStatusRequest;
import ba.unsa.etf.suds.security.JwtUtil;
import ba.unsa.etf.suds.service.UposlenikService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UposlenikControllerTest {

    @Mock
    private UposlenikService uposlenikService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UposlenikController uposlenikController;

    @Test
    @DisplayName("Treba vratiti listu uposlenika za stanicu iz tokena")
    void getAll_Success() {
        String mockToken = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + mockToken);
        when(jwtUtil.extractStanicaId(mockToken)).thenReturn(1L);
        
        UposlenikDTO u = new UposlenikDTO();
        u.setIme("Test");
        when(uposlenikService.getUposleniciPoStanici(1L)).thenReturn(List.of(u));

        ResponseEntity<List<UposlenikDTO>> response = uposlenikController.getAll(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Treba vratiti 403 Forbidden ako šef pokušava mijenjati uposlenika iz druge stanice")
    void promijeniStatus_RazliciteStanice_Returns403() {
        String mockToken = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + mockToken);
        when(jwtUtil.extractStanicaId(mockToken)).thenReturn(1L);
        when(jwtUtil.extractUserId(mockToken)).thenReturn("10");

        PromijeniStatusRequest statusReq = new PromijeniStatusRequest();
        statusReq.setStatus("Otpušten");

        doThrow(new SecurityException("Nemate pravo!"))
            .when(uposlenikService).promijeniStatus(anyLong(), any(), anyLong(), anyLong());

        ResponseEntity<?> response = uposlenikController.promijeniStatus(99L, statusReq, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Nemate pravo!", response.getBody());
    }

    @Test
    @DisplayName("Treba vratiti 400 Bad Request ako email već postoji kod ažuriranja")
    void azurirajPodatke_EmailExists_Returns400() {
        String mockToken = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + mockToken);
        when(jwtUtil.extractStanicaId(mockToken)).thenReturn(1L);

        UposlenikDTO dto = new UposlenikDTO();
        dto.setEmail("vec@postoji.com");

        doThrow(new RuntimeException("Email je već zauzet!"))
            .when(uposlenikService).azurirajPodatke(anyLong(), any(), anyLong());

        ResponseEntity<?> response = uposlenikController.azurirajPodatke(10L, dto, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Email je već zauzet!"));
    }
}