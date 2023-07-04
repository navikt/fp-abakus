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

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@Table(name = "IAY_AKTOER_YTELSE")
@Entity(name = "AktørYtelse")
public class AktørYtelse extends BaseEntitet implements IndexKey {

    /**
     * Her legger man inn ytelser/kilder som er innhentet tidligere, men som ikke blir reinnhentet etter sanering av integrasjon
     * Nye søknader vil ikke ha disse i opptjeningen -> saner integrasjon.
     * - SVP Siste SVP / Infotrygd ble innvilget høst 2019 og løp ut mars 2020.
     * - FP Siste utbetaling av foreldrepenger var tom januar 2022
     */
    private static final Set<YtelseType> UTGÅTT_INFOTRYGD = Set.of(YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER);
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTOER_YTELSE")
    private Long id;
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_arbeid_ytelser_id", nullable = false, updatable = false)
    private InntektArbeidYtelseAggregat inntektArbeidYtelser;
    @OneToMany(mappedBy = "aktørYtelse")
    @ChangeTracked
    private Set<Ytelse> ytelser = new LinkedHashSet<>();
    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public AktørYtelse() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørYtelse(AktørYtelse aktørYtelse) {
        this.aktørId = aktørYtelse.getAktørId();
        this.ytelser = aktørYtelse.getAlleYtelser().stream().map(ytelse -> {
            Ytelse ytelseEntitet = new Ytelse(ytelse);
            ytelseEntitet.setAktørYtelse(this);
            return ytelseEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean beholdLegacyYtelseFraKilde(Ytelse y) {
        return Fagsystem.INFOTRYGD.equals(y.getKilde()) && UTGÅTT_INFOTRYGD.contains(y.getRelatertYtelseType());
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {getAktørId()};
        return IndexKeyComposer.createKey(keyParts);
    }

    /**
     * Aktøren tilstøtende ytelser gjelder for
     *
     * @return aktørId
     */
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    /**
     * Alle tilstøende ytelser (ufiltrert).
     */
    public Collection<Ytelse> getAlleYtelser() {
        return List.copyOf(ytelser);
    }

    void setInntektArbeidYtelser(InntektArbeidYtelseAggregat inntektArbeidYtelser) {
        this.inntektArbeidYtelser = inntektArbeidYtelser;
    }

    boolean hasValues() {
        return ytelser != null && !ytelser.isEmpty();
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem, YtelseType type, Saksnummer saksnummer) {
        Optional<Ytelse> ytelse = getAlleYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type) && ya.getSaksreferanse().equals(saksnummer))
            .findFirst();
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medSaksreferanse(saksnummer);
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem,
                                          YtelseType type,
                                          Saksnummer saksnummer,
                                          IntervallEntitet periode,
                                          Optional<LocalDate> tidligsteAnvistFom) {
        // OBS kan være flere med samme Saksnummer+FOM: Konvensjon ifm satsjustering
        List<Ytelse> aktuelleYtelser = getAlleYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type) && (saksnummer.equals(ya.getSaksreferanse())
                && periode.getFomDato().equals(ya.getPeriode().getFomDato())))
            .collect(Collectors.toList());
        Optional<Ytelse> ytelse = aktuelleYtelser.stream().filter(ya -> periode.equals(ya.getPeriode())).findFirst();
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
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medSaksreferanse(saksnummer);
    }

    YtelseBuilder getYtelseBuilderForType(Fagsystem fagsystem,
                                          YtelseType type,
                                          TemaUnderkategori typeKategori,
                                          IntervallEntitet periode,
                                          Optional<LocalDate> tidligsteAnvistFom) {
        // OBS kan være flere med samme Tema/TUK+FOM: Konvensjon ifm rammevedtak BS
        List<Ytelse> aktuelleYtelser = getAlleYtelser().stream()
            .filter(ya -> ya.getKilde().equals(fagsystem) && ya.getRelatertYtelseType().equals(type) && ya.getBehandlingsTema().equals(typeKategori)
                && (periode.getFomDato().equals(ya.getPeriode().getFomDato())))
            .collect(Collectors.toList());
        Optional<Ytelse> ytelse = aktuelleYtelser.stream().filter(ya -> periode.equals(ya.getPeriode())).findFirst();
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
        return YtelseBuilder.oppdatere(ytelse).medYtelseType(type).medKilde(fagsystem).medPeriode(periode).medBehandlingsTema(typeKategori);
    }

    void leggTilYtelse(Ytelse ytelse) {
        this.ytelser.add(ytelse);
        ytelse.setAktørYtelse(this);
    }

    void tilbakestillYtelser() {
        this.ytelser = ytelser.stream().filter(AktørYtelse::beholdLegacyYtelseFraKilde).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørYtelse)) {
            return false;
        }
        AktørYtelse other = (AktørYtelse) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "aktørId=" + aktørId + ", ytelser=" + ytelser + '>';
    }

}
