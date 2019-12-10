package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Entitetsklasse for oppgitte arbeidsforhold.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */
@Table(name = "IAY_OPPGITT_ARBEIDSFORHOLD")
@Entity(name = "OppgittArbeidsforhold")
public class OppgittArbeidsforholdEntitet extends BaseEntitet implements OppgittArbeidsforhold, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPPGITT_ARBEIDSFORHOLD")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "utenlandsk_inntekt", nullable = false)
    private boolean erUtenlandskInntekt;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "arbeid_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ArbeidType.DISCRIMINATOR + "'"))
    @ChangeTracked
    private ArbeidType arbeidType;

    @Convert(converter = Landkoder.KodeverdiConverter.class)
    @Column(name="land", nullable = false)
    private Landkoder landkode;

    @Column(name = "utenlandsk_virksomhet_navn")
    private String utenlandskVirksomhetNavn;

    public OppgittArbeidsforholdEntitet() {
        // hibernate
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, landkode, utenlandskVirksomhetNavn, arbeidType);
    }

    @Override
    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    @Override
    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public Boolean erUtenlandskInntekt() {
        return erUtenlandskInntekt;
    }

    @Override
    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    void setArbeidType(ArbeidType arbeidType) {
        this.arbeidType = arbeidType;
    }

    @Override
    public Landkoder getLandkode() {
        return landkode;
    }

    void setLandkode(Landkoder landkode) {
        this.landkode = Objects.requireNonNull(landkode, "landkode");
    }

    @Override
    public String getUtenlandskVirksomhetNavn() {
        return utenlandskVirksomhetNavn;
    }

    void setUtenlandskVirksomhetNavn(String utenlandskVirksomhetNavn) {
        this.utenlandskVirksomhetNavn = utenlandskVirksomhetNavn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittArbeidsforholdEntitet)) return false;

        var that = (OppgittArbeidsforholdEntitet) o;

        return Objects.equals(periode, that.periode) &&
            Objects.equals(arbeidType, that.arbeidType) &&
            Objects.equals(utenlandskVirksomhetNavn, that.utenlandskVirksomhetNavn) &&
            Objects.equals(landkode, that.landkode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidType, landkode, utenlandskVirksomhetNavn);
    }

    @Override
    public String toString() {
        return "OppgittArbeidsforholdImpl{" +
            "id=" + id +
            ", periode=" + periode +
            ", erUtenlandskInntekt=" + erUtenlandskInntekt +
            ", arbeidType=" + arbeidType +
            ", landkode=" + landkode +
            ", utenlandskVirksomhetNavn=" + utenlandskVirksomhetNavn +
            '}';
    }

    void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    void setErUtenlandskInntekt(Boolean erUtenlandskInntekt) {
        this.erUtenlandskInntekt = erUtenlandskInntekt;
    }
}
