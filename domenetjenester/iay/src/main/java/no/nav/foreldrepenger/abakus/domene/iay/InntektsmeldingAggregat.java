package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyring;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;

@Entity(name = "Inntektsmeldinger")
@Table(name = "IAY_INNTEKTSMELDINGER")
public class InntektsmeldingAggregat extends BaseEntitet {

    private static final Logger logger = LoggerFactory.getLogger(InntektsmeldingAggregat.class);
    private static final String ALTINN_SYSTEM_NAVN = "AltinnPortal";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDINGER")
    private Long id;

    @OneToMany(mappedBy = "inntektsmeldinger")
    @ChangeTracked
    private Set<Inntektsmelding> inntektsmeldinger = new HashSet<>();

    @Transient
    private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public InntektsmeldingAggregat() {
    }

    InntektsmeldingAggregat(InntektsmeldingAggregat inntektsmeldingAggregat) {
        this(inntektsmeldingAggregat.getAlleInntektsmeldinger());
    }

    public InntektsmeldingAggregat(Collection<Inntektsmelding> inntektsmeldinger) {
        this.inntektsmeldinger.addAll(inntektsmeldinger.stream().map(i -> {
            final Inntektsmelding inntektsmeldingEntitet = new Inntektsmelding(i);
            inntektsmeldingEntitet.setInntektsmeldinger(this);
            return inntektsmeldingEntitet;
        }).collect(Collectors.toList()));
    }

    /**
     * Gjeldende inntektsmeldinger i behandlingen (de som skal brukes).
     *
     * @return Liste med {@link Inntektsmelding}
     */
    public List<Inntektsmelding> getInntektsmeldinger() {
        return inntektsmeldinger.stream().filter(this::skalBrukes).collect(Collectors.toUnmodifiableList());
    }

    /** Get alle inntektsmeldinger (både de som skal brukes og ikke brukes). */
    public List<Inntektsmelding> getAlleInntektsmeldinger() {
        return inntektsmeldinger.stream().collect(Collectors.toUnmodifiableList());
    }

    public Long getId() {
        return id;
    }

    private boolean skalBrukes(Inntektsmelding im) {
        return arbeidsforholdInformasjon == null || arbeidsforholdInformasjon.getOverstyringer()
            .stream()
            .noneMatch(ov -> erFjernet(im, ov));
    }

    private boolean erFjernet(Inntektsmelding im, ArbeidsforholdOverstyring ov) {
        return (ov.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
            && ov.getArbeidsgiver().equals(im.getArbeidsgiver())
            && (Objects.equals(ArbeidsforholdHandlingType.IKKE_BRUK, ov.getHandling())
            || Objects.equals(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING, ov.getHandling())
            || Objects.equals(ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE, ov.getHandling())
            || Objects.equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET, ov.getHandling()));
    }


    /**
     * Alle gjeldende inntektsmeldinger for en virksomhet i behandlingen.
     *
     * @return Liste med {@link Inntektsmelding}
     */
    public List<Inntektsmelding> getInntektsmeldingerFor(Arbeidsgiver arbeidsgiver) {
        return getInntektsmeldinger().stream().filter(i -> i.getArbeidsgiver().equals(arbeidsgiver)).collect(Collectors.toList());
    }

    /**
     * Den persisterte inntektsmeldingen kan være av nyere dato, bestemmes av
     * innsendingstidspunkt på inntektsmeldingen.
     */
    public void leggTilEllerErstatt(Inntektsmelding inntektsmelding) {

        boolean fjernet = inntektsmeldinger.removeIf(it -> skalFjerneInntektsmelding(it, inntektsmelding));
        inntektsmeldinger.stream().filter(it -> it.gjelderSammeArbeidsforhold(inntektsmelding) && !fjernet).findFirst().ifPresent(e -> {
            logger.info("Persistert inntektsmelding med journalpostid {} er nyere enn den mottatte med journalpostid {}. Ignoreres", e.getJournalpostId(),
                inntektsmelding.getJournalpostId());
        });

        if (fjernet || inntektsmeldinger.stream().noneMatch(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))) {
            final Inntektsmelding entitet = inntektsmelding;
            entitet.setInntektsmeldinger(this);
            inntektsmeldinger.add(entitet);
        }

    }

    private boolean skalFjerneInntektsmelding(Inntektsmelding gammel, Inntektsmelding ny) {
        if (gammel.gjelderSammeArbeidsforhold(ny)) {
            if (ALTINN_SYSTEM_NAVN.equals(gammel.getKildesystem()) || ALTINN_SYSTEM_NAVN.equals(ny.getKildesystem())) {
                // WTF?  Hvorfor trengs ALTINN å spesialbehandles?
                if (gammel.getKanalreferanse() != null && ny.getKanalreferanse() != null) {
                    // skummelt å stole på stigende arkivreferanser fra Altinn. :-(
                    return ny.getKanalreferanse().compareTo(gammel.getKanalreferanse()) > 0;
                }
            }
            if (gammel.getInnsendingstidspunkt().isBefore(ny.getInnsendingstidspunkt())) {
                return true;
            }
            if (gammel.getInnsendingstidspunkt().equals(ny.getInnsendingstidspunkt()) && ny.getKanalreferanse() != null) {
                if (gammel.getKanalreferanse() != null) {
                    return ny.getKanalreferanse().compareTo(gammel.getKanalreferanse()) > 0;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof InntektsmeldingAggregat))
            return false;
        var that = (InntektsmeldingAggregat) o;
        return Objects.equals(inntektsmeldinger, that.inntektsmeldinger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektsmeldinger);
    }
}
