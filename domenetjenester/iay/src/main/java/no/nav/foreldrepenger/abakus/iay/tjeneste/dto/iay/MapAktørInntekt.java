package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.AktørInntekt;
import no.nav.foreldrepenger.abakus.domene.iay.AktørInntektEntitet.InntektBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.Inntekt;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektEntitet.InntektspostBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Inntektspost;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
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
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseType;

public class MapAktørInntekt {
    static class MapFraDto {

        private final AktørId aktørId;
        private final InntektArbeidYtelseAggregatBuilder aggregatBuilder;

        MapFraDto(AktørId aktørId, InntektArbeidYtelseAggregatBuilder aggregatBuilder) {
            this.aktørId = aktørId;
            this.aggregatBuilder = aggregatBuilder;
        }

        List<AktørInntektBuilder> map(Collection<InntekterDto> dtos) {
            if(dtos==null || dtos.isEmpty()) {
                return Collections.emptyList();
            }
            var builders = dtos.stream().map(idto -> {
                var builder = aggregatBuilder.getAktørInntektBuilder(aktørId);
                idto.getUtbetalinger().forEach(utbetalingDto -> builder.leggTilInntekt(mapUtbetaling(utbetalingDto)));
                return builder;
            }).collect(Collectors.toUnmodifiableList());

            return builders;
        }

        private InntektBuilder mapUtbetaling(UtbetalingDto dto) {
            InntektBuilder inntektBuilder = InntektBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(mapArbeidsgiver(dto.getUtbetaler()))
                .medInntektsKilde(new InntektsKilde(dto.getKilde().getKode()));
            dto.getPoster()
                .forEach(post -> inntektBuilder.leggTilInntektspost(mapInntektspost(post)));
            return inntektBuilder;
        }

        private InntektspostBuilder mapInntektspost(UtbetalingsPostDto post) {
            return InntektspostBuilder.ny()
                    .medBeløp(post.getBeløp())
                    .medInntektspostType(post.getInntektspostType().getKode())
                    .medPeriode(post.getPeriode().getFom(), post.getPeriode().getTom())
                    .medSkatteOgAvgiftsregelType(post.getSkattAvgiftType().getKode())
                    .medYtelse(mapYtelseType(post.getYtelseType()));
        }

        private YtelseType mapYtelseType(UtbetaltYtelseType type) {
            return KodeverkMapper.mapUtbetaltYtelseTypeTilGrunnlag(type);
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if(arbeidsgiver== null) return null;
            if (arbeidsgiver.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(arbeidsgiver.getIdent()));
            }
            return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
        }

    }

    static class MapTilDto {
        List<InntekterDto> map(Collection<AktørInntekt> aktørInntekt) {
            if(aktørInntekt==null || aktørInntekt.isEmpty()) {
                return Collections.emptyList();
            }
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
            var ytelseType = mapYtelseType(inntektspost.getYtelseType());
            var skattOgAvgiftType = new SkatteOgAvgiftsregelType(inntektspost.getSkatteOgAvgiftsregelType().getKode());

            UtbetalingsPostDto dto = new UtbetalingsPostDto(ytelseType, periode, inntektspostType)
                .medSkattAvgiftType(skattOgAvgiftType)
                .medBeløp(inntektspost.getBeløp().getVerdi());

            return dto;
        }

        private UtbetaltYtelseType mapYtelseType(YtelseType ytelseType) {
            return KodeverkMapper.mapYtelseTypeTilDto(ytelseType);
        }
    }
}
