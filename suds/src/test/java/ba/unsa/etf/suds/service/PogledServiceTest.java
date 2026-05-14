package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.PogledLanacNadzoraPregledDTO;
import ba.unsa.etf.suds.dto.PogledSlucajPregledDTO;
import ba.unsa.etf.suds.dto.PogledTimNaSlucajuPregledDTO;
import ba.unsa.etf.suds.repository.PogledRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PogledServiceTest {

    @Mock
    private PogledRepository pogledRepository;

    @InjectMocks
    private PogledService pogledService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPreglediSlucajeva_VracaListuIzRepozitorija() {
        PogledSlucajPregledDTO red = new PogledSlucajPregledDTO();
        red.setSlucajId(1L);
        red.setBrojSlucaja("SLU-2026-001");
        red.setBrojDokaza(5L);
        List<PogledSlucajPregledDTO> ocekivano = List.of(red);
        when(pogledRepository.findSviPreglediSlucajeva()).thenReturn(ocekivano);

        List<PogledSlucajPregledDTO> rezultat = pogledService.getPreglediSlucajeva();

        assertSame(ocekivano, rezultat);
        assertEquals(1, rezultat.size());
        assertEquals("SLU-2026-001", rezultat.get(0).getBrojSlucaja());
        verify(pogledRepository, times(1)).findSviPreglediSlucajeva();
        verifyNoMoreInteractions(pogledRepository);
    }

    @Test
    void getPreglediLancaNadzora_VracaListuIzRepozitorija() {
        PogledLanacNadzoraPregledDTO red = new PogledLanacNadzoraPregledDTO();
        red.setUnosId(42L);
        red.setPotvrdaStatus("Potvrđeno");
        List<PogledLanacNadzoraPregledDTO> ocekivano = List.of(red);
        when(pogledRepository.findSviPreglediLancaNadzora()).thenReturn(ocekivano);

        List<PogledLanacNadzoraPregledDTO> rezultat = pogledService.getPreglediLancaNadzora();

        assertSame(ocekivano, rezultat);
        assertEquals(42L, rezultat.get(0).getUnosId());
        verify(pogledRepository, times(1)).findSviPreglediLancaNadzora();
        verifyNoMoreInteractions(pogledRepository);
    }

    @Test
    void getPreglediTimaNaSlucaju_VracaPraznuListuKadNemaPodataka() {
        when(pogledRepository.findSviPreglediTimaNaSlucaju()).thenReturn(Collections.emptyList());

        List<PogledTimNaSlucajuPregledDTO> rezultat = pogledService.getPreglediTimaNaSlucaju();

        assertEquals(0, rezultat.size());
        verify(pogledRepository, times(1)).findSviPreglediTimaNaSlucaju();
        verifyNoMoreInteractions(pogledRepository);
    }
}
