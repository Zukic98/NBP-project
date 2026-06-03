--------------------------------------------------------------------------------
-- SUDS — Pogledi (Oracle 19c)
--
-- Tri složenija pogleda definisana za potrebe taska #32. Svaki pogled spaja
-- ≥ 3 tabele i kreiran je sa direktivom WITH READ ONLY, čime je pogled na
-- nivou kataloga zaključan za sve DML operacije (INSERT/UPDATE/DELETE kroz
-- pogled bacaju ORA-42399). To je tražena "opciona mogućnost zaključavanja"
-- iz opisa taska i ujedno štiti append-only domene poput LANAC_NADZORA.
--
-- DDL u ovom repozitoriju se NE migrira automatski. Pokrenuti ručno na
-- ETF Oracle serveru (npr. SQL*Plus, SQLDeveloper ili `mvn` task po izboru)
-- pod schema-vlasnikom (NBPT5):
--
--   @suds/src/main/resources/db/pogledi.sql
--
-- Za rollback su pripremljene DROP naredbe na dnu fajla.
--------------------------------------------------------------------------------

-- =============================================================================
-- 1) POGLED_SLUCAJ_PREGLED
--    Sažeti pregled svakog slučaja sa imenom stanice, voditeljem i
--    agregatnim metrikama (broj dokaza, osumnjičenih, svjedoka, članova tima).
--    Tabele: SLUCAJEVI, STANICE, nbp.NBP_USER + skalarni pod-upiti nad
--            DOKAZI, SLUCAJ_OSUMNJICENI, SVJEDOCI, TIM_NA_SLUCAJU.
-- =============================================================================
CREATE OR REPLACE VIEW POGLED_SLUCAJ_PREGLED AS
SELECT
    s.SLUCAJ_ID                                                       AS SLUCAJ_ID,
    s.BROJ_SLUCAJA                                                    AS BROJ_SLUCAJA,
    s.OPIS                                                            AS OPIS,
    s.STATUS                                                          AS STATUS,
    s.DATUM_KREIRANJA                                                 AS DATUM_KREIRANJA,
    st.STANICA_ID                                                     AS STANICA_ID,
    st.IME_STANICE                                                    AS IME_STANICE,
    s.VODITELJ_USER_ID                                                AS VODITELJ_USER_ID,
    (u.FIRST_NAME || ' ' || u.LAST_NAME)                              AS VODITELJ_IME,
    (SELECT COUNT(*) FROM DOKAZI d
        WHERE d.SLUCAJ_ID = s.SLUCAJ_ID)                              AS BROJ_DOKAZA,
    (SELECT COUNT(*) FROM SLUCAJ_OSUMNJICENI so
        WHERE so.SLUCAJ_ID = s.SLUCAJ_ID)                             AS BROJ_OSUMNJICENIH,
    (SELECT COUNT(*) FROM SVJEDOCI sv
        WHERE sv.SLUCAJ_ID = s.SLUCAJ_ID)                             AS BROJ_SVJEDOKA,
    (SELECT COUNT(*) FROM TIM_NA_SLUCAJU t
        WHERE t.SLUCAJ_ID = s.SLUCAJ_ID)                              AS BROJ_CLANOVA_TIMA
FROM SLUCAJEVI s
JOIN STANICE st        ON st.STANICA_ID = s.STANICA_ID
JOIN nbp.NBP_USER u    ON u.ID          = s.VODITELJ_USER_ID
WITH READ ONLY;

COMMENT ON TABLE POGLED_SLUCAJ_PREGLED IS
    'Read-only pregled slučaja: spaja SLUCAJEVI sa STANICE i voditeljem te agregira metrike (#32).';


