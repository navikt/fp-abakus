package no.nav.foreldrepenger.abakus.iay.impl;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.domene.iay.AktivitetsAvtale;
import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.util.IntervallUtil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

@ApplicationScoped
public class InntektArbeidYtelseTjenesteImpl implements InntektArbeidYtelseTjeneste {

    // Arbeidtyper som kommer fra AA-reg
    private static final Set<ArbeidType> AA_REG_TYPER = Set.of(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, ArbeidType.MARITIMT_ARBEIDSFORHOLD, ArbeidType.FORENKLET_OPPGJØRSORDNING);

    private InntektArbeidYtelseRepository repository;
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private AktørConsumer aktørConsumer;
    private VirksomhetTjeneste virksomhetTjeneste;

    InntektArbeidYtelseTjenesteImpl() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseTjenesteImpl(InntektArbeidYtelseRepository repository,
                                           ArbeidsforholdTjeneste arbeidsforholdTjeneste,
                                           AktørConsumer aktørConsumer,
                                           VirksomhetTjeneste virksomhetTjeneste) {
        Objects.requireNonNull(repository, "repository");
        this.aktørConsumer = aktørConsumer;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.repository = repository;
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentAggregat(Kobling behandling) {
        return repository.hentInntektArbeidYtelseForBehandling(behandling.getId());
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(Long behandlingId) {
        return repository.hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId);
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentFørsteVersjon(Kobling behandling) {
        return repository.hentFørsteVersjon(behandling.getId());
    }

    @Override
    public List<Inntektsmelding> hentAlleInntektsmeldinger(Kobling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> iayGrunnlag = repository.hentInntektArbeidYtelseGrunnlagForBehandling(behandling.getId());
        if (iayGrunnlag.isPresent()) {
            return iayGrunnlag.get().getInntektsmeldinger().map(InntektsmeldingAggregat::getInntektsmeldinger).orElse(emptyList());
        }
        return emptyList();
    }

    private Map<String, Inntektsmelding> hentIMMedIndexKey(Long behandlingId) {
        List<Inntektsmelding> inntektsmeldinger = repository.hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId)
            .map(InntektArbeidYtelseGrunnlag::getInntektsmeldinger)
            .filter(Optional::isPresent)
            .map(imAggregat -> imAggregat.get().getInntektsmeldinger())
            .orElse(Collections.emptyList());

        return inntektsmeldinger.stream()
            .collect(Collectors.toMap(im -> ((InntektsmeldingEntitet) im).getIndexKey(), im -> im));
    }

    @Override
    public List<InntektsmeldingSomIkkeKommer> hentAlleInntektsmeldingerSomIkkeKommer(Long behandlingId) {
        List<InntektsmeldingSomIkkeKommer> result = new ArrayList<>();
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId);
        inntektArbeidYtelseGrunnlag.ifPresent(iayg -> result.addAll(iayg.getInntektsmeldingerSomIkkeKommer()));
        return result;
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmeldingForBehandling(Long behandlingId) {
        return repository.hentForrigeVersjonAvInntektsmeldingForBehandling(behandlingId);
    }

    @Override
    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(Long behandlingId) {
        return repository.hentArbeidsforholdInformasjonForBehandling(behandlingId);
    }

    @Override
    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForGrunnlag(Long inntektArbeidYtelseGrunnlagId) {
        return repository.hentArbeidsforholdInformasjonForGrunnlagId(inntektArbeidYtelseGrunnlagId);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(Long behandlingId) {
        return repository.opprettBuilderFor(behandlingId, VersjonType.REGISTER);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlet(Long behandlingId) {
        return repository.opprettBuilderFor(behandlingId, VersjonType.SAKSBEHANDLET);
    }

    @Override
    public void lagre(Long behandlingId, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        repository.lagre(behandlingId, inntektArbeidYtelseAggregatBuilder);
    }

    @Override
    public Collection<Yrkesaktivitet> hentYrkesaktiviteterForSøker(Kobling behandling, boolean overstyrt) {
        AktørId søker = behandling.getAktørId();
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOptional = repository.hentInntektArbeidYtelseGrunnlagForBehandling(
            behandling.getId());
        if (grunnlagOptional.isPresent()) {
            LocalDate skjæringstidspunkt = behandling.getSkjæringstidspunkt();
            return grunnlagOptional.get().hentAlleYrkesaktiviteterFørStpFor(søker, skjæringstidspunkt, overstyrt);
        }
        return emptyList();
    }

    @Override
    public Map<String, Set<String>> utledManglendeInntektsmeldingerFraArkiv(Kobling behandling) {
        Objects.requireNonNull(behandling, "behandling");
        Map<String, Set<String>> påkrevdeInntektsmeldinger = utledPåkrevdeInntektsmeldingerFraArkiv(behandling);
        filtrerUtMottatteInntektsmeldinger(behandling, påkrevdeInntektsmeldinger);
        return påkrevdeInntektsmeldinger;
    }

    @Override
    public Map<String, Set<String>> utledManglendeInntektsmeldingerFraGrunnlag(Kobling behandling) {
        final Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = repository.hentInntektArbeidYtelseGrunnlagForBehandling(behandling.getId());
        Map<String, Set<String>> påkrevdeInntektsmeldinger = utledPåkrevdeInntektsmeldingerFraGrunnlag(behandling, inntektArbeidYtelseGrunnlag);
        filtrerUtMottatteInntektsmeldinger(behandling, påkrevdeInntektsmeldinger);
        return påkrevdeInntektsmeldinger;
    }

    private void filtrerUtMottatteInntektsmeldinger(Kobling behandling, Map<String, Set<String>> påkrevdeInntektsmeldinger) {
        inntektsmeldingerSomHarKommet(behandling, påkrevdeInntektsmeldinger);
        fjernInntektsmeldingerSomAltErAvklart(behandling, påkrevdeInntektsmeldinger);
    }

    private void inntektsmeldingerSomHarKommet(Kobling behandling, Map<String, Set<String>> påkrevdeInntektsmeldinger) {
        List<Inntektsmelding> inntektsmeldinger;

        inntektsmeldinger = hentAlleInntektsmeldinger(behandling);

        for (Inntektsmelding inntektsmelding : inntektsmeldinger) {
            if (påkrevdeInntektsmeldinger.containsKey(inntektsmelding.getArbeidsgiver().getIdentifikator())) {
                final Set<String> arbeidsforhold = påkrevdeInntektsmeldinger.get(inntektsmelding.getArbeidsgiver().getIdentifikator());
                if (inntektsmelding.gjelderForEtSpesifiktArbeidsforhold()) {
                    arbeidsforhold.remove(inntektsmelding.getArbeidsforholdRef().getReferanse());
                } else {
                    arbeidsforhold.clear();
                }
                if (arbeidsforhold.isEmpty()) {
                    påkrevdeInntektsmeldinger.remove(inntektsmelding.getArbeidsgiver().getIdentifikator());
                }
            }
        }
    }

    private void fjernInntektsmeldingerSomAltErAvklart(Kobling behandling, Map<String, Set<String>> påkrevdeInntektsmeldinger) {
        final Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = hentArbeidsforholdInformasjonForBehandling(behandling.getId());
        if (arbeidsforholdInformasjon.isPresent()) {
            final ArbeidsforholdInformasjon informasjon = arbeidsforholdInformasjon.get();
            final List<ArbeidsforholdOverstyringEntitet> inntektsmeldingSomIkkeKommer = informasjon.getOverstyringer()
                .stream()
                .filter(it -> (it.getHandling().equals(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING)
                    || it.getHandling().equals(ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE)))
                .collect(Collectors.toList());

            for (ArbeidsforholdOverstyringEntitet im : inntektsmeldingSomIkkeKommer) {
                if (påkrevdeInntektsmeldinger.containsKey(im.getArbeidsgiver().getIdentifikator())) {
                    final Set<String> arbeidsforhold = påkrevdeInntektsmeldinger.get(im.getArbeidsgiver().getIdentifikator());
                    if (im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                        arbeidsforhold.remove(im.getArbeidsforholdRef().getReferanse());
                    } else {
                        arbeidsforhold.clear();
                    }
                    if (arbeidsforhold.isEmpty()) {
                        påkrevdeInntektsmeldinger.remove(im.getArbeidsgiver().getIdentifikator());
                    }
                }
            }
        }
    }

    /**
     * Utleder påkrevde inntektsmeldinger fra grunnlaget basert på informasjonen som har blitt innhentet fra aa-reg
     * (under INNREG-steget)
     * <p>
     * Sjekker opp mot mottatt dato, og melder påkrevde på de som har gjeldende(bruker var ansatt) på mottatt-dato.
     * <p>
     * Skal ikke benytte sjekk mot arkivet slik som gjøres i utledManglendeInntektsmeldingerFraArkiv da
     * disse verdiene skal ikke påvirkes av endringer i arkivet.
     */
    private Map<String, Set<String>> utledPåkrevdeInntektsmeldingerFraGrunnlag(Kobling behandling,
                                                                               Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag) {
        Map<String, Set<String>> påkrevdeInntektsmeldinger = new HashMap<>();
        final LocalDate skjæringstidspunkt = behandling.getSkjæringstidspunkt();

        inntektArbeidYtelseGrunnlag.ifPresent(grunnlag -> {
            Optional<AktørArbeid> aktørArbeid = grunnlag.getAktørArbeidFørStp(behandling.getAktørId(), skjæringstidspunkt);
            aktørArbeid.ifPresent(arbeid -> arbeid.getYrkesaktiviteter().stream()
                .filter(ya -> AA_REG_TYPER.contains(ya.getArbeidType()))
                .filter(ya -> harRelevantAnsettelsesperiodeSomDekkerAngittDato(ya, skjæringstidspunkt))
                .forEach(relevantYrkesaktivitet -> {
                    String identifikator = relevantYrkesaktivitet.getArbeidsgiver().getIdentifikator();
                    String referanse = relevantYrkesaktivitet.getArbeidsforholdRef().orElse(ArbeidsforholdRef.ref(null)).getReferanse();
                    if (påkrevdeInntektsmeldinger.containsKey(identifikator)) {
                        påkrevdeInntektsmeldinger.get(identifikator).add(referanse);
                    } else {
                        final Set<String> arbeidsforholdSet = new LinkedHashSet<>();
                        arbeidsforholdSet.add(referanse);
                        påkrevdeInntektsmeldinger.put(identifikator, arbeidsforholdSet);
                    }
                }));
        });
        return påkrevdeInntektsmeldinger;
    }

    private boolean harRelevantAnsettelsesperiodeSomDekkerAngittDato(Yrkesaktivitet yrkesaktivitet, LocalDate dato) {
        if (yrkesaktivitet.erArbeidsforhold()) {
            List<AktivitetsAvtale> ansettelsesPerioder = yrkesaktivitet.getAnsettelsesPerioder();
            if (!ansettelsesPerioder.isEmpty()) {
                return ansettelsesPerioder.stream().anyMatch(avtale -> IntervallUtil.byggIntervall(avtale.getFraOgMed(), avtale.getTilOgMed()).overlaps(IntervallUtil.tilIntervall(dato)));
            }
        }
        return false;
    }

    private Map<String, Set<String>> utledPåkrevdeInntektsmeldingerFraArkiv(Kobling behandling) {
        final PersonIdent fnr = aktørConsumer.hentPersonIdentForAktørId(behandling.getAktørId().getId()).map(PersonIdent::new).orElseThrow();
        final LocalDate skjæringstidspunkt = behandling.getSkjæringstidspunkt();
        final Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholds = arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(fnr,
            IntervallUtil.tilIntervall(skjæringstidspunkt));

        return mapTilArbeidsgivereOgArbeidsforhold(behandling, arbeidsforholds);
    }

    private Map<String, Set<String>> mapTilArbeidsgivereOgArbeidsforhold(Kobling behandling, Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholds) {
        Map<String, Set<String>> påkrevdeInntektsmeldinger = new HashMap<>();

        for (ArbeidsforholdIdentifikator arbeidsforhold : arbeidsforholds.keySet()) {
            final String identifikator = arbeidsforhold.getArbeidsgiver().getIdentifikator();
            final ArbeidsforholdRef arbeidsforholdId = utledArbeidsforholdIdentifikator(arbeidsforhold, behandling.getId());
            if (påkrevdeInntektsmeldinger.containsKey(identifikator)) {
                påkrevdeInntektsmeldinger.get(identifikator).add(arbeidsforholdId.getReferanse());
            } else {
                final Set<String> arbeidsforholdSet = new HashSet<>();
                if (arbeidsforholdId != null) {
                    arbeidsforholdSet.add(arbeidsforholdId.getReferanse());
                }
                påkrevdeInntektsmeldinger.put(identifikator, arbeidsforholdSet);
            }
        }
        return påkrevdeInntektsmeldinger;
    }

    @Override
    public ArbeidsforholdRef finnReferanseFor(Long behandlingId, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef, boolean beholdErstattetVerdi) {
        final Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = repository.hentArbeidsforholdInformasjonForBehandling(behandlingId);
        if (arbeidsforholdInformasjon.isPresent()) {
            final ArbeidsforholdInformasjon informasjon = arbeidsforholdInformasjon.get();
            if (beholdErstattetVerdi) {
                return informasjon.finnForEksternBeholdHistoriskReferanse(arbeidsgiver, arbeidsforholdRef);
            }
            return informasjon.finnForEkstern(arbeidsgiver, arbeidsforholdRef);
        }
        return arbeidsforholdRef;
    }

    private ArbeidsforholdRef utledArbeidsforholdIdentifikator(ArbeidsforholdIdentifikator arbeidsforhold, Long behandlingId) {
        return finnReferanseFor(behandlingId, mapTilArbeidsgiver(arbeidsforhold),
            arbeidsforhold.getArbeidsforholdId(), false);
    }

    private Arbeidsgiver mapTilArbeidsgiver(ArbeidsforholdIdentifikator arbeidsforhold) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            return Arbeidsgiver.person(new AktørId(((Person) arbeidsforhold.getArbeidsgiver()).getAktørId()));
        } else if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            String orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgNummer();
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(orgnr));
        }
        throw new IllegalArgumentException("Utvikler feil: Arbeidsgiver av ukjent type.");
    }

    @Override
    public boolean søkerHarOppgittEgenNæring(Kobling behandling) {
        return hentInntektArbeidYtelseGrunnlagForBehandling(behandling.getId())
            .flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjening)
            .map(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty())
            .orElse(false);
    }
}
