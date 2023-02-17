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
import javax.persistence.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;

@Entity(name = "Inntektsmeldinger")
@Table(name = "IAY_INNTEKTSMELDINGER")
public class InntektsmeldingAggregat extends BaseEntitet {

    private static final Logger logger = LoggerFactory.getLogger(InntektsmeldingAggregat.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDINGER")
    private Long id;

    @OneToMany(mappedBy = "inntektsmeldinger")
    @ChangeTracked
    private Set<Inntektsmelding> inntektsmeldinger = new HashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public InntektsmeldingAggregat() {
    }

    InntektsmeldingAggregat(InntektsmeldingAggregat inntektsmeldingAggregat) {
        this(inntektsmeldingAggregat.getInntektsmeldinger());
    }

    public InntektsmeldingAggregat(Collection<Inntektsmelding> inntektsmeldinger) {
        this.inntektsmeldinger.addAll(inntektsmeldinger.stream().sorted(Inntektsmelding.COMP_REKKEFØLGE).map(i -> {
            var inntektsmeldingEntitet = new Inntektsmelding(i);
            inntektsmeldingEntitet.setInntektsmeldinger(this);
            return inntektsmeldingEntitet;
        }).collect(Collectors.toList()));
    }

    /**
     * Get alle inntektsmeldinger (både de som skal brukes og ikke brukes).
     */
    public List<Inntektsmelding> getInntektsmeldinger() {
        return inntektsmeldinger.stream().sorted(Inntektsmelding.COMP_REKKEFØLGE).collect(Collectors.toUnmodifiableList());
    }

    public Long getId() {
        return id;
    }

    /**
     * Den persisterte inntektsmeldingen kan være av nyere dato, bestemmes av
     * innsendingstidspunkt på inntektsmeldingen.
     *
     * @return lagtTilEllerIkke
     */
    public Set<JournalpostId> leggTilEllerErstattMedUtdatertForHistorikk(Inntektsmelding inntektsmelding) {
        var collect = inntektsmeldinger.stream()
            .filter(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))
            .map(Inntektsmelding::getJournalpostId)
            .collect(Collectors.toCollection(HashSet::new));
        boolean fjernet = inntektsmeldinger.removeIf(it -> it.gjelderSammeArbeidsforhold(inntektsmelding));

        if (fjernet || inntektsmeldinger.stream().noneMatch(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))) {
            final Inntektsmelding entitet = inntektsmelding;
            entitet.setInntektsmeldinger(this);
            inntektsmeldinger.add(entitet);
        } else {
            collect.add(inntektsmelding.getJournalpostId());
        }
        return collect;
    }

    /**
     * Den persisterte inntektsmeldingen kan være av nyere dato, bestemmes av
     * innsendingstidspunkt på inntektsmeldingen.
     *
     * @return lagtTilEllerIkke
     */
    public Set<JournalpostId> leggTilEllerErstatt(Inntektsmelding inntektsmelding) {
        var collect = inntektsmeldinger.stream()
            .filter(it -> skalFjerneInntektsmelding(it, inntektsmelding))
            .map(Inntektsmelding::getJournalpostId)
            .collect(Collectors.toCollection(HashSet::new));
        boolean fjernet = inntektsmeldinger.removeIf(it -> skalFjerneInntektsmelding(it, inntektsmelding));
        inntektsmeldinger.stream().filter(it -> it.gjelderSammeArbeidsforhold(inntektsmelding) && !fjernet).findFirst().ifPresent(e -> {
            logger.info("Persistert inntektsmelding med journalpostid {} er nyere enn den mottatte med journalpostid {}. Ignoreres",
                e.getJournalpostId(), inntektsmelding.getJournalpostId());
        });

        if (fjernet || inntektsmeldinger.stream().noneMatch(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))) {
            final Inntektsmelding entitet = inntektsmelding;
            entitet.setInntektsmeldinger(this);
            inntektsmeldinger.add(entitet);
        } else {
            collect.add(inntektsmelding.getJournalpostId());
        }

        return collect;
    }

    private boolean skalFjerneInntektsmelding(Inntektsmelding gammel, Inntektsmelding ny) {
        if (gammel.gjelderSammeArbeidsforhold(ny)) {
            if (gammel.getKanalreferanse() != null && ny.getKanalreferanse() != null) {
                return ny.getKanalreferanse().compareTo(gammel.getKanalreferanse()) > 0;
            }
            if (gammel.getInnsendingstidspunkt().compareTo(ny.getInnsendingstidspunkt()) <= 0) {
                // crazy fallback - enkelte inntektsmeldinger har ikke blitt journalført med kanalreferanse.
                // Oppstår når SBH journalfører IM på annen sak i Gosys. Skal fikses der.
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof InntektsmeldingAggregat)) {
            return false;
        }
        var that = (InntektsmeldingAggregat) o;
        return Objects.equals(inntektsmeldinger, that.inntektsmeldinger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektsmeldinger);
    }
}
