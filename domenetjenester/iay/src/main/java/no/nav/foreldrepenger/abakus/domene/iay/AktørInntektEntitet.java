package no.nav.foreldrepenger.abakus.domene.iay;


import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
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

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;

@Table(name = "IAY_AKTOER_INNTEKT")
@Entity(name = "AktørInntekt")
public class AktørInntektEntitet extends BaseEntitet implements AktørInntekt, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTOER_INNTEKT")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false)))
    private AktørId aktørId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_arbeid_ytelser_id", nullable = false, updatable = false)
    private InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser;

    @OneToMany(mappedBy = "aktørInntekt")
    @ChangeTracked
    private Set<InntektEntitet> inntekt = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    AktørInntektEntitet() {
        //hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørInntektEntitet(AktørInntekt aktørInntekt) {
        final AktørInntektEntitet aktørInntektEntitet = (AktørInntektEntitet) aktørInntekt; //NOSONAR
        this.aktørId = aktørInntektEntitet.getAktørId();

        this.inntekt = aktørInntektEntitet.inntekt.stream().map(i -> {
            InntektEntitet inntektTmpEntitet = new InntektEntitet(i);
            inntektTmpEntitet.setAktørInntekt(this);
            return inntektTmpEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(getAktørId());
    }

    void setInntektArbeidYtelser(InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser) {
        this.inntektArbeidYtelser = inntektArbeidYtelser;
    }

    @Override
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public Collection<Inntekt> getInntekt() {
        return Collections.unmodifiableSet(inntekt);
    }

    @Override
    public List<Inntekt> getBeregnetSkatt() {
        return inntekt.stream()
            .filter(it -> InntektsKilde.SIGRUN.equals(it.getInntektsKilde()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Inntekt> getInntektPensjonsgivende() {
        return inntekt.stream()
            .filter(it -> InntektsKilde.INNTEKT_OPPTJENING.equals(it.getInntektsKilde()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Inntekt> getInntektBeregningsgrunnlag() {
        return inntekt.stream()
            .filter(it -> InntektsKilde.INNTEKT_BEREGNING.equals(it.getInntektsKilde()))
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Inntekt> getInntektSammenligningsgrunnlag() {
        return inntekt.stream()
            .filter(it -> InntektsKilde.INNTEKT_SAMMENLIGNING.equals(it.getInntektsKilde()))
            .collect(Collectors.toUnmodifiableList());
    }

    public boolean hasValues() {
        return aktørId != null || inntekt != null;
    }

    public InntektBuilder getInntektBuilder(InntektsKilde inntektsKilde, Opptjeningsnøkkel nøkkel) {
        Optional<Inntekt> inntektOptional = getInntekt()
            .stream()
            .filter(i -> inntektsKilde.equals(i.getInntektsKilde()))
            .filter(i -> i.getArbeidsgiver() != null && new Opptjeningsnøkkel(i.getArbeidsgiver()).matcher(nøkkel)
                || inntektsKilde.equals(InntektsKilde.SIGRUN)).findFirst();
        InntektBuilder oppdatere = InntektBuilder.oppdatere(inntektOptional);
        if (!oppdatere.getErOppdatering()) {
            oppdatere.medInntektsKilde(inntektsKilde);
        }
        return oppdatere;
    }

    public InntektBuilder getInntektBuilderForYtelser(InntektsKilde inntektsKilde) {
        Optional<Inntekt> inntektOptional = getInntekt()
            .stream()
            .filter(i -> i.getArbeidsgiver() == null)
            .filter(i -> inntektsKilde.equals(i.getInntektsKilde()))
            .filter(i -> i.getInntektspost().stream()
                .anyMatch(post -> post.getInntektspostType().equals(InntektspostType.YTELSE)))
            .findFirst();
        InntektBuilder oppdatere = InntektBuilder.oppdatere(inntektOptional);
        if (!oppdatere.getErOppdatering()) {
            oppdatere.medInntektsKilde(inntektsKilde);
        }
        return oppdatere;
    }

    void leggTilInntekt(Inntekt inntekt) {
        InntektEntitet inntektEntitet = (InntektEntitet) inntekt;
        this.inntekt.add(inntektEntitet);
        inntektEntitet.setAktørInntekt(this);
    }

    void fjernInntekterFraKilde(InntektsKilde inntektsKilde) {
        this.inntekt.removeIf(it -> it.getInntektsKilde().equals(inntektsKilde));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørInntektEntitet)) {
            return false;
        }
        AktørInntektEntitet other = (AktørInntektEntitet) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId()) &&
            Objects.equals(this.getInntekt(), other.getInntekt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId, inntekt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørId=" + aktørId +
            ", inntekt=" + inntekt +
            '>';
    }

    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        for (InntektEntitet entitet : inntekt) {
            entitet.setSkjæringstidspunkt(skjæringstidspunkt, ventreSide);
        }
    }

    public static class InntektBuilder {
        private final boolean oppdaterer;
        private InntektEntitet inntektEntitet;

        private InntektBuilder(InntektEntitet inntektEntitet, boolean oppdaterer) {
            this.inntektEntitet = inntektEntitet;
            this.oppdaterer = oppdaterer;
        }

        static InntektBuilder ny() {
            return new InntektBuilder(new InntektEntitet(), false);
        }

        static InntektBuilder oppdatere(Inntekt oppdatere) {
            return new InntektBuilder((InntektEntitet) oppdatere, true);
        }

        public static InntektBuilder oppdatere(Optional<Inntekt> oppdatere) {
            return oppdatere.map(InntektBuilder::oppdatere).orElseGet(InntektBuilder::ny);
        }

        public InntektBuilder medInntektsKilde(InntektsKilde inntektsKilde) {
            this.inntektEntitet.setInntektsKilde(inntektsKilde);
            return this;
        }

        public InntektBuilder leggTilInntektspost(InntektEntitet.InntektspostBuilder inntektspost) {
            InntektspostEntitet inntektspostEntitet = (InntektspostEntitet) inntektspost.build();
            inntektEntitet.leggTilInntektspost(inntektspostEntitet);
            return this;
        }

        public InntektBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            this.inntektEntitet.setArbeidsgiver((ArbeidsgiverEntitet) arbeidsgiver);
            return this;
        }

        public InntektEntitet.InntektspostBuilder getInntektspostBuilder() {
            return inntektEntitet.getInntektspostBuilder();
        }

        boolean getErOppdatering() {
            return this.oppdaterer;
        }

        public Inntekt build() {
            if (inntektEntitet.hasValues()) {
                return inntektEntitet;
            }
            throw new IllegalStateException();
        }
    }
}
