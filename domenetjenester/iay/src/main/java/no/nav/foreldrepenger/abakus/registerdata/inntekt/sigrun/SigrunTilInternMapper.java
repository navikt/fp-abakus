package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.vedtak.felles.integrasjon.sigrun.BeregnetSkatt;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SSGGrunnlag;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SSGResponse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

class SigrunTilInternMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SigrunTilInternMapper.class);

    static Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> mapFraSigrunTilIntern(Map<Year, List<BeregnetSkatt>> beregnetSkatt, Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap) {
        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> årTilInntektMap = new HashMap<>();

        mapBeregnetSkatt(beregnetSkatt, årTilInntektMap);
        mapSummertskattegrunnlag(summertskattegrunnlagMap, årTilInntektMap);
        return årTilInntektMap;
    }

    private static void mapSummertskattegrunnlag(Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap, Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> årTilInntektMap) {
        if (!summertskattegrunnlagMap.isEmpty()) {
            Set<Map.Entry<Year, Optional<SSGResponse>>> entrySet = summertskattegrunnlagMap.entrySet();
            for (Map.Entry<Year, Optional<SSGResponse>> entry : entrySet) {
                boolean harSummertskattegrunnlagForÅr = entry.getValue().isPresent();
                if (harSummertskattegrunnlagForÅr) {
                    SSGResponse ssgResponse = entry.getValue().get();
                    Optional<SSGGrunnlag> ssggrunnlag = ssgResponse.getSvalbardGrunnlag()
                        .stream()
                        .filter(f -> TekniskNavn.fraKode(f.getTekniskNavn()) != null)
                        .findFirst();
                    ssggrunnlag.ifPresent(grunnlag -> {
                        DatoIntervallEntitet datoIntervallEntitet = lagDatoIntervall(entry.getKey());
                        InntektspostType inntektspostType = TekniskNavn.fraKode(grunnlag.getTekniskNavn()).getInntektspostType();
                        Map<InntektspostType, BigDecimal> inntektspost = årTilInntektMap.get(datoIntervallEntitet);
                        if (inntektspost == null) {
                            Map<InntektspostType, BigDecimal> typeTilVerdiMap = new HashMap<>();
                            typeTilVerdiMap.put(inntektspostType, new BigDecimal(grunnlag.getBeloep()));
                            årTilInntektMap.put(datoIntervallEntitet, typeTilVerdiMap);
                        } else {
                            BigDecimal beløp = new BigDecimal(grunnlag.getBeloep());
                            if (inntektspost.get(inntektspostType) == null) {
                                inntektspost.put(inntektspostType, new BigDecimal(grunnlag.getBeloep()));
                            } else {
                                inntektspost.replace(inntektspostType, inntektspost.get(InntektspostType.LØNN).add(beløp));
                            }
                        }
                        LOGGER.info("Lagt til {} fra summertskattegrunnlag for svalbard år {}", grunnlag.getBeloep(), entry.getKey());
                    });
                }
            }
        }
    }

    private static void mapBeregnetSkatt(Map<Year, List<BeregnetSkatt>> beregnetSkatt, Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> årTilInntektMap) {
        for (Map.Entry<Year, List<BeregnetSkatt>> entry : beregnetSkatt.entrySet()) {
            DatoIntervallEntitet intervallEntitet = lagDatoIntervall(entry.getKey());
            Map<InntektspostType, BigDecimal> typeTilVerdiMap = new HashMap<>();
            for (BeregnetSkatt beregnetSkatteobjekt : entry.getValue()) {
                InntektspostType type = TekniskNavn.fraKode(beregnetSkatteobjekt.getTekniskNavn()).getInntektspostType();
                if (type != null) {
                    BigDecimal beløp = typeTilVerdiMap.get(type);
                    if (beløp == null) {
                        typeTilVerdiMap.put(type, new BigDecimal(beregnetSkatteobjekt.getVerdi()));
                    } else {
                        typeTilVerdiMap.replace(type, beløp.add(new BigDecimal(beregnetSkatteobjekt.getVerdi())));
                    }
                } else {
                    LOGGER.info("Inntektposttype er null for skatteobjekt med kode {} og inntekt {} ", beregnetSkatteobjekt.getTekniskNavn(), beregnetSkatteobjekt.getVerdi());
                }
            }
            årTilInntektMap.put(intervallEntitet, typeTilVerdiMap);
        }
    }

    private static DatoIntervallEntitet lagDatoIntervall(Year år) {
        LocalDateTime førsteDagIÅret = LocalDateTime.now().withYear(år.getValue()).withDayOfYear(1);
        LocalDateTime sisteDagIÅret = LocalDateTime.now().withYear(år.getValue()).withDayOfYear(år.length());
        return DatoIntervallEntitet.fraOgMedTilOgMed(førsteDagIÅret.toLocalDate(), sisteDagIÅret.toLocalDate());
    }
}
