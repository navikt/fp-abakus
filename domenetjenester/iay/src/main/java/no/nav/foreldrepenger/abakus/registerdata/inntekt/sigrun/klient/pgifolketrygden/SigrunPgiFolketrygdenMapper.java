package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

public final class SigrunPgiFolketrygdenMapper {

    public static Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> mapFraSigrunTilIntern(SigrunPgiFolketrygdenResponse response) {
        Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> resultat = new LinkedHashMap<>();
        response.pgiFolketrygdenMap().values().stream()
            .map(SigrunPgiFolketrygdenMapper::mapPgiFolketrygden)
            .forEach(resultat::putAll);
        return resultat;
    }

    public static Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> mapPgiFolketrygden(List<PgiFolketrygdenResponse> response) {
        if (response == null || response.isEmpty()) {
            return Map.of();
        }
        var inntektBeløp = new ArrayList<InntektBeløp>();
        for (var responselement : response) {
            for (var pgi : responselement.pensjonsgivendeInntekt()) {
                leggTilHvisVerdi(pgi.pensjonsgivendeInntektAvLoennsinntekt(), InntektspostType.LØNN, inntektBeløp);
                leggTilHvisVerdi(pgi.pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel(), InntektspostType.LØNN, inntektBeløp);
                leggTilHvisVerdi(pgi.pensjonsgivendeInntektAvNaeringsinntekt(), InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE, inntektBeløp);
                leggTilHvisVerdi(pgi.pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage(), InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE, inntektBeløp);
            }
        }
        var inntektBeløpMap = inntektBeløp.stream()
            .collect(Collectors.groupingBy(InntektBeløp::inntektspostType, Collectors.reducing(BigDecimal.ZERO, InntektBeløp::beløp, BigDecimal::add)));
        var år = Year.of(response.get(0).inntektsaar());
        var førsteDagIÅret = LocalDate.now().with(år).withDayOfYear(1);
        var sisteDagIÅret = LocalDate.now().with(år).withDayOfYear(år.length());
        var intervall = IntervallEntitet.fraOgMedTilOgMed(førsteDagIÅret, sisteDagIÅret);
        return new LinkedHashMap<>(Map.of(intervall, inntektBeløpMap));
    }

    private static void leggTilHvisVerdi(Long verdi, InntektspostType type, List<InntektBeløp> liste) {
        if (verdi != null) {
            liste.add(new InntektBeløp(type, BigDecimal.valueOf(verdi)));
        }
    }

    private record InntektBeløp(InntektspostType inntektspostType, BigDecimal beløp) { }

}
