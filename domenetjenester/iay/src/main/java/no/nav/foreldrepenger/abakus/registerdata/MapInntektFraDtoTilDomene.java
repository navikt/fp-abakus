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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektspostBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrganisasjonsNummerValidator;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

@ApplicationScoped
public class MapInntektFraDtoTilDomene {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapInntektFraDtoTilDomene.class);
    private VirksomhetTjeneste virksomhetTjeneste;
    private AktørTjeneste aktørConsumer;

    public MapInntektFraDtoTilDomene() {
        // For CDI
    }

    @Inject
    protected MapInntektFraDtoTilDomene(VirksomhetTjeneste virksomhetTjeneste,
                                        AktørTjeneste aktørConsumer) {
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.aktørConsumer = aktørConsumer;
    }

    public void mapFraSigrun(AktørId aktørId, Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> map, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørInntektBuilder(aktørId);

        InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektskildeType.SIGRUN, null);

        for (Map.Entry<IntervallEntitet, Map<InntektspostType, BigDecimal>> entry : map.entrySet()) {
            for (Map.Entry<InntektspostType, BigDecimal> type : entry.getValue().entrySet()) {
                InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
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

    public void mapFraInntektskomponent(AktørId aktørId, InntektArbeidYtelseAggregatBuilder builder, InntektsInformasjon inntektsInformasjon, YtelseType ytelseType) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        InntektskildeType kilde = inntektsInformasjon.getKilde();
        aktørInntektBuilder.fjernInntekterFraKilde(kilde);

        inntektsInformasjon.getMånedsinntekterGruppertPåArbeidsgiver()
            .forEach((identifikator, inntektOgRegelListe) -> leggTilInntekterPåArbeidsforhold(builder, aktørInntektBuilder, inntektOgRegelListe, identifikator,
                kilde, ytelseType)); // Loopes 3 ganger.

        final List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt = inntektsInformasjon.getYtelsesTrygdEllerPensjonInntektSummert();
        if (!ytelsesTrygdEllerPensjonInntekt.isEmpty()) {
            leggTilYtelseInntekter(ytelsesTrygdEllerPensjonInntekt, builder, aktørId, kilde);
        }
    }

    private void leggTilYtelseInntekter(List<Månedsinntekt> ytelsesTrygdEllerPensjonInntekt, InntektArbeidYtelseAggregatBuilder builder, AktørId aktørId,
                                        InntektskildeType inntektOpptjening) {
        final InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = builder.getAktørInntektBuilder(aktørId);
        final InntektBuilder inntektBuilderForYtelser = aktørInntektBuilder.getInntektBuilderForYtelser(inntektOpptjening);
        ytelsesTrygdEllerPensjonInntekt.forEach(mi -> lagInntektsposterYtelse(mi, inntektBuilderForYtelser));

        aktørInntektBuilder.leggTilInntekt(inntektBuilderForYtelser);
        builder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    private void leggTilInntekterPåArbeidsforhold(InntektArbeidYtelseAggregatBuilder builder,
                                                  InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                                  Map<YearMonth, List<InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel>> månedsinntekterGruppertPåArbeidsgiver,
                                                  String arbeidsgiverIdentifikator, InntektskildeType inntektOpptjening,
                                                  YtelseType ytelse) {

        Arbeidsgiver arbeidsgiver;
        if (OrganisasjonsNummerValidator.erGyldig(arbeidsgiverIdentifikator)) {
            boolean orgledd = virksomhetTjeneste.sjekkOmOrganisasjonErOrgledd(arbeidsgiverIdentifikator);
            if (!orgledd) {
                LocalDate hentedato = finnHentedatoForJuridisk(månedsinntekterGruppertPåArbeidsgiver.keySet());
                arbeidsgiver = Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOrganisasjonMedHensynTilJuridisk(arbeidsgiverIdentifikator, hentedato));
                if (arbeidsgiver.getOrgnr() != null && !arbeidsgiver.getOrgnr().getId().equals(arbeidsgiverIdentifikator)) {
                    aktørInntektBuilder.leggTilInntekt(byggInntekt(månedsinntekterGruppertPåArbeidsgiver, arbeidsgiver, aktørInntektBuilder, inntektOpptjening, arbeidsgiverIdentifikator));
                } else {
                    aktørInntektBuilder.leggTilInntekt(byggInntekt(månedsinntekterGruppertPåArbeidsgiver, arbeidsgiver, aktørInntektBuilder, inntektOpptjening));
                }
                builder.leggTilAktørInntekt(aktørInntektBuilder);
            } else {
                LOGGER.info("Inntekter rapportert på orglegg({}), blir IKKE lagret", arbeidsgiverIdentifikator);
            }
        } else {
            if (PersonIdent.erGyldigFnr(arbeidsgiverIdentifikator)) {
                Optional<AktørId> arbeidsgiverOpt = aktørConsumer.hentAktørForIdent(new PersonIdent(arbeidsgiverIdentifikator), ytelse);
                if (arbeidsgiverOpt.isEmpty()) {
                    throw InnhentingFeil.FACTORY.finnerIkkeAktørIdForArbeidsgiverSomErPrivatperson().toException();
                }
                arbeidsgiver = Arbeidsgiver.person(arbeidsgiverOpt.get());
            } else {
                LOGGER.info("Arbeidsgiveridentifikator: {}", arbeidsgiverIdentifikator);
                arbeidsgiver = Arbeidsgiver.person(new AktørId(arbeidsgiverIdentifikator));
            }
            aktørInntektBuilder.leggTilInntekt(byggInntekt(månedsinntekterGruppertPåArbeidsgiver, arbeidsgiver, aktørInntektBuilder, inntektOpptjening));
            builder.leggTilAktørInntekt(aktørInntektBuilder);
        }
    }

    private LocalDate finnHentedatoForJuridisk(Set<YearMonth> inntekterForMåneder) {
        return inntekterForMåneder.stream()
            .map(m -> LocalDate.of(m.getYear(), m.getMonth(), 1))
            .max(Comparator.naturalOrder()).orElse(LocalDate.now());
    }

    private InntektBuilder byggInntekt(Map<YearMonth, List<InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel>> inntekter,
                                       Arbeidsgiver arbeidsgiver,
                                       InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                       InntektskildeType inntektOpptjening) {
        return byggInntekt(inntekter, arbeidsgiver, aktørInntektBuilder, inntektOpptjening, null);
    }


    private InntektBuilder byggInntekt(Map<YearMonth, List<InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel>> inntekter,
                                       Arbeidsgiver arbeidsgiver,
                                       InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                       InntektskildeType inntektOpptjening, String opprineligUtbetalerId) {
        InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(inntektOpptjening, new Opptjeningsnøkkel(arbeidsgiver));

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
            BigDecimal beløpSum = månedsinnteker.stream().map(InntektsInformasjon.MånedsbeløpOgSkatteOgAvgiftsregel::getBeløp).reduce(BigDecimal.ZERO,
                BigDecimal::add);
            if (antalInntekterForAvgiftsregel.keySet().size() > 1) {
                String skatteOgAvgiftsregler = antalInntekterForAvgiftsregel.keySet().stream().collect(Collectors.joining(", "));
                // TODO Diamant velger her en random verdi.
                valgtSkatteOgAvgiftsregel = Optional.of(antalInntekterForAvgiftsregel.keySet().iterator().next());
                LOGGER.error("ArbeidsgiverEntitet orgnr {} har flere månedsinntekter for måned {} med forskjellige skatte -og avgiftsregler {}. Velger {}",
                    arbeidsgiver.getIdentifikator(), måned, skatteOgAvgiftsregler, valgtSkatteOgAvgiftsregel);
            } else if (antalInntekterForAvgiftsregel.keySet().size() == 1) {
                valgtSkatteOgAvgiftsregel = Optional.of(antalInntekterForAvgiftsregel.keySet().iterator().next());
            }

            lagInntektsposter(måned, beløpSum, valgtSkatteOgAvgiftsregel, inntektBuilder, opprineligUtbetalerId);
        }

        return inntektBuilder
            .medArbeidsgiver(arbeidsgiver);
    }

    private void lagInntektsposterYtelse(Månedsinntekt månedsinntekt, InntektBuilder inntektBuilder) {
        inntektBuilder.leggTilInntektspost(inntektBuilder.getInntektspostBuilder()
            .medBeløp(månedsinntekt.getBeløp())
            .medPeriode(månedsinntekt.getMåned().atDay(1), månedsinntekt.getMåned().atEndOfMonth())
            .medInntektspostType(InntektspostType.YTELSE)
            .medYtelse(mapTilKodeliste(månedsinntekt)));
    }

    private void lagInntektsposter(YearMonth måned, BigDecimal sumInntektsbeløp, Optional<String> valgtSkatteOgAvgiftsregel,
                                   InntektBuilder inntektBuilder, String originalUtbetalerId) {

        InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
        inntektspostBuilder
            .medBeløp(sumInntektsbeløp)
            .medPeriode(måned.atDay(1), måned.atEndOfMonth())
            .medInntektspostType(InntektspostType.LØNN);
        if (OrganisasjonsNummerValidator.erGyldig(originalUtbetalerId)) {
            inntektspostBuilder.medOpprinneligUtbetalerOrgnr(originalUtbetalerId);
        }

        if (valgtSkatteOgAvgiftsregel.isPresent()) {
            SkatteOgAvgiftsregelType skatteOgAvgiftsregelType = SkatteOgAvgiftsregelType.finnForKodeverkEiersKode(valgtSkatteOgAvgiftsregel.get());
            inntektspostBuilder.medSkatteOgAvgiftsregelType(skatteOgAvgiftsregelType);
        }

        inntektBuilder.leggTilInntektspost(inntektspostBuilder);
    }

    private UtbetaltYtelseType mapTilKodeliste(Månedsinntekt månedsinntekt) {
        if (månedsinntekt.getPensjonKode() != null) {
            return UtbetaltPensjonTrygdType.finnForKodeverkEiersKode(månedsinntekt.getPensjonKode());
        } else if (månedsinntekt.getYtelseKode() != null) {
            return UtbetaltYtelseFraOffentligeType.finnForKodeverkEiersKode(månedsinntekt.getYtelseKode());
        }
        return UtbetaltNæringsYtelseType.finnForKodeverkEiersKode(månedsinntekt.getNæringsinntektKode());
    }
}
