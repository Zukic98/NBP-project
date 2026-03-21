package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.Adresa;
import ba.unsa.etf.suds.repository.AdresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdresaServiceTest {

    @Mock // "Lažiramo" konekciju ka bazi
    private AdresaRepository adresaRepository;

    @InjectMocks // Ubacujemo lažni repozitorij u pravi servis
    private AdresaService adresaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAdrese() {
        // 1. Arrange: Priprema lažnih podataka (tačno po kolonama iz tvoje baze)
        Adresa adresa1 = new Adresa(1L, "Zmaja od Bosne bb", "Sarajevo", "71000", "BiH");
        Adresa adresa2 = new Adresa(2L, "Titova 1", "Sarajevo", "71000", "BiH");

        when(adresaRepository.findAll()).thenReturn(Arrays.asList(adresa1, adresa2));

        // 2. Act: Pozivamo metodu servisa
        List<Adresa> rezultat = adresaService.getAllAdrese();

        // 3. Assert: Provjeravamo rezultate
        assertEquals(2, rezultat.size());
        assertEquals("Zmaja od Bosne bb", rezultat.get(0).getUlicaIBroj());

        // Provjeri da li je metoda pozvana tačno jednom
        verify(adresaRepository, times(1)).findAll();
    }

    @Test
    void testCreateAdresa_PraznaUlica_BacaIzuzetak() {
        // Pokušavamo napraviti adresu bez ulice
        Adresa losaAdresa = new Adresa(null, "", "Sarajevo", "71000", "BiH");

        // Očekujemo da će Service baciti IllegalArgumentException koji smo definisali u AdresaService
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            adresaService.createAdresa(losaAdresa);
        });

        assertEquals("Ulica i broj su obavezni!", exception.getMessage());

        // Provjeravamo da repozitorij NIJE pozvan (jer je validacija pala)
        verify(adresaRepository, never()).save(any(Adresa.class));
    }
}