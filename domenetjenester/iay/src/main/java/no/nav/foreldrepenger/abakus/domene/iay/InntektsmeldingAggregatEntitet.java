package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingEntitet;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;
import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "Inntektsmeldinger")
@Table(name = "INNTEKTSMELDINGER")
public class InntektsmeldingAggregatEntitet extends BaseEntitet implements InntektsmeldingAggregat {

    private static final Logger logger = LoggerFactory.getLogger(InntektsmeldingAggregatEntitet.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDINGER")
    private Long id;

    @OneToMany(mappedBy = "inntektsmeldinger")
    @ChangeTracked
    private List<InntektsmeldingEntitet> inntektsmeldinger = new ArrayList<>();

    @Transient
    private ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjon;

    InntektsmeldingAggregatEntitet() {
    }

    InntektsmeldingAggregatEntitet(InntektsmeldingAggregat inntektsmeldingAggregat) {
        final InntektsmeldingAggregatEntitet inntektsmeldingAggregat1 = (InntektsmeldingAggregatEntitet) inntektsmeldingAggregat; // NOSONAR
        this.inntektsmeldinger = inntektsmeldingAggregat1.inntektsmeldinger.stream().map(i -> {
            final InntektsmeldingEntitet inntektsmeldingEntitet = new InntektsmeldingEntitet(i);
            inntektsmeldingEntitet.setInntektsmeldinger(this);
            return inntektsmeldingEntitet;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Inntektsmelding> getInntektsmeldinger() {
        return Collections.unmodifiableList(inntektsmeldinger.stream().filter(this::skalBrukes).collect(Collectors.toList()));
    }

    public List<Inntektsmelding> getAlleInntektsmeldinger() {
        return Collections.unmodifiableList(inntektsmeldinger);
    }

    private boolean skalBrukes(InntektsmeldingEntitet im) {
        return arbeidsforholdInformasjon == null || arbeidsforholdInformasjon.getOverstyringer()
            .stream()
            .noneMatch(ov -> erFjernet(im, ov));
    }

    private boolean erFjernet(InntektsmeldingEntitet im, ArbeidsforholdOverstyringEntitet ov) {
        return (ov.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
            && ov.getArbeidsgiver().equals(im.getArbeidsgiver())
            && (Objects.equals(ArbeidsforholdHandlingType.IKKE_BRUK, ov.getHandling())
            || Objects.equals(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING, ov.getHandling())
            || Objects.equals(ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE, ov.getHandling())
            || Objects.equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET, ov.getHandling()));
    }

    @Override
    public List<Inntektsmelding> getInntektsmeldingerFor(Arbeidsgiver arbeidsgiver) {
        return getInntektsmeldinger().stream().filter(i -> i.getArbeidsgiver().equals(arbeidsgiver)).collect(Collectors.toList());
    }

    /**
     * Den persisterte inntektsmeldingen kan være av nyere dato, bestemmes av
     * innsendingstidspunkt på inntektsmeldingen.
     */
    void leggTil(Inntektsmelding inntektsmelding) {

        boolean fjernet = inntektsmeldinger.removeIf(it -> skalFjerneInntektsmelding(it, inntektsmelding));

        if (fjernet || inntektsmeldinger.stream().noneMatch(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))) {
            final InntektsmeldingEntitet entitet = (InntektsmeldingEntitet) inntektsmelding;
            entitet.setInntektsmeldinger(this);
            inntektsmeldinger.add(entitet);
        }

        inntektsmeldinger.stream().filter(it -> it.gjelderSammeArbeidsforhold(inntektsmelding) && !fjernet).findFirst().ifPresent(e -> {
            logger.info("Persistert inntektsmelding med journalpostid {} er nyere enn den mottatte med journalpostid {}. Ignoreres", e.getMottattDokumentId(), inntektsmelding.getMottattDokumentId());
        });
    }

    private boolean skalFjerneInntektsmelding(Inntektsmelding gammel, Inntektsmelding ny) {
        if (gammel.gjelderSammeArbeidsforhold(ny)) {
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

    void taHensynTilBetraktninger(ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjon) {
        this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InntektsmeldingAggregatEntitet that = (InntektsmeldingAggregatEntitet) o;
        return Objects.equals(inntektsmeldinger, that.inntektsmeldinger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektsmeldinger);
    }
}
