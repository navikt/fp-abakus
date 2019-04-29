package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "VedtakYtelseEntitet")
@Table(name = "VEDTAK_YTELSE")
public class VedtakYtelseEntitet extends BaseEntitet implements VedtattYtelse, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAK_YTELSE")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "ytelse_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + RelatertYtelseType.DISCRIMINATOR + "'"))})
    private RelatertYtelseType ytelseType;

    @DiffIgnore
    @Column(name = "vedtatt_tidspunkt")
    private LocalDateTime vedtattTidspunkt;

    @NaturalId
    @Column(name = "vedtak_referanse")
    private UUID vedtakReferanse;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "status", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + RelatertYtelseTilstand.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private RelatertYtelseTilstand status;

    /**
     * Saksnummer (fra Arena, Infotrygd, ..).
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer")))
    private Saksnummer saksnummer;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "kilde", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Fagsystem.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private Fagsystem kilde;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "temaUnderkategori", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
            + TemaUnderkategori.DISCRIMINATOR
            + "'"))})
    @ChangeTracked
    private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;

    @OneToMany(mappedBy = "ytelse")
    @ChangeTracked
    private Set<YtelseAnvistEntitet> ytelseAnvist = new LinkedHashSet<>();

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public VedtakYtelseEntitet() {
        // hibernate
    }

    public VedtakYtelseEntitet(VedtattYtelse ytelse) {
        this.ytelseType = ytelse.getYtelseType();
        this.vedtakReferanse = ytelse.getVedtakReferanse();
        this.status = ytelse.getStatus();
        this.periode = ytelse.getPeriode();
        this.saksnummer = ytelse.getSaksnummer();
        this.temaUnderkategori = ytelse.getBehandlingsTema();
        this.kilde = ytelse.getKilde();
        this.ytelseAnvist = ytelse.getYtelseAnvist()
            .stream()
            .map(YtelseAnvistEntitet::new)
            .peek(it -> it.setYtelse(this))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, aktørId, ytelseType, saksnummer, kilde);
    }

    @Override
    public AktørId getAktør() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    @Override
    public RelatertYtelseType getYtelseType() {
        return ytelseType;
    }

    void setYtelseType(RelatertYtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    @Override
    public TemaUnderkategori getBehandlingsTema() {
        return temaUnderkategori;
    }

    void setBehandlingsTema(TemaUnderkategori behandlingsTema) {
        this.temaUnderkategori = behandlingsTema;
    }

    @Override
    public RelatertYtelseTilstand getStatus() {
        return status;
    }

    void setStatus(RelatertYtelseTilstand status) {
        this.status = status;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    void medSakId(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    @Override
    public UUID getVedtakReferanse() {
        return vedtakReferanse;
    }

    void setVedtakReferanse(UUID vedtakReferanse) {
        this.vedtakReferanse = vedtakReferanse;
    }

    @Override
    public Fagsystem getKilde() {
        return kilde;
    }

    void setKilde(Fagsystem kilde) {
        this.kilde = kilde;
    }

    @Override
    public Collection<YtelseAnvist> getYtelseAnvist() {
        return Collections.unmodifiableCollection(ytelseAnvist);
    }

    void leggTilYtelseAnvist(YtelseAnvist ytelseAnvist) {
        YtelseAnvistEntitet ytelseAnvistEntitet = (YtelseAnvistEntitet) ytelseAnvist;
        ytelseAnvistEntitet.setYtelse(this);
        this.ytelseAnvist.add(ytelseAnvistEntitet);

    }

    @Override
    public LocalDateTime getVedtattTidspunkt() {
        return vedtattTidspunkt;
    }

    void setVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        this.vedtattTidspunkt = vedtattTidspunkt;
    }

    void tilbakestillAnvisteYtelser() {
        ytelseAnvist.clear();
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VedtakYtelseEntitet that = (VedtakYtelseEntitet) o;
        return Objects.equals(ytelseType, that.ytelseType) &&
            Objects.equals(temaUnderkategori, that.temaUnderkategori) &&
            (Objects.equals(periode, that.periode) || Objects.equals(periode.getFomDato(), that.periode.getFomDato())) &&
            Objects.equals(saksnummer, that.saksnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseType, periode, saksnummer);
    }

    @Override
    public String toString() {
        return "VedtakYtelseEntitet{" + //$NON-NLS-1$
            "ytelseType=" + ytelseType + //$NON-NLS-1$
            ", typeUnderkategori=" + temaUnderkategori + //$NON-NLS-1$
            ", periode=" + periode + //$NON-NLS-1$
            ", relatertYtelseStatus=" + status + //$NON-NLS-1$
            ", saksNummer='" + saksnummer + '\'' + //$NON-NLS-1$
            '}';
    }

}
