package no.nav.foreldrepenger.abakus.registerdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.domene.iay.AktørInntektEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.ArbeidsgiverEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.NæringsinntektType;
import no.nav.foreldrepenger.abakus.domene.iay.OffentligYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.foreldrepenger.abakus.domene.iay.PensjonTrygdType;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FrilansArbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.util.FPDateUtil;

abstract class IAYRegisterInnhentingFellesTjenesteImpl implements IAYRegisterInnhentingTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(IAYRegisterInnhentingFellesTjenesteImpl.class);

    private VirksomhetTjeneste virksomhetTjeneste;
    private YtelseRegisterInnhenting ytelseRegisterInnhenting;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private InnhentingSamletTjeneste innhentingSamletTjeneste;
    private KodeverkRepository kodeverkRepository;
    private ByggYrkesaktiviteterTjeneste byggYrkesaktiviteterTjeneste;
    private AktørConsumer aktørConsumer;
    private SigrunTjeneste sigrunTjeneste;

    IAYRegisterInnhentingFellesTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                            KodeverkRepository kodeverkRepository,
                                            VirksomhetTjeneste virksomhetTjeneste,
                                            InnhentingSamletTjeneste innhentingSamletTjeneste,
                                            AktørConsumer aktørConsumer, SigrunTjeneste sigrunTjeneste, VedtakYtelseRepository vedtakYtelseRepository) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.aktørConsumer = aktørConsumer;
        this.sigrunTjeneste = sigrunTjeneste;
        this.ytelseRegisterInnhenting = new YtelseRegisterInnhenting(virksomhetTjeneste, innhentingSamletTjeneste, vedtakYtelseRepository);
        this.byggYrkesaktiviteterTjeneste = new ByggYrkesaktiviteterTjeneste(kodeverkRepository);
    }

    IAYRegisterInnhentingFellesTjenesteImpl() {
    }

    private void innhentNæringsOpplysninger(Kobling kobling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> map = sigrunTjeneste.beregnetSkatt(kobling.getAktørId());
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(kobling.getAktørId());

        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektsKilde.SIGRUN, null);

        for (Map.Entry<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> entry : map.entrySet()) {
            for (Map.Entry<InntektspostType, BigDecimal> type : entry.getValue().entrySet()) {
                InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
                inntektspostBuilder
                    .medInntektspostType(type.getKey())
                    .medBeløp(type.getValue())
                    .medPeriode(entry.getKey().getFomDato(), entry.getKey().getTomDato());
                inntektBuilder.leggTilInntektspost(inntektspostBuilder);
            }
        }
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder innhentInntekterFor(Kobling kobling, AktørId aktørId,
                                                                  InntektsKilde... kilder) {
        final InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(kobling.getKoblingReferanse());
        return innhentInntekterFor(kobling, aktørId, builder, kilder);
    }

    private InntektArbeidYtelseAggregatBuilder innhentInntekterFor(Kobling kobling, AktørId aktørId, InntektArbeidYtelseAggregatBuilder builder, InntektsKilde... kilder) {
        if (kilder.length == 0) {
            return builder;
        }
        for (InntektsKilde kilde : kilder) {
            final InntektsInformasjon inntektsInformasjon = innhentingSamletTjeneste.getInntektsInformasjon(aktørId, kobling, kobling.getOpplysningsperiode().tilIntervall(), kilde);
            leggTilInntekter(aktørId, builder, inntektsInformasjon);
            if (kilde.equals(InntektsKilde.INNTEKT_OPPTJENING)) {
                inntektsInformasjon.getFrilansArbeidsforhold()
                    .entrySet()
                    .forEach(frilansArbeidsforhold -> oversettFrilanseArbeidsforhold(builder, frilansArbeidsforhold, aktørId));
            }
        }
        return builder;
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder innhentRegisterdata(Kobling kobling) {
        final InntektArbeidYtelseAggregatBuilder builder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(kobling.getKoblingReferanse());
        // Arbeidsforhold & inntekter
        innhentArbeidsforhold(kobling, builder);
        if (skalInnhenteNæringsInntekterFor(kobling)) {
            innhentNæringsOpplysninger(kobling, builder);
        }
        // Ytelser
        innhentYtelser(kobling, builder);
        return builder;
    }

    private void innhentYtelser(Kobling kobling, InntektArbeidYtelseAggregatBuilder builder) {
        ytelseRegisterInnhenting.byggYtelser(kobling, kobling.getAktørId(), kobling.getOpplysningsperiode().tilIntervall(), builder, skalInnhenteYtelseGrunnlag(kobling));
        final Optional<AktørId> annenPartAktørId = kobling.getAnnenPartAktørId();
        annenPartAktørId.ifPresent(a -> ytelseRegisterInnhenting.byggYtelser(kobling, a, kobling.getOpplysningsperiode().tilIntervall(), builder, false));
    }

    private void innhentArbeidsforhold(Kobling kobling, InntektArbeidYtelseAggregatBuilder builder) {
        byggOpptjeningOpplysningene(kobling, kobling.getAktørId(), kobling.getOpplysningsperiode().tilIntervall(), builder);

        // For annen forelder
        final Optional<AktørId> annenPartAktørId = kobling.getAnnenPartAktørId();
        annenPartAktørId.ifPresent(a -> byggOpptjeningOpplysningene(kobling, a, kobling.getOpplysningsperiode().tilIntervall(), builder));
    }

    private void leggTilInntekter(AktørId aktørId, InntektArbeidYtelseAggregatBuilder builder, InntektsInformasjon inntektsInformasjon) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        InntektsKilde kilde = inntektsInformasjon.getKilde();
        aktørInntektBuilder.fjernInntekterFraKilde(kilde);

        inntektsInformasjon.getMånedsinntekterGruppertPåArbeidsgiver()
            .forEach((identifikator, inntektOgRegelListe) -> leggTilInntekterPåArbeidsforhold(builder, aktørInntektBuilder, inntektOgRegelListe, identifikator, kilde));

        final List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt = inntektsInformasjon.getYtelsesTrygdEllerPensjonInntektSummert();
        if (!ytelsesTrygdEllerPensjonInntekt.isEmpty()) {
            leggTilYtelseInntekter(ytelsesTrygdEllerPensjonInntekt, builder, aktørId, kilde);
        }
    }

    private void leggTilYtelseInntekter(List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt, InntektArbeidYtelseAggregatBuilder builder, AktørId aktørId, InntektsKilde inntektOpptjening) {
        final InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        final AktørInntektEntitet.InntektBuilder inntektBuilderForYtelser = aktørInntektBuilder.getInntektBuilderForYtelser(inntektOpptjening);
        ytelsesTrygdEllerPensjonInntekt.forEach(mi -> lagInntektsposterYtelse(mi, inntektBuilderForYtelser));

        aktørInntektBuilder.leggTilInntekt(inntektBuilderForYtelser);
        builder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    private void oversettFrilanseArbeidsforhold(InntektArbeidYtelseAggregatBuilder builder,
                                                Map.Entry<ArbeidsforholdIdentifikator, List<FrilansArbeidsforhold>> frilansArbeidsforhold, AktørId aktørId) {
        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        final ArbeidsforholdIdentifikator arbeidsforholdIdentifikator = frilansArbeidsforhold.getKey();
        final Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsforholdIdentifikator);
        final Opptjeningsnøkkel nøkkel = mapOpptjeningsnøkkel(arbeidsgiver, arbeidsforholdIdentifikator.getArbeidsforholdId());
        final ArbeidType arbeidType = kodeverkRepository.finn(ArbeidType.class, arbeidsforholdIdentifikator.getType());
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidType);
        yrkesaktivitetBuilder.medArbeidsforholdId(arbeidsforholdIdentifikator.getArbeidsforholdId())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(arbeidType);
        for (FrilansArbeidsforhold avtale : frilansArbeidsforhold.getValue()) {
            yrkesaktivitetBuilder.leggTilAktivitetsAvtale(opprettAktivitetsAvtaleFrilans(avtale, yrkesaktivitetBuilder));
        }

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        builder.leggTilAktørArbeid(aktørArbeidBuilder);
    }

    private Opptjeningsnøkkel mapOpptjeningsnøkkel(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        return new Opptjeningsnøkkel(arbeidsforholdRef, arbeidsgiver);
    }

    private void leggTilInntekterPåArbeidsforhold(InntektArbeidYtelseAggregatBuilder builder, InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                                  Map<YearMonth, List<InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel>> månedsinntekterGruppertPåArbeidsgiver,
                                                  String arbeidsgiverIdentifikator, InntektsKilde inntektOpptjening) {

        Arbeidsgiver arbeidsgiver;
        if (OrganisasjonsNummerValidator.erGyldig(arbeidsgiverIdentifikator)) {
            boolean orgledd = virksomhetTjeneste.sjekkOmVirksomhetErOrgledd(arbeidsgiverIdentifikator);
            if (!orgledd) {
                LocalDate hentedato = finnHentedatoForJuridisk(månedsinntekterGruppertPåArbeidsgiver.keySet());
                arbeidsgiver = ArbeidsgiverEntitet.virksomhet(virksomhetTjeneste.hentOgLagreOrganisasjonMedHensynTilJuridisk(arbeidsgiverIdentifikator, hentedato));
                aktørInntektBuilder.leggTilInntekt(byggInntekt(månedsinntekterGruppertPåArbeidsgiver, arbeidsgiver, aktørInntektBuilder, inntektOpptjening));
                builder.leggTilAktørInntekt(aktørInntektBuilder);
            } else {
                LOGGER.info("Inntekter rapportert på orglegg({}), blir IKKE lagret", arbeidsgiverIdentifikator);
            }
        } else {
            if (PersonIdent.erGyldigFnr(arbeidsgiverIdentifikator)) {
                Optional<String> arbeidsgiverOpt = aktørConsumer.hentAktørIdForPersonIdent(arbeidsgiverIdentifikator);
                if (arbeidsgiverOpt.isEmpty()) {
                    throw InnhentingFeil.FACTORY.finnerIkkeAktørIdForArbeidsgiverSomErPrivatperson().toException();
                }
                arbeidsgiver = ArbeidsgiverEntitet.person(new AktørId(arbeidsgiverOpt.get()));
            } else {
                arbeidsgiver = ArbeidsgiverEntitet.person(new AktørId(arbeidsgiverIdentifikator));
            }
            aktørInntektBuilder.leggTilInntekt(byggInntekt(månedsinntekterGruppertPåArbeidsgiver, arbeidsgiver, aktørInntektBuilder, inntektOpptjening));
            builder.leggTilAktørInntekt(aktørInntektBuilder);
        }
    }

    private LocalDate finnHentedatoForJuridisk(Set<YearMonth> inntekterForMåneder) {
        return inntekterForMåneder.stream()
            .map(m -> LocalDate.of(m.getYear(), m.getMonth(), 1))
            .max(Comparator.naturalOrder()).orElse(FPDateUtil.iDag());
    }

    private void byggOpptjeningOpplysningene(Kobling kobling, AktørId aktørId, Interval opplysningsPeriode,
                                             InntektArbeidYtelseAggregatBuilder builder) {
        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = innhentingSamletTjeneste.getArbeidsforhold(aktørId, opplysningsPeriode);
        arbeidsforhold.entrySet().forEach(forholdet -> oversettArbeidsforholdTilYrkesaktivitet(builder, forholdet, aktørId, kobling));

        final InntektsInformasjon inntektsInformasjon = innhentingSamletTjeneste.getInntektsInformasjon(aktørId, kobling, opplysningsPeriode, InntektsKilde.INNTEKT_OPPTJENING);
        leggTilInntekter(aktørId, builder, inntektsInformasjon);
        inntektsInformasjon.getFrilansArbeidsforhold()
            .entrySet()
            .forEach(frilansArbeidsforhold -> oversettFrilanseArbeidsforhold(builder, frilansArbeidsforhold, aktørId));
    }

    private void oversettArbeidsforholdTilYrkesaktivitet(InntektArbeidYtelseAggregatBuilder builder,
                                                         Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold, AktørId aktørId, Kobling kobling) {
        final ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        final Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsgiverIdent);
        final String arbeidsforholdId = arbeidsgiverIdent.harArbeidsforholdRef() ? arbeidsgiverIdent.getArbeidsforholdId().getReferanse() : null;
        final ArbeidsforholdRef arbeidsforholdRef = inntektArbeidYtelseTjeneste.finnReferanseFor(kobling.getKoblingReferanse(), arbeidsgiver, ArbeidsforholdRef.ref(arbeidsforholdId), true);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);

        YrkesaktivitetBuilder yrkesaktivitetBuilder = byggYrkesaktiviteterTjeneste
            .byggYrkesaktivitetForSøker(arbeidsforhold, arbeidsgiver, mapOpptjeningsnøkkel(arbeidsgiver, arbeidsforholdRef), aktørArbeidBuilder);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);
    }

    private Arbeidsgiver mapArbeidsgiver(ArbeidsforholdIdentifikator arbeidsforhold) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            return ArbeidsgiverEntitet.person(new AktørId(((Person) arbeidsforhold.getArbeidsgiver()).getAktørId()));
        } else if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            String orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgNummer();
            return ArbeidsgiverEntitet.virksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(orgnr));
        }
        throw new IllegalArgumentException("Utvikler feil: ArbeidsgiverEntitet av ukjent type.");
    }

    private YrkesaktivitetEntitet.AktivitetsAvtaleBuilder opprettAktivitetsAvtaleFrilans(FrilansArbeidsforhold frilansArbeidsforhold,
                                                                                         YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        DatoIntervallEntitet periode;
        if (frilansArbeidsforhold.getTom() == null || frilansArbeidsforhold.getTom().isBefore(frilansArbeidsforhold.getFom())) {
            periode = DatoIntervallEntitet.fraOgMed(frilansArbeidsforhold.getFom());
        } else {
            periode = DatoIntervallEntitet.fraOgMedTilOgMed(frilansArbeidsforhold.getFom(), frilansArbeidsforhold.getTom());
        }
        return yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true);
    }


    private AktørInntektEntitet.InntektBuilder byggInntekt(Map<YearMonth, List<InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel>> inntekter, Arbeidsgiver arbeidsgiver,
                                                           InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                                           InntektsKilde inntektOpptjening) {

        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(inntektOpptjening, new Opptjeningsnøkkel(arbeidsgiver));

        for (YearMonth måned : inntekter.keySet()) {
            List<InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel> månedsinnteker = inntekter.get(måned);
            Map<String, Integer> antalInntekterForAvgiftsregel = månedsinnteker
                .stream()
                .filter(e -> e.getSkatteOgAvgiftsregelType() != null)
                .collect(Collectors.groupingBy(
                    InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel::getSkatteOgAvgiftsregelType,
                    Collectors.collectingAndThen(
                        Collectors.mapping(InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel::getBeløp, Collectors.toSet()),
                        Set::size)));

            Optional<String> valgtSkatteOgAvgiftsregel = Optional.empty();
            BigDecimal beløpSum = månedsinnteker.stream().map(InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel::getBeløp).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (antalInntekterForAvgiftsregel.keySet().size() > 1) {
                String skatteOgAvgiftsregler = antalInntekterForAvgiftsregel.keySet().stream().collect(Collectors.joining(", "));
                // TODO Diamant velger her en random verdi.
                valgtSkatteOgAvgiftsregel = Optional.of(antalInntekterForAvgiftsregel.keySet().iterator().next());
                LOGGER.error("ArbeidsgiverEntitet orgnr {} har flere månedsinntekter for måned {} med forskjellige skatte -og avgiftsregler {}. Velger {}", arbeidsgiver.getIdentifikator(), måned, skatteOgAvgiftsregler, valgtSkatteOgAvgiftsregel);
            } else if (antalInntekterForAvgiftsregel.keySet().size() == 1) {
                valgtSkatteOgAvgiftsregel = Optional.of(antalInntekterForAvgiftsregel.keySet().iterator().next());
            }

            lagInntektsposter(måned, beløpSum, valgtSkatteOgAvgiftsregel, inntektBuilder);
        }

        return inntektBuilder
            .medArbeidsgiver(arbeidsgiver);
    }

    private void lagInntektsposterYtelse(Månedsinntekt månedsinntekt, AktørInntektEntitet.InntektBuilder inntektBuilder) {
        inntektBuilder.leggTilInntektspost(inntektBuilder.getInntektspostBuilder()
            .medBeløp(månedsinntekt.getBeløp())
            .medPeriode(månedsinntekt.getMåned().atDay(1), månedsinntekt.getMåned().atEndOfMonth())
            .medInntektspostType(InntektspostType.YTELSE)
            .medYtelse(mapTilKodeliste(månedsinntekt)));
    }

    private void lagInntektsposter(YearMonth måned, BigDecimal sumInntektsbeløp, Optional<String> valgtSkatteOgAvgiftsregel, AktørInntektEntitet.InntektBuilder inntektBuilder) {

        InntektEntitet.InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
        inntektspostBuilder
            .medBeløp(sumInntektsbeløp)
            .medPeriode(måned.atDay(1), måned.atEndOfMonth())
            .medInntektspostType(InntektspostType.LØNN);

        if (valgtSkatteOgAvgiftsregel.isPresent()) {
            SkatteOgAvgiftsregelType skatteOgAvgiftsregelType = kodeverkRepository.finnForKodeverkEiersKode(SkatteOgAvgiftsregelType.class, valgtSkatteOgAvgiftsregel.get());
            inntektspostBuilder.medSkatteOgAvgiftsregelType(skatteOgAvgiftsregelType);
        }

        inntektBuilder.leggTilInntektspost(inntektspostBuilder);
    }

    private YtelseType mapTilKodeliste(Månedsinntekt månedsinntekt) {
        if (månedsinntekt.getPensjonKode() != null) {
            return kodeverkRepository.finnForKodeverkEiersKode(PensjonTrygdType.class, månedsinntekt.getPensjonKode());
        } else if (månedsinntekt.getYtelseKode() != null) {
            return kodeverkRepository.finnForKodeverkEiersKode(OffentligYtelseType.class, månedsinntekt.getYtelseKode());
        }
        return kodeverkRepository.finnForKodeverkEiersKode(NæringsinntektType.class, månedsinntekt.getNæringsinntektKode());
    }
}
