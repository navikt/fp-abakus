package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.LinkedHashSet;
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

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;

@Table(name = "IAY_AKTOER_ARBEID")
@Entity(name = "AktørArbeid")
public class AktørArbeidEntitet extends BaseEntitet implements AktørArbeid, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTOER_ARBEID")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false)))
    private AktørId aktørId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_arbeid_ytelser_id", nullable = false, updatable = false)
    private InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser;

    @OneToMany(mappedBy = "aktørArbeid")
    @ChangeTracked
    private Set<YrkesaktivitetEntitet> yrkesaktiviter = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    AktørArbeidEntitet() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørArbeidEntitet(AktørArbeid aktørArbeid) {
        final AktørArbeidEntitet aktørArbeid1 = (AktørArbeidEntitet) aktørArbeid; // NOSONAR
        this.aktørId = aktørArbeid1.getAktørId();

        this.yrkesaktiviter = aktørArbeid1.yrkesaktiviter.stream().map(yrkesaktivitet -> {
            YrkesaktivitetEntitet yrkesaktivitetEntitet = new YrkesaktivitetEntitet(yrkesaktivitet);
            yrkesaktivitetEntitet.setAktørArbeid(this);
            return yrkesaktivitetEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(getAktørId());
    }

    @Override
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    @Override
    public Collection<Yrkesaktivitet> hentAlleYrkesaktiviteter() {
        return Set.copyOf(yrkesaktiviter);
    }

    void setYrkesaktiviter() {
        this.yrkesaktiviter = new LinkedHashSet<>();
    }

    void setInntektArbeidYtelser(InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser) {
        this.inntektArbeidYtelser = inntektArbeidYtelser;
    }

    boolean hasValues() {
        return aktørId != null || yrkesaktiviter != null;
    }

    YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkel(Opptjeningsnøkkel identifikator, ArbeidType arbeidType) {
        Optional<YrkesaktivitetEntitet> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> ya.getArbeidType().equals(arbeidType) && new Opptjeningsnøkkel(ya).equals(identifikator))
            .findFirst();
        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(arbeidType);
        return oppdatere;
    }

    YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkel(Opptjeningsnøkkel identifikator, Set<ArbeidType> arbeidTyper) {
        Optional<YrkesaktivitetEntitet> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> arbeidTyper.contains(ya.getArbeidType()) && new Opptjeningsnøkkel(ya).equals(identifikator))
            .findFirst();
        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(yrkesaktivitet);
        if (!oppdatere.getErOppdatering()) {
            // Defaulter til ordinert arbeidsforhold hvis saksbehandler har lagt til fra GUI
            oppdatere.medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        }
        return oppdatere;
    }

    void fjernYrkesaktivitetForBuilder(YrkesaktivitetBuilder builder) {
        YrkesaktivitetEntitet yrkesaktivitetKladd = builder.getKladd();
        ArbeidType arbeidType = yrkesaktivitetKladd.getArbeidType();
        if (arbeidType.erAnnenOpptjening() || ArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(arbeidType)) {
            yrkesaktiviter.removeIf(ya -> ya.getArbeidType().equals(arbeidType));
        } else {
            Opptjeningsnøkkel nøkkel = new Opptjeningsnøkkel(yrkesaktivitetKladd);
            yrkesaktiviter.removeIf(ya -> ya.getArbeidType().equals(arbeidType)
                && new Opptjeningsnøkkel(ya).matcher(nøkkel));
        }
    }

    YrkesaktivitetBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
        Optional<YrkesaktivitetEntitet> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> ya.getArbeidType().equals(type))
            .findFirst();
        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(type);
        return oppdatere;
    }

    void leggTilYrkesaktivitet(Yrkesaktivitet yrkesaktivitet) {
        YrkesaktivitetEntitet yrkesaktivitetEntitet = (YrkesaktivitetEntitet) yrkesaktivitet;
        this.yrkesaktiviter.add(yrkesaktivitetEntitet);
        yrkesaktivitetEntitet.setAktørArbeid(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørArbeidEntitet)) {
            return false;
        }
        AktørArbeidEntitet other = (AktørArbeidEntitet) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørId=" + aktørId +
            ", yrkesaktiviteter=" + yrkesaktiviter +
            '>';
    }

}
