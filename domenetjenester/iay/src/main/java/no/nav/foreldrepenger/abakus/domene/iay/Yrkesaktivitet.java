package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.ArbeidTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

@Entity(name = "Yrkesaktivitet")
@Table(name = "IAY_YRKESAKTIVITET")
public class Yrkesaktivitet extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YRKESAKTIVITET")
    private Long id;

    @OneToMany(mappedBy = "yrkesaktivitet")
    @ChangeTracked
    private Set<AktivitetsAvtale> aktivitetsAvtale = new LinkedHashSet<>();

    @OneToMany(mappedBy = "yrkesaktivitet")
    @ChangeTracked
    private Set<Permisjon> permisjon = new LinkedHashSet<>();

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
    private AktørArbeid aktørArbeid;

    @ChangeTracked
    @Convert(converter = ArbeidTypeKodeverdiConverter.class)
    @Column(name = "arbeid_type", nullable = false, updatable = false)
    private ArbeidType arbeidType;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Yrkesaktivitet() {
        // hibernate
    }

    public Yrkesaktivitet(Yrkesaktivitet yrkesaktivitet) {
        this.arbeidType = yrkesaktivitet.getArbeidType();
        this.arbeidsgiver = yrkesaktivitet.getArbeidsgiver();
        this.arbeidsforholdRef = yrkesaktivitet.getArbeidsforholdRef();
        this.navnArbeidsgiverUtland = yrkesaktivitet.getNavnArbeidsgiverUtland();

        this.aktivitetsAvtale = yrkesaktivitet.aktivitetsAvtale.stream().map(aa -> {
            AktivitetsAvtale aktivitetsAvtaleEntitet = new AktivitetsAvtale(aa);
            aktivitetsAvtaleEntitet.setYrkesaktivitet(this);
            return aktivitetsAvtaleEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));

        this.permisjon = yrkesaktivitet.permisjon.stream().map(p -> {
            Permisjon permisjon = new Permisjon(p);
            permisjon.setYrkesaktivitet(this);
            return permisjon;
        }).collect(Collectors.toCollection(LinkedHashSet::new));

    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { arbeidsgiver, arbeidsforholdRef, arbeidType };
        return IndexKeyComposer.createKey(keyParts);
    }

    void setAktørArbeid(AktørArbeid aktørArbeid) {
        this.aktørArbeid = aktørArbeid;
    }

    /**
     * Kategorisering av aktivitet som er enten pensjonsgivende inntekt eller likestilt med pensjonsgivende inntekt
     * <p>
     * Fra aa-reg
     * <ul>
     * <li>{@value ArbeidType#ORDINÆRT_ARBEIDSFORHOLD}</li>
     * <li>{@value ArbeidType#MARITIMT_ARBEIDSFORHOLD}</li>
     * <li>{@value ArbeidType#FORENKLET_OPPGJØRSORDNING}</li>
     * </ul>
     * <p>
     * Fra inntektskomponenten
     * <ul>
     * <li>{@value ArbeidType#FRILANSER_OPPDRAGSTAKER_MED_MER}</li>
     * </ul>
     * <p>
     * De resterende kommer fra søknaden
     *
     * @return {@link ArbeidType}
     */
    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    void setArbeidType(ArbeidType arbeidType) {
        this.arbeidType = arbeidType;
    }

    /**
     * Unik identifikator for arbeidsforholdet til aktøren i bedriften. Selve nøkkelen er ikke unik, men er unik for arbeidstaker hos arbeidsgiver.
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return referanse
     */
    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    void setArbeidsforholdId(InternArbeidsforholdRef arbeidsforholdId) {
        this.arbeidsforholdRef = arbeidsforholdId;
    }

    /**
     * Liste over fremtidige / historiske permisjoner hos arbeidsgiver.
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return liste med permisjoner
     */
    public Collection<Permisjon> getPermisjon() {
        return Collections.unmodifiableSet(permisjon);
    }

    void leggTilPermisjon(Permisjon permisjon) {
        this.permisjon.add(permisjon);
        permisjon.setYrkesaktivitet(this);
    }

    /**
     * Alle aktivitetsavtaler
     */
    public Collection<AktivitetsAvtale> getAlleAktivitetsAvtaler() {
        return Collections.unmodifiableSet(aktivitetsAvtale);
    }

    void leggTilAktivitetsAvtale(AktivitetsAvtale aktivitetsAvtale) {
        AktivitetsAvtale aktivitetsAvtaleEntitet = aktivitetsAvtale;
        this.aktivitetsAvtale.add(aktivitetsAvtaleEntitet);
        aktivitetsAvtaleEntitet.setYrkesaktivitet(this);
    }

    /**
     * ArbeidsgiverEntitet
     * <p>
     * NB! Vil kun forekomme i aktiviteter som er hentet inn fra aa-reg
     *
     * @return {@link ArbeidsgiverEntitet}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    /**
     * Navn på utenlands arbeidsgiver
     *
     * @return Navn
     */
    public String getNavnArbeidsgiverUtland() {
        return navnArbeidsgiverUtland;
    }

    void setNavnArbeidsgiverUtland(String navnArbeidsgiverUtland) {
        this.navnArbeidsgiverUtland = navnArbeidsgiverUtland;
    }

    /**
     * Identifiser om yrkesaktiviteten gjelder for arbeidsgiver og arbeidsforholdRef.
     *
     * @param arbeidsgiver en {@link Arbeidsgiver}
     * @param arbeidsforholdRef et {@link InternArbeidsforholdRef}
     * @return true hvis arbeidsgiver og arbeidsforholdRef macther
     */
    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef arbeidsforholdRef) {
        boolean gjelderForArbeidsgiver = Objects.equals(getArbeidsgiver(), arbeidsgiver);
        boolean gjelderFor = gjelderForArbeidsgiver && getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
        return gjelderFor;
    }

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
        if (erYrkesaktivitetMedLegacyInnhold()) {
            this.aktivitetsAvtale = aktivitetsAvtale.stream()
                .filter(this::erLegacyAktivitetsAvtale)
                .collect(Collectors.toSet());
        } else {
            aktivitetsAvtale.clear();
        }
    }

    /*
     * Her legger man inn data som er innhentet tidligere, men som ikke blir reinnhentet etter sanering av integrasjon eller endring av logikk
     * For Yrkesaktivitet gjelder dette frilansaktiviteter innhentet fra Inntektskomponenten
     */
    static final LocalDate CUTOFF_FRILANS_AAREG = LocalDate.of(2020,1,1);

    boolean erYrkesaktivitetMedLegacyInnhold() {
        return ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER.equals(arbeidType)
            && aktivitetsAvtale.stream().anyMatch(this::erLegacyAktivitetsAvtale);
    }

    private boolean erLegacyAktivitetsAvtale(AktivitetsAvtale avtale) {
        return avtale.getPeriode().getTomDato() != null && CUTOFF_FRILANS_AAREG.isAfter(avtale.getPeriode().getTomDato());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Yrkesaktivitet)) {
            return false;
        }
        Yrkesaktivitet other = (Yrkesaktivitet) obj;
        return Objects.equals(this.getArbeidsforholdRef(), other.getArbeidsforholdRef()) &&
            Objects.equals(this.getNavnArbeidsgiverUtland(), other.getNavnArbeidsgiverUtland()) &&
            Objects.equals(this.getArbeidType(), other.getArbeidType()) &&
            Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getArbeidsforholdRef(), getNavnArbeidsgiverUtland(), getArbeidType(), getArbeidsgiver());
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

    void fjernPeriode(IntervallEntitet aktivitetsPeriode) {
        aktivitetsAvtale.removeIf(aa -> aa.matcherPeriode(aktivitetsPeriode));
    }

}
