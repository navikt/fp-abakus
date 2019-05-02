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
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.UtenlandskVirksomhet;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
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

    @ChangeTracked
    @Embedded
    private OrgNummer orgNummer;

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

    @Embedded
    private UtenlandskVirksomhetEntitet utenlandskVirksomhet = new UtenlandskVirksomhetEntitet();

    public OppgittArbeidsforholdEntitet() {
        // hibernate
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, orgNummer, utenlandskVirksomhet, arbeidType);
    }

    @Override
    public OrgNummer getOrgnummer() {
        return orgNummer;
    }

    void setVirksomhet(OrgNummer orgNummer) {
        this.orgNummer = orgNummer;
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
    public UtenlandskVirksomhet getUtenlandskVirksomhet() {
        return utenlandskVirksomhet;
    }

    void setUtenlandskVirksomhet(UtenlandskVirksomhet utenlandskVirksomhet) {
        this.utenlandskVirksomhet = (UtenlandskVirksomhetEntitet) utenlandskVirksomhet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OppgittArbeidsforholdEntitet that = (OppgittArbeidsforholdEntitet) o;

        return Objects.equals(orgNummer, that.orgNummer) &&
            Objects.equals(periode, that.periode) &&
            Objects.equals(arbeidType, that.arbeidType) &&
            Objects.equals(utenlandskVirksomhet, that.utenlandskVirksomhet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgNummer, periode, arbeidType, utenlandskVirksomhet);
    }

    @Override
    public String toString() {
        return "OppgittArbeidsforholdImpl{" +
            "id=" + id +
            ", orgNummer=" + orgNummer +
            ", periode=" + periode +
            ", erUtenlandskInntekt=" + erUtenlandskInntekt +
            ", arbeidType=" + arbeidType +
            ", utenlandskVirksomhet=" + utenlandskVirksomhet +
            '}';
    }

    void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    void setErUtenlandskInntekt(Boolean erUtenlandskInntekt) {
        this.erUtenlandskInntekt = erUtenlandskInntekt;
    }
}
