package no.nav.foreldrepenger.abakus.domene.iay.søknad;

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

@Entity(name = "OppgittOpptjeningAggregat")
@Table(name = "IAY_OPPGITT_OPPTJENING_AGGR")
public class OppgittOpptjeningAggregat extends BaseEntitet {

    private static final Logger logger = LoggerFactory.getLogger(OppgittOpptjeningAggregat.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDINGER")
    private Long id;

    @OneToMany(mappedBy = "inntektsmeldinger")
    @ChangeTracked
    private Set<OppgittOpptjening> oppgitteOpptjeninger = new HashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public OppgittOpptjeningAggregat() {
    }

    OppgittOpptjeningAggregat(OppgittOpptjeningAggregat oppgittOpptjeningAggregat) {
        this(oppgittOpptjeningAggregat.getOppgitteOpptjeninger());
    }

    public OppgittOpptjeningAggregat(Collection<OppgittOpptjening> oppgitteOpptjeninger) {
        this.oppgitteOpptjeninger.addAll(oppgitteOpptjeninger.stream()
            //.sorted(Inntektsmelding.COMP_REKKEFØLGE)
            .sorted()
            .map(oppgittOpptjening -> {
                // TODO: Skrive copy-ctor
                var inntektsmeldingEntitet = new OppgittOpptjening(oppgittOpptjening);
                //inntektsmeldingEntitet.setInntektsmeldinger(this);
                return inntektsmeldingEntitet;
            })
            .collect(Collectors.toList()));
    }

    /**
     * Get alle inntektsmeldinger (både de som skal brukes og ikke brukes).
     * @return
     */
    public List<OppgittOpptjening> getOppgitteOpptjeninger() {
        return oppgitteOpptjeninger.stream()
            //.sorted(Inntektsmelding.COMP_REKKEFØLGE)
            .sorted()
            .collect(Collectors.toUnmodifiableList());
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
        var collect = oppgitteOpptjeninger.stream()
            .filter(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))
            .map(Inntektsmelding::getJournalpostId)
            .collect(Collectors.toCollection(HashSet::new));
        boolean fjernet = oppgitteOpptjeninger.removeIf(it -> it.gjelderSammeArbeidsforhold(inntektsmelding));

        if (fjernet || oppgitteOpptjeninger.stream().noneMatch(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))) {
            final Inntektsmelding entitet = inntektsmelding;
            entitet.setInntektsmeldinger(this);
            oppgitteOpptjeninger.add(entitet);
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
        var collect = oppgitteOpptjeninger.stream()
            .filter(it -> skalFjerneInntektsmelding(it, inntektsmelding))
            .map(Inntektsmelding::getJournalpostId)
            .collect(Collectors.toCollection(HashSet::new));
        boolean fjernet = oppgitteOpptjeninger.removeIf(it -> skalFjerneInntektsmelding(it, inntektsmelding));
        oppgitteOpptjeninger.stream().filter(it -> it.gjelderSammeArbeidsforhold(inntektsmelding) && !fjernet).findFirst().ifPresent(e -> {
            logger.info("Persistert inntektsmelding med journalpostid {} er nyere enn den mottatte med journalpostid {}. Ignoreres", e.getJournalpostId(),
                inntektsmelding.getJournalpostId());
        });

        if (fjernet || oppgitteOpptjeninger.stream().noneMatch(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))) {
            final Inntektsmelding entitet = inntektsmelding;
            entitet.setInntektsmeldinger(this);
            oppgitteOpptjeninger.add(entitet);
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
        if (this == o)
            return true;
        if (o == null || !(o instanceof OppgittOpptjeningAggregat))
            return false;
        var that = (OppgittOpptjeningAggregat) o;
        return Objects.equals(oppgitteOpptjeninger, that.oppgitteOpptjeninger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgitteOpptjeninger);
    }
}
