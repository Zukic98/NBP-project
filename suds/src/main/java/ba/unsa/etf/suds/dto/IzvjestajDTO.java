package ba.unsa.etf.suds.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Odgovor za GET /api/slucajevi/{id}/izvjestaj.
 *
 * <p>Kompletan izvještaj slučaja koji agregira sve relevantne podatke:
 * detalje slučaja, dokaze s fotografijama, lanac nadzora, tim, svjedoke,
 * osumnjičene, krivična djela i forenzičke izvještaje. Koristi se za
 * generisanje PDF izvještaja putem iText 7.
 */
public class IzvjestajDTO {

    /** Osnovni podaci o slučaju. */
    private SlucajInfo slucaj;

    /** Lista dokaza vezanih za slučaj. */
    private List<DokazInfo> dokazi;

    /** Hronološki niz svih primopredaja dokaza u slučaju. */
    private List<LanacNadzoraInfo> lanacNadzora;

    /** Članovi tima dodjeljeni slučaju. */
    private List<TimInfo> tim;

    /** Svjedoci vezani za slučaj. */
    private List<SvjedokInfo> svjedoci;

    /** Osumnjičeni vezani za slučaj. */
    private List<OsumnjiceniInfo> osumnjiceni;

    /** Krivična djela vezana za slučaj. */
    private List<KrivicnoDjeloInfo> krivicnaDjela;

    /** Forenzički izvještaji vezani za dokaze slučaja. */
    private List<ForenzickiIzvjestajInfo> forenzickiIzvjestaji;

    // Getter i Setter
    public List<ForenzickiIzvjestajInfo> getForenzickiIzvjestaji() { return forenzickiIzvjestaji; }
    public void setForenzickiIzvjestaji(List<ForenzickiIzvjestajInfo> forenzickiIzvjestaji) {
        this.forenzickiIzvjestaji = forenzickiIzvjestaji;
    }

    // Getteri i Setteri
    public SlucajInfo getSlucaj() { return slucaj; }
    public void setSlucaj(SlucajInfo slucaj) { this.slucaj = slucaj; }

    public List<DokazInfo> getDokazi() { return dokazi; }
    public void setDokazi(List<DokazInfo> dokazi) { this.dokazi = dokazi; }

    public List<LanacNadzoraInfo> getLanacNadzora() { return lanacNadzora; }
    public void setLanacNadzora(List<LanacNadzoraInfo> lanacNadzora) { this.lanacNadzora = lanacNadzora; }

    public List<TimInfo> getTim() { return tim; }
    public void setTim(List<TimInfo> tim) { this.tim = tim; }

    public List<SvjedokInfo> getSvjedoci() { return svjedoci; }
    public void setSvjedoci(List<SvjedokInfo> svjedoci) { this.svjedoci = svjedoci; }

    public List<OsumnjiceniInfo> getOsumnjiceni() { return osumnjiceni; }
    public void setOsumnjiceni(List<OsumnjiceniInfo> osumnjiceni) { this.osumnjiceni = osumnjiceni; }

    public List<KrivicnoDjeloInfo> getKrivicnaDjela() { return krivicnaDjela; }
    public void setKrivicnaDjela(List<KrivicnoDjeloInfo> krivicnaDjela) { this.krivicnaDjela = krivicnaDjela; }

    // Inner klase

    /** Sažeti podaci o slučaju za potrebe izvještaja. */
    public static class SlucajInfo {
        /** Primarni ključ slučaja. */
        private Long slucajId;

        /** Jedinstveni broj slučaja. */
        private String brojSlucaja;

        /** Tekstualni opis slučaja. */
        private String opis;

        /** Trenutni status slučaja. */
        private String status;

        /** Ime i prezime voditelja slučaja. */
        private String voditeljSlucaja;

        /** Naziv policijske stanice koja vodi slučaj. */
        private String stanica;

        /** Datum i vrijeme kreiranja slučaja. */
        private Timestamp datumKreiranja;

        // Getteri i Setteri
        public Long getSlucajId() { return slucajId; }
        public void setSlucajId(Long slucajId) { this.slucajId = slucajId; }

