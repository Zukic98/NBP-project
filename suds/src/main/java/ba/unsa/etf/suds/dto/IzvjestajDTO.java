package ba.unsa.etf.suds.dto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class IzvjestajDTO {

    private SlucajInfo slucaj;
    private List<DokazInfo> dokazi;
    private List<LanacNadzoraInfo> lanacNadzora;
    private List<TimInfo> tim;
    private List<SvjedokInfo> svjedoci;
    private List<OsumnjiceniInfo> osumnjiceni;
    private List<KrivicnoDjeloInfo> krivicnaDjela;
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
    public static class SlucajInfo {
        private Long slucajId;
        private String brojSlucaja;
        private String opis;
        private String status;
        private String voditeljSlucaja;
        private String stanica;
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

    public static class DokazInfo {
        private Long dokazId;
        private String opis;
        private String tipDokaza;
        private String lokacijaPronalaska;
        private String status;
        private String prikupioIme;
        private Timestamp datumPrikupa;
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

    public static class LanacNadzoraInfo {
        private Long unosId;
        private String dokazOpis;
        private Timestamp datumPrimopredaje;
        private String predaoIme;
        private String preuzeoIme;
        private String svrhaPrimopredaje;
        private String potvrdaStatus;
        private String potvrdaNapomena;
        private Timestamp potvrdaDatum;
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

    public static class TimInfo {
        private String imePrezime;
        private String nazivUloge;
        private String ulogaNaSlucaju;
        private String email;
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

    public static class SvjedokInfo {
        private String imePrezime;
        private String jmbg;
        private String adresa;
        private String kontaktTelefon;
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

    public static class OsumnjiceniInfo {
        private Long osumnjiceniId;
        private String imePrezime;
        private String jmbg;
        private Date datumRodjenja;
        private String adresa;
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

    public static class KrivicnoDjeloInfo {
        private String naziv;
        private String kategorija;
        private String kazneniZakonClan;

        // Getteri i Setteri
        public String getNaziv() { return naziv; }
        public void setNaziv(String naziv) { this.naziv = naziv; }

        public String getKategorija() { return kategorija; }
        public void setKategorija(String kategorija) { this.kategorija = kategorija; }

        public String getKazneniZakonClan() { return kazneniZakonClan; }
        public void setKazneniZakonClan(String kazneniZakonClan) { this.kazneniZakonClan = kazneniZakonClan; }
    }

    public static class ForenzickiIzvjestajInfo {
        private Long izvjestajId;
        private Long dokazId;
        private String dokazOpis;
        private String sadrzaj;
        private String zakljucak;
        private java.sql.Timestamp datumKreiranja;
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