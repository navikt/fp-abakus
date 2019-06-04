package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

/** Gjelder ekstra informasjon om arbeidsforhold (overstyringer, angitte eksterne/interne referanser for arbeidsforhold). */
@Table(name = "IAY_INFORMASJON")
@Entity(name = "ArbeidsforholdInformasjon")
public class ArbeidsforholdInformasjonEntitet extends BaseEntitet implements ArbeidsforholdInformasjon {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_INFORMASJON")
    private Long id;

    @ChangeTracked
    @OneToMany(mappedBy = "informasjon")
    private Set<ArbeidsforholdReferanseEntitet> referanser = new LinkedHashSet<>();

    @ChangeTracked
    @OneToMany(mappedBy = "informasjon")
    private List<ArbeidsforholdOverstyringEntitet> overstyringer = new ArrayList<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    public ArbeidsforholdInformasjonEntitet() {
    }

    public ArbeidsforholdInformasjonEntitet(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjonEntitet = (ArbeidsforholdInformasjonEntitet) arbeidsforholdInformasjon; // NOSONAR
        for (ArbeidsforholdReferanseEntitet arbeidsforholdReferanseEntitet : arbeidsforholdInformasjonEntitet.referanser) {
            final ArbeidsforholdReferanseEntitet referanseEntitet = new ArbeidsforholdReferanseEntitet(arbeidsforholdReferanseEntitet);
            referanseEntitet.setInformasjon(this);
            this.referanser.add(referanseEntitet);
        }
        for (ArbeidsforholdOverstyringEntitet arbeidsforholdOverstyringEntitet : arbeidsforholdInformasjonEntitet.overstyringer) {
            final ArbeidsforholdOverstyringEntitet overstyringEntitet = new ArbeidsforholdOverstyringEntitet(arbeidsforholdOverstyringEntitet);
            overstyringEntitet.setInformasjon(this);
            this.overstyringer.add(overstyringEntitet);
        }
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<ArbeidsforholdReferanseEntitet> getArbeidsforholdReferanser() {
        return Collections.unmodifiableSet(this.referanser);
    }

    @Override
    public List<ArbeidsforholdOverstyringEntitet> getOverstyringer() {
        return Collections.unmodifiableList(this.overstyringer);
    }

    @Deprecated(forRemoval=true)
    @Override
    public ArbeidsforholdRef finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef arbeidsforholdRef) {
        // For å sike at det ikke mistes data ved sammenslåing av og innhenting av registerdata
        final Optional<ArbeidsforholdReferanseEntitet> referanseEntitet = referanser.stream().filter(re -> overstyringer.stream()
            .anyMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiverEntitet)
                && re.getEksternReferanse().equals(arbeidsforholdRef)
                && re.getInternReferanse().equals(ov.getArbeidsforholdRef())))
            .findAny();
        if (referanseEntitet.isPresent()) {
            return referanseEntitet.get().getInternReferanse();
        }
        return ArbeidsforholdRef.ref(finnForEkstern(arbeidsgiverEntitet, EksternArbeidsforholdRef.ref(arbeidsforholdRef.getReferanse())).orElseThrow().getReferanse());
    }
    
    @Override
    public Optional<InternArbeidsforholdRef> finnForEkstern(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef ref) {
        var arbeidsforholdReferanser = this.referanser.stream()
            .filter(this::erIkkeMerget)
            .collect(Collectors.toList());
        return arbeidsforholdReferanser.stream()
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getEksternReferanse().equals(ref))
            .findFirst().map(ArbeidsforholdReferanseEntitet::getInternReferanse)
            .map(r -> InternArbeidsforholdRef.ref(r.getReferanse()));
    }

    private boolean erIkkeMerget(ArbeidsforholdReferanseEntitet arbeidsforholdReferanseEntitet) {
        return overstyringer.stream().noneMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && ov.getArbeidsgiver().equals(arbeidsforholdReferanseEntitet.getArbeidsgiver())
            && ov.getArbeidsforholdRef().gjelderFor(arbeidsforholdReferanseEntitet.getInternReferanse()));
    }

    @Override
    public ArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiverEntitet, final ArbeidsforholdRef ref) {
        final Optional<ArbeidsforholdOverstyringEntitet> erstattning = overstyringer.stream()
            .filter(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiverEntitet)
                && (ov.getArbeidsforholdRef().gjelderFor(ref)
                || ov.getArbeidsforholdRef().gjelderFor(finnForEksternBeholdHistoriskReferanse(arbeidsgiverEntitet, ref))))
            .findAny();
        if (erstattning.isPresent() && !erstattning.get().getNyArbeidsforholdRef().equals(ref)) {
            return finnEllerOpprett(arbeidsgiverEntitet, erstattning.get().getNyArbeidsforholdRef());
        } else {
            final ArbeidsforholdReferanseEntitet referanse = this.referanser.stream()
                .filter(this::erIkkeMerget)
                .filter(it -> it.getArbeidsgiver().equals(arbeidsgiverEntitet) && it.getInternReferanse().equals(ref))
                .findAny().orElseGet(() -> finnEksisterendeInternReferanseEllerOpprettNy(arbeidsgiverEntitet, ref));

            return referanse.getInternReferanse();
        }
    }

    @Deprecated(forRemoval=true)
    private Optional<ArbeidsforholdReferanseEntitet> referanseEksistererIkke(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef ref) {
        return this.referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiverEntitet) && it.getInternReferanse().equals(ref))
            .findAny();
    }

    @Deprecated(forRemoval=true)
    private ArbeidsforholdReferanseEntitet finnEksisterendeInternReferanseEllerOpprettNy(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef ref) {
        return finnEksisterendeReferanse(arbeidsgiverEntitet, ref)
            .orElseGet(() -> opprettNyReferanse(arbeidsgiverEntitet, ArbeidsforholdRef.ref(UUID.randomUUID().toString()), ref));
    }
    
    @Deprecated(forRemoval=true)
    private Optional<ArbeidsforholdReferanseEntitet> finnEksisterendeReferanse(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef ref) {
        return this.referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiverEntitet) && it.getEksternReferanse().equals(ref))
            .findAny();
    }

    @Deprecated(forRemoval=true)
    private ArbeidsforholdReferanseEntitet opprettNyReferanse(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef internReferanse, ArbeidsforholdRef eksternReferanse) {
        final ArbeidsforholdReferanseEntitet arbeidsforholdReferanseEntitet = new ArbeidsforholdReferanseEntitet(arbeidsgiverEntitet,
            internReferanse, eksternReferanse);
        arbeidsforholdReferanseEntitet.setInformasjon(this);
        referanser.add(arbeidsforholdReferanseEntitet);
        return arbeidsforholdReferanseEntitet;
    }

    @Deprecated(forRemoval=true)
    ArbeidsforholdOverstyringBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef ref) {
        return ArbeidsforholdOverstyringBuilder.oppdatere(this.overstyringer
            .stream()
            .filter(ov -> ov.getArbeidsgiver().equals(arbeidsgiverEntitet)
                && ov.getArbeidsforholdRef().gjelderFor(ref))
            .findFirst())
            .medInformasjon(this)
            .medArbeidsforholdRef(ref)
            .medArbeidsgiver(arbeidsgiverEntitet);
    }

    void leggTilOverstyring(ArbeidsforholdOverstyringEntitet build) {
        build.setInformasjon(this);
        this.overstyringer.add(build);
    }

    void tilbakestillOverstyringer() {
        this.overstyringer.clear();
    }

    @Deprecated(forRemoval=true)
    void erstattArbeidsforhold(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef gammelRef, ArbeidsforholdRef ref) {
        final Optional<ArbeidsforholdReferanseEntitet> referanseEntitet = referanseEksistererIkke(arbeidsgiverEntitet, gammelRef);
        referanseEntitet.ifPresent(it -> opprettNyReferanse(arbeidsgiverEntitet, ref, it.getEksternReferanse()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsforholdInformasjonEntitet that = (ArbeidsforholdInformasjonEntitet) o;
        return Objects.equals(referanser, that.referanser) &&
            Objects.equals(overstyringer, that.overstyringer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanser, overstyringer);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdInformasjonEntitet{" +
            "referanser=" + referanser +
            ", overstyringer=" + overstyringer +
            '}';
    }

    void fjernOverstyringVedrørende(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef arbeidsforholdRef) {
        overstyringer.removeIf(ov -> !Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && ov.getArbeidsgiver().getErVirksomhet()
            && ov.getArbeidsgiver().equals(arbeidsgiverEntitet)
            && ov.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef));
    }

    /**
     * @deprecated FIXME (FC): Trengs denne eller kan vi alltid stole på ref er den vi skal returnere? Skal egentlig returnere ref,
     *             men per nå har vi antagelig interne ider som har erstattet andre interne id'er. Må isåfall avsjekke migrering av disse.
     */
    @Deprecated(forRemoval = true)
    @Override
    public InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, final InternArbeidsforholdRef ref) {
        final Optional<ArbeidsforholdOverstyringEntitet> erstattning = overstyringer.stream()
            .filter(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiver)
                && ov.getArbeidsforholdRef().gjelderFor(ArbeidsforholdRef.ref(ref.getReferanse())))
            .findAny();
        if (erstattning.isPresent() && !erstattning.get().getNyArbeidsforholdRef().equals(ref)) {
            var r =finnEllerOpprett(arbeidsgiver, erstattning.get().getNyArbeidsforholdRef());
            return InternArbeidsforholdRef.ref(r.getReferanse());
        } else {
            final ArbeidsforholdReferanseEntitet referanse = this.referanser.stream()
                .filter(this::erIkkeMerget)
                .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getInternReferanse().equals(ref))
                .findFirst().orElseThrow(() -> new IllegalStateException("InternArbeidsforholdReferanse må eksistere fra før, fant ikke: " + ref));

            var r = referanse.getInternReferanse();
            return InternArbeidsforholdRef.ref(r.getReferanse());
        }
    }

    @Override
    public InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, final EksternArbeidsforholdRef ref) {
        final Optional<ArbeidsforholdOverstyringEntitet> erstattning = overstyringer.stream()
            .filter(ov -> {
                var historiskReferanse = finnForEksternBeholdHistoriskReferanse(arbeidsgiver, ref);
                return historiskReferanse.isPresent()
                    && ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                    && ov.getArbeidsgiver().equals(arbeidsgiver)
                    && ov.getArbeidsforholdRef().gjelderFor(ArbeidsforholdRef.ref(historiskReferanse.get().getReferanse()));
            })
            .findAny();
        if (erstattning.isPresent()) {
            var r = finnEllerOpprett(arbeidsgiver, erstattning.get().getNyArbeidsforholdRef());
            return InternArbeidsforholdRef.ref(r.getReferanse());
        } else {
            var referanse = finnEksisterendeInternReferanseEllerOpprettNy(arbeidsgiver, ArbeidsforholdRef.ref(ref.getReferanse()));
            return InternArbeidsforholdRef.ref(referanse.getInternReferanse().getReferanse());
        }
    }

    @Override
    public Optional<InternArbeidsforholdRef> finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef arbeidsforholdRef) {
        // For å sike at det ikke mistes data ved sammenslåing av og innhenting av registerdata
        final Optional<ArbeidsforholdReferanseEntitet> referanseEntitet = referanser.stream().filter(re -> overstyringer.stream()
            .anyMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiver)
                && re.getEksternReferanse().equals(arbeidsforholdRef)
                && re.getInternReferanse().equals(ov.getArbeidsforholdRef())))
            .findAny();
        if (referanseEntitet.isPresent()) {
            var internRef = referanseEntitet.get().getInternReferanse();
            return Optional.ofNullable(internRef==null?null:InternArbeidsforholdRef.ref(internRef.getReferanse()));
        }
        return finnForEkstern(arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public EksternArbeidsforholdRef finnEkstern(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internReferanse) {
        return referanser.stream()
                .filter(this::erIkkeMerget)
                .filter(r -> Objects.equals(r.getInternReferanse(), internReferanse) && Objects.equals(r.getArbeidsgiver(), arbeidsgiver))
                .findFirst()
                .map(ArbeidsforholdReferanseEntitet::getEksternReferanse)
                .map(r -> EksternArbeidsforholdRef.ref(r.getReferanse()))
                .orElseThrow(
                    () -> new IllegalStateException("Mangler eksternReferanse for internReferanse: " + internReferanse + ", arbeidsgiver: " + arbeidsgiver));
    }

    void leggTilNyReferanse(ArbeidsforholdReferanseEntitet arbeidsforholdReferanse) {
        arbeidsforholdReferanse.setInformasjon(this);
        referanser.add(arbeidsforholdReferanse);
    }
}
