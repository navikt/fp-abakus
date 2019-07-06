package no.nav.foreldrepenger.abakus.domene.iay;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyrtePerioderEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.AntallTimer;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Yrkesaktivitet")
@Table(name = "IAY_YRKESAKTIVITET")
public class YrkesaktivitetEntitet extends BaseEntitet implements Yrkesaktivitet, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YRKESAKTIVITET")
    private Long id;

    @OneToMany(mappedBy = "yrkesaktivitet")
    @ChangeTracked
    private Set<AktivitetsAvtaleEntitet> aktivitetsAvtale = new LinkedHashSet<>();

    @OneToMany(mappedBy = "yrkesaktivitet")
    @ChangeTracked
    private Set<PermisjonEntitet> permisjon = new LinkedHashSet<>();

    @Column(name = "NAVN_ARBEIDSGIVER_UTLAND")
    @ChangeTracked
    private String navnArbeidsgiverUtland;

    /**
     * Kan være privat eller virksomhet som arbeidsgiver. Dersom {@link #arbeidType} = 'NÆRING', er denne null.
     */
    @Embedded
    @ChangeTracked
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aktoer_arbeid_id", nullable = false, updatable = false)
    private AktørArbeidEntitet aktørArbeid;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "arbeid_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ArbeidType.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private ArbeidType arbeidType;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public YrkesaktivitetEntitet() {
        // hibernate
    }

    public YrkesaktivitetEntitet(Yrkesaktivitet yrkesaktivitet) {
        final YrkesaktivitetEntitet yrkesaktivitetEntitet = (YrkesaktivitetEntitet) yrkesaktivitet; // NOSONAR
        this.arbeidType = yrkesaktivitetEntitet.getArbeidType();
        this.arbeidsgiver = yrkesaktivitetEntitet.getArbeidsgiver();
        this.arbeidsforholdRef = yrkesaktivitetEntitet.getArbeidsforholdRef();

        this.aktivitetsAvtale = yrkesaktivitetEntitet.aktivitetsAvtale.stream().map(aa -> {
            AktivitetsAvtaleEntitet aktivitetsAvtaleEntitet = new AktivitetsAvtaleEntitet(aa);
            aktivitetsAvtaleEntitet.setYrkesaktivitet(this);
            return aktivitetsAvtaleEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));

        this.permisjon = yrkesaktivitetEntitet.permisjon.stream().map(p -> {
            PermisjonEntitet permisjonEntitet = new PermisjonEntitet(p);
            permisjonEntitet.setYrkesaktivitet(this);
            return permisjonEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(arbeidsgiver, arbeidsforholdRef, arbeidType);
    }

    void setAktørArbeid(AktørArbeidEntitet aktørArbeid) {
        this.aktørArbeid = aktørArbeid;
    }

    @Override
    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    void setArbeidType(ArbeidType arbeidType) {
        this.arbeidType = arbeidType;
    }

    @Override
    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    void setArbeidsforholdId(InternArbeidsforholdRef arbeidsforholdId) {
        this.arbeidsforholdRef = arbeidsforholdId;
    }

    @Override
    public Collection<Permisjon> getPermisjon() {
        return Collections.unmodifiableSet(permisjon);
    }

    void leggTilPermisjon(Permisjon permisjon) {
        PermisjonEntitet permisjonEntitet = (PermisjonEntitet) permisjon;
        this.permisjon.add(permisjonEntitet);
        permisjonEntitet.setYrkesaktivitet(this);
    }

    @Override
    public Collection<AktivitetsAvtale> getAktivitetsAvtalerForArbeid() {
        return aktivitetsAvtale.stream()
            .filter(av -> (!this.erArbeidsforhold() || !av.erAnsettelsesPeriode()))
            .filter(AktivitetsAvtaleEntitet::skalMedEtterSkjæringstidspunktVurdering)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<AktivitetsAvtale> getAlleAktivitetsAvtaler() {
        return Collections.unmodifiableSet(aktivitetsAvtale);
    }

    void leggTilAktivitetsAvtale(AktivitetsAvtale aktivitetsAvtale) {
        AktivitetsAvtaleEntitet aktivitetsAvtaleEntitet = (AktivitetsAvtaleEntitet) aktivitetsAvtale;
        this.aktivitetsAvtale.add(aktivitetsAvtaleEntitet);
        aktivitetsAvtaleEntitet.setYrkesaktivitet(this);
    }

    @Override
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @Override
    public String getNavnArbeidsgiverUtland() {
        return navnArbeidsgiverUtland;
    }

    void setNavnArbeidsgiverUtland(String navnArbeidsgiverUtland) {
        this.navnArbeidsgiverUtland = navnArbeidsgiverUtland;
    }

    @Override
    public List<AktivitetsAvtale> getAnsettelsesPerioder() {
        if (erArbeidsforhold()) {
            List<AktivitetsAvtale> overstyrtAnsettelsesperiode = hentOverstyrteAnsettelsesperioder();
            return overstyrtAnsettelsesperiode.isEmpty() ? hentAnsettelsesperioder() : overstyrtAnsettelsesperiode;
        }
        return Collections.emptyList();
    }

    private List<AktivitetsAvtale> hentAnsettelsesperioder() {
        return aktivitetsAvtale.stream()
            .filter(AktivitetsAvtale::erAnsettelsesPeriode)
            .collect(Collectors.toList());
    }

    private List<AktivitetsAvtale> hentOverstyrteAnsettelsesperioder() {
        return aktivitetsAvtale.stream()
            .filter(avtale -> avtale.getOverstyrtPeriode() != null)
            .filter(AktivitetsAvtale::erAnsettelsesPeriode)
            .collect(Collectors.toList());
    }

    @Override
    public boolean erArbeidsforhold() {
        return ArbeidType.AA_REGISTER_TYPER.contains(arbeidType);
    }

    @Override
    public Long getId() {
        return id;
    }

    void tilbakestillPermisjon() {
        permisjon.clear();
    }

    void tilbakestillAvtaler() {
        aktivitetsAvtale.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof YrkesaktivitetEntitet)) {
            return false;
        }
        YrkesaktivitetEntitet other = (YrkesaktivitetEntitet) obj;
        return Objects.equals(this.getArbeidsforholdRef(), other.getArbeidsforholdRef()) &&
            Objects.equals(this.getNavnArbeidsgiverUtland(), other.getNavnArbeidsgiverUtland()) &&
            Objects.equals(this.getArbeidType(), other.getArbeidType()) &&
            Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver());
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforholdRef, getNavnArbeidsgiverUtland(), getArbeidType(), getArbeidsgiver());
    }

    @Override
    public String toString() {
        return "YrkesaktivitetEntitet{" +
            "id=" + id +
            ", arbeidsgiver=" + arbeidsgiver +
            ", arbeidsforholdRef=" + arbeidsforholdRef +
            ", arbeidType=" + arbeidType +
            '}';
    }

    void fjernPeriode(DatoIntervallEntitet aktivitetsPeriode) {
        aktivitetsAvtale.removeIf(aa -> aa.matcherPeriode(aktivitetsPeriode));
    }

    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        for (AktivitetsAvtaleEntitet avtale : aktivitetsAvtale) {
            avtale.setSkjæringstidspunkt(skjæringstidspunkt, ventreSide);
        }
    }

    void setOverstyrtPeriode(ArbeidsforholdOverstyringEntitet overstyring) {
        Optional<ArbeidsforholdOverstyrtePerioderEntitet> overstyrtPeriode = finnOverstyrtPeriode(overstyring);
        overstyrtPeriode.ifPresent(arbeidsforholdOverstyrtePerioderEntitet -> aktivitetsAvtale.stream()
            .filter(AktivitetsAvtaleEntitet::erAnsettelsesPeriode)
            .filter(AktivitetsAvtale::getErLøpende)
            .forEach(avtale -> avtale.setOverstyrtPeriode(arbeidsforholdOverstyrtePerioderEntitet.getOverstyrtePeriode())));
    }

    private Optional<ArbeidsforholdOverstyrtePerioderEntitet> finnOverstyrtPeriode(ArbeidsforholdOverstyringEntitet overstyring) {
        return overstyring.getArbeidsforholdOverstyrtePerioder().stream()
            .filter(p -> p.getOverstyrtePeriode().getTomDato() != null)
            .filter(p -> !p.getOverstyrtePeriode().getTomDato().isEqual(TIDENES_ENDE))
            .findFirst(); // TODO : Kanskje ikke findFirst her? Kan i teorien være flere ved feil data
    }

    public static class PermisjonBuilder {
        private final PermisjonEntitet permisjonEntitet;

        PermisjonBuilder(PermisjonEntitet permisjonEntitet) {
            this.permisjonEntitet = permisjonEntitet;
        }

        static PermisjonBuilder ny() {
            return new PermisjonBuilder(new PermisjonEntitet());
        }

        public PermisjonBuilder medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
            this.permisjonEntitet.setPermisjonsbeskrivelseType(permisjonsbeskrivelseType);
            return this;
        }

        public PermisjonBuilder medProsentsats(BigDecimal prosentsats) {
            this.permisjonEntitet.setProsentsats(new Stillingsprosent(prosentsats));
            return this;
        }

        public PermisjonBuilder medPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
            this.permisjonEntitet.setPeriode(fraOgMed, tilOgMed);
            return this;
        }

        public Permisjon build() {
            if (permisjonEntitet.hasValues()) {
                return permisjonEntitet;
            }
            throw new IllegalStateException();
        }

        public PermisjonBuilder medPermisjonsbeskrivelseType(String kode) {
            return medPermisjonsbeskrivelseType(new PermisjonsbeskrivelseType(kode));
        }
    }

    public static class AktivitetsAvtaleBuilder {
        private final AktivitetsAvtaleEntitet aktivitetsAvtaleEntitet;
        private boolean oppdatering = false;

        AktivitetsAvtaleBuilder(AktivitetsAvtale aktivitetsAvtaleEntitet, boolean oppdatering) {
            this.aktivitetsAvtaleEntitet = (AktivitetsAvtaleEntitet) aktivitetsAvtaleEntitet; // NOSONAR
            this.oppdatering = oppdatering;
        }

        public static AktivitetsAvtaleBuilder ny() {
            return new AktivitetsAvtaleBuilder(new AktivitetsAvtaleEntitet(), false);
        }

        static AktivitetsAvtaleBuilder oppdater(Optional<AktivitetsAvtale> aktivitetsAvtale) {
            return new AktivitetsAvtaleBuilder(aktivitetsAvtale.orElse(new AktivitetsAvtaleEntitet()), aktivitetsAvtale.isPresent());
        }

        public AktivitetsAvtaleBuilder medProsentsats(Stillingsprosent prosentsats) {
            this.aktivitetsAvtaleEntitet.setProsentsats(prosentsats);
            return this;
        }

        public AktivitetsAvtaleBuilder medProsentsats(BigDecimal prosentsats) {
            this.aktivitetsAvtaleEntitet.setProsentsats(prosentsats == null ? null : new Stillingsprosent(prosentsats));
            return this;
        }
        
        public AktivitetsAvtaleBuilder medAntallTimer(BigDecimal antallTimer) {
            this.aktivitetsAvtaleEntitet.setAntallTimer(antallTimer == null ? null : new AntallTimer(antallTimer));
            return this;
        }

        public AktivitetsAvtaleBuilder medAntallTimerFulltid(BigDecimal antallTimerFulltid) {
            this.aktivitetsAvtaleEntitet.setAntallTimerFulltid(antallTimerFulltid == null ? null : new AntallTimer(antallTimerFulltid));
            return this;
        }

        public AktivitetsAvtaleBuilder medPeriode(DatoIntervallEntitet periode) {
            this.aktivitetsAvtaleEntitet.setPeriode(periode);
            return this;
        }

        public AktivitetsAvtaleBuilder medBeskrivelse(String begrunnelse) {
            this.aktivitetsAvtaleEntitet.setBeskrivelse(begrunnelse);
            return this;
        }

        public AktivitetsAvtale build() {
            if (aktivitetsAvtaleEntitet.hasValues()) {
                return aktivitetsAvtaleEntitet;
            }
            throw new IllegalStateException();
        }

        public boolean isOppdatering() {
            return oppdatering;
        }

        public AktivitetsAvtaleBuilder medSisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
            this.aktivitetsAvtaleEntitet.sisteLønnsendringsdato(sisteLønnsendringsdato);
            return this;
        }
    }
}
