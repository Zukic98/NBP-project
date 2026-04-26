package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.MojSlucajDTO;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlucajRepositoryTest {

    @Mock
    private DatabaseManager dbManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private SlucajRepository slucajRepository;

    @BeforeEach
    void setUp() throws SQLException {
        when(dbManager.getConnection()).thenReturn(connection);
    }

    /**
     * Regresijski test za GitHub issue #18.
     *
     * Ranije je upit za "moji slučajevi" za rolu "Inspektor"/"INSPEKTOR"
     * filtrirao isključivo po VODITELJ_USER_ID, pa kad bi Šef stanice
     * dodijelio slučaj inspektoru kao članu tima (TIM_NA_SLUCAJU), inspektor
     * taj slučaj nije vidio na dashboardu. Novi upit mora obuhvatiti i
     * voditeljske slučajeve i tim slučajeve, mora imati priority 'Voditelj'
     * uloga, i mora vezati userId 4 puta (jednom za CASE, jednom za LEFT
     * JOIN filter, dvaput za WHERE OR uslov).
     */
    @Test
    @DisplayName("findMojiSlucajevi za INSPEKTOR-a koristi UNION upit (voditelj OR tim) - issue #18")
    void findMojiSlucajevi_Inspektor_IncludesTeamCases_Issue18() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("SLUCAJ_ID")).thenReturn(11L, 22L);
        when(resultSet.getString("BROJ_SLUCAJA")).thenReturn("SLU-001", "SLU-002");
        when(resultSet.getString("OPIS")).thenReturn("Voditelj slučaj", "Tim slučaj");
        when(resultSet.getString("STATUS")).thenReturn("Otvoren", "Otvoren");
        when(resultSet.getString("IME_VODITELJA")).thenReturn("Inspektor X", "Šef Y");
        when(resultSet.getString("ULOGA")).thenReturn("Voditelj", "Istražitelj");
        when(resultSet.getTimestamp("DATUM_KREIRANJA")).thenReturn(new Timestamp(0L), new Timestamp(0L));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        List<MojSlucajDTO> rezultat = slucajRepository.findMojiSlucajevi(7L, "INSPEKTOR");

        verify(connection).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("LEFT JOIN TIM_NA_SLUCAJU"),
                "Inspektor upit mora LEFT JOIN-ati TIM_NA_SLUCAJU da bi vraćao i tim slučajeve");
        assertTrue(sql.contains("VODITELJ_USER_ID = ? OR t.USER_ID = ?"),
                "WHERE klauzula mora imati OR između voditelj-a i tim member-a");
        assertTrue(sql.contains("'Voditelj'"),
                "CASE mora postaviti ulogu 'Voditelj' kad je korisnik voditelj slučaja");

        verify(preparedStatement, times(4)).setLong(anyInt(), eq(7L));

        assertEquals(2, rezultat.size(), "Inspektor mora vidjeti i voditeljske i tim slučajeve");
        assertEquals(11L, rezultat.get(0).getSlucajId());
        assertEquals("Voditelj", rezultat.get(0).getUlogaNaSlucaju());
        assertEquals(22L, rezultat.get(1).getSlucajId());
        assertEquals("Istražitelj", rezultat.get(1).getUlogaNaSlucaju());
    }

    /**
     * Regresijski test za GitHub issue #18.
     *
     * Ako se isti slučaj pojavi dva puta u rezultatu (npr. korisnik je
     * istovremeno i voditelj i u TIM_NA_SLUCAJU), repo mora deduplicirati
     * po SLUCAJ_ID kako se isti slučaj ne bi prikazivao dvaput na dashboardu.
     */
    @Test
    @DisplayName("findMojiSlucajevi deduplicira slučajeve po SLUCAJ_ID - issue #18")
    void findMojiSlucajevi_Inspektor_DeduplicatesBySlucajId_Issue18() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("SLUCAJ_ID")).thenReturn(99L, 99L);
        when(resultSet.getString("BROJ_SLUCAJA")).thenReturn("SLU-099");
        when(resultSet.getString("OPIS")).thenReturn("Duplikat");
        when(resultSet.getString("STATUS")).thenReturn("Otvoren");
        when(resultSet.getString("IME_VODITELJA")).thenReturn("Inspektor X");
        when(resultSet.getString("ULOGA")).thenReturn("Voditelj");
        when(resultSet.getTimestamp("DATUM_KREIRANJA")).thenReturn(new Timestamp(0L));

        List<MojSlucajDTO> rezultat = slucajRepository.findMojiSlucajevi(7L, "Inspektor");

        assertEquals(1, rezultat.size(), "Isti slučaj ne smije se pojaviti dvaput");
        assertEquals(99L, rezultat.get(0).getSlucajId());
    }

    @Test
    @DisplayName("findMojiSlucajevi za FORENZIČAR-a koristi samo TIM_NA_SLUCAJU JOIN (nema voditelj OR)")
    void findMojiSlucajevi_Forenzicar_OnlyTeamCases() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        slucajRepository.findMojiSlucajevi(5L, "Forenzičar");

        verify(connection).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("JOIN TIM_NA_SLUCAJU t ON s.SLUCAJ_ID = t.SLUCAJ_ID"),
                "Ne-voditeljske role moraju INNER JOIN-ati TIM_NA_SLUCAJU");
        assertFalse(sql.contains("OR t.USER_ID = ?"),
                "Forenzičar branch ne smije imati OR uvjet (samo tim članstvo)");
        verify(preparedStatement, times(1)).setLong(1, 5L);
    }
}
