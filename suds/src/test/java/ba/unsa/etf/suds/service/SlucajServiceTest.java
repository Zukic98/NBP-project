package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.KreirajSlucajRequest;
import ba.unsa.etf.suds.model.Slucaj;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.SlucajRepository;
import ba.unsa.etf.suds.repository.TimNaSlucajuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SlucajServiceTest {

    @Mock
    private SlucajRepository slucajRepository;
    @Mock
    private AdresaRepository adresaRepository;
    @Mock
    private TimNaSlucajuRepository timRepository;
    @Mock
    private DatabaseManager dbManager;
    @Mock
    private Connection mockConnection;

    private SlucajService slucajService;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        slucajService = new SlucajService(slucajRepository, adresaRepository, timRepository, dbManager);
        when(dbManager.getConnection()).thenReturn(mockConnection);
    }

    @Test
    void kreirajSlucaj_TimInsertFails_TransactionRolledBack() throws SQLException {
        KreirajSlucajRequest request = new KreirajSlucajRequest();
        request.setStanicaId(1L);
        request.setBrojSlucaja("SLU-2026-001");
        request.setOpis("Test slučaj");
        request.setUlicaIBroj("Testna 1");
        request.setGrad("Sarajevo");
        request.setPostanskiBroj("71000");
        request.setDrzava("BiH");

        KreirajSlucajRequest.ClanTima clan = new KreirajSlucajRequest.ClanTima();
        clan.setUserId(100L);
        clan.setUloga("Istražitelj");
        request.setTim(List.of(clan));

        when(adresaRepository.saveWithConnection(any(Connection.class), any())).thenReturn(1L);
        when(slucajRepository.saveWithConnection(any(Connection.class), any())).thenReturn(1L);
        doThrow(new SQLException("DB failure during team insert"))
                .when(timRepository).saveWithConnection(any(Connection.class), any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slucajService.kreirajSlucaj(request, 1L);
        });

        assertTrue(exception.getMessage().contains("Transaction failed"));
        verify(mockConnection, times(1)).rollback();
        verify(mockConnection, never()).commit();
    }

    @Test
    void kreirajSlucaj_AllStepsSucceed_TransactionCommitted() throws SQLException {
        KreirajSlucajRequest request = new KreirajSlucajRequest();
        request.setStanicaId(1L);
        request.setBrojSlucaja("SLU-2026-002");
        request.setOpis("Uspješan slučaj");
        request.setUlicaIBroj("Testna 2");
        request.setGrad("Sarajevo");
        request.setPostanskiBroj("71000");
        request.setDrzava("BiH");
        request.setTim(List.of());

        when(adresaRepository.saveWithConnection(any(Connection.class), any())).thenReturn(2L);
        when(slucajRepository.saveWithConnection(any(Connection.class), any())).thenReturn(2L);

        Slucaj result = slucajService.kreirajSlucaj(request, 1L);

        assertNotNull(result);
        assertEquals(2L, result.getSlucajId());
        verify(mockConnection, times(1)).commit();
        verify(mockConnection, never()).rollback();
    }
}
