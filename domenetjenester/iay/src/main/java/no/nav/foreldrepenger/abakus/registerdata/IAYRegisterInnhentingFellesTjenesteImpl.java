package no.nav.foreldrepenger.abakus.registerdata;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.domene.iay.AktivitetsAvtaleBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
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
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FrilansArbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataElement;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

public abstract class IAYRegisterInnhentingFellesTjenesteImpl implements IAYRegisterInnhentingTjeneste {

    public static final Map<RegisterdataElement, InntektskildeType> ELEMENT_TIL_INNTEKTS_KILDE_MAP = Map.of(RegisterdataElement.INNTEKT_PENSJONSGIVENDE, InntektskildeType.INNTEKT_OPPTJENING, RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG, InntektskildeType.INNTEKT_BEREGNING, RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG, InntektskildeType.INNTEKT_SAMMENLIGNING);
    private static final Logger LOGGER = LoggerFactory.getLogger(IAYRegisterInnhentingFellesTjenesteImpl.class);
    private static final LocalDate CUTOFF_FRILANS_AAREG = LocalDate.of(2019,12,31);

    protected InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private YtelseRegisterInnhenting ytelseRegisterInnhenting;
    private InnhentingSamletTjeneste innhentingSamletTjeneste;
    private ByggYrkesaktiviteterTjeneste byggYrkesaktiviteterTjeneste;
    private AktørTjeneste aktørTjeneste;
    private SigrunTjeneste sigrunTjeneste;
    private MapInntektFraDtoTilDomene mapInntektFraDtoTilDomene;

    protected IAYRegisterInnhentingFellesTjenesteImpl() {
    }

    protected IAYRegisterInnhentingFellesTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                      VirksomhetTjeneste virksomhetTjeneste,
                                                      InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                      SigrunTjeneste sigrunTjeneste,
                                                      VedtakYtelseRepository vedtakYtelseRepository,
                                                      MapInntektFraDtoTilDomene mapInntektFraDtoTilDomene,
                                                      AktørTjeneste aktørTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.sigrunTjeneste = sigrunTjeneste;
        this.ytelseRegisterInnhenting = new YtelseRegisterInnhenting(innhentingSamletTjeneste, vedtakYtelseRepository);
        this.byggYrkesaktiviteterTjeneste = new ByggYrkesaktiviteterTjeneste();
        this.mapInntektFraDtoTilDomene = mapInntektFraDtoTilDomene;
        this.aktørTjeneste = aktørTjeneste;
    }

    @Override
    public InntektArbeidYtelseGrunnlagBuilder innhentRegisterdata(Kobling kobling, Set<RegisterdataElement> informasjonsElementer) {
        var grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse()));
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

        grunnlagBuilder
            .medData(builder)
            .medInformasjon(informasjonBuilder.build());

        return grunnlagBuilder;
    }

    private void innhentNæringsOpplysninger(Kobling kobling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        var map = sigrunTjeneste.beregnetSkatt(kobling.getAktørId());
        mapInntektFraDtoTilDomene.mapFraSigrun(kobling.getAktørId(), map, inntektArbeidYtelseAggregatBuilder);
    }

    private PersonIdent getFnrFraAktørId(AktørId aktørId, YtelseType ytelse) {
        return aktørTjeneste.hentIdentForAktør(aktørId, ytelse).orElseThrow();
    }

    private void innhentYtelser(Kobling kobling, InntektArbeidYtelseAggregatBuilder builder) {
        ytelseRegisterInnhenting.byggYtelser(kobling, kobling.getAktørId(), getFnrFraAktørId(kobling.getAktørId(), kobling.getYtelseType()),
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

        var aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        var arbeidsforholdIdentifikator = frilansArbeidsforhold.getKey();
        var arbeidsgiver = mapArbeidsgiver(arbeidsforholdIdentifikator);

        var arbeidsforholdRef = finnReferanseFor(koblingReferanse, arbeidsgiver, arbeidsforholdIdentifikator.getArbeidsforholdId());

        final String arbeidsforholdId = arbeidsforholdIdentifikator.harArbeidsforholdRef() ? arbeidsforholdIdentifikator.getArbeidsforholdId().getReferanse()
            : null;
        var eksternReferanse = EksternArbeidsforholdRef.ref(arbeidsforholdId);
        var internReferanse = arbeidsforholdRef.orElseGet(() -> {
            return builder.medNyInternArbeidsforholdRef(arbeidsgiver, eksternReferanse);
        });

        Opptjeningsnøkkel nøkkel = new Opptjeningsnøkkel(internReferanse, arbeidsgiver);
        ArbeidType arbeidType = ArbeidType.fraKode(arbeidsforholdIdentifikator.getType());
        var yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidType);
        yrkesaktivitetBuilder.medArbeidsforholdId(internReferanse)
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(arbeidType);
        for (var avtale : frilansArbeidsforhold.getValue()) {
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
            Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = innhentingSamletTjeneste.getArbeidsforhold(aktørId,
                getFnrFraAktørId(aktørId, kobling.getYtelseType()), opplysningsPeriode);
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
        var inntektsKilde = ELEMENT_TIL_INNTEKTS_KILDE_MAP.get(registerdataElement);
        var inntektsInformasjon = innhentingSamletTjeneste.getInntektsInformasjon(aktørId, kobling.getOpplysningsperiode().tilIntervall(), inntektsKilde, kobling.getYtelseType());

        // En slags ytelse som er utbetalt fra NAV til bruker som LØNN ...
        if (innhentingSamletTjeneste.skalInnhenteLønnskompensasjon(kobling, inntektsKilde)) {
            inntektsInformasjon.leggTilMånedsinntekter(innhentingSamletTjeneste.getLønnskompensasjon(aktørId, kobling.getOpplysningsperiode().tilIntervall()));
        }
        if (informasjonsElementer.contains(registerdataElement)) {
            mapInntektFraDtoTilDomene.mapFraInntektskomponent(aktørId, builder, inntektsInformasjon, kobling.getYtelseType());
        }

        if (inntektsKilde.equals(InntektskildeType.INNTEKT_OPPTJENING) && informasjonsElementer.contains(RegisterdataElement.ARBEIDSFORHOLD)) {
            inntektsInformasjon.getFrilansArbeidsforhold()
                .entrySet()
                .forEach(frilansArbeidsforhold -> oversettFrilanseArbeidsforhold(kobling, builder, frilansArbeidsforhold, aktørId));
            // Midlertidig logging
            try {
                Map<ArbeidsforholdIdentifikator, List<FrilansArbeidsforhold>> frilansINNTK = new LinkedHashMap<>();
                inntektsInformasjon.getFrilansArbeidsforhold().entrySet().stream()
                    .filter(e -> e.getValue().stream().anyMatch(this::skalTasMedFraInntk))
                    .forEach(e -> frilansINNTK.put(new ArbeidsforholdIdentifikator(e.getKey().getArbeidsgiver(), e.getKey().getArbeidsforholdId(), ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER.getOffisiellKode()),
                        e.getValue().stream().filter(this::skalTasMedFraInntk).collect(Collectors.toList())));
                if (frilansINNTK.isEmpty())
                    return;

                var frilansAAREG = innhentingSamletTjeneste.getArbeidsforholdFrilans(aktørId,
                    getFnrFraAktørId(aktørId, kobling.getYtelseType()), kobling.getOpplysningsperiode().tilIntervall());

                Set<ArbeidsforholdIdentifikator> arbeidsgivereFL = Stream.concat(frilansAAREG.keySet().stream(), frilansINNTK.keySet().stream()).collect(Collectors.toSet());
                arbeidsgivereFL.forEach(a -> {
                    Set<FrilansSammenligner> inntk = frilansINNTK.getOrDefault(a, List.of()).stream().map(FrilansSammenligner::new).collect(Collectors.toSet());
                    Set<FrilansSammenligner> aareg = frilansAAREG.getOrDefault(a, List.of()).stream().map(FrilansSammenligner::new).collect(Collectors.toSet());
                    if (inntk.size() != aareg.size() || !inntk.containsAll(aareg)) {
                        var alleinntk = inntektsInformasjon.getFrilansArbeidsforhold()
                            .getOrDefault(new ArbeidsforholdIdentifikator(a.getArbeidsgiver(), a.getArbeidsforholdId(), ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER.getKode()), List.of()).stream()
                            .map(FrilansSammenligner::new).collect(Collectors.toSet());
                        LOGGER.info("ABAKUS AAREG FRILANS avvik for {} inntk {} aareg {} alleinntk {}", a.getArbeidsforholdId(), inntk, aareg, alleinntk);
                    } else {
                        LOGGER.info("ABAKUS AAREG FRILANS like svar for {} aareg {}", a.getArbeidsforholdId(), aareg);
                    }
                });
            } catch (Exception e) {
                LOGGER.info("ABAKUS AAREG FRILANS feil", e);
            }
        }
    }

    private boolean skalTasMedFraInntk(FrilansArbeidsforhold frilansArbeidsforhold) {
        return frilansArbeidsforhold.getTom() == null || CUTOFF_FRILANS_AAREG.isBefore(frilansArbeidsforhold.getTom());
    }

    private static class FrilansSammenligner {
        private EksternArbeidsforholdRef arbeidsforholdId;
        private LocalDate arbeidFom;
        private LocalDate arbeidTom;
        private String agIdent;

        FrilansSammenligner(FrilansArbeidsforhold fa) {
            this.arbeidFom = fa.getFom();
            this.arbeidTom = fa.getTom();
            this.agIdent = fa.getArbeidsgiverOrgnr() != null ? fa.getArbeidsgiverOrgnr() : fa.getArbeidsgiverAktørId().getId();
            this.arbeidsforholdId  = fa.getArbeidsforholdRef();
        }

        FrilansSammenligner(Arbeidsforhold fa) {
            this.arbeidFom = fa.getArbeidFom();
            this.arbeidTom = fa.getArbeidTom();
            this.agIdent = fa.getArbeidsgiver().getIdentifikator();
            this.arbeidsforholdId  = fa.getArbeidsforholdId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FrilansSammenligner that = (FrilansSammenligner) o;
            return Objects.equals(arbeidsforholdId, that.arbeidsforholdId) && Objects.equals(arbeidFom, that.arbeidFom) && Objects.equals(arbeidTom, that.arbeidTom) && Objects.equals(agIdent, that.agIdent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(arbeidsforholdId, arbeidFom, arbeidTom, agIdent);
        }

        @Override
        public String toString() {
            return "FrilansSammenligner{" +
                "arbeidFom=" + arbeidFom +
                ", arbeidTom=" + arbeidTom +
                '}';
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
        ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        Arbeidsgiver arbeidsgiver = mapArbeidsgiver(arbeidsgiverIdent);
        String arbeidsforholdId = arbeidsgiverIdent.harArbeidsforholdRef() ? arbeidsgiverIdent.getArbeidsforholdId().getReferanse() : null;
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
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOrganisasjon(orgnr));
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
