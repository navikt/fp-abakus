package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.TemaUnderkategoriKodeverdiConverter;
import no.nav.foreldrepenger.abakus.iay.jpa.YtelseStatusKodeverdiConverter;
import no.nav.foreldrepenger.abakus.iay.jpa.YtelseTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.FagsystemKodeverdiConverter;

@Entity(name = "YtelseEntitet")
@Table(name = "IAY_RELATERT_YTELSE")
public class Ytelse extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE")
    private Long id;

    @OneToOne(mappedBy = "ytelse")
    private YtelseGrunnlag ytelseGrunnlag;

    @Convert(converter = YtelseTypeKodeverdiConverter.class)
    @Column(name="ytelse_type", nullable = false)
    private YtelseType relatertYtelseType;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ChangeTracked
    @Convert(converter = YtelseStatusKodeverdiConverter.class)
    @Column(name="status", nullable = false)
    private YtelseStatus status;

    /**
     * Saksnummer (fra Arena, Infotrygd, ..).
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer")))
    private Saksnummer saksnummer;

    @ChangeTracked
    @Convert(converter= FagsystemKodeverdiConverter.class)
    @Column(name="kilde", nullable = false)
    private Fagsystem kilde;

    @Convert(converter = TemaUnderkategoriKodeverdiConverter.class)
    @Column(name="temaUnderkategori", nullable = false)
    @ChangeTracked
    private TemaUnderkategori temaUnderkategori = TemaUnderkategori.UDEFINERT;

    @OneToMany(mappedBy = "ytelse")
    @ChangeTracked
    private Set<YtelseAnvist> ytelseAnvist = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "aktoer_ytelse_id", nullable = false, updatable = false)
    private AktørYtelse aktørYtelse;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Ytelse() {
        // hibernate
    }

    public Ytelse(Ytelse ytelse) {
        this.relatertYtelseType = ytelse.getRelatertYtelseType();
        this.status = ytelse.getStatus();
        this.periode = ytelse.getPeriode();
        this.saksnummer = ytelse.getSaksnummer();
        this.temaUnderkategori = ytelse.getBehandlingsTema();
        this.kilde = ytelse.getKilde();
        ytelse.getYtelseGrunnlag().ifPresent(yg -> {
            YtelseGrunnlag ygn = new YtelseGrunnlag(yg);
            ygn.setYtelse(this);
            this.ytelseGrunnlag = ygn;
        });
        this.ytelseAnvist = ytelse.getYtelseAnvist()
            .stream()
            .map(YtelseAnvist::new)
            .peek(it -> it.setYtelse(this))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { periode, relatertYtelseType, saksnummer };
        return IndexKeyComposer.createKey(keyParts);
    }

    void setAktørYtelse(AktørYtelse aktørYtelse) {
        this.aktørYtelse = aktørYtelse;
    }

    public YtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    void setRelatertYtelseType(YtelseType relatertYtelseType) {
        this.relatertYtelseType = relatertYtelseType;
    }

    public TemaUnderkategori getBehandlingsTema() {
        return temaUnderkategori;
    }

    void setBehandlingsTema(TemaUnderkategori behandlingsTema) {
        this.temaUnderkategori = behandlingsTema;
    }

    public YtelseStatus getStatus() {
        return status;
    }

    void setStatus(YtelseStatus status) {
        this.status = status;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    void medSakId(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    void setKilde(Fagsystem kilde) {
        this.kilde = kilde;
    }

    public Optional<YtelseGrunnlag> getYtelseGrunnlag() {
        return Optional.ofNullable(ytelseGrunnlag);
    }

    void setYtelseGrunnlag(YtelseGrunnlag ytelseGrunnlag) {
        if (ytelseGrunnlag != null) {
            ytelseGrunnlag.setYtelse(this);
            this.ytelseGrunnlag = ytelseGrunnlag;
        }
    }

    public Collection<YtelseAnvist> getYtelseAnvist() {
        return Collections.unmodifiableCollection(ytelseAnvist);
    }

    void leggTilYtelseAnvist(YtelseAnvist ytelseAnvist) {
        ytelseAnvist.setYtelse(this);
        this.ytelseAnvist.add(ytelseAnvist);

    }

    void tilbakestillAnvisteYtelser() {
        ytelseAnvist.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof Ytelse))
            return false;
        var that = (Ytelse) o;
        return Objects.equals(relatertYtelseType, that.relatertYtelseType) &&
            Objects.equals(temaUnderkategori, that.temaUnderkategori) &&
            (Objects.equals(periode, that.periode) || Objects.equals(periode.getFomDato(), that.periode.getFomDato())) &&
            Objects.equals(saksnummer, that.saksnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relatertYtelseType, temaUnderkategori, periode, saksnummer);
    }

    @Override
    public String toString() {
        return "YtelseEntitet{" + //$NON-NLS-1$
            "relatertYtelseType=" + relatertYtelseType + //$NON-NLS-1$
            ", typeUnderkategori=" + temaUnderkategori + //$NON-NLS-1$
            ", periode=" + periode + //$NON-NLS-1$
            ", relatertYtelseStatus=" + status + //$NON-NLS-1$
            ", saksNummer='" + saksnummer + '\'' + //$NON-NLS-1$
            '}';
    }
}
