package ba.unsa.etf.suds.service;

import ba.unsa.etf.suds.dto.IzvjestajDTO;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * Servis za generisanje PDF izvještaja o slučajevima.
 *
 * <p>Koristi iText 7 biblioteku za kreiranje strukturiranih PDF dokumenata.
 * Font {@code DejaVuSans.ttf} učitava se s {@code classpath:fonts/} kako bi
 * se ispravno prikazali bosanski dijakritički znakovi. Servis nema zavisnosti
 * prema repozitorijima — prima gotov {@link IzvjestajDTO} i vraća sirove
 * bajtove PDF dokumenta.
 */
@Service
public class PdfGeneratorService {

    /**
     * Generiše PDF izvještaj za dati slučaj.
     *
     * <p>Dokument sadrži sljedeće sekcije (ako postoje podaci):
     * osnovni podaci o slučaju, krivična djela, tim na slučaju, osumnjičeni
     * (s fotografijama, max 3 po osobi), svjedoci, forenzički izvještaji,
     * dokazi (s fotografijama, max 5 po dokazu), lanac nadzora i footer
     * s datumom generisanja i imenom korisnika.
     *
     * @param izvjestaj    DTO koji sadrži sve sekcije izvještaja
     * @param generisaoIme puno ime korisnika koji generiše izvještaj (za footer)
     * @return niz bajtova koji predstavlja generisani PDF dokument
     * @throws RuntimeException ako dođe do greške pri generisanju PDF-a
     */
    public byte[] generatePdf(IzvjestajDTO izvjestaj, String generisaoIme) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Učitaj font sa classpath-a
            ClassPathResource fontResource = new ClassPathResource("fonts/DejaVuSans.ttf");
            InputStream fontStream = fontResource.getInputStream();
            byte[] fontBytes = fontStream.readAllBytes();
            fontStream.close();

