package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonValue;

@Embeddable
public class GrunnlagReferanse {

    @JsonValue
    @Column(name = "grunnlag_referanse", updatable = false)
    private final UUID referanse;

    public GrunnlagReferanse(UUID referanse) {
        Objects.requireNonNull(referanse, "referanse");
        this.referanse = referanse;
    }

    public GrunnlagReferanse(String referanse) {
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
        GrunnlagReferanse other = (GrunnlagReferanse) obj;
        return Objects.equals(referanse, other.referanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanse);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + referanse.toString() + ">";
    }

}
