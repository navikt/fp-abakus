package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import jakarta.persistence.*;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;
import no.nav.abakus.iaygrunnlag.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.LandKodeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.iay.jpa.VirksomhetTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;


@Table(name = "IAY_EGEN_NAERING")
@Entity(name = "EgenNæring")
public class OppgittEgenNæring extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EGEN_NAERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjening oppgittOpptjening;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ChangeTracked
    @Embedded
    private OrgNummer orgNummer;

    @Convert(converter = VirksomhetTypeKodeverdiConverter.class)
    @Column(name = "virksomhet_type", nullable = false, updatable = false)
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

    @Convert(converter = LandKodeKodeverdiConverter.class)
    @Column(name = "land", nullable = false)
    private Landkode landkode;

    @Column(name = "utenlandsk_virksomhet_navn")
    private String utenlandskVirksomhetNavn;

    OppgittEgenNæring() {
        // for hibernate
    }

    /* copy ctor */
    public OppgittEgenNæring(OppgittEgenNæring orginal) {
        periode = orginal.getPeriode();
        orgNummer = orginal.getOrgnummer();
        virksomhetType = orginal.getVirksomhetType();
        regnskapsførerNavn = orginal.getRegnskapsførerNavn();
        regnskapsførerTlf = orginal.getRegnskapsførerTlf();
        endringDato = orginal.getEndringDato();
        begrunnelse = orginal.getBegrunnelse();
        bruttoInntekt = orginal.getBruttoInntekt();
        nyoppstartet = orginal.getNyoppstartet();
        varigEndring = orginal.getVarigEndring();
        nærRelasjon = orginal.getNærRelasjon();
        nyIArbeidslivet = orginal.getNyIArbeidslivet();
        landkode = orginal.getLandkode();
        utenlandskVirksomhetNavn = orginal.getUtenlandskVirksomhetNavn();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode, orgNummer, landkode, utenlandskVirksomhetNavn};
        return IndexKeyComposer.createKey(keyParts);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    public VirksomhetType getVirksomhetType() {
        return virksomhetType;
    }

    void setVirksomhetType(VirksomhetType virksomhetType) {
        this.virksomhetType = virksomhetType;
    }

    public OrgNummer getOrgnummer() {
        return orgNummer;
    }

    void setVirksomhet(OrgNummer orgNummer) {
        this.orgNummer = orgNummer;
    }

    public String getRegnskapsførerNavn() {
        return regnskapsførerNavn;
    }

    void setRegnskapsførerNavn(String regnskapsførerNavn) {
        this.regnskapsførerNavn = regnskapsførerNavn;
    }

    public String getRegnskapsførerTlf() {
        return regnskapsførerTlf;
    }

    void setRegnskapsførerTlf(String regnskapsførerTlf) {
        this.regnskapsførerTlf = regnskapsførerTlf;
    }

    public LocalDate getEndringDato() {
        return endringDato;
    }

    void setEndringDato(LocalDate endringDato) {
        this.endringDato = endringDato;
    }

    public BigDecimal getBruttoInntekt() {
        return bruttoInntekt;
    }

    void setBruttoInntekt(BigDecimal bruttoInntekt) {
        this.bruttoInntekt = bruttoInntekt;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public boolean getNyoppstartet() {
        return nyoppstartet;
    }

    void setNyoppstartet(boolean nyoppstartet) {
        this.nyoppstartet = nyoppstartet;
    }

    public boolean getNyIArbeidslivet() {
        return nyIArbeidslivet;
    }

    void setNyIArbeidslivet(boolean nyIArbeidslivet) {
        this.nyIArbeidslivet = nyIArbeidslivet;
    }

    public boolean getVarigEndring() {
        return varigEndring;
    }

    void setVarigEndring(boolean varigEndring) {
        this.varigEndring = varigEndring;
    }

    public boolean getNærRelasjon() {
        return nærRelasjon;
    }

    void setNærRelasjon(boolean nærRelasjon) {
        this.nærRelasjon = nærRelasjon;
    }

    public Landkode getLandkode() {
        return landkode;
    }

    void setLandkode(Landkode landkode) {
        this.landkode = Objects.requireNonNull(landkode, "landkode");
    }

    public String getUtenlandskVirksomhetNavn() {
        return utenlandskVirksomhetNavn;
    }

    void setUtenlandskVirksomhetNavn(String utenlandskVirksomhetNavn) {
        this.utenlandskVirksomhetNavn = utenlandskVirksomhetNavn;
    }

    void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof OppgittEgenNæring)) {
            return false;
        }
        var that = (OppgittEgenNæring) o;
        return Objects.equals(periode, that.periode) && Objects.equals(orgNummer, that.orgNummer) && Objects.equals(nyoppstartet, that.nyoppstartet)
            && Objects.equals(virksomhetType, that.virksomhetType) && Objects.equals(regnskapsførerNavn, that.regnskapsførerNavn) && Objects.equals(
            regnskapsførerTlf, that.regnskapsførerTlf) && Objects.equals(endringDato, that.endringDato) && Objects.equals(begrunnelse,
            that.begrunnelse) && Objects.equals(bruttoInntekt, that.bruttoInntekt) && Objects.equals(landkode, that.landkode) && Objects.equals(
            utenlandskVirksomhetNavn, that.utenlandskVirksomhetNavn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, orgNummer, virksomhetType, nyoppstartet, regnskapsførerNavn, regnskapsførerTlf, endringDato, begrunnelse,
            bruttoInntekt, landkode, utenlandskVirksomhetNavn);
    }

    @Override
    public String toString() {
        return "OppgittEgenNæring{" + "id=" + id + ", periode=" + periode + ", virksomhet=" + orgNummer + ", nyoppstartet=" + nyoppstartet
            + ", virksomhetType=" + virksomhetType + ", regnskapsførerNavn='" + regnskapsførerNavn + '\'' + ", regnskapsførerTlf='"
            + regnskapsførerTlf + '\'' + ", endringDato=" + endringDato + ", begrunnelse='" + begrunnelse + '\'' + ", bruttoInntekt=" + bruttoInntekt
            + ", landkode=" + landkode + ", utenlandskVirksomhetNavn=" + utenlandskVirksomhetNavn + '}';
    }
}