-- =============================================================================
-- 2) POGLED_LANAC_NADZORA_PREGLED
--    Detaljan pregled svakog unosa lanca nadzora sa punim imenima učesnika,
--    informacijama o dokazu i stanici. Potvrđivač je opcionalan (LEFT JOIN).
--    Tabele: LANAC_NADZORA, DOKAZI, STANICE, nbp.NBP_USER (predao),
--            nbp.NBP_USER (preuzeo), nbp.NBP_USER (potvrdio, opcionalan).
--    Pogled je striktno read-only — LANAC_NADZORA je append-only po domenu.
-- =============================================================================
CREATE OR REPLACE VIEW POGLED_LANAC_NADZORA_PREGLED AS
SELECT
    ln.UNOS_ID                                                        AS UNOS_ID,
    ln.DOKAZ_ID                                                       AS DOKAZ_ID,
    d.OPIS                                                            AS DOKAZ_OPIS,
    d.TIP_DOKAZA                                                      AS TIP_DOKAZA,
    ln.STANICA_ID                                                     AS STANICA_ID,
    st.IME_STANICE                                                    AS IME_STANICE,
    ln.DATUM_PRIMOPREDAJE                                             AS DATUM_PRIMOPREDAJE,
    ln.PREDAO_USER_ID                                                 AS PREDAO_USER_ID,
    (up.FIRST_NAME || ' ' || up.LAST_NAME)                            AS PREDAO_IME,
    ln.PREUZEO_USER_ID                                                AS PREUZEO_USER_ID,
    (uz.FIRST_NAME || ' ' || uz.LAST_NAME)                            AS PREUZEO_IME,
    ln.SVRHA_PRIMOPREDAJE                                             AS SVRHA_PRIMOPREDAJE,
    ln.POTVRDA_STATUS                                                 AS POTVRDA_STATUS,
    ln.POTVRDA_NAPOMENA                                               AS POTVRDA_NAPOMENA,
    ln.POTVRDA_DATUM                                                  AS POTVRDA_DATUM,
    ln.POTVRDIO_USER_ID                                               AS POTVRDIO_USER_ID,
    (po.FIRST_NAME || ' ' || po.LAST_NAME)                            AS POTVRDIO_IME
FROM LANAC_NADZORA ln
JOIN DOKAZI d           ON d.DOKAZ_ID    = ln.DOKAZ_ID
JOIN STANICE st         ON st.STANICA_ID = ln.STANICA_ID
LEFT JOIN nbp.NBP_USER up ON up.ID       = ln.PREDAO_USER_ID
JOIN nbp.NBP_USER uz    ON uz.ID         = ln.PREUZEO_USER_ID
LEFT JOIN nbp.NBP_USER po ON po.ID       = ln.POTVRDIO_USER_ID
WITH READ ONLY;

COMMENT ON TABLE POGLED_LANAC_NADZORA_PREGLED IS
    'Read-only pregled lanca nadzora sa imenima učesnika i dokazom (#32). Append-only domen.';


-- =============================================================================
-- 3) POGLED_TIM_NA_SLUCAJU_PREGLED
--    Roster članova tima po slučaju sa sistemskom ulogom (iz nbp.NBP_ROLE) i
--    ulogom na slučaju, te imenom stanice u kojoj se slučaj vodi.
--    Tabele: TIM_NA_SLUCAJU, SLUCAJEVI, STANICE, nbp.NBP_USER, nbp.NBP_ROLE.
-- =============================================================================
CREATE OR REPLACE VIEW POGLED_TIM_NA_SLUCAJU_PREGLED AS
SELECT
    t.DODJELA_ID                                                      AS DODJELA_ID,
    s.SLUCAJ_ID                                                       AS SLUCAJ_ID,
    s.BROJ_SLUCAJA                                                    AS BROJ_SLUCAJA,
    s.STATUS                                                          AS STATUS_SLUCAJA,
    st.STANICA_ID                                                     AS STANICA_ID,
    st.IME_STANICE                                                    AS IME_STANICE,
    u.ID                                                              AS USER_ID,
    (u.FIRST_NAME || ' ' || u.LAST_NAME)                              AS CLAN_IME,
    r.NAME                                                            AS SISTEMSKA_ULOGA,
    t.ULOGA_NA_SLUCAJU                                                AS ULOGA_NA_SLUCAJU
FROM TIM_NA_SLUCAJU t
JOIN SLUCAJEVI s        ON s.SLUCAJ_ID   = t.SLUCAJ_ID
JOIN STANICE st         ON st.STANICA_ID = s.STANICA_ID
JOIN nbp.NBP_USER u     ON u.ID          = t.USER_ID
JOIN nbp.NBP_ROLE r     ON r.ID          = u.ROLE_ID
WITH READ ONLY;

COMMENT ON TABLE POGLED_TIM_NA_SLUCAJU_PREGLED IS
    'Read-only roster tima na slučaju sa sistemskom ulogom i ulogom na slučaju (#32).';


-- =============================================================================
-- ROLLBACK (uklanja sva tri pogleda)
-- =============================================================================
-- DROP VIEW POGLED_TIM_NA_SLUCAJU_PREGLED;
-- DROP VIEW POGLED_LANAC_NADZORA_PREGLED;
-- DROP VIEW POGLED_SLUCAJ_PREGLED;
