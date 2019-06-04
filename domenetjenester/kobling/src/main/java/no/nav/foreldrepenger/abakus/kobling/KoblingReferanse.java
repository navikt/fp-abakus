package no.nav.foreldrepenger.abakus.kobling;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonValue;

@Embeddable
public class KoblingReferanse {

    @JsonValue
    @Column(name = "kobling_referanse", updatable = false)
    private final UUID referanse;

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

}