        public String getBrojSlucaja() { return brojSlucaja; }
        public void setBrojSlucaja(String brojSlucaja) { this.brojSlucaja = brojSlucaja; }

        public String getOpis() { return opis; }
        public void setOpis(String opis) { this.opis = opis; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getVoditeljSlucaja() { return voditeljSlucaja; }
        public void setVoditeljSlucaja(String voditeljSlucaja) { this.voditeljSlucaja = voditeljSlucaja; }

        public String getStanica() { return stanica; }
        public void setStanica(String stanica) { this.stanica = stanica; }

        public Timestamp getDatumKreiranja() { return datumKreiranja; }
        public void setDatumKreiranja(Timestamp datumKreiranja) { this.datumKreiranja = datumKreiranja; }
    }

    /** Podaci o jednom dokazu za potrebe izvještaja. */
    public static class DokazInfo {
        /** Primarni ključ dokaza. */
        private Long dokazId;

        /** Tekstualni opis dokaza. */
        private String opis;

        /** Tip dokaza. */
        private String tipDokaza;

        /** Lokacija pronalaska dokaza. */
        private String lokacijaPronalaska;

        /** Trenutni status dokaza. */
        private String status;

        /** Ime i prezime uposlenika koji je prikupio dokaz. */
        private String prikupioIme;

        /** Datum i vrijeme prikupljanja dokaza. */
        private Timestamp datumPrikupa;

        /** Lista Base64-enkodiranih fotografija dokaza. */
        private List<String> fotografije;

        // Getteri i Setteri
        public Long getDokazId() { return dokazId; }
        public void setDokazId(Long dokazId) { this.dokazId = dokazId; }

        public String getOpis() { return opis; }
        public void setOpis(String opis) { this.opis = opis; }

        public String getTipDokaza() { return tipDokaza; }
        public void setTipDokaza(String tipDokaza) { this.tipDokaza = tipDokaza; }

        public String getLokacijaPronalaska() { return lokacijaPronalaska; }
        public void setLokacijaPronalaska(String lokacijaPronalaska) { this.lokacijaPronalaska = lokacijaPronalaska; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPrikupioIme() { return prikupioIme; }
        public void setPrikupioIme(String prikupioIme) { this.prikupioIme = prikupioIme; }

        public Timestamp getDatumPrikupa() { return datumPrikupa; }
        public void setDatumPrikupa(Timestamp datumPrikupa) { this.datumPrikupa = datumPrikupa; }

        public List<String> getFotografije() { return fotografije; }
        public void setFotografije(List<String> fotografije) { this.fotografije = fotografije; }
    }

    /** Jedan unos u lancu nadzora za potrebe izvještaja. */
    public static class LanacNadzoraInfo {
        /** Primarni ključ unosa u lancu nadzora. */
        private Long unosId;

        /** Opis dokaza koji je predmet primopredaje. */
        private String dokazOpis;

        /** Datum i vrijeme primopredaje. */
        private Timestamp datumPrimopredaje;

        /** Ime i prezime uposlenika koji je predao dokaz. */
        private String predaoIme;

        /** Ime i prezime uposlenika koji je preuzeo dokaz. */
        private String preuzeoIme;

        /** Svrha primopredaje. */
        private String svrhaPrimopredaje;

        /** Status potvrde primopredaje. */
        private String potvrdaStatus;

        /** Napomena uz potvrdu primopredaje. */
        private String potvrdaNapomena;

        /** Datum i vrijeme potvrde primopredaje. */
        private Timestamp potvrdaDatum;

        /** Ime i prezime uposlenika koji je potvrdio primopredaju. */
        private String potvrdioIme;

        // Getteri i Setteri (dodaj sve)
        public Long getUnosId() { return unosId; }
        public void setUnosId(Long unosId) { this.unosId = unosId; }

        public String getDokazOpis() { return dokazOpis; }
        public void setDokazOpis(String dokazOpis) { this.dokazOpis = dokazOpis; }

