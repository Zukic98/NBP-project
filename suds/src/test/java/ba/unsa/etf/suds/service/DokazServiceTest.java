package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DokazListDTO;
import ba.unsa.etf.suds.dto.DokazStanjeDTO;
import ba.unsa.etf.suds.dto.KreirajDokazRequest;
import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.ForenzickiIzvjestajRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DokazServiceTest {

    @Mock
    private DokazRepository dokazRepository;
    @Mock
    private LanacNadzoraRepository lanacNadzoraRepository;
    @Mock
    private ForenzickiIzvjestajRepository izvjestajRepository;

    private DokazService dokazService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dokazService = new DokazService(dokazRepository, lanacNadzoraRepository, izvjestajRepository);
    }

    @Test
    void kreirajZaSlucaj_ValidRequest_ReturnsSavedDokaz() {
        Long slucajId = 1L;
        Long userId = 100L;

        KreirajDokazRequest request = new KreirajDokazRequest();
        setField(request, "opis", "Nož");
        setField(request, "lokacijaPronalaska", "Mjesto događaja");
        setField(request, "tipDokaza", "Fizički");

        Dokaz savedDokaz = new Dokaz();
        setField(savedDokaz, "dokazId", 55L);
        setField(savedDokaz, "slucajId", slucajId);
        setField(savedDokaz, "opis", "Nož");
        setField(savedDokaz, "lokacijaPronalaska", "Mjesto događaja");
        setField(savedDokaz, "tipDokaza", "Fizički");
        setField(savedDokaz, "status", "U posjedu");
        setField(savedDokaz, "datumPrikupa", new Timestamp(System.currentTimeMillis()));

        DokazListDTO dto = new DokazListDTO();
        setField(dto, "dokazId", 55L);
        setField(dto, "opis", "Nož");

        when(dokazRepository.findStanicaIdByUserId(userId)).thenReturn(5L);
        when(dokazRepository.save(any(Dokaz.class))).thenReturn(savedDokaz);
        when(dokazRepository.findBySlucajId(slucajId)).thenReturn(List.of(dto));

        DokazListDTO result = dokazService.kreirajZaSlucaj(slucajId, request, userId);

        assertNotNull(result);
        assertEquals(55L, getField(result, "dokazId"));
        verify(dokazRepository).findStanicaIdByUserId(userId);
        verify(dokazRepository).save(any(Dokaz.class));
        verify(dokazRepository).findBySlucajId(slucajId);
    }

    @Test
    void kreirajZaSlucaj_NoProfile_ThrowsIllegalArgument() {
        when(dokazRepository.findStanicaIdByUserId(100L)).thenReturn(null);

        KreirajDokazRequest request = new KreirajDokazRequest();
        setField(request, "opis", "Opis");

        assertThrows(IllegalArgumentException.class,
                () -> dokazService.kreirajZaSlucaj(1L, request, 100L));
        verify(dokazRepository, never()).save(any());
    }

    @Test
    void getStanje_CurrentHolder_MozePredatiTrue() {
        Long dokazId = 10L;
        Long userId = 200L;
        DokazRepository.DokazStanjeInfo stanjeInfo = new DokazRepository.DokazStanjeInfo(
                userId, "Ime Prezime", "U posjedu", new Timestamp(System.currentTimeMillis()), false
        );
        when(dokazRepository.findStanje(dokazId)).thenReturn(Optional.of(stanjeInfo));

        DokazStanjeDTO result = dokazService.getStanje(dokazId, userId);

        assertTrue((Boolean) getField(result, "mozePredati"));
    }

    @Test
    void getStanje_NotHolder_MozePredatiFalse() {
        DokazRepository.DokazStanjeInfo stanjeInfo = new DokazRepository.DokazStanjeInfo(
                201L, "Drugi", "U posjedu", new Timestamp(System.currentTimeMillis()), false
        );
        when(dokazRepository.findStanje(10L)).thenReturn(Optional.of(stanjeInfo));

        DokazStanjeDTO result = dokazService.getStanje(10L, 200L);

        assertFalse((Boolean) getField(result, "mozePredati"));
    }

    @Test
    void getStanje_PendingHandover_MozePredatiFalse() {
        DokazRepository.DokazStanjeInfo stanjeInfo = new DokazRepository.DokazStanjeInfo(
                200L, "Ime", "Čeka potvrdu", new Timestamp(System.currentTimeMillis()), true
        );
        when(dokazRepository.findStanje(10L)).thenReturn(Optional.of(stanjeInfo));

        DokazStanjeDTO result = dokazService.getStanje(10L, 200L);

        assertFalse((Boolean) getField(result, "mozePredati"));
    }

    @Test
    void getStanje_NonExisting_ThrowsIllegalArgument() {
        when(dokazRepository.findStanje(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> dokazService.getStanje(999L, 1L));
    }

    @Test
    void azurirajStatus_ExistingDokaz_SuccessfulUpdate() {
        when(dokazRepository.updateStatus(1L, "Arhiviran")).thenReturn(true);

        assertDoesNotThrow(() -> dokazService.azurirajStatus(1L, "Arhiviran"));
        verify(dokazRepository).updateStatus(1L, "Arhiviran");
    }

    @Test
    void azurirajStatus_NonExisting_ThrowsIllegalArgument() {
        when(dokazRepository.updateStatus(404L, "Arhiviran")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> dokazService.azurirajStatus(404L, "Arhiviran"));
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
