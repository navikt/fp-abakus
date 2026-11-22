package no.nav.foreldrepenger.abakus.domene.iay;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class GrunnlagReferanse implements Serializable {

    @JsonValue
    @NotNull
    @Column(name = "grunnlag_referanse", nullable = false, updatable = false)
    private UUID referanse;

    protected GrunnlagReferanse() {
        // For hibernate
    }

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
        return getClass().getSimpleName() + "<" + referanse + ">";
    }

}
