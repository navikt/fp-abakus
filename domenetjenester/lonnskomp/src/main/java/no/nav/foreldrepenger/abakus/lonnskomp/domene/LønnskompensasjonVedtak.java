package no.nav.foreldrepenger.abakus.lonnskomp.domene;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvist;

@Entity(name = "LonnskompVedtakEntitet")
@Table(name = "LONNSKOMP_VEDTAK")
public class LønnskompensasjonVedtak extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_LONNSKOMP_VEDTAK")
    private Long id;

    @ChangeTracked
    @Column(name = "sakid", nullable = false, updatable = false)
    private String sakId;  // Eg en ULID

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @ChangeTracked
    @Embedded
    private OrgNummer orgNummer;

    @DiffIgnore
    @Column(name = "forrige_vedtak_dato")
    private LocalDate forrigeVedtakDato;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep", nullable = false)))
    @ChangeTracked
    private Beløp beløp;

    @OneToMany(mappedBy = "vedtak")
    @ChangeTracked
    private Set<LønnskompensasjonAnvist> anvistePerioder = new LinkedHashSet<>();

    @Column(name = "aktiv", nullable = false)
    private Boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public LønnskompensasjonVedtak() {
        // hibernate
    }

    public LønnskompensasjonVedtak(LønnskompensasjonVedtak ytelse) {
        this.sakId = ytelse.getSakId();
        this.aktørId = ytelse.getAktørId();
        this.orgNummer = ytelse.getOrgNummer();
        this.forrigeVedtakDato = ytelse.getForrigeVedtakDato();
        this.periode = ytelse.getPeriode();
        this.beløp = ytelse.getBeløp();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { periode, aktørId, sakId, };
        return IndexKeyComposer.createKey(keyParts);
    }

    public String getSakId() {
        return sakId;
    }

    public void setSakId(String sakId) {
        this.sakId = sakId;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public OrgNummer getOrgNummer() {
        return orgNummer;
    }

    public void setOrgNummer(OrgNummer orgNummer) {
        this.orgNummer = orgNummer;
    }

    public LocalDate getForrigeVedtakDato() {
        return forrigeVedtakDato;
    }

    public void setForrigeVedtakDato(LocalDate forrigeVedtakDato) {
        this.forrigeVedtakDato = forrigeVedtakDato;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    boolean getAktiv() {
        return aktiv;
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public Set<LønnskompensasjonAnvist> getAnvistePerioder() {
        return anvistePerioder;
    }

    public void leggTilAnvistPeriode(LønnskompensasjonAnvist anvist) {
        anvist.setVedtak(this);
        this.anvistePerioder.add(anvist);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LønnskompensasjonVedtak that = (LønnskompensasjonVedtak) o;
        return Objects.equals(sakId, that.sakId) &&
            Objects.equals(aktørId, that.aktørId) &&
            Objects.equals(orgNummer, that.orgNummer) &&
            Objects.equals(forrigeVedtakDato, that.forrigeVedtakDato) &&
            Objects.equals(periode, that.periode) &&
            Objects.equals(beløp, that.beløp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sakId, aktørId, orgNummer, forrigeVedtakDato, periode, beløp);
    }

    @Override
    public String toString() {
        return "LønnskompensasjonVedtak{" +
            "sakId='" + sakId + '\'' +
            ", aktørId=" + aktørId +
            ", orgNummer=" + orgNummer +
            ", forrigeVedtakDato=" + forrigeVedtakDato +
            ", periode=" + periode +
            ", beløp=" + beløp +
            '}';
    }
}
