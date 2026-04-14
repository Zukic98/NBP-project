package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.DodajSvjedokaRequest;
import ba.unsa.etf.suds.dto.SvjedokDTO;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.SvjedokRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SvjedokServiceTest {

    @Mock
    private SvjedokRepository svjedokRepository;
    @Mock
    private AdresaRepository adresaRepository;
    @Mock
    private DatabaseManager dbManager;
    @Mock
    private Connection connection;

    private SvjedokService svjedokService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        svjedokService = new SvjedokService(svjedokRepository, adresaRepository, dbManager);
    }

    @Test
    void getSvjedociBySlucajId_ReturnsList() {
        SvjedokDTO dto = new SvjedokDTO();
        setField(dto, "svjedokId", 1L);
        when(svjedokRepository.findBySlucajId(8L)).thenReturn(List.of(dto));

        List<SvjedokDTO> result = svjedokService.getSvjedociBySlucajId(8L);

        assertEquals(1, result.size());
        assertEquals(1L, getField(result.get(0), "svjedokId"));
    }

    @Test
    void dodajSvjedoka_ValidRequest_CommitsTransaction() throws SQLException {
        DodajSvjedokaRequest request = new DodajSvjedokaRequest();
        setField(request, "imePrezime", "Svjedok Test");
        setField(request, "jmbg", "1234567890123");
        setField(request, "kontaktTelefon", "+38761111222");
        setField(request, "biljeska", "Biljeska");
        setField(request, "ulicaIBroj", "Ulica 1");
        setField(request, "grad", "Sarajevo");
        setField(request, "postanskiBroj", "71000");
        setField(request, "drzava", "BiH");

        when(dbManager.getConnection()).thenReturn(connection);
        when(adresaRepository.saveWithConnection(any(Connection.class), any())).thenReturn(1L);

        svjedokService.dodajSvjedoka(2L, request);

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void dodajSvjedoka_DbFailure_RollsBack() throws SQLException {
        DodajSvjedokaRequest request = new DodajSvjedokaRequest();
        setField(request, "imePrezime", "Svjedok Test");
        setField(request, "jmbg", "1234567890123");
        setField(request, "ulicaIBroj", "Ulica 1");
        setField(request, "grad", "Sarajevo");
        setField(request, "postanskiBroj", "71000");
        setField(request, "drzava", "BiH");

        when(dbManager.getConnection()).thenReturn(connection);
        when(adresaRepository.saveWithConnection(any(Connection.class), any()))
                .thenThrow(new SQLException("DB fail"));

        assertThrows(RuntimeException.class, () -> svjedokService.dodajSvjedoka(2L, request));

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
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
