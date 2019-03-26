package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.sigrun.BeregnetSkatt;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunConsumer;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunResponse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@ApplicationScoped
public class SigrunTjeneste {

    private static final Map<String, InntektspostType> TEKNISK_NAVN_TIL_KODE_MAP = new HashMap<>();
    private SigrunConsumer sigrunConsumer;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunConsumer sigrunConsumer) {
        this.sigrunConsumer = sigrunConsumer;
        fyllMap(TEKNISK_NAVN_TIL_KODE_MAP);
    }

    public Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> beregnetSkatt(AktørId aktørId) {
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()));

        return mapFraSigrunTilIntern(beregnetskatt.getBeregnetSkatt());
    }

    private Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> mapFraSigrunTilIntern(Map<Year, List<BeregnetSkatt>> beregnetSkatt) {
        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> årTilInntektMap = new HashMap<>();

        for (Map.Entry<Year, List<BeregnetSkatt>> entry : beregnetSkatt.entrySet()) {
            DatoIntervallEntitet intervallEntitet = lagDatoIntervall(entry);
            Map<InntektspostType, BigDecimal> typeTilVerdiMap = new HashMap<>();
            for (BeregnetSkatt beregnetSkatteobjekt : entry.getValue()) {
                InntektspostType type = TEKNISK_NAVN_TIL_KODE_MAP.get(beregnetSkatteobjekt.getTekniskNavn());
                if (type != null) {
                    BigDecimal beløp = typeTilVerdiMap.get(type);
                    if (beløp == null) {
                        typeTilVerdiMap.put(type, new BigDecimal(beregnetSkatteobjekt.getVerdi()));
                    } else {
                        typeTilVerdiMap.replace(type, beløp.add(new BigDecimal(beregnetSkatteobjekt.getVerdi())));
                    }
                }
            }
            årTilInntektMap.put(intervallEntitet, typeTilVerdiMap);
        }
        return årTilInntektMap;
    }

    private DatoIntervallEntitet lagDatoIntervall(Map.Entry<Year, List<BeregnetSkatt>> entry) {
        Year år = entry.getKey();
        LocalDateTime førsteDagIÅret = LocalDateTime.now().withYear(år.getValue()).withDayOfYear(1);
        LocalDateTime sisteDagIÅret = LocalDateTime.now().withYear(år.getValue()).withDayOfYear(år.length());
        return DatoIntervallEntitet.fraOgMedTilOgMed(førsteDagIÅret.toLocalDate(), sisteDagIÅret.toLocalDate());
    }

    //TODO(OJR) vurder å flytte til kodeliste
    private void fyllMap(Map<String, InntektspostType> map) {
        map.put("personinntektLoenn", InntektspostType.LØNN);
        map.put("personinntektBarePensjonsdel", InntektspostType.LØNN);
        map.put("personinntektNaering", InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put("personinntektFiskeFangstFamiliebarnehage", InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE);
        map.put("svalbardLoennLoennstrekkordningen", InntektspostType.LØNN);
        map.put("svalbardPersoninntektNaering", InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put("skatteoppgjoersdato", null);
    }
}
