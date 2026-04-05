package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.model.KrivicnoDjelo;
import ba.unsa.etf.suds.repository.KrivicnoDjeloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KrivicnoDjeloServiceTest {

    @Mock // Pravimo "lažni" repozitorij koji ne gađa Oracle bazu
    private KrivicnoDjeloRepository repository;

    @InjectMocks // Ubacujemo lažni repozitorij u pravi servis
    private KrivicnoDjeloService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll_VracaListuDjela() {
        // 1. Arrange (Pripremi lažne podatke)
        // Dodali smo kategorije: "Imovinski delikti"
        KrivicnoDjelo djelo1 = new KrivicnoDjelo(1L, "Teška krađa", "Imovinski delikti", "Član 287");
        KrivicnoDjelo djelo2 = new KrivicnoDjelo(2L, "Iznuđivanje", "Imovinski delikti", "Član 295");

        // Kažemo: Kada servis pozove repository.findAll(), nemoj ići u bazu, već mu vrati ovu listu iznad!
        when(repository.findAll()).thenReturn(Arrays.asList(djelo1, djelo2));

        // 2. Act (Izvrši akciju u pravom servisu)
        List<KrivicnoDjelo> rezultat = service.getAll();

        // 3. Assert (Provjeri da li je servis uradio dobar posao)
        assertEquals(2, rezultat.size());
        assertEquals("Teška krađa", rezultat.get(0).getNaziv());

        // Provjeravamo da li je servis tačno 1 put pozvao repository (ni manje ni više)
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetById_KadaNePostoji_BacaIzuzetak() {
        // Kažemo: Kada neko traži ID 99, vrati prazno (kao da ne postoji u bazi)
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Očekujemo da će servis baciti RuntimeException sa našom porukom
        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.getById(99L);
        });

        assertEquals("Krivično djelo nije pronađeno!", exception.getMessage());
    }
}