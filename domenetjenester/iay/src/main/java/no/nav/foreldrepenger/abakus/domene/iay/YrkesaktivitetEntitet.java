package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
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

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
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
    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef arbeidsforholdRef) {
        boolean gjelderForArbeidsgiver = Objects.equals(getArbeidsgiver(), arbeidsgiver);
        boolean gjelderFor = gjelderForArbeidsgiver && getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
        return gjelderFor;
    }

    @Override
    public boolean erArbeidsforhold() {
        return ArbeidType.AA_REGISTER_TYPER.contains(arbeidType);
    }

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

}