        public Timestamp getDatumPrimopredaje() { return datumPrimopredaje; }
        public void setDatumPrimopredaje(Timestamp datumPrimopredaje) { this.datumPrimopredaje = datumPrimopredaje; }

        public String getPredaoIme() { return predaoIme; }
        public void setPredaoIme(String predaoIme) { this.predaoIme = predaoIme; }

        public String getPreuzeoIme() { return preuzeoIme; }
        public void setPreuzeoIme(String preuzeoIme) { this.preuzeoIme = preuzeoIme; }

        public String getSvrhaPrimopredaje() { return svrhaPrimopredaje; }
        public void setSvrhaPrimopredaje(String svrhaPrimopredaje) { this.svrhaPrimopredaje = svrhaPrimopredaje; }

        public String getPotvrdaStatus() { return potvrdaStatus; }
        public void setPotvrdaStatus(String potvrdaStatus) { this.potvrdaStatus = potvrdaStatus; }

        public String getPotvrdaNapomena() { return potvrdaNapomena; }
        public void setPotvrdaNapomena(String potvrdaNapomena) { this.potvrdaNapomena = potvrdaNapomena; }

        public Timestamp getPotvrdaDatum() { return potvrdaDatum; }
        public void setPotvrdaDatum(Timestamp potvrdaDatum) { this.potvrdaDatum = potvrdaDatum; }

        public String getPotvrdioIme() { return potvrdioIme; }
        public void setPotvrdioIme(String potvrdioIme) { this.potvrdioIme = potvrdioIme; }
    }

    /** Jedan član tima za potrebe izvještaja. */
    public static class TimInfo {
        /** Ime i prezime člana tima. */
        private String imePrezime;

        /** Naziv sistemske uloge člana tima (npr. "INSPEKTOR"). */
        private String nazivUloge;

        /** Uloga člana na ovom konkretnom slučaju. */
        private String ulogaNaSlucaju;

        /** Email adresa člana tima. */
        private String email;

        /** Broj značke člana tima. */
        private String brojZnacke;

        // Getteri i Setteri
        public String getImePrezime() { return imePrezime; }
        public void setImePrezime(String imePrezime) { this.imePrezime = imePrezime; }

        public String getNazivUloge() { return nazivUloge; }
        public void setNazivUloge(String nazivUloge) { this.nazivUloge = nazivUloge; }

        public String getUlogaNaSlucaju() { return ulogaNaSlucaju; }
        public void setUlogaNaSlucaju(String ulogaNaSlucaju) { this.ulogaNaSlucaju = ulogaNaSlucaju; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getBrojZnacke() { return brojZnacke; }
        public void setBrojZnacke(String brojZnacke) { this.brojZnacke = brojZnacke; }
    }

    /** Jedan svjedok za potrebe izvještaja. */
    public static class SvjedokInfo {
        /** Ime i prezime svjedoka. */
        private String imePrezime;

        /** Jedinstveni matični broj građana svjedoka. */
        private String jmbg;

        /** Adresa stanovanja svjedoka. */
        private String adresa;

        /** Kontakt telefon svjedoka. */
        private String kontaktTelefon;

        /** Bilješka o svjedoku ili iskazu. */
        private String biljeska;

        // Getteri i Setteri
        public String getImePrezime() { return imePrezime; }
        public void setImePrezime(String imePrezime) { this.imePrezime = imePrezime; }

        public String getJmbg() { return jmbg; }
        public void setJmbg(String jmbg) { this.jmbg = jmbg; }

        public String getAdresa() { return adresa; }
        public void setAdresa(String adresa) { this.adresa = adresa; }

        public String getKontaktTelefon() { return kontaktTelefon; }
        public void setKontaktTelefon(String kontaktTelefon) { this.kontaktTelefon = kontaktTelefon; }

        public String getBiljeska() { return biljeska; }
        public void setBiljeska(String biljeska) { this.biljeska = biljeska; }
    }

    /** Jedan osumnjičeni za potrebe izvještaja. */
    public static class OsumnjiceniInfo {
        /** Primarni ključ osumnjičenog. */
        private Long osumnjiceniId;

