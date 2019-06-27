package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@Table(name = "IAY_EGEN_NAERING")
@Entity(name = "EgenNæring")
public class OppgittEgenNæringEntitet extends BaseEntitet implements OppgittEgenNæring, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EGEN_NAERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @Embedded
    private OrgNummer orgNummer;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "virksomhet_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VirksomhetType.DISCRIMINATOR + "'"))
    private VirksomhetType virksomhetType;

    @Column(name = "regnskapsfoerer_navn")
    private String regnskapsførerNavn;

    @Column(name = "regnskapsfoerer_tlf")
    private String regnskapsførerTlf;

    @Column(name = "endring_dato")
    private LocalDate endringDato;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    @Column(name = "brutto_inntekt")
    private BigDecimal bruttoInntekt;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "nyoppstartet", nullable = false)
    private boolean nyoppstartet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "varig_endring", nullable = false)
    private boolean varigEndring;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "naer_relasjon", nullable = false)
    private boolean nærRelasjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "ny_i_arbeidslivet", nullable = false)
    private boolean nyIArbeidslivet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "land", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Landkoder.DISCRIMINATOR + "'"))})
    private Landkoder landkode = Landkoder.NOR;

    @Column(name = "utenlandsk_virksomhet_navn")
    private String utenlandskVirksomhetNavn;

    OppgittEgenNæringEntitet() {
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, orgNummer, landkode, utenlandskVirksomhetNavn);
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
    public VirksomhetType getVirksomhetType() {
        return virksomhetType;
    }

    void setVirksomhetType(VirksomhetType virksomhetType) {
        this.virksomhetType = virksomhetType;
    }

    @Override
    public OrgNummer getOrgnummer() {
        return orgNummer;
    }

    void setVirksomhet(OrgNummer orgNummer) {
        this.orgNummer = orgNummer;
    }

    @Override
    public String getRegnskapsførerNavn() {
        return regnskapsførerNavn;
    }

    void setRegnskapsførerNavn(String regnskapsførerNavn) {
        this.regnskapsførerNavn = regnskapsførerNavn;
    }

    @Override
    public String getRegnskapsførerTlf() {
        return regnskapsførerTlf;
    }

    void setRegnskapsførerTlf(String regnskapsførerTlf) {
        this.regnskapsførerTlf = regnskapsførerTlf;
    }

    @Override
    public LocalDate getEndringDato() {
        return endringDato;
    }

    void setEndringDato(LocalDate endringDato) {
        this.endringDato = endringDato;
    }

    @Override
    public BigDecimal getBruttoInntekt() {
        return bruttoInntekt;
    }

    void setBruttoInntekt(BigDecimal bruttoInntekt) {
        this.bruttoInntekt = bruttoInntekt;
    }

    @Override
    public String getBegrunnelse() {
        return begrunnelse;
    }

    void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    @Override
    public boolean getNyoppstartet() {
        return nyoppstartet;
    }

    void setNyoppstartet(boolean nyoppstartet) {
        this.nyoppstartet = nyoppstartet;
    }

    @Override
    public boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    void setNyIArbeidslivet(boolean nyIArbeidslivet) {
        this.nyIArbeidslivet = nyIArbeidslivet;
    }

    @Override
    public boolean getVarigEndring() {
        return varigEndring;
    }

    void setVarigEndring(boolean varigEndring) {
        this.varigEndring = varigEndring;
    }

    @Override
    public boolean getNærRelasjon() {
        return nærRelasjon;
    }

    void setNærRelasjon(boolean nærRelasjon) {
        this.nærRelasjon = nærRelasjon;
    }

    @Override
    public Landkoder getLandkode() {
        return landkode;
    }

    void setLandkode(Landkoder landkode) {
        this.landkode = landkode;
    }

    @Override
    public String getUtenlandskVirksomhetNavn() {
        return utenlandskVirksomhetNavn;
    }

    void setUtenlandskVirksomhetNavn(String utenlandskVirksomhetNavn) {
        this.utenlandskVirksomhetNavn = utenlandskVirksomhetNavn;
    }

    void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittEgenNæringEntitet)) return false;
        var that = (OppgittEgenNæringEntitet) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(orgNummer, that.orgNummer) &&
            Objects.equals(nyoppstartet, that.nyoppstartet) &&
            Objects.equals(virksomhetType, that.virksomhetType) &&
            Objects.equals(regnskapsførerNavn, that.regnskapsførerNavn) &&
            Objects.equals(regnskapsførerTlf, that.regnskapsførerTlf) &&
            Objects.equals(endringDato, that.endringDato) &&
            Objects.equals(begrunnelse, that.begrunnelse) &&
            Objects.equals(bruttoInntekt, that.bruttoInntekt) &&
            Objects.equals(landkode, that.landkode) &&
            Objects.equals(utenlandskVirksomhetNavn, that.utenlandskVirksomhetNavn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, orgNummer, virksomhetType, nyoppstartet, regnskapsførerNavn, regnskapsførerTlf, endringDato, begrunnelse,
            bruttoInntekt, landkode, utenlandskVirksomhetNavn);
    }

    @Override
    public String toString() {
        return "EgenNæringEntitet{" +
            "id=" + id +
            ", periode=" + periode +
            ", virksomhet=" + orgNummer +
            ", nyoppstartet=" + nyoppstartet +
            ", virksomhetType=" + virksomhetType +
            ", regnskapsførerNavn='" + regnskapsførerNavn + '\'' +
            ", regnskapsførerTlf='" + regnskapsførerTlf + '\'' +
            ", endringDato=" + endringDato +
            ", begrunnelse='" + begrunnelse + '\'' +
            ", bruttoInntekt=" + bruttoInntekt +
            ", landkode=" + landkode +
            ", utenlandskVirksomhetNavn=" + utenlandskVirksomhetNavn +
            '}';
    }
}
