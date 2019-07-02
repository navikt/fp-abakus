package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Aktør;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.PersonIdent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntekt.v1.InntekterDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntekt.v1.UtbetalingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntekt.v1.UtbetalingsPostDto;

public class MapAktørInntekt {
    static class MapFraDto {

        @SuppressWarnings("unused")
        private final AktørId søkerAktørId;
        private final InntektArbeidYtelseAggregatBuilder aggregatBuilder;

        MapFraDto(AktørId søkerAktørId, InntektArbeidYtelseAggregatBuilder aggregatBuilder) {
            this.søkerAktørId = søkerAktørId;
            this.aggregatBuilder = aggregatBuilder;
        }

        List<AktørInntektBuilder> map(Collection<InntekterDto> dtos) {
            if (dtos == null || dtos.isEmpty()) {
                return Collections.emptyList();
            }
            var builders = dtos.stream().map(idto -> {
                var builder = aggregatBuilder.getAktørInntektBuilder(tilAktørId(idto.getPerson()));
                idto.getUtbetalinger().forEach(utbetalingDto -> builder.leggTilInntekt(mapUtbetaling(utbetalingDto)));
                return builder;
            }).collect(Collectors.toUnmodifiableList());

            return builders;
        }

        /** Returnerer person sin aktørId. Denne trenger ikke være samme som søkers aktørid men kan f.eks. være annen part i en sak. */
        private AktørId tilAktørId(PersonIdent person) {
            if (!(person instanceof AktørIdPersonident)) {
                throw new IllegalArgumentException("Støtter kun " + AktørIdPersonident.class.getSimpleName() + " her");
            }
            return new AktørId(person.getIdent());
        }

        private InntektBuilder mapUtbetaling(UtbetalingDto dto) {
            InntektBuilder inntektBuilder = InntektBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(mapArbeidsgiver(dto.getUtbetaler()))
                .medInntektsKilde(KodeverkMapper.mapInntektsKildeFraDto(dto.getKilde()));
            dto.getPoster()
                .forEach(post -> inntektBuilder.leggTilInntektspost(mapInntektspost(post)));
            return inntektBuilder;
        }

        private InntektspostBuilder mapInntektspost(UtbetalingsPostDto post) {
            return InntektspostBuilder.ny()
                .medBeløp(post.getBeløp())
                .medInntektspostType(KodeverkMapper.mapInntektspostTypeFraDto(post.getInntektspostType()))
                .medPeriode(post.getPeriode().getFom(), post.getPeriode().getTom())
                .medSkatteOgAvgiftsregelType(KodeverkMapper.mapSkatteOgAvgiftsregelFraDto(post.getSkattAvgiftType()))
                .medYtelse(KodeverkMapper.mapUtbetaltYtelseTypeTilGrunnlag(post.getYtelseType()));
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if (arbeidsgiver == null)
                return null;
            if (arbeidsgiver.getErOrganisasjon()) {
                return Arbeidsgiver.virksomhet(new OrgNummer(arbeidsgiver.getIdent()));
            }
            return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
        }

    }

    static class MapTilDto {
        List<InntekterDto> map(Collection<AktørInntekt> aktørInntekt) {
            if (aktørInntekt == null || aktørInntekt.isEmpty()) {
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
            Comparator<UtbetalingDto> compUtb = Comparator.comparing((UtbetalingDto dto) -> dto.getKilde() == null ? null : dto.getKilde().getKode())
                .thenComparing(dto -> dto.getUtbetaler().getIdent());

            return inntekter.stream().map(in -> tilUtbetaling(in, kilde)).sorted(compUtb).collect(Collectors.toList());
        }

        private UtbetalingDto tilUtbetaling(Inntekt inntekt, InntektsKilde kilde) {
            Arbeidsgiver arbeidsgiver = inntekt.getArbeidsgiver();
            UtbetalingDto dto = new UtbetalingDto(KodeverkMapper.mapInntektsKildeTilDto(kilde));
            dto.medArbeidsgiver(mapArbeidsgiver(arbeidsgiver));
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
            Comparator<UtbetalingsPostDto> compUtb = Comparator.comparing((UtbetalingsPostDto dto) -> dto.getInntektspostType().getKode())
                .thenComparing(dto -> dto.getPeriode().getFom())
                .thenComparing(dto -> dto.getPeriode().getTom());

            return inntektspost.stream().map(this::tilPost).sorted(compUtb).collect(Collectors.toList());
        }

        private UtbetalingsPostDto tilPost(Inntektspost inntektspost) {
            var periode = new Periode(inntektspost.getFraOgMed(), inntektspost.getTilOgMed());
            var inntektspostType = KodeverkMapper.mapInntektspostTypeTilDto(inntektspost.getInntektspostType());
            var ytelseType = KodeverkMapper.mapYtelseTypeTilDto(inntektspost.getYtelseType());
            var skattOgAvgiftType = KodeverkMapper.mapSkatteOgAvgiftsregelTilDto(inntektspost.getSkatteOgAvgiftsregelType());

            UtbetalingsPostDto dto = new UtbetalingsPostDto(periode, inntektspostType)
                .medUtbetaltYtelseType(ytelseType)
                .medSkattAvgiftType(skattOgAvgiftType)
                .medBeløp(inntektspost.getBeløp().getVerdi());

            return dto;
        }

    }
}
