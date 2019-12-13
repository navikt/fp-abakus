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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
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
     * @param referanse (ekstern referanse for kobling (eks. behandlingUuid)).
     * @return henter koblingen grunnlagsreferansen er koblet til.
     */
    public Long hentKoblingIdFor(GrunnlagReferanse referanse) {
        return repository.hentKoblingIdFor(referanse);
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

    /**
     * Hent alle grunnlag for angitt koblingsreferanse (behandling)
     *
     * @param koblingReferanse
     * @param boolean          kunAktive - hvis true henter kun aktive grunnlag (ikke historiske versjoner)
     * @return henter optional aggregat
     */
    public List<InntektArbeidYtelseGrunnlag> hentAlleGrunnlagFor(AktørId aktørId, KoblingReferanse koblingReferanse, boolean kunAktive) {
        return repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, koblingReferanse, kunAktive);
    }

    public Set<Inntektsmelding> hentAlleInntektsmeldingerFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        return repository.hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
    }


    public Map<Inntektsmelding, ArbeidsforholdInformasjon> hentArbeidsforholdinfoInntektsmeldingerMapFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        return repository.hentArbeidsforholdInfoInntektsmeldingerMapFor(aktørId, saksnummer, ytelseType);
    }

    ;

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

        var grunnlagByKobling = grunnlag.stream()
            .collect(Collectors.groupingBy(InntektArbeidYtelseGrunnlag::getKoblingId));

        var grunnlagEtterspurt = grunnlagByKobling.entrySet().stream()
            .flatMap(e -> filterGrunnlag(e.getKey(), e.getValue(), grunnlagVersjon).stream());

        return grunnlagEtterspurt.collect(Collectors.toList());
    }

    /**
     * @param grunnlagReferanse
     * @return henter optional aggregat
     */
    public Optional<InntektArbeidYtelseGrunnlag> hentGrunnlagFor(GrunnlagReferanse grunnlagReferanse) {
        return repository.hentInntektArbeidYtelseForReferanse(grunnlagReferanse);
    }

    /**
     * Oopprett builder for register data.
     *
     * @param koblingReferanse
     * @return Register inntekt og arbeid før skjæringstidspunktet (Opprett for å endre eller legge til registeropplysning)
     */
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(KoblingReferanse koblingReferanse, UUID angittReferanse,
                                                                        LocalDateTime angittOpprettetTidspunkt) {
        return repository.opprettBuilderFor(koblingReferanse, angittReferanse, angittOpprettetTidspunkt, VersjonType.REGISTER);
    }

    /**
     * Opprett builder for saksbehandlers overstyringer.
     *
     * @param koblingReferanse
     * @return Saksbehandlers overstyringer av IAY (primært {@link no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid}).
     */
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlet(KoblingReferanse koblingReferanse, UUID angittReferanse,
                                                                             LocalDateTime angittOpprettetTidspunkt) {
        return repository.opprettBuilderFor(koblingReferanse, angittReferanse, angittOpprettetTidspunkt, VersjonType.SAKSBEHANDLET);
    }

    /**
     * @param koblingId
     * @param inntektArbeidYtelseAggregatBuilder lagrer ned aggregat (builder bestemmer hvilke del av treet som blir lagret)
     */
    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        repository.lagre(koblingReferanse, inntektArbeidYtelseAggregatBuilder);
    }

    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseGrunnlagBuilder builder) {
        repository.lagre(koblingReferanse, builder);
    }

    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse).orElseGet(ArbeidsforholdInformasjon::new);
    }

    public Optional<OppgittOpptjeningEntitet> hentOppgittOpptjeningFor(UUID oppgittOpptjeningEksternReferanse) {
        return repository.hentOppgittOpptjeningFor(oppgittOpptjeningEksternReferanse);
    }

    public Optional<InntektArbeidYtelseAggregat> hentIAYAggregatFor(UUID eksternReferanse) {
        return repository.hentIAYAggregatFor(eksternReferanse);
    }

    public boolean erGrunnlagAktivt(UUID eksternReferanse) {
        return repository.erGrunnlagAktivt(eksternReferanse);
    }

    /**
     * Kopier grunnlag fra en behandling til en annen.
     *
     * @param ytelseType
     * @param dataset
     */
    public void kopierGrunnlagFraEksisterendeBehandling(YtelseType ytelseType, AktørId aktørId, Saksnummer saksnummer, KoblingReferanse fraKobling,
                                                        KoblingReferanse tilKobling, Set<Dataset> dataset) {
        var origAggregat = hentGrunnlagFor(fraKobling);
        if (origAggregat.isPresent()) {
            var builder = kopierGrunnlagPlussNyereInntektsmeldingerForFagsak(ytelseType, aktørId, saksnummer, origAggregat.get(), dataset);
            lagre(tilKobling, builder);
        }
    }

    /**
     * Dersom det finnes en eller flere nyere inntektsmeldinger på en annen behandling på samme fagsak, så vil
     * disse kopieres til den angitte behandlingen. Dette trengs i tilfeller der en behandling har fått en ny
     * inntektsmelding, blitt henlagt, og en revurdering er opprettet basert på en tidligere behandling med vedtak
     * som ikke inkluderer en eller flere inntektsmeldinger som er mottatt etter vedtaket, men før revurderingen.
     *
     * @param arbeidsforholdInformasjon
     * @param dataset
     * @return
     */
    private InntektArbeidYtelseGrunnlagBuilder kopierGrunnlagPlussNyereInntektsmeldingerForFagsak(YtelseType ytelseType,
                                                                                                  AktørId aktørId,
                                                                                                  Saksnummer saksnummer,
                                                                                                  InntektArbeidYtelseGrunnlag original,
                                                                                                  Set<Dataset> dataset) {

        var builder = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(original, dataset);
        if(dataset.contains(Dataset.INNTEKTSMELDING)) {
            var gjeldendeInntektsmeldinger = original.getInntektsmeldinger().map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(List.of());

            var innsendingstidspunkt = finnInnsendingstidspunktForNyesteEksisterendeIm(gjeldendeInntektsmeldinger);
            var arbeidsforholdInformasjon = original.getArbeidsforholdInformasjon().orElseGet(ArbeidsforholdInformasjon::new);

            var inntektsmeldinger = hentAlleInntektsmeldingerFor(aktørId, saksnummer, ytelseType);
            var kopi = inntektsmeldinger.stream()
                .filter(im -> im.getInnsendingstidspunkt().isAfter(innsendingstidspunkt))
                .sorted(Comparator.comparing(Inntektsmelding::getInnsendingstidspunkt))
                .map(InntektsmeldingBuilder::kopi)
                .map(InntektsmeldingBuilder::build)
                .collect(Collectors.toList());

            var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(arbeidsforholdInformasjon);
            repository.oppdaterBuilderMedNyeInntektsmeldinger(informasjonBuilder, kopi, builder);
        }

        return builder;

    }

    private LocalDateTime finnInnsendingstidspunktForNyesteEksisterendeIm(Collection<Inntektsmelding> inntektsmeldinger) {
        return inntektsmeldinger.stream()
            .max(Comparator.comparing(Inntektsmelding::getInnsendingstidspunkt))
            .map(Inntektsmelding::getInnsendingstidspunkt).orElse(LocalDateTime.MIN);
    }

    public KoblingReferanse hentKoblingReferanse(GrunnlagReferanse grunnlagReferanse) {
        return repository.hentKoblingReferanseFor(grunnlagReferanse);
    }

    private List<InntektArbeidYtelseGrunnlag> filterGrunnlag(Long koblingId, List<InntektArbeidYtelseGrunnlag> grunnlagPerKobling,
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

        switch (grunnlagVersjon) {
            case FØRSTE:
                return List.of(første);
            case SISTE:
                return List.of(siste);
            case FØRSTE_OG_SISTE:
                return Objects.equals(første, siste) ? List.of(første) : List.of(første, siste);
            default:
                throw new UnsupportedOperationException("GrunnlagVersjon " + grunnlagVersjon + " er ikke støttet her for " + koblingId);
        }

    }
}
