package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import no.nav.abakus.iaygrunnlag.*;
import no.nav.abakus.iaygrunnlag.inntekt.v1.InntekterDto;
import no.nav.abakus.iaygrunnlag.inntekt.v1.UtbetalingDto;
import no.nav.abakus.iaygrunnlag.inntekt.v1.UtbetalingsPostDto;
import no.nav.foreldrepenger.abakus.domene.iay.*;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

import java.util.*;
import java.util.stream.Collectors;

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

        /**
         * Returnerer person sin aktørId. Denne trenger ikke være samme som søkers aktørid men kan f.eks. være annen part i en sak.
         */
        private AktørId tilAktørId(PersonIdent person) {
            if (!(person instanceof AktørIdPersonident)) {
                throw new IllegalArgumentException("Støtter kun " + AktørIdPersonident.class.getSimpleName() + " her");
            }
            return new AktørId(person.getIdent());
        }

        private InntektBuilder mapUtbetaling(UtbetalingDto dto) {
            InntektBuilder inntektBuilder = InntektBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(mapArbeidsgiver(dto.getUtbetaler()))
                .medInntektsKilde(dto.getKilde());
            dto.getPoster().forEach(post -> inntektBuilder.leggTilInntektspost(mapInntektspost(post)));
            return inntektBuilder;
        }

        private InntektspostBuilder mapInntektspost(UtbetalingsPostDto post) {
            return InntektspostBuilder.ny()
                .medBeløp(post.getBeløp())
                .medInntektspostType(post.getInntektspostType())
                .medPeriode(post.getPeriode().getFom(), post.getPeriode().getTom())
                .medSkatteOgAvgiftsregelType(post.getSkattAvgiftType())
                .medLønnsinntektBeskrivelse(post.getLønnsinntektBeskrivelse())
                .medYtelse(post.getInntektYtelseType());
        }

        private Arbeidsgiver mapArbeidsgiver(Aktør arbeidsgiver) {
            if (arbeidsgiver == null) {
                return null;
            }
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
