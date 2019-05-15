package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import no.nav.foreldrepenger.abakus.domene.iay.AktørInntekt;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.Inntekt;
import no.nav.foreldrepenger.abakus.domene.iay.Inntektspost;
import no.nav.foreldrepenger.abakus.domene.iay.NæringsinntektType;
import no.nav.foreldrepenger.abakus.domene.iay.OffentligYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.PensjonTrygdType;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntekt.v1.InntekterDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntekt.v1.UtbetalingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntekt.v1.UtbetalingsPostDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseType;

public class MapAktørInntekt {
    public List<InntekterDto> mapTilDto(Collection<AktørInntekt> aktørInntekt) {
        return new MapTilDto().map(aktørInntekt);
    }

    public List<AktørInntekt> mapFraDto(Collection<InntekterDto> aktørInntekt) {
        return new MapFraDto().map(aktørInntekt);
    }

    private class MapFraDto {

        List<AktørInntekt> map(Collection<InntekterDto> aktørInntekt) {
            // FIXME Map AktørInntekt på vei inn
            throw new UnsupportedOperationException("Not Yet Implemented");
        }

    }

    private class MapTilDto {
        List<InntekterDto> map(Collection<AktørInntekt> aktørInntekt) {
            return aktørInntekt.stream().map(this::mapTilInntekt).collect(Collectors.toList());
        }

        private InntekterDto mapTilInntekt(AktørInntekt ai) {
            InntekterDto dto = new InntekterDto(new AktørIdPersonident(ai.getAktørId().getId()));
            List<UtbetalingDto> pensjonsgivende = tilUtbetalinger(ai.getInntektPensjonsgivende(), InntektsKilde.INNTEKT_OPPTJENING);
            List<UtbetalingDto> sammenligning = tilUtbetalinger(ai.getInntektSammenligningsgrunnlag(), InntektsKilde.INNTEKT_SAMMENLIGNING);
            List<UtbetalingDto> beregning = tilUtbetalinger(ai.getInntektBeregningsgrunnlag(), InntektsKilde.INNTEKT_BEREGNING);
            List<UtbetalingDto> sigrun = tilUtbetalinger(ai.getBeregnetSkatt(), InntektsKilde.SIGRUN);
            ArrayList<UtbetalingDto> utbetalinger = new ArrayList<>(pensjonsgivende);
            utbetalinger.addAll(sammenligning);
            utbetalinger.addAll(beregning);
            utbetalinger.addAll(sigrun);
            dto.setUtbetalinger(utbetalinger);
            return dto;
        }

        private List<UtbetalingDto> tilUtbetalinger(List<Inntekt> inntekter, InntektsKilde kilde) {
            return inntekter.stream().map(in -> tilUtbetaling(in, kilde)).collect(Collectors.toList());
        }

        private UtbetalingDto tilUtbetaling(Inntekt inntekt, InntektsKilde kilde) {
            Arbeidsgiver arbeidsgiver = inntekt.getArbeidsgiver();
            UtbetalingDto dto = new UtbetalingDto(mapArbeidsgiver(arbeidsgiver));
            dto.medKilde(new InntektskildeType(kilde.getKode()));
            dto.setPoster(tilPoster(inntekt.getInntektspost()));
            return dto;
        }

        private Aktør mapArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            if (arbeidsgiver == null) {
                return null;
            }
            if (arbeidsgiver.getErVirksomhet()) {
                return new Organisasjon(arbeidsgiver.getIdentifikator());
            } else {
                return new AktørIdPersonident(arbeidsgiver.getAktørId().getId());
            }
        }

        private List<UtbetalingsPostDto> tilPoster(Collection<Inntektspost> inntektspost) {
            return inntektspost.stream().map(this::tilPost).collect(Collectors.toList());
        }

        private UtbetalingsPostDto tilPost(Inntektspost inntektspost) {
            var periode = new Periode(inntektspost.getFraOgMed(), inntektspost.getTilOgMed());
            var inntektspostType = new InntektspostType(inntektspost.getInntektspostType().getKode());
            var ytelseType = mapUtbetaltYtelseType(inntektspost.getYtelseType());
            var skattOgAvgiftType = new SkatteOgAvgiftsregelType(inntektspost.getSkatteOgAvgiftsregelType().getKode());

            UtbetalingsPostDto dto = new UtbetalingsPostDto(ytelseType, periode, inntektspostType)
                .medSkattAvgiftType(skattOgAvgiftType)
                .medBeløp(inntektspost.getBeløp().getVerdi());

            return dto;
        }

        private UtbetaltYtelseType mapUtbetaltYtelseType(YtelseType ytelseType) {
            if (ytelseType == null) {
                return new UtbetaltYtelseFraOffentligeType("-");
            }
            if (ytelseType.getKodeverk().equals(OffentligYtelseType.DISCRIMINATOR)) {
                return new UtbetaltYtelseFraOffentligeType(ytelseType.getKode());
            }
            if (ytelseType.getKodeverk().equals(NæringsinntektType.DISCRIMINATOR)) {
                return new UtbetaltYtelseFraOffentligeType(ytelseType.getKode());
            }
            if (ytelseType.getKodeverk().equals(PensjonTrygdType.DISCRIMINATOR)) {
                return new UtbetaltYtelseFraOffentligeType(ytelseType.getKode());
            }
            throw new IllegalArgumentException("Ukjent utbetalt ytelse. " + ytelseType.toString());
        }
    }
}
