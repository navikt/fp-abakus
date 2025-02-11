package no.nav.foreldrepenger.abakus.registerdata;

import static no.nav.foreldrepenger.abakus.registerdata.ByggLønnsinntektInntektTjeneste.mapLønnsinntekter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektYtelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektspostBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataElement;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.exception.TekniskException;

/**
 * Standard IAY register innhenter.
 */
@ApplicationScoped
public class IAYRegisterInnhentingTjeneste {

    public static final Map<RegisterdataElement, InntektskildeType> ELEMENT_TIL_INNTEKTS_KILDE_MAP = Map.of(
        RegisterdataElement.INNTEKT_PENSJONSGIVENDE, InntektskildeType.INNTEKT_OPPTJENING, RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG,
        InntektskildeType.INNTEKT_BEREGNING, RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG, InntektskildeType.INNTEKT_SAMMENLIGNING);
    private static final Logger LOG = LoggerFactory.getLogger(IAYRegisterInnhentingTjeneste.class);

    protected InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private YtelseRegisterInnhenting ytelseRegisterInnhenting;
    private InnhentingSamletTjeneste innhentingSamletTjeneste;
    private ByggYrkesaktiviteterTjeneste byggYrkesaktiviteterTjeneste;
    private AktørTjeneste aktørConsumer;
    private SigrunTjeneste sigrunTjeneste;

    IAYRegisterInnhentingTjeneste() {
        // CDI
    }

    @Inject
    public IAYRegisterInnhentingTjeneste(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                         VirksomhetTjeneste virksomhetTjeneste,
                                         InnhentingSamletTjeneste innhentingSamletTjeneste,
                                         AktørTjeneste aktørConsumer,
                                         SigrunTjeneste sigrunTjeneste,
                                         VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.aktørConsumer = aktørConsumer;
        this.sigrunTjeneste = sigrunTjeneste;
        this.ytelseRegisterInnhenting = new YtelseRegisterInnhenting(innhentingSamletTjeneste, vedtattYtelseInnhentingTjeneste);
        this.byggYrkesaktiviteterTjeneste = new ByggYrkesaktiviteterTjeneste();
    }

