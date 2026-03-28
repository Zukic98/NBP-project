package ba.unsa.etf.suds.dto;

public class InspektorDTO {
    private Long id;
    private String ime;
    private String prezime;
    private String email;
    private String brojZnacke;
    private String nazivStanice;

    public InspektorDTO(Long id, String ime, String prezime, String email, String brojZnacke, String nazivStanice) {
        this.id = id;
        this.ime = ime;
        this.prezime = prezime;
        this.email = email;
        this.brojZnacke = brojZnacke;
        this.nazivStanice = nazivStanice;
    }

    public Long getId() { return id; }
    public String getIme() { return ime; }
    public String getPrezime() { return prezime; }
    public String getEmail() { return email; }
    public String getBrojZnacke() { return brojZnacke; }
    public String getNazivStanice() { return nazivStanice; }
}