package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.DodajClanaTRequest;
import ba.unsa.etf.suds.dto.TimClanDTO;
import ba.unsa.etf.suds.repository.TimNaSlucajuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TimServiceTest {

    @Mock
    private TimNaSlucajuRepository timNaSlucajuRepository;

    private TimService timService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        timService = new TimService(timNaSlucajuRepository);
    }

    @Test
    void getClanoviTima_ReturnsList() {
        TimClanDTO clan = new TimClanDTO();
        setField(clan, "dodjelaId", 1L);
        when(timNaSlucajuRepository.findByCaseId(10L)).thenReturn(List.of(clan));

        List<TimClanDTO> result = timService.getClanoviTima(10L);

        assertEquals(1, result.size());
        assertEquals(1L, getField(result.get(0), "dodjelaId"));
    }

    @Test
    void dodajClanaTima_SavesAndReturnsDTO() {
        DodajClanaTRequest request = new DodajClanaTRequest();
        setField(request, "uposlenikId", 7L);
        setField(request, "ulogaNaSlucaju", "Analiticar");

        TimClanDTO clan = new TimClanDTO();
        setField(clan, "dodjelaId", 42L);
        when(timNaSlucajuRepository.save(any())).thenReturn(42L);
        when(timNaSlucajuRepository.findByCaseId(5L)).thenReturn(List.of(clan));

        TimClanDTO result = timService.dodajClanaTima(5L, request);

        assertNotNull(result);
        assertEquals(42L, getField(result, "dodjelaId"));
    }

    @Test
    void dodajClanaTima_SavedButNotFound_ThrowsException() {
        DodajClanaTRequest request = new DodajClanaTRequest();
        setField(request, "uposlenikId", 7L);
        setField(request, "ulogaNaSlucaju", "Analiticar");

        when(timNaSlucajuRepository.save(any())).thenReturn(99L);
        when(timNaSlucajuRepository.findByCaseId(5L)).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> timService.dodajClanaTima(5L, request));
    }

    @Test
    void ukloniClanaTima_CallsDelete() {
        timService.ukloniClanaTima(123L);

        verify(timNaSlucajuRepository).deleteById(123L);
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