    private void innhentNæringsOpplysninger(Kobling kobling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        LOG.info("Henter lignet inntekt for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse());

        var personIdent = getFnrFraAktørId(kobling.getAktørId());

        var map = sigrunTjeneste.hentPensjonsgivende(personIdent, kobling.getOpplysningsperiodeSkattegrunnlag());
        var aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(kobling.getAktørId());

        var inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektskildeType.SIGRUN, null);
        inntektBuilder.tilbakestillInntektsposterForPerioder(map.keySet());

        for (var entry : map.entrySet()) {
            LOG.info("Fant lignet inntekt for periode {} for sak=[{}, {}] med behandling='{}'", entry.getKey(), kobling.getSaksnummer(),
                kobling.getYtelseType(), kobling.getKoblingReferanse());
            for (Map.Entry<InntektspostType, BigDecimal> type : entry.getValue().entrySet()) {
                InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
                inntektspostBuilder.medInntektspostType(type.getKey())
                    .medBeløp(type.getValue())
                    .medPeriode(entry.getKey().getFomDato(), entry.getKey().getTomDato());
                inntektBuilder.leggTilInntektspost(inntektspostBuilder);
            }
        }
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    private class FnrSupplier {

        private final AktørId aktørId;

        public FnrSupplier(AktørId aktørId) {
            this.aktørId = aktørId;
        }

        private PersonIdent tilPersonIdent() {
            try {
                return aktørConsumer.hentIdentForAktør(this.aktørId).orElse(null);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public InntektArbeidYtelseGrunnlagBuilder innhentRegisterdata(Kobling kobling, Set<RegisterdataElement> informasjonsElementer) {
        var grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(
            inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse()));
        var builder = grunnlagBuilder.getRegisterBuilder();

        // Arbeidsforhold & inntekter
        Set<ArbeidsforholdIdentifikator> innhentetArbeidsforhold = innhentArbeidsforhold(kobling, builder, informasjonsElementer);

        if (informasjonsElementer.contains(RegisterdataElement.LIGNET_NÆRING) && skalInnhenteNæringsInntekterFor(kobling)) {
            innhentNæringsOpplysninger(kobling, builder);
        }
        // Ytelser
        if (informasjonsElementer.contains(RegisterdataElement.YTELSE)) {
            innhentYtelser(kobling, builder);
        }

        // Fjerner overstyringer for bortfalte arbeidsforhold (f.eks. grunnet endringer i aareg)
        var informasjonBuilder = FjernOverstyringerForBortfalteArbeidsforhold.fjern(grunnlagBuilder, innhentetArbeidsforhold);

        builder.getNyeInternArbeidsforholdReferanser().forEach(informasjonBuilder::leggTilNyReferanse);

        grunnlagBuilder.medData(builder).medInformasjon(informasjonBuilder.build());

        return grunnlagBuilder;
    }

    private PersonIdent getFnrFraAktørId(AktørId aktørId) {
        return aktørConsumer.hentIdentForAktør(aktørId).orElseThrow();
    }

    private void innhentYtelser(Kobling kobling, InntektArbeidYtelseAggregatBuilder builder) {
        ytelseRegisterInnhenting.byggYtelser(kobling, kobling.getAktørId(), getFnrFraAktørId(kobling.getAktørId()), kobling.getOpplysningsperiode(),
            builder);
    }

    private Set<ArbeidsforholdIdentifikator> innhentArbeidsforhold(Kobling kobling,
                                                                   InntektArbeidYtelseAggregatBuilder builder,
                                                                   Set<RegisterdataElement> informasjonsElementer) {
        return byggOpptjeningOpplysningene(kobling, kobling.getAktørId(), kobling.getOpplysningsperiode(), builder, informasjonsElementer);
    }

    private void leggTilInntekter(AktørId aktørId, InntektArbeidYtelseAggregatBuilder builder, InntektsInformasjon inntektsInformasjon) {
        var aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        InntektskildeType kilde = inntektsInformasjon.getKilde();
        aktørInntektBuilder.fjernInntekterFraKilde(kilde);

        Map<String, Arbeidsgiver> arbeidsgivereLookup = lagArbeidsgiverLookup(inntektsInformasjon);

        mapLønnsinntekter(inntektsInformasjon, aktørInntektBuilder, arbeidsgivereLookup);
        builder.leggTilAktørInntekt(aktørInntektBuilder);

        List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt = inntektsInformasjon.getYtelsesTrygdEllerPensjonInntektSummert();
        if (!ytelsesTrygdEllerPensjonInntekt.isEmpty()) {
            leggTilYtelseInntekter(ytelsesTrygdEllerPensjonInntekt, builder, aktørId, kilde);
        }
    }

    private Map<String, Arbeidsgiver> lagArbeidsgiverLookup(InntektsInformasjon inntektsInformasjon) {
        Map<String, Set<YearMonth>> alleArbeidsgivereMedMåneder = inntektsInformasjon.getMånedsinntekterUtenomYtelser()
            .stream()
            .collect(Collectors.groupingBy(Månedsinntekt::getArbeidsgiver, Collectors.mapping(Månedsinntekt::getMåned, Collectors.toSet())));
        Map<String, Arbeidsgiver> arbeidsgivereLookup = new HashMap<>();
        alleArbeidsgivereMedMåneder.forEach((agString, måneder) -> Optional.ofNullable(finnArbeidsgiverForInntektsData(agString, måneder))
            .ifPresent(ag -> arbeidsgivereLookup.put(agString, ag)));
        return arbeidsgivereLookup;
    }

    private void leggTilYtelseInntekter(List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt,
                                        InntektArbeidYtelseAggregatBuilder builder,
                                        AktørId aktørId,
                                        InntektskildeType inntektOpptjening) {
        var aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        var inntektBuilderForYtelser = aktørInntektBuilder.getInntektBuilderForYtelser(inntektOpptjening);
        ytelsesTrygdEllerPensjonInntekt.forEach(mi -> lagInntektsposterYtelse(mi, inntektBuilderForYtelser));

        aktørInntektBuilder.leggTilInntekt(inntektBuilderForYtelser);
        builder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    private Arbeidsgiver finnArbeidsgiverForInntektsData(String arbeidsgiverString, Set<YearMonth> inntekterForMåneder) {

        if (OrganisasjonsNummerValidator.erGyldig(arbeidsgiverString)) {
            boolean orgledd = virksomhetTjeneste.sjekkOmOrganisasjonErOrgledd(arbeidsgiverString);
            if (!orgledd) {
                LocalDate hentedato = finnHentedatoForJuridisk(inntekterForMåneder);
                return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOrganisasjonMedHensynTilJuridisk(arbeidsgiverString, hentedato));
            } else {
                LOG.info("Inntekter rapportert på orgledd({}), blir IKKE lagret", getIdentifikatorString(arbeidsgiverString));
                return null;
            }
        } else {
            if (PersonIdent.erGyldigFnr(arbeidsgiverString)) {
                var arbeidsgiverAktørId = aktørConsumer.hentAktørForIdent(new PersonIdent(arbeidsgiverString))
                    .orElseThrow(() -> new TekniskException("FP-464378",
                        "Feil ved oppslag av aktørID for en arbeidgiver som er en privatperson registrert med fnr/dnr"));
                return Arbeidsgiver.person(arbeidsgiverAktørId);
            } else {
                return Arbeidsgiver.person(new AktørId(arbeidsgiverString));
            }
        }
    }

    private String getIdentifikatorString(String arbeidsgiverIdentifikator) {
        if (arbeidsgiverIdentifikator == null) {
            return null;
        }
        int length = arbeidsgiverIdentifikator.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + arbeidsgiverIdentifikator.substring(length - 4);
    }

    private LocalDate finnHentedatoForJuridisk(Set<YearMonth> inntekterForMåneder) {
        return inntekterForMåneder.stream()
            .map(m -> LocalDate.of(m.getYear(), m.getMonth(), 1))
            .max(Comparator.naturalOrder())
            .orElse(LocalDate.now());
    }

    private Set<ArbeidsforholdIdentifikator> byggOpptjeningOpplysningene(Kobling kobling,
                                                                         AktørId aktørId,
                                                                         IntervallEntitet opplysningsPeriode,
                                                                         InntektArbeidYtelseAggregatBuilder builder,
                                                                         Set<RegisterdataElement> informasjonsElementer) {
        var inntektselementer = Set.of(RegisterdataElement.INNTEKT_PENSJONSGIVENDE, RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG,
            RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG);

        if (informasjonsElementer.stream().noneMatch(inntektselementer::contains) && !informasjonsElementer.contains(
            RegisterdataElement.ARBEIDSFORHOLD)) {
            return Collections.emptySet();
        }

        Set<ArbeidsforholdIdentifikator> arbeidsforholdList = new HashSet<>();

        if (informasjonsElementer.contains(RegisterdataElement.ARBEIDSFORHOLD)) {
            InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
            aktørArbeidBuilder.tilbakestillYrkesaktiviteter();
            // Hvis/Når AAREG en gang i framtiden gir frilans som del av default arbeidsforholdtype - så kan følgende kuttes
            Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholdFrilans = innhentingSamletTjeneste.getArbeidsforholdFrilans(aktørId,
                getFnrFraAktørId(aktørId), opplysningsPeriode);
            arbeidsforholdFrilans.entrySet()
                .forEach(forholdet -> oversettArbeidsforholdTilYrkesaktivitet(kobling, builder, forholdet, aktørArbeidBuilder));
            arbeidsforholdList.addAll(arbeidsforholdFrilans.keySet());

            Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = innhentingSamletTjeneste.getArbeidsforhold(aktørId,
                getFnrFraAktørId(aktørId), opplysningsPeriode);
            arbeidsforhold.entrySet().forEach(forholdet -> oversettArbeidsforholdTilYrkesaktivitet(kobling, builder, forholdet, aktørArbeidBuilder));
            arbeidsforholdList.addAll(arbeidsforhold.keySet());
        }

        if (informasjonsElementer.stream().anyMatch(inntektselementer::contains)) {
            informasjonsElementer.stream()
                .filter(ELEMENT_TIL_INNTEKTS_KILDE_MAP::containsKey)
                .forEach(registerdataElement -> innhentInntektsopplysningFor(kobling, aktørId, opplysningsPeriode, builder, informasjonsElementer,
                    registerdataElement));
        } else {
            Set.of(RegisterdataElement.INNTEKT_PENSJONSGIVENDE)
                .forEach(registerdataElement -> innhentInntektsopplysningFor(kobling, aktørId, opplysningsPeriode, builder, informasjonsElementer,
                    registerdataElement));
        }
        return arbeidsforholdList;
    }

    private void innhentInntektsopplysningFor(Kobling kobling,
                                              AktørId aktørId,
                                              IntervallEntitet opplysningsPeriode,
                                              InntektArbeidYtelseAggregatBuilder builder,
                                              Set<RegisterdataElement> informasjonsElementer,
                                              RegisterdataElement registerdataElement) {
        var inntektsKilde = ELEMENT_TIL_INNTEKTS_KILDE_MAP.get(registerdataElement);
        var inntektsInformasjon = innhentingSamletTjeneste.getInntektsInformasjon(aktørId, opplysningsPeriode, inntektsKilde);

        // En slags ytelse som er utbetalt fra NAV til bruker som LØNN ...
        if (innhentingSamletTjeneste.skalInnhenteLønnskompensasjon(kobling, inntektsKilde)) {
            inntektsInformasjon.leggTilMånedsinntekter(innhentingSamletTjeneste.getLønnskompensasjon(aktørId, opplysningsPeriode));
        }
        if (informasjonsElementer.contains(registerdataElement)) {
            leggTilInntekter(aktørId, builder, inntektsInformasjon);
        }
    }

    private Optional<InternArbeidsforholdRef> finnReferanseFor(KoblingReferanse koblingReferanse,
                                                               Arbeidsgiver arbeidsgiver,
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
        ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsgiverIdent);
        String arbeidsforholdId = arbeidsgiverIdent.harArbeidsforholdRef() ? arbeidsgiverIdent.getArbeidsforholdId().getReferanse() : null;
        var eksternReferanse = EksternArbeidsforholdRef.ref(arbeidsforholdId);
        var arbeidsforholdRef = finnReferanseFor(koblingReferanse, arbeidsgiver, eksternReferanse);
        var internReferanse = arbeidsforholdRef.orElseGet(() -> builder.medNyInternArbeidsforholdRef(arbeidsgiver, eksternReferanse));

        YrkesaktivitetBuilder yrkesaktivitetBuilder = byggYrkesaktiviteterTjeneste.byggYrkesaktivitetForSøker(arbeidsforhold, arbeidsgiver,
            internReferanse, aktørArbeidBuilder);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

    }

    private Arbeidsgiver mapArbeidsgiver(ArbeidsforholdIdentifikator arbeidsforhold) {
        var arbeidsgiver = arbeidsforhold.getArbeidsgiver();
        if (arbeidsgiver instanceof Person person) {
            return Arbeidsgiver.person(new AktørId(person.getAktørId()));
        } else if (arbeidsgiver instanceof Organisasjon organisasjon) {
            String orgnr = organisasjon.getOrgNummer();
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOrganisasjon(orgnr));
        }
        throw new IllegalArgumentException("Utvikler feil: ArbeidsgiverEntitet av ukjent type.");
    }

    private void lagInntektsposterYtelse(Månedsinntekt månedsinntekt, InntektBuilder inntektBuilder) {
        inntektBuilder.leggTilInntektspost(inntektBuilder.getInntektspostBuilder()
            .medBeløp(månedsinntekt.getBeløp())
            .medPeriode(månedsinntekt.getMåned().atDay(1), månedsinntekt.getMåned().atEndOfMonth())
            .medInntektspostType(InntektspostType.YTELSE)
            .medYtelse(mapTilKodeliste(månedsinntekt)));
    }


    private InntektYtelseType mapTilKodeliste(Månedsinntekt månedsinntekt) {
        if (månedsinntekt.getPensjonKode() != null) {
            return InntektYtelseType.finnForKodeverkEiersKode(InntektYtelseType.Kategori.TRYGD, månedsinntekt.getPensjonKode());
        } else if (månedsinntekt.getYtelseKode() != null) {
            return InntektYtelseType.finnForKodeverkEiersKode(InntektYtelseType.Kategori.YTELSE, månedsinntekt.getYtelseKode());
        } else if (månedsinntekt.getNæringsinntektKode() != null) {
            return InntektYtelseType.finnForKodeverkEiersKode(InntektYtelseType.Kategori.NÆRING, månedsinntekt.getNæringsinntektKode());
        }
        return null;
    }

    private boolean skalInnhenteNæringsInntekterFor(Kobling kobling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());

        // FP, SVP bruker ikke aggregat for oppgitt opptjening (støtter kun en pr behandling)
        return grunnlag.flatMap(InntektArbeidYtelseGrunnlag::getGjeldendeOppgittOpptjening)
            .map(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty())
            .orElse(false);
    }


}
