package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DokazRepositoryTest {

    @Mock
    private DatabaseManager dbManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement dokazStmt;

    @Mock
    private PreparedStatement confirmedStmt;

    @Mock
    private PreparedStatement pendingStmt;

    @Mock
    private PreparedStatement imeStmt;

    @Mock
    private ResultSet dokazRs;

    @Mock
    private ResultSet confirmedRs;

    @Mock
    private ResultSet pendingRs;

    @Mock
    private ResultSet imeRs;

    @InjectMocks
    private DokazRepository dokazRepository;

    @BeforeEach
    void setUp() throws SQLException {
        when(dbManager.getConnection()).thenReturn(connection);
    }

    /**
     * Regresijski test za GitHub issue #19.
     *
     * Inspektor potvrdi primopredaju → backend mora vratiti njegov
     * USER_ID kao trenutniNosilacId (a ne PRIKUPIO_USER_ID forenzičara).
     * Frontend EvidenceSection se oslanja na ovaj invariant da bi
     * prikazao "Trenutno kod: Vas" inspektoru u panelu Dokazi.
     *
     * findStanje radi tri SQL upita: (1) dohvat osnovnih podataka iz
     * DOKAZI, (2) dohvat zadnje 'Potvrđeno' primopredaje, (3) provjera
     * postoji li 'Čeka potvrdu' upis. Posljednji potvrđeni preuzimac
     * mora postati trenutni nosilac.
     */
    @Test
    @DisplayName("findStanje vraća zadnjeg potvrđenog preuzimaoca kao trenutnog nosioca - issue #19")
    void findStanje_LatestConfirmedHandover_BecomesCurrentHolder_Issue19() throws SQLException {
        Long dokazId = 50L;
        Long forenzicarUserId = 1L;
        Long inspektorUserId = 2L;
        Timestamp datumPrimopredaje = new Timestamp(1714000000000L);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        when(connection.prepareStatement(sqlCaptor.capture()))
                .thenReturn(dokazStmt, confirmedStmt, pendingStmt, imeStmt);

        when(dokazStmt.executeQuery()).thenReturn(dokazRs);
        when(dokazRs.next()).thenReturn(true);
        when(dokazRs.getLong("PRIKUPIO_USER_ID")).thenReturn(forenzicarUserId);
        when(dokazRs.wasNull()).thenReturn(false);
        when(dokazRs.getString("STATUS")).thenReturn("Odobren");

        when(confirmedStmt.executeQuery()).thenReturn(confirmedRs);
        when(confirmedRs.next()).thenReturn(true);
        when(confirmedRs.getLong("PREUZEO_USER_ID")).thenReturn(inspektorUserId);
        when(confirmedRs.wasNull()).thenReturn(false);
        when(confirmedRs.getTimestamp("DATUM_PRIMOPREDAJE")).thenReturn(datumPrimopredaje);

        when(pendingStmt.executeQuery()).thenReturn(pendingRs);
        when(pendingRs.next()).thenReturn(true);
        when(pendingRs.getInt(1)).thenReturn(0);

        when(imeStmt.executeQuery()).thenReturn(imeRs);
        when(imeRs.next()).thenReturn(true);
        when(imeRs.getString("FIRST_NAME")).thenReturn("Inspektor");
        when(imeRs.getString("LAST_NAME")).thenReturn("Test");

        Optional<DokazRepository.DokazStanjeInfo> result = dokazRepository.findStanje(dokazId);

        assertTrue(result.isPresent());
        DokazRepository.DokazStanjeInfo stanje = result.get();
        assertEquals(inspektorUserId, stanje.trenutniNosilacId(),
                "Inspektor mora postati trenutni nosilac nakon potvrđene primopredaje (#19)");
        assertEquals("Inspektor Test", stanje.trenutniNosilacIme());
        assertEquals("Odobren", stanje.status());
        assertEquals(datumPrimopredaje, stanje.zadnjaPrimopredaja());
        assertFalse(stanje.imaCekajucuPotvrdu());

        boolean confirmedSqlPresent = sqlCaptor.getAllValues().stream()
                .anyMatch(sql -> sql.contains("POTVRDA_STATUS = 'Potvrđeno'")
                        && sql.contains("ORDER BY DATUM_PRIMOPREDAJE DESC")
                        && sql.contains("FETCH FIRST 1 ROW ONLY"));
        assertTrue(confirmedSqlPresent,
                "Mora postojati upit koji uzima ZADNJU 'Potvrđeno' primopredaju za dokaz");
    }

    /**
     * Negativna kontrola za issue #19: ako nema niti jedne potvrđene
     * primopredaje, trenutni nosilac mora ostati osoba koja je dokaz
     * prikupila. Ovo garantira da fix za #19 nije slučajno ostavio
     * dokaz "kod nikoga" u slučajevima bez potvrđenog hand-over-a.
     */
    @Test
    @DisplayName("findStanje bez potvrđene primopredaje ostavlja prikupljača kao nosioca")
    void findStanje_NoConfirmedHandover_KeepsCollector() throws SQLException {
        Long dokazId = 51L;
        Long forenzicarUserId = 1L;

        when(connection.prepareStatement(anyString()))
                .thenReturn(dokazStmt, confirmedStmt, pendingStmt, imeStmt);

        when(dokazStmt.executeQuery()).thenReturn(dokazRs);
        when(dokazRs.next()).thenReturn(true);
        when(dokazRs.getLong("PRIKUPIO_USER_ID")).thenReturn(forenzicarUserId);
        when(dokazRs.wasNull()).thenReturn(false);
        when(dokazRs.getString("STATUS")).thenReturn("Odobren");

        when(confirmedStmt.executeQuery()).thenReturn(confirmedRs);
        when(confirmedRs.next()).thenReturn(false);

        when(pendingStmt.executeQuery()).thenReturn(pendingRs);
        when(pendingRs.next()).thenReturn(true);
        when(pendingRs.getInt(1)).thenReturn(0);

        when(imeStmt.executeQuery()).thenReturn(imeRs);
        when(imeRs.next()).thenReturn(true);
        when(imeRs.getString("FIRST_NAME")).thenReturn("Forenzičar");
        when(imeRs.getString("LAST_NAME")).thenReturn("Test");

        Optional<DokazRepository.DokazStanjeInfo> result = dokazRepository.findStanje(dokazId);

        assertTrue(result.isPresent());
        assertEquals(forenzicarUserId, result.get().trenutniNosilacId());
        assertEquals("Odobren", result.get().status());
    }

    /**
     * Pomoćni invariant za issue #19: kad postoji 'Čeka potvrdu' upis,
     * efektivni status mora biti "Čeka potvrdu" - frontend EvidenceSection
     * koristi taj status da zaključa daljnje predaje. Ako bi se ovaj test
     * srušio, dokaz bi se mogao predati dvaput istovremeno.
     */
    @Test
    @DisplayName("findStanje sa 'Čeka potvrdu' upisom postavlja efektivni status na 'Čeka potvrdu'")
    void findStanje_WithPendingHandover_SetsEffectiveStatusToCekaPotvrdu() throws SQLException {
        Long dokazId = 52L;

        when(connection.prepareStatement(anyString()))
                .thenReturn(dokazStmt, confirmedStmt, pendingStmt, imeStmt);

        when(dokazStmt.executeQuery()).thenReturn(dokazRs);
        when(dokazRs.next()).thenReturn(true);
        when(dokazRs.getLong("PRIKUPIO_USER_ID")).thenReturn(1L);
        when(dokazRs.wasNull()).thenReturn(false);
        when(dokazRs.getString("STATUS")).thenReturn("Odobren");

        when(confirmedStmt.executeQuery()).thenReturn(confirmedRs);
        when(confirmedRs.next()).thenReturn(false);

        when(pendingStmt.executeQuery()).thenReturn(pendingRs);
        when(pendingRs.next()).thenReturn(true);
        when(pendingRs.getInt(1)).thenReturn(1);

        when(imeStmt.executeQuery()).thenReturn(imeRs);
        when(imeRs.next()).thenReturn(true);
        when(imeRs.getString("FIRST_NAME")).thenReturn("Forenzičar");
        when(imeRs.getString("LAST_NAME")).thenReturn("Test");

        Optional<DokazRepository.DokazStanjeInfo> result = dokazRepository.findStanje(dokazId);

        assertTrue(result.isPresent());
        assertEquals("Čeka potvrdu", result.get().status());
        assertTrue(result.get().imaCekajucuPotvrdu());
    }
}
