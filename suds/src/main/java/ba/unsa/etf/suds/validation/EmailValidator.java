package ba.unsa.etf.suds.validation;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Jednostavan validator email adresa.
 * Provjerava format i da li domena ima MX record (moze primati postu).
 */
public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );

    /**
     * Provjerava format email adrese.
     */
    public static boolean isValidFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Provjerava da li domena ima MX record (moze primati email).
     */
    public static boolean hasMxRecord(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase().trim();

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            InitialDirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            return attrs.get("MX") != null;
        } catch (NamingException e) {
            return false;
        }
    }

    /**
     * Vraca poruku greske ili null ako je email validan.
     */
    public static String validate(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email adresa je obavezna.";
        }

        email = email.trim().toLowerCase();

        if (!isValidFormat(email)) {
            return "Format email adrese nije ispravan.";
        }

        if (!hasMxRecord(email)) {
            return "Email domena ne postoji ili ne moze primati postu. Provjerite adresu.";
        }

        return null;
    }
}