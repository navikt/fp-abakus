package no.nav.foreldrepenger.abakus.typer;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;

/**
 * Intern arbeidsforhold referanse.
 * <p>
 * Hvis null gjelder det flere arbeidsforhold, ellers for et spesifikt forhold
 */

@Embeddable
public class InternArbeidsforholdRef implements IndexKey, Serializable {

    /**
     * Instans som representerer alle arbeidsforhold (for en arbeidsgiver).
     */
    private static final InternArbeidsforholdRef NULL_OBJECT = new InternArbeidsforholdRef(null);

    @Column(name = "arbeidsforhold_intern_id")
    private UUID referanse;

    InternArbeidsforholdRef() {
    }

    private InternArbeidsforholdRef(UUID referanse) {
        this.referanse = referanse;
    }

    public static InternArbeidsforholdRef ref(UUID referanse) {
        return referanse == null ? NULL_OBJECT : new InternArbeidsforholdRef(referanse);
    }

    public static InternArbeidsforholdRef ref(String referanse) {
        return referanse == null ? NULL_OBJECT : new InternArbeidsforholdRef(UUID.fromString(referanse));
    }

    public static InternArbeidsforholdRef nullRef() {
        return NULL_OBJECT;
    }

    public static InternArbeidsforholdRef nyRef() {
        return ref(UUID.randomUUID().toString());
    }

    /**
     * Genererer en UUID type 3 basert på angitt seed. Gir konsekvente UUIDer
     */
    public static InternArbeidsforholdRef namedRef(String seed) {
        return ref(UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString());
    }

    public String getReferanse() {
        return referanse == null ? null : referanse.toString();
    }

    public UUID getUUIDReferanse() {
        return referanse;
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { referanse == null ? null : referanse.toString() };
        return IndexKeyComposer.createKey(keyParts);
    }

    public boolean gjelderForSpesifiktArbeidsforhold() {
        return referanse != null;
    }

    public boolean gjelderFor(InternArbeidsforholdRef ref) {
        Objects.requireNonNull(ref, "Forventer InternArbeidsforholdRef.nullRef()");
        if (!gjelderForSpesifiktArbeidsforhold() || !ref.gjelderForSpesifiktArbeidsforhold()) {
            return true;
        }
        return Objects.equals(referanse, ref.referanse);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null && this.referanse == null) {
            return true;
        }
        if (o == null || getClass() != o.getClass())
            return false;
        InternArbeidsforholdRef that = (InternArbeidsforholdRef) o;
        return Objects.equals(referanse, that.referanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanse);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + (referanse == null ? "" : referanse.toString()) + ">";
    }
}