            PdfFont font = PdfFontFactory.createFont(fontBytes, "Identity-H", PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            PdfFont boldFont = font; // Koristimo isti font, DejaVuSans je dovoljno dobar

            // Naslov
            Paragraph naslov = new Paragraph("IZVJEŠTAJ O SLUČAJU")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setMarginBottom(10);
            document.add(naslov);

            // Broj slučaja
            Paragraph brojSlucaja = new Paragraph("Broj slučaja: " + izvjestaj.getSlucaj().getBrojSlucaja())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(font)
                    .setFontSize(14)
                    .setMarginBottom(20);
            document.add(brojSlucaja);

            // Sekcija: Osnovni podaci
            document.add(createSectionHeader("OSNOVNI PODACI", boldFont));
            document.add(createInfoTable(izvjestaj.getSlucaj(), font, boldFont));

            // Sekcija: Krivična djela
            if (izvjestaj.getKrivicnaDjela() != null && !izvjestaj.getKrivicnaDjela().isEmpty()) {
                document.add(createSectionHeader("KRIVIČNA DJELA", boldFont));
                for (IzvjestajDTO.KrivicnoDjeloInfo djelo : izvjestaj.getKrivicnaDjela()) {
                    document.add(new Paragraph("• " + djelo.getNaziv() +
                            " (Član " + djelo.getKazneniZakonClan() + ", " + djelo.getKategorija() + ")")
                            .setFont(font)
                            .setFontSize(10));
                }
            }

            // Sekcija: Tim na slučaju
            if (izvjestaj.getTim() != null && !izvjestaj.getTim().isEmpty()) {
                document.add(createSectionHeader("TIM NA SLUČAJU", boldFont));
                for (IzvjestajDTO.TimInfo clan : izvjestaj.getTim()) {
                    document.add(new Paragraph("• " + clan.getImePrezime() +
                            " - " + clan.getUlogaNaSlucaju() +
                            " | " + clan.getNazivUloge() +
                            (clan.getEmail() != null ? " | " + clan.getEmail() : "") +
                            (clan.getBrojZnacke() != null ? " | Značka: " + clan.getBrojZnacke() : ""))
                            .setFont(font)
                            .setFontSize(10));
                }
            }

            // Sekcija: Osumnjičeni
            if (izvjestaj.getOsumnjiceni() != null && !izvjestaj.getOsumnjiceni().isEmpty()) {
                document.add(createSectionHeader("OSUMNJIČENI", boldFont));
                for (IzvjestajDTO.OsumnjiceniInfo os : izvjestaj.getOsumnjiceni()) {
                    document.add(new Paragraph("• " + os.getImePrezime() +
                            (os.getJmbg() != null ? " | JMBG: " + os.getJmbg() : "") +
                            (os.getDatumRodjenja() != null ? " | Rođen: " + new SimpleDateFormat("dd.MM.yyyy").format(os.getDatumRodjenja()) : "") +
                            (os.getAdresa() != null ? " | " + os.getAdresa() : ""))
                            .setFont(font)
                            .setFontSize(10));

                    // Dodaj fotografije osumnjičenog
                    if (os.getFotografije() != null && !os.getFotografije().isEmpty()) {
                        for (int i = 0; i < Math.min(3, os.getFotografije().size()); i++) {
                            try {
                                byte[] imgBytes = Base64.getDecoder().decode(os.getFotografije().get(i));
                                Image img = new Image(ImageDataFactory.create(imgBytes))
                                        .setWidth(100)
                                        .setHeight(100)
                                        .setMarginLeft(10);
                                document.add(img);
                            } catch (Exception e) {
                                document.add(new Paragraph("  [Fotografija nije dostupna]")
                                        .setFont(font)
                                        .setFontSize(8));
                            }
                        }
                    }
                }
            }

            // Sekcija: Svjedoci
            if (izvjestaj.getSvjedoci() != null && !izvjestaj.getSvjedoci().isEmpty()) {
                document.add(createSectionHeader("SVJEDOCI", boldFont));
                for (IzvjestajDTO.SvjedokInfo svjedok : izvjestaj.getSvjedoci()) {
                    document.add(new Paragraph("• " + svjedok.getImePrezime() +
                            (svjedok.getJmbg() != null ? " | JMBG: " + svjedok.getJmbg() : "") +
                            (svjedok.getAdresa() != null ? " | " + svjedok.getAdresa() : "") +
                            (svjedok.getKontaktTelefon() != null ? " | Tel: " + svjedok.getKontaktTelefon() : "") +
                            (svjedok.getBiljeska() != null ? " | Bilješka: " + svjedok.getBiljeska() : ""))
                            .setFont(font)
                            .setFontSize(10));
                }
            }

            // Sekcija: Forenzički izvještaji
            if (izvjestaj.getForenzickiIzvjestaji() != null && !izvjestaj.getForenzickiIzvjestaji().isEmpty()) {
                document.add(createSectionHeader("FORENZIČKI IZVJEŠTAJI", boldFont));
                for (IzvjestajDTO.ForenzickiIzvjestajInfo fi : izvjestaj.getForenzickiIzvjestaji()) {
                    document.add(new Paragraph("• Vezano za dokaz: " + fi.getDokazOpis())
                            .setFont(boldFont)
                            .setFontSize(10));

                    if (fi.getSadrzaj() != null && !fi.getSadrzaj().trim().isEmpty()) {
                        document.add(new Paragraph("    Sadržaj: " + fi.getSadrzaj())
                                .setFont(font)
                                .setFontSize(9));
                    }

                    if (fi.getZakljucak() != null && !fi.getZakljucak().trim().isEmpty()) {
                        document.add(new Paragraph("    Zaključak: " + fi.getZakljucak())
                                .setFont(boldFont)
                                .setFontSize(9));
                    }

                    document.add(new Paragraph("    Kreirao: " + fi.getKreatorIme() +
                            " | Datum: " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(fi.getDatumKreiranja()))
                            .setFont(font)
                            .setFontSize(8)
                            .setMarginBottom(5));
                }
            }

            // Sekcija: Dokazi
            if (izvjestaj.getDokazi() != null && !izvjestaj.getDokazi().isEmpty()) {
                document.add(createSectionHeader("DOKAZI", boldFont));
                for (IzvjestajDTO.DokazInfo dokaz : izvjestaj.getDokazi()) {
                    document.add(new Paragraph("• " + dokaz.getOpis())
                            .setFont(boldFont)
                            .setFontSize(10));
                    document.add(new Paragraph("    Tip: " + dokaz.getTipDokaza() +
                            " | Lokacija: " + dokaz.getLokacijaPronalaska() +
                            " | Status: " + dokaz.getStatus() +
                            " | Prikupio: " + (dokaz.getPrikupioIme() != null ? dokaz.getPrikupioIme() : "N/A"))
                            .setFont(font)
                            .setFontSize(9));

                    // Dodaj fotografije dokaza
                    if (dokaz.getFotografije() != null && !dokaz.getFotografije().isEmpty()) {
                        for (int i = 0; i < Math.min(5, dokaz.getFotografije().size()); i++) {
                            try {
                                byte[] imgBytes = Base64.getDecoder().decode(dokaz.getFotografije().get(i));
                                Image img = new Image(ImageDataFactory.create(imgBytes))
                                        .setWidth(100)
                                        .setHeight(100)
                                        .setMarginLeft(10);
                                document.add(img);
                            } catch (Exception e) {
                                document.add(new Paragraph("    [Fotografija nije dostupna]")
                                        .setFont(font)
                                        .setFontSize(8));
                            }
                        }
                    }
                }
            }

            // Sekcija: Lanac nadzora
            if (izvjestaj.getLanacNadzora() != null && !izvjestaj.getLanacNadzora().isEmpty()) {
                document.add(createSectionHeader("LANAC NADZORA", boldFont));
                for (IzvjestajDTO.LanacNadzoraInfo unos : izvjestaj.getLanacNadzora()) {
                    document.add(new Paragraph("• " + unos.getDokazOpis())
                            .setFont(boldFont)
                            .setFontSize(10));
                    document.add(new Paragraph("    Predao: " + (unos.getPredaoIme() != null ? unos.getPredaoIme() : "N/A") +
                            " | Preuzeo: " + (unos.getPreuzeoIme() != null ? unos.getPreuzeoIme() : "N/A") +
                            " | Datum: " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(unos.getDatumPrimopredaje()) +
                            " | Status: " + unos.getPotvrdaStatus() +
                            (unos.getSvrhaPrimopredaje() != null ? "\n    Svrha: " + unos.getSvrhaPrimopredaje() : ""))
                            .setFont(font)
                            .setFontSize(9));
                }
            }

            // Footer
            Paragraph footer = new Paragraph("\n\nDokument generisan: " +
                    new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) +
                    "\nGenerisao: " + generisaoIme)
                    .setFont(font)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Greška pri generisanju PDF-a: " + e.getMessage(), e);
        }
    }

    private Paragraph createSectionHeader(String title, PdfFont boldFont) {
        return new Paragraph(title)
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(5);
    }

    private Table createInfoTable(IzvjestajDTO.SlucajInfo slucaj, PdfFont font, PdfFont boldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth();

        addTableRow(table, "Status:", slucaj.getStatus(), font, boldFont);
        addTableRow(table, "Stanica:", slucaj.getStanica(), font, boldFont);
        addTableRow(table, "Voditelj:", slucaj.getVoditeljSlucaja(), font, boldFont);
        addTableRow(table, "Opis:", slucaj.getOpis() != null ? slucaj.getOpis() : "N/A", font, boldFont);
        addTableRow(table, "Datum kreiranja:", new SimpleDateFormat("dd.MM.yyyy").format(slucaj.getDatumKreiranja()), font, boldFont);

        return table;
    }

    private void addTableRow(Table table, String label, String value, PdfFont font, PdfFont boldFont) {
        table.addCell(new Paragraph(label).setFont(boldFont).setFontSize(10));
        table.addCell(new Paragraph(value).setFont(font).setFontSize(10));
    }
}