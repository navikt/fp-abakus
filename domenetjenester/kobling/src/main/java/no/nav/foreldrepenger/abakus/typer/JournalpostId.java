package no.nav.foreldrepenger.abakus.typer;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Journalpostid refererer til journalpost registret i Joark.
 */
@Embeddable
public class JournalpostId implements Serializable, IndexKey {
    private static final String CHARS = "a-z0-9_:-";

    private static final Pattern VALID = Pattern.compile("^(-?[1-9]|[a-z0])[" + CHARS + "]*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVALID = Pattern.compile("[^" + CHARS + "]+", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @JsonValue
    @Column(name = "journalpost_id", updatable = false)
    private String journalpostId;  // NOSONAR

    JournalpostId() {
        // for hibernate
    }

    public JournalpostId(Long journalpostId) {
        Objects.requireNonNull(journalpostId, "journalpostId");
        this.journalpostId = Long.toString(journalpostId);
    }

    public JournalpostId(String journalpostId) {
        Objects.requireNonNull(journalpostId, "journalpostId");
        if (!VALID.matcher(journalpostId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException(
                "Ugyldig aktørId, støtter kun A-Z/0-9/:/-/_ tegn. Var: " + journalpostId.replaceAll(INVALID.pattern(), "?") + " (vasket)");
        }
        this.journalpostId = journalpostId;
    }

    public static boolean erGyldig(String input) {
        return input != null && !input.isEmpty() && VALID.matcher(input.trim()).matches();
    }

    @Override
    public String getIndexKey() {
        return journalpostId;
    }

    public String getVerdi() {
        return journalpostId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        JournalpostId other = (JournalpostId) obj;
        return Objects.equals(journalpostId, other.journalpostId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journalpostId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + journalpostId + ">";
    }
}
