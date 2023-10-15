package no.nav.foreldrepenger.abakus.domene.iay;


import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;

@Table(name = "IAY_AKTOER_INNTEKT")
@Entity(name = "AktørInntekt")
public class AktørInntekt extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTOER_INNTEKT")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false)))
    private AktørId aktørId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_arbeid_ytelser_id", nullable = false, updatable = false)
    private InntektArbeidYtelseAggregat inntektArbeidYtelser;

    @OneToMany(mappedBy = "aktørInntekt")
    @ChangeTracked
    private Set<Inntekt> inntekt = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    AktørInntekt() {
        //hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørInntekt(AktørInntekt aktørInntekt) {
        this.aktørId = aktørInntekt.getAktørId();

        this.inntekt = aktørInntekt.inntekt.stream().map(i -> {
            Inntekt inntektTmpEntitet = new Inntekt(i);
            inntektTmpEntitet.setAktørInntekt(this);
            return inntektTmpEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {getAktørId()};
        return IndexKeyComposer.createKey(keyParts);
    }

    void setInntektArbeidYtelser(InntektArbeidYtelseAggregat inntektArbeidYtelser) {
        this.inntektArbeidYtelser = inntektArbeidYtelser;
    }

    /**
     * Aktøren inntekten er relevant for
     *
     * @return aktørid
     */
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    /**
     * Returner alle inntekter, ufiltrert.
     */
    public Collection<Inntekt> getInntekt() {
        return Collections.unmodifiableSet(inntekt);
    }

    public boolean hasValues() {
        return aktørId != null || inntekt != null;
    }

    public InntektBuilder getInntektBuilder(InntektskildeType inntektskildeType, Opptjeningsnøkkel nøkkel) {
        Optional<Inntekt> inntektOptional = getInntekt().stream()
            .filter(i -> inntektskildeType.equals(i.getInntektsKilde()))
            .filter(i -> i.getArbeidsgiver() != null && new Opptjeningsnøkkel(i.getArbeidsgiver()).matcher(nøkkel) || inntektskildeType.equals(
                InntektskildeType.SIGRUN))
            .findFirst();
        InntektBuilder oppdatere = InntektBuilder.oppdatere(inntektOptional);
        if (!oppdatere.getErOppdatering()) {
            oppdatere.medInntektsKilde(inntektskildeType);
        }
        return oppdatere;
    }

    public InntektBuilder getInntektBuilderForYtelser(InntektskildeType inntektskildeType) {
        Optional<Inntekt> inntektOptional = getInntekt().stream()
            .filter(i -> i.getArbeidsgiver() == null)
            .filter(i -> inntektskildeType.equals(i.getInntektsKilde()))
            .filter(i -> i.getAlleInntektsposter().stream().anyMatch(post -> post.getInntektspostType().equals(InntektspostType.YTELSE)))
            .findFirst();
        InntektBuilder oppdatere = InntektBuilder.oppdatere(inntektOptional);
        if (!oppdatere.getErOppdatering()) {
            oppdatere.medInntektsKilde(inntektskildeType);
        }
        return oppdatere;
    }

    void leggTilInntekt(Inntekt inntekt) {
        this.inntekt.add(inntekt);
        inntekt.setAktørInntekt(this);
    }

    void fjernInntekterFraKilde(InntektskildeType inntektskildeType) {
        this.inntekt.removeIf(it -> it.getInntektsKilde().equals(inntektskildeType));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørInntekt)) {
            return false;
        }
        AktørInntekt other = (AktørInntekt) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "aktørId=" + aktørId + ", inntekt=" + inntekt + '>';
    }
}
