package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.inntekt.v1.InntekterDto;
import no.nav.abakus.iaygrunnlag.inntekt.v1.UtbetalingDto;
import no.nav.abakus.iaygrunnlag.inntekt.v1.UtbetalingsPostDto;
import no.nav.foreldrepenger.abakus.domene.iay.AktørInntekt;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.Inntekt;
import no.nav.foreldrepenger.abakus.domene.iay.Inntektspost;

public class MapAktørInntekt {

    private static final Comparator<UtbetalingDto> COMP_UTBETALING = Comparator.comparing(
            (UtbetalingDto dto) -> dto.getKilde() == null ? null : dto.getKilde().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getUtbetaler() == null ? null : dto.getUtbetaler().getIdent(), Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<UtbetalingsPostDto> COMP_UTBETALINGSPOST = Comparator.comparing(
            (UtbetalingsPostDto dto) -> dto.getInntektspostType().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing((UtbetalingsPostDto dto) -> dto.getInntektYtelseType() == null ? null : dto.getInntektYtelseType().getKode(),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

    static class MapTilDto {
        List<InntekterDto> map(Collection<AktørInntekt> aktørInntekt) {
            if (aktørInntekt == null || aktørInntekt.isEmpty()) {
                return Collections.emptyList();
            }
            return aktørInntekt.stream().map(this::mapTilInntekt).collect(Collectors.toList());
        }

        private InntekterDto mapTilInntekt(AktørInntekt ai) {
            InntekterDto dto = new InntekterDto(new AktørIdPersonident(ai.getAktørId().getId()));
            List<UtbetalingDto> utbetalinger = tilUtbetalinger(ai.getInntekt());
            dto.setUtbetalinger(utbetalinger);
            return dto;
        }

        private List<UtbetalingDto> tilUtbetalinger(Collection<Inntekt> inntekter) {
            return inntekter.stream().map(in -> tilUtbetaling(in)).sorted(COMP_UTBETALING).collect(Collectors.toList());
        }

        private UtbetalingDto tilUtbetaling(Inntekt inntekt) {
            Arbeidsgiver arbeidsgiver = inntekt.getArbeidsgiver();
            UtbetalingDto dto = new UtbetalingDto(inntekt.getInntektsKilde());
            dto.medArbeidsgiver(mapArbeidsgiver(arbeidsgiver));
            dto.setPoster(tilPoster(inntekt.getAlleInntektsposter()));
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
            return inntektspost.stream().map(this::tilPost).sorted(COMP_UTBETALINGSPOST).collect(Collectors.toList());
        }

        private UtbetalingsPostDto tilPost(Inntektspost inntektspost) {
            var periode = new Periode(inntektspost.getPeriode().getFomDato(), inntektspost.getPeriode().getTomDato());
            var inntektspostType = inntektspost.getInntektspostType();
            var skattOgAvgiftType = inntektspost.getSkatteOgAvgiftsregelType();

            UtbetalingsPostDto dto = new UtbetalingsPostDto(periode, inntektspostType)
                .medInntektYtelseType(inntektspost.getInntektYtelseType())
                .medSkattAvgiftType(skattOgAvgiftType)
                .medLønnsinntektbeskrivelse(inntektspost.getLønnsinntektBeskrivelse())
                .medBeløp(inntektspost.getBeløp().getVerdi());

            return dto;
        }

    }
}
