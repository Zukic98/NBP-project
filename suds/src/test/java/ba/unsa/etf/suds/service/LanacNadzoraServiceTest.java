package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.dto.PonistiRequest;
import ba.unsa.etf.suds.dto.PrimopredajaRequest;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LanacNadzoraServiceTest {

    @Mock
    private LanacNadzoraRepository lanacRepository;

    @Mock
    private DokazRepository dokazRepository;

    private LanacNadzoraService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new LanacNadzoraService(lanacRepository, dokazRepository);
    }

    @Test
    void posaljiDokaz_SetsStatusToCekaPotvrdu() {
        PosaljiDokazRequest request = new PosaljiDokazRequest();
        setField(request, "dokazId", 1L);
        setField(request, "primaocUserId", 2L);
        setField(request, "stanicaId", 3L);
        setField(request, "svrhaPrimopredaje", "Forenzička analiza");

        Long predaoUserId = 10L;

        when(lanacRepository.save(any(LanacNadzora.class))).thenAnswer(invocation -> {
            LanacNadzora lanac = invocation.getArgument(0);
            setField(lanac, "unosId", 100L);
            return lanac;
        });

        LanacNadzora result = service.posaljiDokaz(request, predaoUserId);

        ArgumentCaptor<LanacNadzora> captor = ArgumentCaptor.forClass(LanacNadzora.class);
        verify(lanacRepository).save(captor.capture());
        LanacNadzora saved = captor.getValue();

        assertEquals("Čeka potvrdu", getField(saved, "potvrdaStatus"));
        assertEquals(1L, getField(saved, "dokazId"));
        assertEquals(2L, getField(saved, "preuzeoUserId"));
        assertEquals(10L, getField(saved, "predaoUserId"));
    }

    @Test
    void prihvatiDokaz_ValidRequest_UpdatesStatusToPotvrdjeno() {
        Long unosId = 100L;
        Long potvrdioUserId = 2L;

        LanacNadzora existingLanac = new LanacNadzora();
        setField(existingLanac, "unosId", unosId);
        setField(existingLanac, "dokazId", 1L);
        setField(existingLanac, "preuzeoUserId", potvrdioUserId);
        setField(existingLanac, "potvrdaStatus", "Čeka potvrdu");

        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(existingLanac));

        service.prihvatiDokaz(unosId, potvrdioUserId);

        verify(lanacRepository).prihvati(unosId, potvrdioUserId);
    }

    @Test
    void prihvatiDokaz_WrongUser_ThrowsException() {
        Long unosId = 100L;
        Long wrongUserId = 999L;

        LanacNadzora existingLanac = new LanacNadzora();
        setField(existingLanac, "unosId", unosId);
        setField(existingLanac, "dokazId", 1L);
        setField(existingLanac, "preuzeoUserId", 2L);
        setField(existingLanac, "potvrdaStatus", "Čeka potvrdu");

        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(existingLanac));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.prihvatiDokaz(unosId, wrongUserId);
        });

        assertEquals("Samo primaoc može potvrditi primopredaju", exception.getMessage());
        verify(lanacRepository, never()).prihvati(any(), any());
        verify(dokazRepository, never()).updateStatus(any(), any());
    }

    @Test
    void prihvatiDokaz_AlreadyProcessed_ThrowsException() {
        Long unosId = 100L;
        Long potvrdioUserId = 2L;

        LanacNadzora existingLanac = new LanacNadzora();
        setField(existingLanac, "unosId", unosId);
        setField(existingLanac, "dokazId", 1L);
        setField(existingLanac, "preuzeoUserId", potvrdioUserId);
        setField(existingLanac, "potvrdaStatus", "Potvrđeno");

        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(existingLanac));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.prihvatiDokaz(unosId, potvrdioUserId);
        });

        assertEquals("Primopredaja je već obrađena", exception.getMessage());
        verify(lanacRepository, never()).prihvati(any(), any());
    }

    @Test
    void prihvatiDokaz_NotFound_ThrowsException() {
        Long unosId = 999L;
        Long potvrdioUserId = 2L;

        when(lanacRepository.findById(unosId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.prihvatiDokaz(unosId, potvrdioUserId);
        });

        assertTrue(exception.getMessage().contains("Unos lanca nadzora ne postoji"));
    }

    @Test
    void kreirajPrimopredaju_ValidDokaz_SavesAndUpdatesStatus() {
        Long dokazId = 11L;
        Long predaoUserId = 22L;

        Dokaz dokaz = new Dokaz();
        setField(dokaz, "dokazId", dokazId);
        setField(dokaz, "stanicaId", 3L);

        PrimopredajaRequest request = new PrimopredajaRequest();
        setField(request, "preuzeoUposlenikId", 33L);
        setField(request, "svrha", "Analiza");

        when(dokazRepository.findById(dokazId)).thenReturn(dokaz);
        when(lanacRepository.save(any(LanacNadzora.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LanacNadzora result = service.kreirajPrimopredaju(dokazId, request, predaoUserId);

        assertNotNull(result);
        ArgumentCaptor<LanacNadzora> captor = ArgumentCaptor.forClass(LanacNadzora.class);
        verify(lanacRepository).save(captor.capture());
        LanacNadzora saved = captor.getValue();

        assertEquals(dokazId, getField(saved, "dokazId"));
        assertEquals(3L, getField(saved, "stanicaId"));
        assertEquals(predaoUserId, getField(saved, "predaoUserId"));
        assertEquals(33L, getField(saved, "preuzeoUserId"));
        assertEquals("Analiza", getField(saved, "svrhaPrimopredaje"));
        assertEquals("Čeka potvrdu", getField(saved, "potvrdaStatus"));
    }

    @Test
    void kreirajPrimopredaju_NonExistingDokaz_ThrowsException() {
        Long dokazId = 99L;
        PrimopredajaRequest request = new PrimopredajaRequest();
        setField(request, "preuzeoUposlenikId", 1L);

        when(dokazRepository.findById(dokazId)).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.kreirajPrimopredaju(dokazId, request, 2L));

        assertTrue(exception.getMessage().contains("Dokaz ne postoji"));
        verify(lanacRepository, never()).save(any());
        verify(dokazRepository, never()).updateStatus(eq(dokazId), any());
    }

    @Test
    void potvrdiIliOdbij_ValidConfirm_UpdatesStatus() {
        Long unosId = 1L;
        Long userId = 10L;
        LanacNadzora lanac = new LanacNadzora();
        setField(lanac, "unosId", unosId);
        setField(lanac, "dokazId", 50L);
        setField(lanac, "preuzeoUserId", userId);
        setField(lanac, "potvrdaStatus", "Čeka potvrdu");
        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(lanac));

        service.potvrdiIliOdbij(unosId, "Potvrđeno", "ok", userId);

        verify(lanacRepository).potvrdiIliOdbij(unosId, "Potvrđeno", "ok", userId);
    }

    @Test
    void potvrdiIliOdbij_Reject_UpdatesStatus() {
        Long unosId = 2L;
        Long userId = 11L;
        LanacNadzora lanac = new LanacNadzora();
        setField(lanac, "unosId", unosId);
        setField(lanac, "dokazId", 51L);
        setField(lanac, "preuzeoUserId", userId);
        setField(lanac, "potvrdaStatus", "Čeka potvrdu");
        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(lanac));

        service.potvrdiIliOdbij(unosId, "Odbijeno", "nije ispravno", userId);

        verify(lanacRepository).potvrdiIliOdbij(unosId, "Odbijeno", "nije ispravno", userId);
    }

    @Test
    void potvrdiIliOdbij_InvalidStatus_ThrowsException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.potvrdiIliOdbij(1L, "INVALID", "", 2L));
        assertTrue(exception.getMessage().contains("Neispravan status potvrde"));
        verify(lanacRepository, never()).findById(any());
    }

    @Test
    void potvrdiIliOdbij_WrongUser_ThrowsException() {
        Long unosId = 3L;
        LanacNadzora lanac = new LanacNadzora();
        setField(lanac, "unosId", unosId);
        setField(lanac, "dokazId", 52L);
        setField(lanac, "preuzeoUserId", 100L);
        setField(lanac, "potvrdaStatus", "Čeka potvrdu");
        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(lanac));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.potvrdiIliOdbij(unosId, "Potvrđeno", "", 200L));
        assertEquals("Samo primaoc može potvrditi ili odbiti primopredaju", exception.getMessage());
        verify(lanacRepository, never()).potvrdiIliOdbij(any(), any(), any(), any());
        verify(dokazRepository, never()).updateStatus(any(), any());
    }

    @Test
    void potvrdiIliOdbij_AlreadyProcessed_ThrowsException() {
        Long unosId = 4L;
        Long userId = 300L;
        LanacNadzora lanac = new LanacNadzora();
        setField(lanac, "unosId", unosId);
        setField(lanac, "dokazId", 53L);
        setField(lanac, "preuzeoUserId", userId);
        setField(lanac, "potvrdaStatus", "Potvrđeno");
        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(lanac));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.potvrdiIliOdbij(unosId, "Potvrđeno", "", userId));
        assertEquals("Primopredaja je već obrađena", exception.getMessage());
        verify(lanacRepository, never()).potvrdiIliOdbij(any(), any(), any(), any());
    }

    @Test
    void ponistiPrimopredaju_ValidSender_Cancels() {
        Long unosId = 5L;
        Long senderId = 40L;

        LanacNadzora lanac = new LanacNadzora();
        setField(lanac, "unosId", unosId);
        setField(lanac, "dokazId", 54L);
        setField(lanac, "predaoUserId", senderId);
        setField(lanac, "potvrdaStatus", "Čeka potvrdu");
        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(lanac));

        PonistiRequest request = new PonistiRequest();
        setField(request, "razlog", "Pogrešan primaoc");

        service.ponistiPrimopredaju(unosId, request, senderId);

        verify(lanacRepository).ponisti(unosId, "Pogrešan primaoc", senderId);
    }

    @Test
    void ponistiPrimopredaju_WrongUser_ThrowsException() {
        Long unosId = 6L;

        LanacNadzora lanac = new LanacNadzora();
        setField(lanac, "unosId", unosId);
        setField(lanac, "dokazId", 55L);
        setField(lanac, "predaoUserId", 41L);
        setField(lanac, "potvrdaStatus", "Čeka potvrdu");
        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(lanac));

        PonistiRequest request = new PonistiRequest();
        setField(request, "razlog", "Razlog");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.ponistiPrimopredaju(unosId, request, 99L));
        assertEquals("Samo pošiljalac može poništiti primopredaju", exception.getMessage());
        verify(lanacRepository, never()).ponisti(any(), any(), any());
        verify(dokazRepository, never()).updateStatus(any(), any());
    }

    @Test
    void ponistiPrimopredaju_AlreadyProcessed_ThrowsException() {
        Long unosId = 7L;
        Long senderId = 42L;

        LanacNadzora lanac = new LanacNadzora();
        setField(lanac, "unosId", unosId);
        setField(lanac, "dokazId", 56L);
        setField(lanac, "predaoUserId", senderId);
        setField(lanac, "potvrdaStatus", "Potvrđeno");
        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(lanac));

        PonistiRequest request = new PonistiRequest();
        setField(request, "razlog", "Kasno");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.ponistiPrimopredaju(unosId, request, senderId));
        assertEquals("Moguće je poništiti samo primopredaju koja čeka potvrdu", exception.getMessage());
        verify(lanacRepository, never()).ponisti(any(), any(), any());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
