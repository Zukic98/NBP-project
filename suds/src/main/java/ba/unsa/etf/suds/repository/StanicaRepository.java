package ba.unsa.etf.suds.repository;

import ba.unsa.etf.suds.config.DatabaseManager;
import ba.unsa.etf.suds.dto.RegistrationRequest;
import ba.unsa.etf.suds.model.Stanica;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij za upravljanje policijskim stanicama iz tabele {@code STANICE}.
 * Koristi čisti JDBC pristup — konekcije se dohvataju putem {@link ba.unsa.etf.suds.config.DatabaseManager#getConnection()}
 * i zatvaraju automatski putem try-with-resources. SQL greške se omotavaju u {@link RuntimeException}.
 */
@Repository
public class StanicaRepository {
    private final DatabaseManager dbManager;

    /** Konstruktorska injekcija {@link DatabaseManager}-a. */
    public StanicaRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Dohvata sve policijske stanice iz tabele {@code STANICE}.
     *
     * @return lista svih stanica
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public List<Stanica> findAll() {
        List<Stanica> stanice = new ArrayList<>();
        String sql = "SELECT * FROM STANICE";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stanice.add(new Stanica(
                        rs.getLong("STANICA_ID"),
                        rs.getString("IME_STANICE"),
                        rs.getLong("ADRESA_ID"),
                        rs.getTimestamp("DATUM_KREIRANJA")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju stanica", e);
        }
        return stanice;
    }

    /**
     * Dohvata stanicu prema identifikatoru.
     *
     * @param id identifikator stanice
     * @return {@link Optional} koji sadrži stanicu, ili prazan ako nije pronađena
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public Optional<Stanica> findById(Long id) {
        String sql = "SELECT * FROM STANICE WHERE STANICA_ID = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Stanica(
                            rs.getLong("STANICA_ID"),
                            rs.getString("IME_STANICE"),
                            rs.getLong("ADRESA_ID"),
                            rs.getTimestamp("DATUM_KREIRANJA")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju stanice po ID-u", e);
        }
        return Optional.empty();
    }

    /**
     * Atomično registruje novu stanicu zajedno s adresom i šefom stanice u jednoj transakciji.
     * Kreira redove u tabelama {@code ADRESE}, {@code STANICE}, {@code nbp.NBP_USER} i {@code UPOSLENIK_PROFIL}.
     * U slučaju greške transakcija se poništava (rollback).
     *
     * @param req       zahtjev za registraciju koji sadrži podatke o stanici, adresi i šefu
     * @param roleIdSef identifikator uloge šefa stanice
     * @throws RuntimeException ako dođe do greške pri izvršavanju SQL upita
     */
    public void registerStanicaITips(RegistrationRequest req, Long roleIdSef) {
    Connection conn = null;
    try {
        conn = dbManager.getConnection();
        conn.setAutoCommit(false); 

        Long generisanAdresaId;
        String sqlAdresa = "INSERT INTO ADRESE (ULICA_I_BROJ, GRAD, POSTANSKI_BROJ, DRZAVA) VALUES (?, ?, ?, 'Bosna i Hercegovina')";
        
        try (PreparedStatement ps = conn.prepareStatement(sqlAdresa, new String[]{"ADRESA_ID"})) {
            ps.setString(1, req.getUlicaIBroj());
            ps.setString(2, req.getGrad());
            ps.setString(3, req.getPostanskiBroj());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generisanAdresaId = rs.getLong(1);
            } else {
                throw new SQLException("Kreiranje adrese nije uspjelo, ADRESA_ID nije generisan.");
            }
        }

        String sqlStanica = "INSERT INTO STANICE (IME_STANICE, ADRESA_ID, DATUM_KREIRANJA) VALUES (?, ?, CURRENT_TIMESTAMP)";
        Long stanicaId;
        try (PreparedStatement ps = conn.prepareStatement(sqlStanica, new String[]{"STANICA_ID"})) {
            ps.setString(1, req.getImeStanice());
            ps.setLong(2, generisanAdresaId); 
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                stanicaId = rs.getLong(1);
            } else {
                throw new SQLException("Kreiranje stanice nije uspjelo.");
            }
        }

        String sqlUser = "INSERT INTO nbp.NBP_USER (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, USERNAME, ROLE_ID) VALUES (?, ?, ?, ?, ?, ?)";
        Long userId;
        try (PreparedStatement ps = conn.prepareStatement(sqlUser, new String[]{"ID"})) {
            ps.setString(1, req.getFirstName());
            ps.setString(2, req.getLastName());
            ps.setString(3, req.getEmail());
            ps.setString(4, req.getPassword()); 
            ps.setString(5, req.getUsername());
            ps.setLong(6, roleIdSef);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                userId = rs.getLong(1);
            } else {
                throw new SQLException("Kreiranje korisnika nije uspjelo.");
            }
        }

        String sqlProfil = "INSERT INTO UPOSLENIK_PROFIL (USER_ID, STANICA_ID, BROJ_ZNACKE, STATUS) VALUES (?, ?, ?, 'Aktivan')";
        try (PreparedStatement ps = conn.prepareStatement(sqlProfil)) {
            ps.setLong(1, userId);
            ps.setLong(2, stanicaId);
            ps.setString(3, req.getBrojZnacke());
            ps.executeUpdate();
        }

        conn.commit(); 
        System.out.println("Uspješno registrovano sve: Adresa ID " + generisanAdresaId + ", Stanica ID " + stanicaId);

    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        throw new RuntimeException("Greška u bazi: " + e.getMessage());
    } finally {
        if (conn != null) {
            try { 
                conn.setAutoCommit(true); 
                conn.close(); 
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
}
