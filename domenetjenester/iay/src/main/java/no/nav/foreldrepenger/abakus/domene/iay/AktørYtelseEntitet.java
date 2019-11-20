package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Collection;
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

import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Table(name = "IAY_AKTOER_YTELSE")
@Entity(name = "AktørYtelse")
public class AktørYtelseEntitet extends BaseEntitet implements AktørYtelse, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTOER_YTELSE")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_arbeid_ytelser_id", nullable = false, updatable = false)
    private InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser;

    @OneToMany(mappedBy = "aktørYtelse")
    @ChangeTracked
    private Set<YtelseEntitet> ytelser = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public AktørYtelseEntitet() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørYtelseEntitet(AktørYtelse aktørYtelse) {
        this.aktørId = aktørYtelse.getAktørId();
        this.ytelser = aktørYtelse.getAlleYtelser().stream().map(ytelse -> {
            YtelseEntitet ytelseEntitet = new YtelseEntitet(ytelse);
            ytelseEntitet.setAktørYtelse(this);
            return ytelseEntitet;
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
    public Collection<Ytelse> getAlleYtelser() {
        return List.copyOf(ytelser);
    }

    void setInntektArbeidYtelser(InntektArbeidYtelseAggregatEntitet inntektArbeidYtelser) {
        this.inntektArbeidYtelser = inntektArbeidYtelser;
    }

    boolean hasValues() {
        return ytelser != null && !ytelser.isEmpty();
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem, YtelseType type, Saksnummer saksnummer) {
        Optional<Ytelse> ytelse = getAlleYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type) && (saksnummer.equals(ya.getSaksnummer())))
            .findFirst();
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medSaksnummer(saksnummer);
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem, YtelseType type, Saksnummer saksnummer, DatoIntervallEntitet periode, Optional<LocalDate> tidligsteAnvistFom) {
        // OBS kan være flere med samme Saksnummer+FOM: Konvensjon ifm satsjustering
        List<Ytelse> aktuelleYtelser = getAlleYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type) && (saksnummer.equals(ya.getSaksnummer())
                && periode.getFomDato().equals(ya.getPeriode().getFomDato())))
            .collect(Collectors.toList());
        Optional<Ytelse> ytelse = aktuelleYtelser.stream()
            .filter(ya -> periode.equals(ya.getPeriode()))
            .findFirst();
        if (ytelse.isEmpty() && !aktuelleYtelser.isEmpty()) {
            // Håndtere endret TOM-dato som regel ifm at ytelsen er opphørt. Hvis flere med samme FOM-dato sjekk anvist-fom
            if (tidligsteAnvistFom.isPresent()) {
                ytelse = aktuelleYtelser.stream()
                    .filter(yt -> yt.getYtelseAnvist().stream().anyMatch(ya -> tidligsteAnvistFom.get().equals(ya.getAnvistFOM())))
                    .findFirst();
            }
            if (ytelse.isEmpty()) {
                ytelse = aktuelleYtelser.stream().filter(yt -> yt.getYtelseAnvist().isEmpty()).findFirst();
            }
        }
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medSaksnummer(saksnummer);
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem, YtelseType type, TemaUnderkategori typeKategori, DatoIntervallEntitet periode) {
        Optional<Ytelse> ytelse = getAlleYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type)
                && ya.getBehandlingsTema().equals(typeKategori) && (periode.getFomDato().equals(ya.getPeriode().getFomDato())))
            .findFirst();
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medPeriode(periode);
    }

    void leggTilYtelse(Ytelse ytelse) {
        YtelseEntitet ytelseEntitet = (YtelseEntitet) ytelse;
        this.ytelser.add(ytelseEntitet);
        ytelseEntitet.setAktørYtelse(this);
    }

    void fjernYtelse(Ytelse ytelse) {
        YtelseEntitet ytelseEntitet = (YtelseEntitet) ytelse;
        ytelseEntitet.setAktørYtelse(null);
        this.ytelser.remove(ytelseEntitet);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørYtelseEntitet)) {
            return false;
        }
        AktørYtelseEntitet other = (AktørYtelseEntitet) obj;
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
            ", ytelser=" + ytelser +
            '>';
    }

}
