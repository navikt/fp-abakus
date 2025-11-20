package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VedtakYtelseEntitet")
@Table(name = "VEDTAK_YTELSE")
public class VedtakYtelse extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAK_YTELSE")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Convert(converter = YtelseTypeKodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private YtelseType ytelseType;

    @DiffIgnore
    @Column(name = "vedtatt_tidspunkt")
    private LocalDateTime vedtattTidspunkt;

    @NaturalId
    @Column(name = "vedtak_referanse")
    private UUID vedtakReferanse;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ChangeTracked
    @Convert(converter = YtelseStatusKodeverdiConverter.class)
    @Column(name = "status", nullable = false)
    private YtelseStatus status = YtelseStatus.UDEFINERT;

    /**
     * Saksnummer (fra Arena, Infotrygd, ..).
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer")))
    private Saksnummer saksnummer;

    @ChangeTracked
    @Convert(converter = FagsystemKodeverdiConverter.class)
    @Column(name = "kilde", nullable = false)
    private Fagsystem kilde;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = "tilleggsopplysninger")
    private String tilleggsopplysninger;

    @OneToMany(mappedBy = "ytelse")
    @ChangeTracked
    private Set<YtelseAnvist> ytelseAnvist = new LinkedHashSet<>();

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public VedtakYtelse() {
        // hibernate
    }

    public VedtakYtelse(VedtakYtelse ytelse) {
        this.ytelseType = ytelse.getYtelseType();
        this.vedtattTidspunkt = ytelse.getVedtattTidspunkt();
        this.vedtakReferanse = ytelse.getVedtakReferanse();
        this.status = ytelse.getStatus();
        this.periode = ytelse.getPeriode();
        this.saksnummer = ytelse.getSaksnummer();
        this.kilde = ytelse.getKilde();
        this.tilleggsopplysninger = ytelse.getTilleggsopplysninger();
        this.ytelseAnvist = ytelse.getYtelseAnvist()
            .stream()
            .map(YtelseAnvist::new)
            .peek(it -> it.setYtelse(this))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode, aktørId, ytelseType, saksnummer, kilde};
        return IndexKeyComposer.createKey(keyParts);
    }

    public AktørId getAktør() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
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

    public UUID getVedtakReferanse() {
        return vedtakReferanse;
    }

    void setVedtakReferanse(UUID vedtakReferanse) {
        this.vedtakReferanse = vedtakReferanse;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    void setKilde(Fagsystem kilde) {
        this.kilde = kilde;
    }

    public Collection<YtelseAnvist> getYtelseAnvist() {
        return Collections.unmodifiableCollection(ytelseAnvist);
    }

    void leggTilYtelseAnvist(YtelseAnvist ytelseAnvist) {
        ytelseAnvist.setYtelse(this);
        this.ytelseAnvist.add(ytelseAnvist);

    }

    public LocalDateTime getVedtattTidspunkt() {
        return vedtattTidspunkt;
    }

    void setVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        this.vedtattTidspunkt = vedtattTidspunkt;
    }

    public String getTilleggsopplysninger() {
        return tilleggsopplysninger;
    }

    public void setTilleggsopplysninger(String tilleggsopplysninger) {
        this.tilleggsopplysninger = tilleggsopplysninger;
    }

    void tilbakestillAnvisteYtelser() {
        ytelseAnvist.clear();
    }

    boolean getAktiv() {
        return aktiv;
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof VedtakYtelse)) {
            return false;
        }
        var that = (VedtakYtelse) o;
        return Objects.equals(ytelseType, that.ytelseType) && Objects.equals(kilde, that.kilde) && Objects.equals(aktørId, that.aktørId) && (
            Objects.equals(periode, that.periode) || Objects.equals(periode.getFomDato(), that.periode.getFomDato())) && Objects.equals(saksnummer,
            that.saksnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseType, kilde, aktørId, periode, saksnummer);
    }

    @Override
    public String toString() {
        return "VedtakYtelseEntitet{" + "aktørId=" + aktørId + ", ytelseType=" + ytelseType + ", vedtattTidspunkt=" + vedtattTidspunkt
            + ", vedtakReferanse=" + vedtakReferanse + ", periode=" + periode + ", saksnummer=" + saksnummer + ", kilde=" + kilde + '}';
    }
}
