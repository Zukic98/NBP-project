package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.UposlenikDTO;
import ba.unsa.etf.suds.dto.UposlenikLoginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UposlenikRepositoryTest {

    @Mock
    private DatabaseManager dbManager;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private UposlenikRepository uposlenikRepository;

    @BeforeEach
    void setUp() throws SQLException {
        when(dbManager.getConnection()).thenReturn(connection);
    }

    @Test
    @DisplayName("findByEmailAndZnacka treba ispravno mapirati red iz baze u UposlenikLoginDTO")
    void findByEmailAndZnacka_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("USER_ID")).thenReturn(1L);
        when(resultSet.getString("FIRST_NAME")).thenReturn("Mujo");
        when(resultSet.getString("LAST_NAME")).thenReturn("Mujić");
        when(resultSet.getString("EMAIL")).thenReturn("mujo@etf.unsa.ba");
        when(resultSet.getString("PASSWORD")).thenReturn("hash_lozinka");
        when(resultSet.getString("ULOGA")).thenReturn("INSPEKTOR");
        when(resultSet.getString("BROJ_ZNACKE")).thenReturn("ZN123");
        when(resultSet.getString("STATUS")).thenReturn("Aktivan");
        when(resultSet.getLong("STANICA_ID")).thenReturn(10L);

        Optional<UposlenikLoginDTO> result = uposlenikRepository.findByEmailAndZnacka("mujo@etf.unsa.ba", "ZN123");

        assertTrue(result.isPresent());
        assertEquals("Mujo", result.get().getIme());
        assertEquals("INSPEKTOR", result.get().getUloga());
        
        verify(preparedStatement).setString(1, "mujo@etf.unsa.ba");
        verify(preparedStatement).setString(2, "ZN123");
    }

    @Test
    @DisplayName("jeLiTokenUcrnojListi treba vratiti true ako count > 0")
    void jeLiTokenUcrnojListi_VratiTrue() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); 

        boolean postoji = uposlenikRepository.jeLiTokenUcrnojListi("neki-token");

        assertTrue(postoji);
        verify(preparedStatement).setString(1, "neki-token");
    }

    @Test
    @DisplayName("updateStatus treba pozvati executeUpdate")
    void updateStatus_PozivaExecuteUpdate() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        uposlenikRepository.updateStatus(1L, "Penzionisan");

        verify(preparedStatement).setString(1, "Penzionisan");
        verify(preparedStatement).setLong(2, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("existsByEmail treba vratiti false ako baza vrati 0")
    void existsByEmail_FalseKadaNemaKorisnika() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0); 

        boolean exists = uposlenikRepository.existsByEmail("nepostojeci@test.com");

        assertFalse(exists);
    }
}