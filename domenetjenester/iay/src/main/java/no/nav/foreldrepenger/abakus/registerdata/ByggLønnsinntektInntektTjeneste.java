package no.nav.foreldrepenger.abakus.registerdata;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.LønnsinntektBeskrivelse;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektspostBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;

/**
 * Lager Inntekt for lønnsinnntekter
 */
class ByggLønnsinntektInntektTjeneste {


    static void mapLønnsinntekter(InntektsInformasjon inntektsInformasjon,
                                                  InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                                  Map<String, Arbeidsgiver> arbeidsgivereLookup) {
        mapTilArbeidsgiver(inntektsInformasjon, arbeidsgivereLookup).entrySet()
            .stream()
            .map(e -> byggInntekt(e.getValue(), e.getKey(), aktørInntektBuilder, inntektsInformasjon.getKilde()))
            .forEach(aktørInntektBuilder::leggTilInntekt);
    }

    private static Map<Arbeidsgiver, Map<YearMonth, List<MånedsbeløpOgSkatteOgAvgiftsregel>>> mapTilArbeidsgiver(InntektsInformasjon inntektsInformasjon,
                                                                                                                 Map<String, Arbeidsgiver> arbeidsgivereLookup) {

        return inntektsInformasjon.getMånedsinntekterUtenomYtelser()
            .stream()
            .filter(mi -> arbeidsgivereLookup.get(mi.getArbeidsgiver()) != null)
            .map(mi -> new MånedsbeløpOgSkatteOgAvgiftsregel(arbeidsgivereLookup.get(mi.getArbeidsgiver()), mi.getMåned(), mi.getBeløp(),
                mi.getSkatteOgAvgiftsregelType(), mi.getLønnsbeskrivelseKode()))
            .collect(Collectors.groupingBy(MånedsbeløpOgSkatteOgAvgiftsregel::getArbeidsgiver,
                Collectors.groupingBy(MånedsbeløpOgSkatteOgAvgiftsregel::getMåned)));
    }

    private static InntektBuilder byggInntekt(Map<YearMonth, List<MånedsbeløpOgSkatteOgAvgiftsregel>> inntekter,
                                              Arbeidsgiver arbeidsgiver,
                                              InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder,
                                              InntektskildeType inntektOpptjening) {

        InntektBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(inntektOpptjening, new Opptjeningsnøkkel(arbeidsgiver));

        for (var måned : inntekter.keySet()) {
            var månedsinnteker = inntekter.get(måned);
            BigDecimal beløpSum = månedsinnteker.stream().map(MånedsbeløpOgSkatteOgAvgiftsregel::getBeløp).reduce(BigDecimal.ZERO, BigDecimal::add);

            Optional<String> valgtSkatteOgAvgiftsregel = finnSkatteOgAvgiftsregel(månedsinnteker);

            var lønnsinntektBeskrivelseKode = finnLønnsbeskrivelseType(månedsinnteker);
            lagInntektsposter(måned, beløpSum, valgtSkatteOgAvgiftsregel, lønnsinntektBeskrivelseKode, inntektBuilder);
        }

        return inntektBuilder.medArbeidsgiver(arbeidsgiver);
    }

    private static Optional<String> finnSkatteOgAvgiftsregel(List<MånedsbeløpOgSkatteOgAvgiftsregel> månedsinnteker) {
        Map<String, Integer> antalInntekterForAvgiftsregel = månedsinnteker.stream()
            .filter(e -> e.getSkatteOgAvgiftsregelType() != null)
            .collect(Collectors.groupingBy(MånedsbeløpOgSkatteOgAvgiftsregel::getSkatteOgAvgiftsregelType,
                Collectors.collectingAndThen(Collectors.mapping(MånedsbeløpOgSkatteOgAvgiftsregel::getBeløp, Collectors.toSet()), Set::size)));
        Optional<String> valgtSkatteOgAvgiftsregel = Optional.empty();
        if (antalInntekterForAvgiftsregel.keySet().size() > 1) {
            valgtSkatteOgAvgiftsregel = Optional.ofNullable(velgSkatteOgAvgiftsRegel(antalInntekterForAvgiftsregel.keySet()));
        } else if (antalInntekterForAvgiftsregel.keySet().size() == 1) {
            valgtSkatteOgAvgiftsregel = Optional.of(antalInntekterForAvgiftsregel.keySet().iterator().next());
        }
        return valgtSkatteOgAvgiftsregel;
    }

