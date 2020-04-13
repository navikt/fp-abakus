package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.InntektsKildeKodeverdiConverter;

@Entity(name = "Inntekt")
@Table(name = "IAY_INNTEKT")
public class Inntekt extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aktoer_inntekt_id", nullable = false, updatable = false)
    private AktørInntekt aktørInntekt;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Convert(converter = InntektsKildeKodeverdiConverter.class)
    @Column(name = "kilde", nullable = false, updatable = false)
    private InntektskildeType inntektskildeType;

    /* TODO: splitt inntektspostentitet klasse ? inntektspostentitet varierer med kilde. */
    @OneToMany(mappedBy = "inntekt")
    @ChangeTracked
    private Set<Inntektspost> inntektspost = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Inntekt() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    Inntekt(Inntekt inntektMal) {
        this.inntektskildeType = inntektMal.getInntektsKilde();
        this.arbeidsgiver = inntektMal.getArbeidsgiver();
        this.inntektspost = inntektMal.getAlleInntektsposter().stream().map(ip -> {
            Inntektspost inntektspostEntitet = new Inntektspost(ip);
            inntektspostEntitet.setInntekt(this);
            return inntektspostEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { getArbeidsgiver(), getInntektsKilde() };
        return IndexKeyComposer.createKey(keyParts);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof Inntekt)) {
            return false;
        }
        Inntekt other = (Inntekt) obj;
        return Objects.equals(this.getInntektsKilde(), other.getInntektsKilde())
            && Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInntektsKilde(), getArbeidsgiver());
    }

    /**
     * System (+ filter) som inntektene er hentet inn fra / med
     *
     * @return {@link InntektskildeType}
     */
    public InntektskildeType getInntektsKilde() {
        return inntektskildeType;
    }

    void setInntektsKilde(InntektskildeType inntektskildeType) {
        this.inntektskildeType = inntektskildeType;
    }

    /**
     * Utbetaler
     *
     * @return {@link ArbeidsgiverEntitet}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    /** Hent alle utbetalinger (ufiltrert). */
    public Collection<Inntektspost> getAlleInntektsposter() {
        return Collections.unmodifiableSet(inntektspost);
    }

    void leggTilInntektspost(Inntektspost inntektspost) {
        inntektspost.setInntekt(this);
        this.inntektspost.add(inntektspost);
    }

    public AktørInntekt getAktørInntekt() {
        return aktørInntekt;
    }

    void setAktørInntekt(AktørInntekt aktørInntekt) {
        this.aktørInntekt = aktørInntekt;
    }

    public InntektspostBuilder getInntektspostBuilder() {
        return InntektspostBuilder.ny();
    }

    public boolean hasValues() {
        return arbeidsgiver != null || inntektskildeType != null || inntektspost != null;
    }
}
