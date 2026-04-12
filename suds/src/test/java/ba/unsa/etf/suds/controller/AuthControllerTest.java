package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.dto.LoginRequest;
import ba.unsa.etf.suds.dto.LoginResponse;
import ba.unsa.etf.suds.service.AuthService;
import ba.unsa.etf.suds.service.UposlenikService;
import ba.unsa.etf.suds.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UposlenikService uposlenikService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_Uspjesan_Vraca200OK() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        
        LoginResponse mockResponse = new LoginResponse("token123", "Bearer");
        when(authService.login(any())).thenReturn(mockResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void login_Penzionisan_Vraca403Forbidden() {
        when(authService.login(any())).thenThrow(new RuntimeException("Pristup odbijen: Penzionisan"));

        LoginRequest request = new LoginRequest();
        
        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Pristup odbijen: Penzionisan", response.getBody());
    }
}