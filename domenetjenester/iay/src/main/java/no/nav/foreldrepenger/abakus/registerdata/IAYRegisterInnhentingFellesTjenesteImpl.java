package no.nav.foreldrepenger.abakus.registerdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.domene.iay.AktivitetsAvtaleBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektspostBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.NæringsinntektType;
import no.nav.foreldrepenger.abakus.domene.iay.OffentligYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.foreldrepenger.abakus.domene.iay.PensjonTrygdType;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseInntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyring;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
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
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataElement;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

public abstract class IAYRegisterInnhentingFellesTjenesteImpl implements IAYRegisterInnhentingTjeneste {

    public static final Map<RegisterdataElement, InntektsKilde> ELEMENT_TIL_INNTEKTS_KILDE_MAP = Map.of(RegisterdataElement.INNTEKT_PENSJONSGIVENDE, InntektsKilde.INNTEKT_OPPTJENING, RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG, InntektsKilde.INNTEKT_BEREGNING, RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG, InntektsKilde.INNTEKT_SAMMENLIGNING);
    private VirksomhetTjeneste virksomhetTjeneste;
    private YtelseRegisterInnhenting ytelseRegisterInnhenting;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private InnhentingSamletTjeneste innhentingSamletTjeneste;
    private KodeverkRepository kodeverkRepository;
    private ByggYrkesaktiviteterTjeneste byggYrkesaktiviteterTjeneste;
    private SigrunTjeneste sigrunTjeneste;
    private InntektMapper inntektMapper;

    protected IAYRegisterInnhentingFellesTjenesteImpl() {
    }

    protected IAYRegisterInnhentingFellesTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                      KodeverkRepository kodeverkRepository,
                                                      VirksomhetTjeneste virksomhetTjeneste,
                                                      InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                      SigrunTjeneste sigrunTjeneste,
                                                      VedtakYtelseRepository vedtakYtelseRepository,
                                                      InntektMapper inntektMapper) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.sigrunTjeneste = sigrunTjeneste;
        this.ytelseRegisterInnhenting = new YtelseRegisterInnhenting(innhentingSamletTjeneste, vedtakYtelseRepository);
        this.byggYrkesaktiviteterTjeneste = new ByggYrkesaktiviteterTjeneste(kodeverkRepository);
        this.inntektMapper = inntektMapper;
    }

    @Override
    public InntektArbeidYtelseGrunnlagBuilder innhentRegisterdata(Kobling kobling, Set<RegisterdataElement> informasjonsElementer) {
        InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse()));
        final InntektArbeidYtelseAggregatBuilder builder = grunnlagBuilder.getRegisterBuilder();

        // Arbeidsforhold & inntekter
        Set<ArbeidsforholdIdentifikator> innhentetArbeidsforhold = innhentArbeidsforhold(kobling, builder, informasjonsElementer);

        if (skalInnhenteNæringsInntekterFor(kobling) && informasjonsElementer.contains(RegisterdataElement.LIGNET_NÆRING)) {
            boolean søkerHarOppgittEgenNæring = inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse())
                .flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjening)
                .map(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty())
                .orElse(false);
            if (søkerHarOppgittEgenNæring) {
                innhentNæringsOpplysninger(kobling, builder);
            }
        }
        // Ytelser
        if (informasjonsElementer.contains(RegisterdataElement.YTELSE)) {
            innhentYtelser(kobling, builder);
        }

        // Fjerner overstyringer for bortfalte arbeidsforhold (f.eks. grunnet endringer i aareg)
        ArbeidsforholdInformasjonBuilder informasjonBuilder = FjernOverstyringerForBortfalteArbeidsforhold.fjern(grunnlagBuilder, innhentetArbeidsforhold);

        builder.getNyeInternArbeidsforholdReferanser().forEach(informasjonBuilder::leggTilNyReferanse);

        grunnlagBuilder
            .medData(builder)
            .medInformasjon(informasjonBuilder.build());

        return grunnlagBuilder;
    }

    private void innhentNæringsOpplysninger(Kobling kobling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> map = sigrunTjeneste.beregnetSkatt(kobling.getAktørId());
        inntektMapper.mapFraSigrun(kobling.getAktørId(), map, inntektArbeidYtelseAggregatBuilder);
    }

    private void innhentYtelser(Kobling kobling, InntektArbeidYtelseAggregatBuilder builder) {
        ytelseRegisterInnhenting.byggYtelser(kobling, kobling.getAktørId(),
            kobling.getOpplysningsperiode().tilIntervall(),
            builder,
            skalInnhenteYtelseGrunnlag(kobling));
    }

    private Set<ArbeidsforholdIdentifikator> innhentArbeidsforhold(Kobling kobling, InntektArbeidYtelseAggregatBuilder builder, Set<RegisterdataElement> informasjonsElementer) {
        return byggOpptjeningOpplysningene(kobling, kobling.getAktørId(), kobling.getOpplysningsperiode().tilIntervall(), builder, informasjonsElementer);
    }

    private void oversettFrilanseArbeidsforhold(Kobling kobling,
                                                InntektArbeidYtelseAggregatBuilder builder,
                                                Map.Entry<ArbeidsforholdIdentifikator, List<FrilansArbeidsforhold>> frilansArbeidsforhold, AktørId aktørId) {

        var koblingReferanse = kobling.getKoblingReferanse();

        final InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        final ArbeidsforholdIdentifikator arbeidsforholdIdentifikator = frilansArbeidsforhold.getKey();
        final Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsforholdIdentifikator);

        var arbeidsforholdRef = finnReferanseFor(koblingReferanse, arbeidsgiver, arbeidsforholdIdentifikator.getArbeidsforholdId());

        final String arbeidsforholdId = arbeidsforholdIdentifikator.harArbeidsforholdRef() ? arbeidsforholdIdentifikator.getArbeidsforholdId().getReferanse()
            : null;
        var eksternReferanse = EksternArbeidsforholdRef.ref(arbeidsforholdId);
        var internReferanse = arbeidsforholdRef.orElseGet(() -> {
            return builder.medNyInternArbeidsforholdRef(arbeidsgiver, eksternReferanse);
        });

        final Opptjeningsnøkkel nøkkel = new Opptjeningsnøkkel(internReferanse, arbeidsgiver);
        final ArbeidType arbeidType = kodeverkRepository.finn(ArbeidType.class, arbeidsforholdIdentifikator.getType());
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidType);
        yrkesaktivitetBuilder.medArbeidsforholdId(internReferanse)
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(arbeidType);
        for (FrilansArbeidsforhold avtale : frilansArbeidsforhold.getValue()) {
            yrkesaktivitetBuilder.leggTilAktivitetsAvtale(opprettAktivitetsAvtaleFrilans(avtale, yrkesaktivitetBuilder));
        }

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        builder.leggTilAktørArbeid(aktørArbeidBuilder);
    }

    private Set<ArbeidsforholdIdentifikator> byggOpptjeningOpplysningene(Kobling kobling, AktørId aktørId, Interval opplysningsPeriode,
                                                                         InntektArbeidYtelseAggregatBuilder builder, Set<RegisterdataElement> informasjonsElementer) {
        var inntektselementer = Set.of(RegisterdataElement.INNTEKT_PENSJONSGIVENDE,
            RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG,
            RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG);

        if (informasjonsElementer.stream().noneMatch(inntektselementer::contains) && !informasjonsElementer.contains(RegisterdataElement.ARBEIDSFORHOLD)) {
            return Collections.emptySet();
        }

        Set<ArbeidsforholdIdentifikator> arbeidsforholdList = new HashSet<>();

        if (informasjonsElementer.contains(RegisterdataElement.ARBEIDSFORHOLD)) {
            InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
            aktørArbeidBuilder.tilbakestillYrkesaktiviteter();
            Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = innhentingSamletTjeneste.getArbeidsforhold(aktørId, opplysningsPeriode);
            arbeidsforhold.entrySet().forEach(forholdet -> oversettArbeidsforholdTilYrkesaktivitet(kobling, builder, forholdet, aktørArbeidBuilder));
            arbeidsforholdList = arbeidsforhold.keySet();
        }

        if (informasjonsElementer.stream().anyMatch(inntektselementer::contains)) {
            informasjonsElementer.stream()
                .filter(ELEMENT_TIL_INNTEKTS_KILDE_MAP::containsKey)
                .forEach(registerdataElement -> innhentInntektsopplysningFor(kobling, aktørId, builder, informasjonsElementer, registerdataElement));
        } else {
            Set.of(RegisterdataElement.INNTEKT_PENSJONSGIVENDE)
                .forEach(registerdataElement -> innhentInntektsopplysningFor(kobling, aktørId, builder, informasjonsElementer, registerdataElement));
        }

        return arbeidsforholdList;
    }

    private void innhentInntektsopplysningFor(Kobling kobling, AktørId aktørId, InntektArbeidYtelseAggregatBuilder builder, Set<RegisterdataElement> informasjonsElementer, RegisterdataElement registerdataElement) {
        final var inntektsKilde = ELEMENT_TIL_INNTEKTS_KILDE_MAP.get(registerdataElement);
        final InntektsInformasjon inntektsInformasjon = innhentingSamletTjeneste.getInntektsInformasjon(aktørId,
            kobling.getOpplysningsperiode().tilIntervall(), inntektsKilde);

        if (informasjonsElementer.contains(registerdataElement)) {
            inntektMapper.mapFraInntektskomponent(aktørId, builder, inntektsInformasjon);
        }

        if (inntektsKilde.equals(InntektsKilde.INNTEKT_OPPTJENING) && informasjonsElementer.contains(RegisterdataElement.ARBEIDSFORHOLD)) {
            inntektsInformasjon.getFrilansArbeidsforhold()
                .entrySet()
                .forEach(frilansArbeidsforhold -> oversettFrilanseArbeidsforhold(kobling, builder, frilansArbeidsforhold, aktørId));
        }
    }

    private Optional<InternArbeidsforholdRef> finnReferanseFor(KoblingReferanse koblingReferanse, Arbeidsgiver arbeidsgiver,
                                                               EksternArbeidsforholdRef arbeidsforholdRef) {
        Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = inntektArbeidYtelseTjeneste.hentGrunnlagFor(koblingReferanse)
            .flatMap(InntektArbeidYtelseGrunnlag::getArbeidsforholdInformasjon);
        if (arbeidsforholdInformasjon.isPresent()) {
            final ArbeidsforholdInformasjon informasjon = arbeidsforholdInformasjon.get();
            return informasjon.finnForEksternBeholdHistoriskReferanse(arbeidsgiver, arbeidsforholdRef);
        }
        return Optional.empty();
    }

    private void oversettArbeidsforholdTilYrkesaktivitet(Kobling kobling,
                                                         InntektArbeidYtelseAggregatBuilder builder,
                                                         Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold,
                                                         InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder) {

        var koblingReferanse = kobling.getKoblingReferanse();
        final ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        final Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsgiverIdent);
        final String arbeidsforholdId = arbeidsgiverIdent.harArbeidsforholdRef() ? arbeidsgiverIdent.getArbeidsforholdId().getReferanse() : null;
        var eksternReferanse = EksternArbeidsforholdRef.ref(arbeidsforholdId);
        var arbeidsforholdRef = finnReferanseFor(koblingReferanse, arbeidsgiver, eksternReferanse);
        var internReferanse = arbeidsforholdRef.orElseGet(() -> builder.medNyInternArbeidsforholdRef(arbeidsgiver, eksternReferanse));

        YrkesaktivitetBuilder yrkesaktivitetBuilder = byggYrkesaktiviteterTjeneste
            .byggYrkesaktivitetForSøker(arbeidsforhold, arbeidsgiver, internReferanse, aktørArbeidBuilder);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

    }

    private Arbeidsgiver mapArbeidsgiver(ArbeidsforholdIdentifikator arbeidsforhold) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            return Arbeidsgiver.person(new AktørId(((Person) arbeidsforhold.getArbeidsgiver()).getAktørId()));
        } else if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            String orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgNummer();
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(orgnr));
        }
        throw new IllegalArgumentException("Utvikler feil: ArbeidsgiverEntitet av ukjent type.");
    }

    private AktivitetsAvtaleBuilder opprettAktivitetsAvtaleFrilans(FrilansArbeidsforhold frilansArbeidsforhold,
                                                                   YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        IntervallEntitet periode;
        if (frilansArbeidsforhold.getTom() == null || frilansArbeidsforhold.getTom().isBefore(frilansArbeidsforhold.getFom())) {
            periode = IntervallEntitet.fraOgMed(frilansArbeidsforhold.getFom());
        } else {
            periode = IntervallEntitet.fraOgMedTilOgMed(frilansArbeidsforhold.getFom(), frilansArbeidsforhold.getTom());
        }
        return yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, true);
    }
}
