package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.IzvjestajDTO;
import ba.unsa.etf.suds.dto.KreirajSlucajRequest;
import ba.unsa.etf.suds.dto.SlucajListDTO;
import ba.unsa.etf.suds.model.Slucaj;
import ba.unsa.etf.suds.repository.AdresaRepository;
import ba.unsa.etf.suds.repository.IzvjestajRepository;
import ba.unsa.etf.suds.repository.SlucajRepository;
import ba.unsa.etf.suds.repository.TimNaSlucajuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private IzvjestajRepository izvjestajRepository;
    @Mock
    private DatabaseManager dbManager;
    @Mock
    private Connection mockConnection;

    private SlucajService slucajService;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        slucajService = new SlucajService(slucajRepository, adresaRepository, timRepository, izvjestajRepository, dbManager);
        when(dbManager.getConnection()).thenReturn(mockConnection);
    }

    @Test
    void kreirajSlucaj_TimInsertFails_TransactionRolledBack() throws SQLException {
        KreirajSlucajRequest request = new KreirajSlucajRequest();
        setField(request, "stanicaId", 1L);
        setField(request, "brojSlucaja", "SLU-2026-001");
        setField(request, "opis", "Test slučaj");
        setField(request, "ulicaIBroj", "Testna 1");
        setField(request, "grad", "Sarajevo");
        setField(request, "postanskiBroj", "71000");
        setField(request, "drzava", "BiH");

        KreirajSlucajRequest.ClanTima clan = new KreirajSlucajRequest.ClanTima();
        setField(clan, "userId", 100L);
        setField(clan, "uloga", "Istražitelj");
        setField(request, "tim", List.of(clan));

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
        setField(request, "stanicaId", 1L);
        setField(request, "brojSlucaja", "SLU-2026-002");
        setField(request, "opis", "Uspješan slučaj");
        setField(request, "ulicaIBroj", "Testna 2");
        setField(request, "grad", "Sarajevo");
        setField(request, "postanskiBroj", "71000");
        setField(request, "drzava", "BiH");
        setField(request, "tim", List.of());

        when(adresaRepository.saveWithConnection(any(Connection.class), any())).thenReturn(2L);
        when(slucajRepository.saveWithConnection(any(Connection.class), any())).thenReturn(2L);

        SlucajListDTO expectedDto = new SlucajListDTO();
        expectedDto.setSlucajId(2L);
        expectedDto.setBrojSlucaja("SLU-2026-002");
        when(slucajRepository.findByIdWithVoditelj(2L)).thenReturn(Optional.of(expectedDto));

        SlucajListDTO result = slucajService.kreirajSlucaj(request, 1L);

        assertNotNull(result);
        assertEquals(2L, result.getSlucajId());
        assertEquals("SLU-2026-002", result.getBrojSlucaja());
        verify(mockConnection, times(1)).commit();
        verify(mockConnection, never()).rollback();
        verify(slucajRepository).findByIdWithVoditelj(2L);
    }

    @Test
    void updateSlucajStatus_ValidStatus_ReturnsTrue() {
        when(slucajRepository.updateStatus(1L, "Zatvoren")).thenReturn(true);

        boolean result = slucajService.updateSlucajStatus(1L, "Zatvoren");

        assertTrue(result);
        verify(slucajRepository).updateStatus(1L, "Zatvoren");
    }

    @Test
    void updateSlucajStatus_InvalidStatus_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> slucajService.updateSlucajStatus(1L, "INVALID"));
        verify(slucajRepository, never()).updateStatus(any(), any());
    }

    @Test
    void updateSlucajStatus_NullStatus_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> slucajService.updateSlucajStatus(1L, null));
        verify(slucajRepository, never()).updateStatus(any(), any());
    }

    /**
     * Regresijski test za GitHub issue #15 / #16.
     *
     * Frontend prikazuje status "Otvoren" (a ne "Aktivan"), pa je backend ranije
     * odbijao prebacivanje slučaja sa "Zatvoren" / "Arhiviran" nazad na "Otvoren"
     * jer "Otvoren" nije bio u dozvoljenom skupu (vraćao je 400 Bad Request).
     * Posljedica: status badge i header u UI-u nisu bili sinhronizirani, a
     * re-otvaranje slučaja je padalo s greškom. Sada "Otvoren" mora biti dozvoljen.
     */
    @Test
    void updateSlucajStatus_OtvorenStatus_IsAllowed_Issue15And16() {
        when(slucajRepository.updateStatus(1L, "Otvoren")).thenReturn(true);

        boolean result = slucajService.updateSlucajStatus(1L, "Otvoren");

        assertTrue(result, "Backend mora dozvoliti prebacivanje na 'Otvoren' (#16)");
        verify(slucajRepository).updateStatus(1L, "Otvoren");
    }

    /**
     * Regresijski test za GitHub issue #15.
     *
     * Stari kod koristio "Aktivan" kao default; frontend nikad ne renderira
     * "Aktivan" pa je padao u žuti "Arhiviran" badge fallback (desinhronizacija
     * između naslova i badge-a). Sad "Aktivan" više nije validan status.
     */
    @Test
    void updateSlucajStatus_AktivanStatus_IsRejected_Issue15() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> slucajService.updateSlucajStatus(1L, "Aktivan"));
        assertTrue(ex.getMessage().contains("Otvoren"),
                "Poruka greške mora navesti dozvoljene statuse uključujući 'Otvoren'");
        verify(slucajRepository, never()).updateStatus(any(), any());
    }

    /**
     * Regresijski test za GitHub issue #15.
     *
     * Novokreirani slučaj mora imati status "Otvoren", a ne legacy "Aktivan",
     * kako bi se naslov i badge u UI-u poklapali odmah po kreiranju.
     */
    @Test
    void kreirajSlucaj_DefaultStatusIsOtvoren_Issue15() throws SQLException {
        KreirajSlucajRequest request = new KreirajSlucajRequest();
        setField(request, "stanicaId", 1L);
        setField(request, "brojSlucaja", "SLU-2026-DEFAULT-STATUS");
        setField(request, "opis", "Provjera defaultnog statusa");
        setField(request, "ulicaIBroj", "Testna 9");
        setField(request, "grad", "Sarajevo");
        setField(request, "postanskiBroj", "71000");
        setField(request, "drzava", "BiH");
        setField(request, "tim", List.of());

        when(adresaRepository.saveWithConnection(any(Connection.class), any())).thenReturn(9L);

        ArgumentCaptor<Slucaj> slucajCaptor = ArgumentCaptor.forClass(Slucaj.class);
        when(slucajRepository.saveWithConnection(any(Connection.class), slucajCaptor.capture())).thenReturn(9L);
        when(slucajRepository.findByIdWithVoditelj(9L)).thenReturn(Optional.of(new SlucajListDTO()));

        slucajService.kreirajSlucaj(request, 1L);

        Slucaj saved = slucajCaptor.getValue();
        assertEquals("Otvoren", saved.getStatus(),
                "Novi slučaj mora imati status 'Otvoren' (#15) da bi se UI badge i naslov sinhronizirali");
    }

    @Test
    void getIzvjestaj_ExistingCase_ReturnsFullReport() {
        Long slucajId = 7L;
        IzvjestajDTO.SlucajInfo slucajInfo = mock(IzvjestajDTO.SlucajInfo.class);

        IzvjestajDTO.DokazInfo dokazInfo = mock(IzvjestajDTO.DokazInfo.class);
        List<IzvjestajDTO.DokazInfo> dokazi = List.of(dokazInfo);

        IzvjestajDTO.LanacInfo lanacInfo = mock(IzvjestajDTO.LanacInfo.class);
        List<IzvjestajDTO.LanacInfo> lanac = List.of(lanacInfo);

        IzvjestajDTO.TimInfo timInfo = mock(IzvjestajDTO.TimInfo.class);
        List<IzvjestajDTO.TimInfo> tim = List.of(timInfo);

        IzvjestajDTO.SvjedokInfo svjedokInfo = mock(IzvjestajDTO.SvjedokInfo.class);
        List<IzvjestajDTO.SvjedokInfo> svjedoci = List.of(svjedokInfo);

        when(izvjestajRepository.findSlucajInfo(slucajId)).thenReturn(Optional.of(slucajInfo));
        when(izvjestajRepository.findDokazi(slucajId)).thenReturn(dokazi);
        when(izvjestajRepository.findLanacNadzora(slucajId)).thenReturn(lanac);
        when(izvjestajRepository.findTim(slucajId)).thenReturn(tim);
        when(izvjestajRepository.findSvjedoci(slucajId)).thenReturn(svjedoci);

        Optional<IzvjestajDTO> result = slucajService.getIzvjestaj(slucajId);

        assertTrue(result.isPresent());
        assertEquals(slucajInfo, getField(result.get(), "slucaj"));
        assertEquals(dokazi, getField(result.get(), "dokazi"));
        assertEquals(lanac, getField(result.get(), "lanacNadzora"));
        assertEquals(tim, getField(result.get(), "tim"));
        assertEquals(svjedoci, getField(result.get(), "svjedoci"));
    }

    @Test
    void getIzvjestaj_NonExistingCase_ReturnsEmpty() {
        Long slucajId = 999L;
        when(izvjestajRepository.findSlucajInfo(slucajId)).thenReturn(Optional.empty());

        Optional<IzvjestajDTO> result = slucajService.getIzvjestaj(slucajId);

        assertTrue(result.isEmpty());
        verify(izvjestajRepository, never()).findDokazi(any());
        verify(izvjestajRepository, never()).findLanacNadzora(any());
        verify(izvjestajRepository, never()).findTim(any());
        verify(izvjestajRepository, never()).findSvjedoci(any());
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