    private static Optional<String> finnLønnsbeskrivelseType(List<MånedsbeløpOgSkatteOgAvgiftsregel> månedsinnteker) {
        Set<String> lønnsinntektBeskrivelse = månedsinnteker.stream()
            .map(MånedsbeløpOgSkatteOgAvgiftsregel::getLønnsinntektBeskrivelseKode)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Optional<String> valgtLønnsbesrivelseType = Optional.empty();
        if (lønnsinntektBeskrivelse.size() > 1) {
            if (lønnsinntektBeskrivelse.contains(LønnsinntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE.getOffisiellKode())) {
                return Optional.of(LønnsinntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE.getOffisiellKode());
            } else {
                return Optional.of(lønnsinntektBeskrivelse.iterator().next());
            }
        } else if (lønnsinntektBeskrivelse.size() == 1) {
            return Optional.of(lønnsinntektBeskrivelse.iterator().next());
        }
        return valgtLønnsbesrivelseType;
    }


    private static void lagInntektsposter(YearMonth måned,
                                          BigDecimal sumInntektsbeløp,
                                          Optional<String> valgtSkatteOgAvgiftsregel,
                                          Optional<String> lønnsinntektBeskrivelseKode,
                                          InntektBuilder inntektBuilder) {
        InntektspostBuilder inntektspostBuilder = inntektBuilder.getInntektspostBuilder();
        inntektspostBuilder.medBeløp(sumInntektsbeløp).medPeriode(måned.atDay(1), måned.atEndOfMonth()).medInntektspostType(InntektspostType.LØNN);
        valgtSkatteOgAvgiftsregel.map(SkatteOgAvgiftsregelType::finnForKodeverkEiersKode).ifPresent(inntektspostBuilder::medSkatteOgAvgiftsregelType);
        lønnsinntektBeskrivelseKode.map(LønnsinntektBeskrivelse::finnForKodeverkEiersKode).ifPresent(inntektspostBuilder::medLønnsinntektBeskrivelse);
        inntektBuilder.leggTilInntektspost(inntektspostBuilder);
    }

    private static String velgSkatteOgAvgiftsRegel(Set<String> alternativ) {
        if (alternativ.contains(SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK.getOffisiellKode())) {
            return SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK.getOffisiellKode();
        } else if (alternativ.contains(SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK.getOffisiellKode())) {
            return SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK.getOffisiellKode();
        } else {
            return alternativ.stream().findFirst().orElse(null);
        }
    }

    public static final class MånedsbeløpOgSkatteOgAvgiftsregel {
        private Arbeidsgiver arbeidsgiver;
        private YearMonth måned;
        private BigDecimal beløp;
        private String skatteOgAvgiftsregelType;

        private String lønnsinntektBeskrivelseKode;

        public MånedsbeløpOgSkatteOgAvgiftsregel(Arbeidsgiver arbeidsgiver,
                                                 YearMonth måned,
                                                 BigDecimal beløp,
                                                 String skatteOgAvgiftsregelType,
                                                 String lønnsinntektBeskrivelseKode) {
            this.arbeidsgiver = arbeidsgiver;
            this.måned = måned;
            this.beløp = beløp;
            this.skatteOgAvgiftsregelType = skatteOgAvgiftsregelType;
            this.lønnsinntektBeskrivelseKode = lønnsinntektBeskrivelseKode;
        }

        public Arbeidsgiver getArbeidsgiver() {
            return arbeidsgiver;
        }

        public YearMonth getMåned() {
            return måned;
        }

        public BigDecimal getBeløp() {
            return beløp;
        }

        public String getSkatteOgAvgiftsregelType() {
            return skatteOgAvgiftsregelType;
        }

        public String getLønnsinntektBeskrivelseKode() {
            return lønnsinntektBeskrivelseKode;
        }
    }

}
