package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DodajUposlenikaRequest;
import ba.unsa.etf.suds.dto.PromijeniStatusRequest;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.repository.UposlenikRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UposlenikServiceTest {

    @Mock
    private UposlenikRepository uposlenikRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UposlenikService uposlenikService;

    private Long targetUserId = 10L;
    private Long myStanicaId = 1L;
    private Long myUserId = 1L;

    @Test
    @DisplayName("Treba baciti SecurityException ako uposlenik nije iz iste stanice")
    void promijeniStatus_RazliciteStanice_ThrowsException() {
        when(uposlenikRepository.isUserInStanica(targetUserId, myStanicaId)).thenReturn(false);

        PromijeniStatusRequest request = new PromijeniStatusRequest();
        request.setStatus("Otpušten");

        assertThrows(SecurityException.class, () -> {
            uposlenikService.promijeniStatus(targetUserId, request, myStanicaId, myUserId);
        });

        verify(uposlenikRepository, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    @DisplayName("Ne smije dozvoliti otpuštanje zadnjeg šefa stanice")
    void promijeniStatus_ZadnjiSef_ThrowsException() {
        when(uposlenikRepository.isUserInStanica(targetUserId, myStanicaId)).thenReturn(true);
        when(uposlenikRepository.getUserRoleName(targetUserId)).thenReturn("SEF_STANICE");
        when(uposlenikRepository.countActiveSefovaPoStanici(myStanicaId)).thenReturn(1);

        PromijeniStatusRequest request = new PromijeniStatusRequest();
        request.setStatus("Otpušten");

        SecurityException ex = assertThrows(SecurityException.class, () -> {
            uposlenikService.promijeniStatus(targetUserId, request, myStanicaId, myUserId);
        });

        assertTrue(ex.getMessage().contains("zadnjeg aktivnog šefa"));
    }

    @Test
    @DisplayName("Uspješna promjena lične lozinke kada je stara ispravna")
    void promijeniLicnuLozinku_Success() {
        String staraLozinka = "stara123";
        String novaLozinka = "nova123";
        String hashUBazi = "hashed_stara";

        when(uposlenikRepository.getPasswordByUserId(myUserId)).thenReturn(hashUBazi);
        when(passwordEncoder.matches(staraLozinka, hashUBazi)).thenReturn(true);
        when(passwordEncoder.encode(novaLozinka)).thenReturn("hashed_nova");

        uposlenikService.promijeniLicnuLozinku(myUserId, staraLozinka, novaLozinka);

        verify(uposlenikRepository).updatePassword(myUserId, "hashed_nova");
    }

    @Test
    @DisplayName("Treba baciti RuntimeException ako stara lozinka nije ispravna")
    void promijeniLicnuLozinku_WrongOldPassword_ThrowsException() {
        when(uposlenikRepository.getPasswordByUserId(myUserId)).thenReturn("pravi_hash");
        when(passwordEncoder.matches("pogresna", "pravi_hash")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            uposlenikService.promijeniLicnuLozinku(myUserId, "pogresna", "nova123");
        });
    }

    @Test
@DisplayName("Ne smije dozvoliti otpuštanje šefa ako je zadnji aktivni u stanici")
void promijeniStatus_ZadnjiSef_ThrowsSecurityException() {
    Long userId = 5L;
    Long stanicaId = 1L;
    PromijeniStatusRequest request = new PromijeniStatusRequest();
    request.setStatus("Otpušten");

    when(uposlenikRepository.isUserInStanica(userId, stanicaId)).thenReturn(true);
    when(uposlenikRepository.getUserRoleName(userId)).thenReturn("SEF_STANICE");
    when(uposlenikRepository.countActiveSefovaPoStanici(stanicaId)).thenReturn(1);

    SecurityException ex = assertThrows(SecurityException.class, () -> {
        uposlenikService.promijeniStatus(userId, request, stanicaId, 1L);
    });

    assertTrue(ex.getMessage().contains("zadnjeg aktivnog šefa stanice"));
    verify(uposlenikRepository, never()).updateStatus(anyLong(), anyString());
}

@Test
@DisplayName("Treba baciti grešku ako se podaci ažuriraju na email koji već postoji")
void azurirajPodatke_EmailZauzet_ThrowsRuntimeException() {
    Long targetId = 10L;
    Long stanicaId = 1L;
    UposlenikDTO request = new UposlenikDTO();
    request.setEmail("postojeci@test.com");
    request.setIme("Neko");
    request.setPrezime("Nekić");

    when(uposlenikRepository.isUserInStanica(targetId, stanicaId)).thenReturn(true);
    when(uposlenikRepository.existsByEmailAndNotUserId("postojeci@test.com", targetId)).thenReturn(true);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
        uposlenikService.azurirajPodatke(targetId, request, stanicaId);
    });

    assertEquals("Email 'postojeci@test.com' je već zauzet!", ex.getMessage());
}

@Test
@DisplayName("Dodavanje uposlenika treba pasti ako broj značke već postoji")
void dodajUposlenika_BrojZnackePostoji_ThrowsException() {
    DodajUposlenikaRequest request = new DodajUposlenikaRequest();
    request.setBrojZnacke("99999");
    request.setEmail("novi@test.com");
    request.setUsername("korisnik123");

    when(uposlenikRepository.existsByEmail(anyString())).thenReturn(false);
    when(uposlenikRepository.existsByUsername(anyString())).thenReturn(false);
    when(uposlenikRepository.existsByBrojZnacke("99999")).thenReturn(true);

    assertThrows(RuntimeException.class, () -> {
        uposlenikService.dodajUposlenika(request, 1L);
    });
}
}