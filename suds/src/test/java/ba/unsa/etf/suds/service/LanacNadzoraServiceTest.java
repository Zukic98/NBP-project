package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.PosaljiDokazRequest;
import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.repository.DokazRepository;
import ba.unsa.etf.suds.repository.LanacNadzoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        request.setDokazId(1L);
        request.setPrimaocUserId(2L);
        request.setStanicaId(3L);
        request.setSvrhaPrimopredaje("Forenzička analiza");

        Long predaoUserId = 10L;

        when(lanacRepository.save(any(LanacNadzora.class))).thenAnswer(invocation -> {
            LanacNadzora lanac = invocation.getArgument(0);
            lanac.setUnosId(100L);
            return lanac;
        });

        LanacNadzora result = service.posaljiDokaz(request, predaoUserId);

        verify(dokazRepository).updateStatus(1L, "Čeka potvrdu");

        ArgumentCaptor<LanacNadzora> captor = ArgumentCaptor.forClass(LanacNadzora.class);
        verify(lanacRepository).save(captor.capture());
        LanacNadzora saved = captor.getValue();

        assertEquals("Čeka potvrdu", saved.getPotvrdaStatus());
        assertEquals(1L, saved.getDokazId());
        assertEquals(2L, saved.getPreuzeoUserId());
        assertEquals(10L, saved.getPredaoUserId());
    }

    @Test
    void prihvatiDokaz_ValidRequest_UpdatesStatusToPotvrdjeno() {
        Long unosId = 100L;
        Long potvrdioUserId = 2L;

        LanacNadzora existingLanac = new LanacNadzora();
        existingLanac.setUnosId(unosId);
        existingLanac.setDokazId(1L);
        existingLanac.setPreuzeoUserId(potvrdioUserId);
        existingLanac.setPotvrdaStatus("Čeka potvrdu");

        when(lanacRepository.findById(unosId)).thenReturn(Optional.of(existingLanac));

        service.prihvatiDokaz(unosId, potvrdioUserId);

        verify(lanacRepository).prihvati(unosId, potvrdioUserId);
        verify(dokazRepository).updateStatus(1L, "U posjedu");
    }

    @Test
    void prihvatiDokaz_WrongUser_ThrowsException() {
        Long unosId = 100L;
        Long wrongUserId = 999L;

        LanacNadzora existingLanac = new LanacNadzora();
        existingLanac.setUnosId(unosId);
        existingLanac.setDokazId(1L);
        existingLanac.setPreuzeoUserId(2L);
        existingLanac.setPotvrdaStatus("Čeka potvrdu");

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
        existingLanac.setUnosId(unosId);
        existingLanac.setDokazId(1L);
        existingLanac.setPreuzeoUserId(potvrdioUserId);
        existingLanac.setPotvrdaStatus("Potvrđeno");

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
}