        /** Ime i prezime osumnjičenog. */
        private String imePrezime;

        /** Jedinstveni matični broj građana osumnjičenog. */
        private String jmbg;

        /** Datum rođenja osumnjičenog. */
        private Date datumRodjenja;

        /** Adresa stanovanja osumnjičenog. */
        private String adresa;

        /** Lista Base64-enkodiranih fotografija osumnjičenog. */
        private List<String> fotografije;

        // Getteri i Setteri
        public Long getOsumnjiceniId() { return osumnjiceniId; }
        public void setOsumnjiceniId(Long osumnjiceniId) { this.osumnjiceniId = osumnjiceniId; }

        public String getImePrezime() { return imePrezime; }
        public void setImePrezime(String imePrezime) { this.imePrezime = imePrezime; }

        public String getJmbg() { return jmbg; }
        public void setJmbg(String jmbg) { this.jmbg = jmbg; }

        public Date getDatumRodjenja() { return datumRodjenja; }
        public void setDatumRodjenja(Date datumRodjenja) { this.datumRodjenja = datumRodjenja; }

        public String getAdresa() { return adresa; }
        public void setAdresa(String adresa) { this.adresa = adresa; }

        public List<String> getFotografije() { return fotografije; }
        public void setFotografije(List<String> fotografije) { this.fotografije = fotografije; }
    }

    /** Jedno krivično djelo za potrebe izvještaja. */
    public static class KrivicnoDjeloInfo {
        /** Naziv krivičnog djela. */
        private String naziv;

        /** Kategorija krivičnog djela. */
        private String kategorija;

        /** Član kaznenog zakona koji reguliše ovo djelo. */
        private String kazneniZakonClan;

        // Getteri i Setteri
        public String getNaziv() { return naziv; }
        public void setNaziv(String naziv) { this.naziv = naziv; }

        public String getKategorija() { return kategorija; }
        public void setKategorija(String kategorija) { this.kategorija = kategorija; }

        public String getKazneniZakonClan() { return kazneniZakonClan; }
        public void setKazneniZakonClan(String kazneniZakonClan) { this.kazneniZakonClan = kazneniZakonClan; }
    }

    /** Jedan forenzički izvještaj za potrebe izvještaja slučaja. */
    public static class ForenzickiIzvjestajInfo {
        /** Primarni ključ forenzičkog izvještaja. */
        private Long izvjestajId;

        /** ID dokaza na koji se izvještaj odnosi. */
        private Long dokazId;

        /** Opis dokaza na koji se izvještaj odnosi. */
        private String dokazOpis;

        /** Sadržaj forenzičkog izvještaja. */
        private String sadrzaj;

        /** Zaključak forenzičara. */
        private String zakljucak;

        /** Datum i vrijeme kreiranja izvještaja. */
        private java.sql.Timestamp datumKreiranja;

        /** Ime i prezime forenzičara koji je kreirao izvještaj. */
        private String kreatorIme;

        // Getteri i Setteri
        public Long getIzvjestajId() { return izvjestajId; }
        public void setIzvjestajId(Long izvjestajId) { this.izvjestajId = izvjestajId; }

        public Long getDokazId() { return dokazId; }
        public void setDokazId(Long dokazId) { this.dokazId = dokazId; }

        public String getDokazOpis() { return dokazOpis; }
        public void setDokazOpis(String dokazOpis) { this.dokazOpis = dokazOpis; }

        public String getSadrzaj() { return sadrzaj; }
        public void setSadrzaj(String sadrzaj) { this.sadrzaj = sadrzaj; }

        public String getZakljucak() { return zakljucak; }
        public void setZakljucak(String zakljucak) { this.zakljucak = zakljucak; }

        public java.sql.Timestamp getDatumKreiranja() { return datumKreiranja; }
        public void setDatumKreiranja(java.sql.Timestamp datumKreiranja) { this.datumKreiranja = datumKreiranja; }

        public String getKreatorIme() { return kreatorIme; }
        public void setKreatorIme(String kreatorIme) { this.kreatorIme = kreatorIme; }
    }
}