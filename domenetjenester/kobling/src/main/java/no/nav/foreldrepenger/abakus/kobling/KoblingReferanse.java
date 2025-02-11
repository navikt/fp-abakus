package no.nav.foreldrepenger.abakus.kobling;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class KoblingReferanse {

    @JsonValue
    @Column(name = "kobling_referanse", updatable = false)
    private UUID referanse;

    protected KoblingReferanse() {
        // For hibernate
    }

    public KoblingReferanse(UUID referanse) {
        Objects.requireNonNull(referanse, "referanse");
        this.referanse = referanse;
    }

    public KoblingReferanse(String referanse) {
        this(UUID.fromString(referanse));
    }

    public UUID getReferanse() {
        return referanse;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        KoblingReferanse other = (KoblingReferanse) obj;
        return Objects.equals(referanse, other.referanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanse);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" + "referanse=" + referanse + '}';
    }

    public String asString() {
        return referanse.toString();
    }
}
