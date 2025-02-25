package no.nav.foreldrepenger.abakus.iay;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.Dataset;
import no.nav.abakus.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest.GrunnlagVersjon;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@ApplicationScoped
public class InntektArbeidYtelseTjeneste {

    private InntektArbeidYtelseRepository repository;

    InntektArbeidYtelseTjeneste() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseTjeneste(InntektArbeidYtelseRepository repository) {
        Objects.requireNonNull(repository, "repository");
        this.repository = repository;
    }

    /**
     * @param koblingen
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    public InntektArbeidYtelseGrunnlag hentAggregat(KoblingReferanse koblingReferanse) {
        return repository.hentInntektArbeidYtelseForBehandling(koblingReferanse);
    }

    /**
     * @param referanse - unik referanse for aggregat
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    public InntektArbeidYtelseGrunnlag hentAggregat(GrunnlagReferanse referanse) {
        return repository.hentInntektArbeidYtelseForReferanse(referanse).orElseThrow();
    }

    /**
     * @param koblingReferanse
     * @return henter optional aggregat
     */
    public Optional<InntektArbeidYtelseGrunnlag> hentGrunnlagFor(KoblingReferanse koblingReferanse) {
        return repository.hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
    }

    /**
     * Hent alle grunnlag for angitt saksnummer
     *
     * @param saksnummer
     * @param boolean    kunAktive - hvis true henter kun aktive grunnlag (ikke historiske versjoner)
     * @return henter optional aggregat
     */
    public List<InntektArbeidYtelseGrunnlag> hentAlleGrunnlagFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType, boolean kunAktive) {
        return repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, saksnummer, ytelseType, kunAktive);
    }

    public Set<Inntektsmelding> hentAlleInntektsmeldingerFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        return repository.hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
    }

    public Map<Inntektsmelding, ArbeidsforholdInformasjon> hentArbeidsforholdinfoInntektsmeldingerMapFor(AktørId aktørId,
                                                                                                         Saksnummer saksnummer,
                                                                                                         YtelseType ytelseType) {
        return repository.hentArbeidsforholdInfoInntektsmeldingerMapFor(aktørId, saksnummer, ytelseType);
    }

    /**
     * Hent grunnlag etterspurt (tar hensyn til GrunnlagVersjon) for angitt aktørId, saksnummer, ytelsetype.
     * Skipper mellomliggende versjoner hvis ikke direkte spurt om.
     */
    public List<InntektArbeidYtelseGrunnlag> hentGrunnlagEtterspurtFor(AktørId aktørId,
                                                                       Saksnummer saksnummer,
                                                                       YtelseType ytelseType,
                                                                       GrunnlagVersjon grunnlagVersjon) {

        boolean kunAktive = GrunnlagVersjon.SISTE.equals(grunnlagVersjon); // shortcutter litt opphenting
        var grunnlag = hentAlleGrunnlagFor(aktørId, saksnummer, ytelseType, kunAktive);

        var grunnlagByKobling = grunnlag.stream().collect(Collectors.groupingBy(InntektArbeidYtelseGrunnlag::getKoblingId));

        var grunnlagEtterspurt = grunnlagByKobling.entrySet()
            .stream()
            .flatMap(e -> filterGrunnlag(e.getKey(), e.getValue(), grunnlagVersjon).stream());

        return grunnlagEtterspurt.collect(Collectors.toList());
    }


    /**
     * Opprett builder for saksbehandlers overstyringer.
     *
     * @param koblingReferanse
     * @return Saksbehandlers overstyringer av IAY (primært {@link no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid}).
     */
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlet(KoblingReferanse koblingReferanse,
                                                                             UUID angittReferanse,
                                                                             LocalDateTime angittOpprettetTidspunkt) {
        return repository.opprettBuilderFor(koblingReferanse, angittReferanse, angittOpprettetTidspunkt, VersjonType.SAKSBEHANDLET);
    }

    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseGrunnlagBuilder builder) {
        repository.lagre(koblingReferanse, builder);
    }

    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse).orElseGet(ArbeidsforholdInformasjon::new);
    }

    public Optional<OppgittOpptjening> hentOppgittOpptjeningFor(UUID oppgittOpptjeningEksternReferanse) {
        return repository.hentOppgittOpptjeningFor(oppgittOpptjeningEksternReferanse);
    }

    public Optional<InntektArbeidYtelseAggregat> hentIAYAggregatFor(KoblingReferanse koblingReferanse, UUID eksternReferanse) {
        return repository.hentIAYAggregatFor(koblingReferanse, eksternReferanse);
    }

    public boolean erGrunnlagAktivt(UUID eksternReferanse) {
        return repository.erGrunnlagAktivt(eksternReferanse);
    }

    /**
     * Kopier grunnlag fra en behandling til en annen.
     *
     * @param ytelseType
     * @param dataset
     * @param beholdOpprinngeligeIM
     */
    public void kopierGrunnlagFraEksisterendeBehandling(YtelseType ytelseType,
                                                        AktørId aktørId,
                                                        Saksnummer saksnummer,
                                                        KoblingReferanse fraKobling,
                                                        KoblingReferanse tilKobling,
                                                        Set<Dataset> dataset,
                                                        boolean beholdOpprinngeligeIM) {
        var origAggregat = hentGrunnlagFor(fraKobling);
        if (origAggregat.isPresent()) {
            if (beholdOpprinngeligeIM) {
                //gjelder spesialbehandlinger(berørt, feriepenger og utsatt start)
                kopierGrunnlagBeholdInntektsmeldinger(tilKobling, origAggregat.get(), dataset);
            } else {
                kopierGrunnlagPlussNyereInntektsmeldingerForFagsak(tilKobling, ytelseType, aktørId, saksnummer, origAggregat.get(), dataset);
            }
        }
    }

    /**
     * Dersom det finnes en eller flere nyere inntektsmeldinger på en annen behandling på samme fagsak, så vil
     * disse kopieres til den angitte behandlingen. Dette trengs i tilfeller der en behandling har fått en ny
     * inntektsmelding, blitt henlagt, og en revurdering er opprettet basert på en tidligere behandling med vedtak
     * som ikke inkluderer en eller flere inntektsmeldinger som er mottatt etter vedtaket, men før revurderingen.
     *
     * @param tilKobling
     * @param dataset
     * @return
     */
    private InntektArbeidYtelseGrunnlagBuilder kopierGrunnlagPlussNyereInntektsmeldingerForFagsak(KoblingReferanse tilKobling,
                                                                                                  YtelseType ytelseType,
                                                                                                  AktørId aktørId,
                                                                                                  Saksnummer saksnummer,
                                                                                                  InntektArbeidYtelseGrunnlag original,
                                                                                                  Set<Dataset> dataset) {

        var builder = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(original, dataset);
        if (dataset.contains(Dataset.INNTEKTSMELDING)) {
            var gjeldendeInntektsmeldinger = original.getInntektsmeldinger().map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(List.of());
            var sisteEksisterendeInntektsmelding = finnSisteEksisterendeInntektsmelding(gjeldendeInntektsmeldinger);

            var arbeidsforholdInformasjon = original.getArbeidsforholdInformasjon().orElseGet(ArbeidsforholdInformasjon::new);
            var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(arbeidsforholdInformasjon);

            var alleInntektsmeldingerForSaksummer = hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer, ytelseType);

            var eksisterendeGrunnlagRef = original.getGrunnlagReferanse().getReferanse();
            var kopierInntektsmeldingerEtterNyeste = alleInntektsmeldingerForSaksummer.entrySet()
                .stream()
                .filter(im -> (Inntektsmelding.COMP_REKKEFØLGE.compare(im.getKey(), sisteEksisterendeInntektsmelding) > 0))
                .map(entry -> {
                    Inntektsmelding nyInntektsmelding = entry.getKey();
                    ArbeidsforholdInformasjon arbForholdInformasjon = entry.getValue();

                    var arbeidsgiver = nyInntektsmelding.getArbeidsgiver();
                    var internRef = nyInntektsmelding.getArbeidsforholdRef();
                    var eksternRef = arbForholdInformasjon.finnEkstern(eksisterendeGrunnlagRef, arbeidsgiver,
                        nyInntektsmelding.getArbeidsforholdRef());

                    InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.kopi(nyInntektsmelding);

                    if (eksternRef.gjelderForSpesifiktArbeidsforhold() && !internRef.gjelderForSpesifiktArbeidsforhold()) {
                        internRef = informasjonBuilder.finnEllerOpprett(arbeidsgiver, eksternRef);
                    } else {
                        if (eksternRef.gjelderForSpesifiktArbeidsforhold() && internRef.gjelderForSpesifiktArbeidsforhold()
                            && informasjonBuilder.erUkjentReferanse(arbeidsgiver, internRef)) {
                            informasjonBuilder.leggTilNyReferanse(new ArbeidsforholdReferanse(arbeidsgiver, internRef, eksternRef));
                        }
                    }
                    return inntektsmeldingBuilder.medArbeidsforholdId(internRef).medArbeidsforholdId(eksternRef);
                })
                .map(InntektsmeldingBuilder::build)
                .sorted(Inntektsmelding.COMP_REKKEFØLGE)
                .collect(Collectors.toList());

            repository.oppdaterBuilderMedNyeInntektsmeldinger(informasjonBuilder, kopierInntektsmeldingerEtterNyeste, builder);
        }
        lagre(tilKobling, builder);
        return builder;
    }

    private InntektArbeidYtelseGrunnlagBuilder kopierGrunnlagBeholdInntektsmeldinger(KoblingReferanse tilKobling,
                                                                                     InntektArbeidYtelseGrunnlag original,
                                                                                     Set<Dataset> dataset) {
        var builder = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(original, dataset);
        lagre(tilKobling, builder);
        return builder;
    }

    private Inntektsmelding finnSisteEksisterendeInntektsmelding(Collection<Inntektsmelding> inntektsmeldinger) {
        return inntektsmeldinger.stream().max(Inntektsmelding.COMP_REKKEFØLGE).orElse(null);
    }

    public KoblingReferanse hentKoblingReferanse(GrunnlagReferanse grunnlagReferanse) {
        return repository.hentKoblingReferanseFor(grunnlagReferanse);
    }

    private List<InntektArbeidYtelseGrunnlag> filterGrunnlag(Long koblingId,
                                                             List<InntektArbeidYtelseGrunnlag> grunnlagPerKobling,
                                                             GrunnlagVersjon grunnlagVersjon) {
        if (!grunnlagPerKobling.stream().allMatch(g -> Objects.equals(koblingId, g.getKoblingId()))) {
            throw new IllegalArgumentException("Utvikler-feil: Fikk grunnlag som ikke har riktig koblingId: " + koblingId);
        }

        // quick returns
        if (GrunnlagVersjon.ALLE.equals(grunnlagVersjon) || grunnlagPerKobling.isEmpty()) {
            return grunnlagPerKobling;
        }

        var sortertKopi = grunnlagPerKobling.stream()
            .sorted(Comparator.comparing(InntektArbeidYtelseGrunnlag::getOpprettetTidspunkt, Comparator.nullsFirst(Comparator.naturalOrder())))
            .collect(Collectors.toCollection(LinkedList::new));

        InntektArbeidYtelseGrunnlag første = sortertKopi.getFirst(); // vil alltid være her da vi sjekker på tom liste først
        InntektArbeidYtelseGrunnlag siste = sortertKopi.getLast();

        // dobbeltsjekk siste skal være aktivt
        if (!siste.isAktiv()) {
            throw new IllegalStateException("Siste grunnlag på " + koblingId + " er ikke aktivt, grunnlagReferanse: " + siste.getGrunnlagReferanse());
        }

        return switch (grunnlagVersjon) {
            case FØRSTE -> List.of(første);
            case SISTE -> List.of(siste);
            case FØRSTE_OG_SISTE -> Objects.equals(første, siste) ? List.of(første) : List.of(første, siste);
            default -> throw new UnsupportedOperationException("GrunnlagVersjon " + grunnlagVersjon + " er ikke støttet her for " + koblingId);
        };

    }
}
