package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.LoginRequest;
import ba.unsa.etf.suds.dto.LoginResponse;
import ba.unsa.etf.suds.dto.UposlenikLoginDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import ba.unsa.etf.suds.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UposlenikRepository uposlenikRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Uspješan login treba vratiti JWT token")
    void login_Uspjesan_VracaToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setBrojZnacke("12345");
        request.setPassword("password");

        UposlenikLoginDTO mockUposlenik = mock(UposlenikLoginDTO.class);
        
        when(mockUposlenik.getPassword()).thenReturn("hashed_password");
        when(mockUposlenik.getStatus()).thenReturn("Aktivan");
        when(mockUposlenik.getUserId()).thenReturn(1L);
        when(mockUposlenik.getUloga()).thenReturn("INSPEKTOR");
        when(mockUposlenik.getStanicaId()).thenReturn(100L);
        
        when(uposlenikRepository.findByEmailAndZnacka(request.getEmail(), request.getBrojZnacke()))
                .thenReturn(Optional.of(mockUposlenik));
        
        when(passwordEncoder.matches("password", "hashed_password")).thenReturn(true);
        
        when(jwtUtil.generateToken(1L, "INSPEKTOR", 100L)).thenReturn("lazni-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("lazni-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
    }

   @Test
@DisplayName("Login treba baciti grešku ako je korisnik penzionisan")
void login_KorisnikPenzionisan_ThrowsException() {
    LoginRequest request = new LoginRequest();
    request.setEmail("p@email.com");
    request.setBrojZnacke("555");
    request.setPassword("pass");

    UposlenikLoginDTO mockUposlenik = mock(UposlenikLoginDTO.class);
    when(mockUposlenik.getStatus()).thenReturn("Penzionisan");
    when(mockUposlenik.getPassword()).thenReturn("neki_hash");

    when(uposlenikRepository.findByEmailAndZnacka(any(), any())).thenReturn(Optional.of(mockUposlenik));
    
    when(passwordEncoder.matches(eq("pass"), anyString())).thenReturn(true);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
    
    System.out.println("Poruka greške: " + ex.getMessage());
    
    assertTrue(ex.getMessage().contains("Pristup odbijen"), 
        "Očekivana poruka treba sadržavati 'Pristup odbijen', ali je bila: " + ex.getMessage());
}

    @Test
    @DisplayName("Login treba baciti grešku za pogrešnu lozinku")
    void login_PogresnaLozinka_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setBrojZnacke("123");
        request.setPassword("pogresna");

        UposlenikLoginDTO mockUposlenik = mock(UposlenikLoginDTO.class);
        when(mockUposlenik.getPassword()).thenReturn("pravi_hash_u_bazi");
        
        when(uposlenikRepository.findByEmailAndZnacka(any(), any())).thenReturn(Optional.of(mockUposlenik));
        when(passwordEncoder.matches("pogresna", "pravi_hash_u_bazi")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Nevalidni podaci: Pogrešna lozinka!", ex.getMessage());
    }

    @Test
    @DisplayName("Logout treba dodati token na crnu listu ako je validan header")
    void logout_ValidanHeader_DodajeNaCrnuListu() {
        String authHeader = "Bearer moj-token-123";
        
        authService.logout(authHeader);
        
        verify(uposlenikRepository).dodajUTabeluCrnaLista("moj-token-123");
    }
}