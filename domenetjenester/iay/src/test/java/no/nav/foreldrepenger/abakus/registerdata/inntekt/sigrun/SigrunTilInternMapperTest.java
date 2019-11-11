package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.vedtak.felles.integrasjon.sigrun.BeregnetSkatt;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SSGGrunnlag;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SSGResponse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class SigrunTilInternMapperTest {
    @Test
    public void skal_mappe_og_beregne_slå_sammen_lønn_fra_beregent_med_lønn_fra_summertskattegrunnlag() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        beregnet.put(iÅr, List.of(new BeregnetSkatt(TekniskNavn.PERSONINNTEKT_LØNN.getKode(), "5000")));
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(), List.of(new SSGGrunnlag(TekniskNavn.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN.getKode(), "5000")), null)));

        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(DatoIntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(new BigDecimal(10000));
    }

    @Test
    public void skal_mappe_og_beregne_når_det_bare_finnes_innslag_fra_summertskattegrunnlag() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(), List.of(new SSGGrunnlag(TekniskNavn.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN.getKode(), "5000")), null)));

        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(DatoIntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(new BigDecimal(5000));
    }

    @Test
    public void skal_mappe_og_beregne_slå_sammen_lønn_fra_beregent_med_lønn_fra_summertskattegrunnlag_og_håndtere_skatteoppgjørsdato() {
        LocalDate iDag = LocalDate.now();
        Map<Year, List<BeregnetSkatt>> beregnet = new HashMap<>();
        Map<Year, Optional<SSGResponse>> summertskattegrunnlag = new HashMap<>();

        Year iÅr = Year.of(iDag.getYear());
        beregnet.put(iÅr, List.of(new BeregnetSkatt(TekniskNavn.PERSONINNTEKT_LØNN.getKode(), "5000"), new BeregnetSkatt(TekniskNavn.SKATTEOPPGJØRSDATO.getKode(), "2018-10-04")));
        summertskattegrunnlag.put(iÅr, Optional.of(new SSGResponse(List.of(), List.of(new SSGGrunnlag(TekniskNavn.LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN.getKode(), "5000")), null)));

        Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> map = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnet, summertskattegrunnlag);
        LocalDate førsteDagIÅr = LocalDate.of(iÅr.getValue(), 1, 1);
        LocalDate sisteDagIÅr = LocalDate.of(iÅr.getValue(), 12, 31);

        assertThat(map.get(DatoIntervallEntitet.fraOgMedTilOgMed(førsteDagIÅr, sisteDagIÅr)).get(InntektspostType.LØNN)).isEqualByComparingTo(new BigDecimal(10000));
    }
}
