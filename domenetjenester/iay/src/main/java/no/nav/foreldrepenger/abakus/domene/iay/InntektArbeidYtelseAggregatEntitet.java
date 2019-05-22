package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;

@Table(name = "IAY_INNTEKT_ARBEID_YTELSER")
@Entity(name = "InntektArbeidYtelser")
public class InntektArbeidYtelseAggregatEntitet extends BaseEntitet implements InntektArbeidYtelseAggregat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKT_ARBEID_YTELSER")
    private Long id;

    @NaturalId
    @DiffIgnore
    @Column(name = "ekstern_referanse", updatable = false, unique = true)
    private UUID eksternReferanse;

    @ChangeTracked
    @OneToMany(mappedBy = "inntektArbeidYtelser")
    private Set<AktørInntektEntitet> aktørInntekt = new LinkedHashSet<>();

    @ChangeTracked
    @OneToMany(mappedBy = "inntektArbeidYtelser")
    private Set<AktørArbeidEntitet> aktørArbeid = new LinkedHashSet<>();

    @ChangeTracked
    @OneToMany(mappedBy = "inntektArbeidYtelser")
    private Set<AktørYtelseEntitet> aktørYtelse = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    InntektArbeidYtelseAggregatEntitet() {
        // hibernate
    }

    InntektArbeidYtelseAggregatEntitet(UUID eksternReferanse, InntektArbeidYtelseAggregat kopierFra) {
        Objects.requireNonNull(eksternReferanse, "eksternReferanse");
        this.setAktørInntekt(kopierFra.getAktørInntekt().stream().map(ai -> {
            AktørInntektEntitet aktørInntektEntitet = new AktørInntektEntitet(ai);
            aktørInntektEntitet.setInntektArbeidYtelser(this);
            return aktørInntektEntitet;
        }).collect(Collectors.toList()));

        this.setAktørArbeid(kopierFra.getAktørArbeid().stream().map(aktørArbied -> {
            AktørArbeidEntitet aktørArbeidEntitet = new AktørArbeidEntitet(aktørArbied);
            aktørArbeidEntitet.setInntektArbeidYtelser(this);
            return aktørArbeidEntitet;
        }).collect(Collectors.toList()));

        this.setAktørYtelse(kopierFra.getAktørYtelse().stream().map(ay -> {
            AktørYtelseEntitet aktørYtelseEntitet = new AktørYtelseEntitet(ay);
            aktørYtelseEntitet.setInntektArbeidYtelser(this);
            return aktørYtelseEntitet;
        }).collect(Collectors.toList()));

        this.eksternReferanse = eksternReferanse;
    }

    InntektArbeidYtelseAggregatEntitet(InntektArbeidYtelseAggregat kopierFra) {
        this(UUID.randomUUID(), kopierFra);
    }

    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    @Override
    public Collection<AktørInntekt> getAktørInntekt() {
        return Collections.unmodifiableSet(aktørInntekt);
    }

    void setAktørInntekt(Collection<AktørInntektEntitet> aktørInntekt) {
        this.aktørInntekt = new LinkedHashSet<>(aktørInntekt);
    }

    void leggTilAktørInntekt(AktørInntekt aktørInntekt) {
        AktørInntektEntitet aktørInntektEntitet = (AktørInntektEntitet) aktørInntekt;
        this.aktørInntekt.add(aktørInntektEntitet);
        aktørInntektEntitet.setInntektArbeidYtelser(this);
    }

    void leggTilAktørArbeid(AktørArbeid aktørArbeid) {
        AktørArbeidEntitet aktørArbeidEntitet = (AktørArbeidEntitet) aktørArbeid;
        this.aktørArbeid.add(aktørArbeidEntitet);
        aktørArbeidEntitet.setInntektArbeidYtelser(this);
    }

    void leggTilAktørYtelse(AktørYtelse aktørYtelse) {
        AktørYtelseEntitet aktørYtelseEntitet = (AktørYtelseEntitet) aktørYtelse;
        this.aktørYtelse.add(aktørYtelseEntitet);
        aktørYtelseEntitet.setInntektArbeidYtelser(this);
    }

    @Override
    public Collection<AktørArbeid> getAktørArbeid() {
        return Collections.unmodifiableSet(aktørArbeid);
    }

    void setAktørArbeid(Collection<AktørArbeidEntitet> aktørArbeid) {
        this.aktørArbeid = new LinkedHashSet<>(aktørArbeid);
    }

    @Override
    public Collection<AktørYtelse> getAktørYtelse() {
        return Collections.unmodifiableSet(aktørYtelse);
    }

    void setAktørYtelse(Collection<AktørYtelseEntitet> aktørYtelse) {
        this.aktørYtelse = new LinkedHashSet<>(aktørYtelse);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof InntektArbeidYtelseAggregatEntitet)) {
            return false;
        }
        InntektArbeidYtelseAggregatEntitet other = (InntektArbeidYtelseAggregatEntitet) obj;
        return Objects.equals(this.getAktørInntekt(), other.getAktørInntekt())
            && Objects.equals(this.getAktørArbeid(), other.getAktørArbeid())
            && Objects.equals(this.getAktørYtelse(), other.getAktørYtelse());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørInntekt, aktørArbeid, aktørYtelse);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "aktørInntekt=" + aktørInntekt +
            ", aktørArbeid=" + aktørArbeid +
            ", aktørYtelse=" + aktørYtelse +
            '>';
    }

    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        for (AktørArbeidEntitet aktør : aktørArbeid) {
            aktør.setSkjæringstidspunkt(skjæringstidspunkt, ventreSide);
        }
        for (AktørInntektEntitet aktør : aktørInntekt) {
            aktør.setSkjæringstidspunkt(skjæringstidspunkt, ventreSide);
        }
        for (AktørYtelseEntitet aktør : aktørYtelse) {
            aktør.setSkjæringstidspunkt(skjæringstidspunkt, ventreSide);
        }
    }

    void taHensynTilOverstyring() {
        for (AktørArbeidEntitet aktør : aktørArbeid) {
            aktør.overstyrAnsettelsesPeriode();
        }
    }

    void taHensynTilBetraktninger(ArbeidsforholdInformasjonEntitet informasjon) {
        for (AktørArbeidEntitet aktør : aktørArbeid) {
            aktør.taHensynTilBetraktninger(informasjon);
        }
    }
}
