package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.Stanica;
import ba.unsa.etf.suds.repository.StanicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StanicaServiceTest {

    @Mock
    private StanicaRepository repository;

    @InjectMocks
    private StanicaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll_VracaListuStanica() {
        Timestamp sada = new Timestamp(System.currentTimeMillis());
        // Pravimo lažne stanice koristeći ispravan konstruktor
        Stanica s1 = new Stanica(1L, "Policijska uprava Centar", 100L, sada);
        Stanica s2 = new Stanica(2L, "Policijska uprava Novo Sarajevo", 101L, sada);

        when(repository.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<Stanica> rezultat = service.getAll();

        assertEquals(2, rezultat.size());
        assertEquals("Policijska uprava Centar", rezultat.get(0).getImeStanice());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetById_StanicaNePostoji_BacaIzuzetak() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.getById(99L);
        });

        assertEquals("Stanica nije pronađena u bazi!", exception.getMessage());
    }
}