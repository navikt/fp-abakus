package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.AktørYtelse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Ytelse;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvist;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelse;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.abakus.typer.*;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.PersonIdent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.AnvisningDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.FordelingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelseDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelseGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelserDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MapAktørYtelse {
    private static final Comparator<YtelseDto> COMP_YTELSE = Comparator
        .comparing((YtelseDto dto) -> dto.getSaksnummer(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getYtelseType() == null ? null : dto.getYtelseType().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getTemaUnderkategori() == null ? null : dto.getTemaUnderkategori().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));
    
    private static final Comparator<FordelingDto> COMP_FORDELING = Comparator
            .comparing((FordelingDto dto) -> dto.getArbeidsgiver() == null ? null : dto.getArbeidsgiver().getIdent(), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(dto -> dto.getHyppighet() == null ? null : dto.getHyppighet().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
            ;

    static class MapFraDto {
        private InntektArbeidYtelseAggregatBuilder aggregatBuilder;

        @SuppressWarnings("unused")
        private AktørId søkerAktørId;

        MapFraDto(AktørId søkerAktørId, InntektArbeidYtelseAggregatBuilder aggregatBuilder) {
            this.søkerAktørId = søkerAktørId;
            this.aggregatBuilder = aggregatBuilder;
        }

        public List<AktørYtelseBuilder> map(Collection<YtelserDto> dtos) {
            if (dtos == null || dtos.isEmpty()) {
                return Collections.emptyList();
            }
            return dtos.stream().map(this::mapAktørYtelse).collect(Collectors.toUnmodifiableList());
        }

        private AktørYtelseBuilder mapAktørYtelse(YtelserDto dto) {
            var builder = aggregatBuilder.getAktørYtelseBuilder(tilAktørId(dto.getPerson()));git sta
            dto.getYtelser().forEach(ytelseDto -> builder.leggTilYtelse(mapYtelse(ytelseDto)));
            return builder;
        }

        /** Returnerer person sin aktørId. Denne trenger ikke være samme som søkers aktørid men kan f.eks. være annen part i en sak. */
        private AktørId tilAktørId(PersonIdent person) {
            if (!(person instanceof AktørIdPersonident)) {
                throw new IllegalArgumentException("Støtter kun " + AktørIdPersonident.class.getSimpleName() + " her");
            }
            return new AktørId(person.getIdent());
        }

        private DatoIntervallEntitet mapPeriode(Periode periode) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
        }

        private YtelseBuilder mapYtelse(YtelseDto ytelseDto) {
            var ytelseBuilder = YtelseBuilder.oppdatere(Optional.empty());
            var behandlingsTema = KodeverkMapper.getTemaUnderkategori(ytelseDto.getTemaUnderkategori());
            ytelseBuilder
                .medYtelseGrunnlag(mapYtelseGrunnlag(ytelseDto.getGrunnlag(), ytelseBuilder.getGrunnlagBuilder()))
                .medYtelseType(KodeverkMapper.mapYtelseTypeFraDto(ytelseDto.getYtelseType()))
                .medBehandlingsTema(behandlingsTema)
                .medKilde(KodeverkMapper.mapFagsystemFraDto(ytelseDto.getFagsystemDto()))
                .medPeriode(mapPeriode(ytelseDto.getPeriode()))
                .medSaksnummer(ytelseDto.getSaksnummer() == null ? null : new Saksnummer(ytelseDto.getSaksnummer()))
                .medStatus(KodeverkMapper.mapYtelseStatusFraDto(ytelseDto.getStatus()));
            ytelseDto.getAnvisninger()
                .forEach(anvisning -> ytelseBuilder.medYtelseAnvist(mapYtelseAnvist(anvisning, ytelseBuilder.getAnvistBuilder())));
            return ytelseBuilder;
        }

        private YtelseAnvist mapYtelseAnvist(AnvisningDto anvisning, YtelseAnvistBuilder anvistBuilder) {
            if (anvisning == null)
                return null;
            return anvistBuilder
                .medAnvistPeriode(mapPeriode(anvisning.getPeriode()))
                .medBeløp(anvisning.getBeløp())
                .medDagsats(anvisning.getDagsats())
                .medUtbetalingsgradProsent(anvisning.getUtbetalingsgrad())
                .build();
        }

        private YtelseGrunnlag mapYtelseGrunnlag(YtelseGrunnlagDto grunnlag, YtelseGrunnlagBuilder grunnlagBuilder) {
            if (grunnlag == null)
                return null;
            grunnlagBuilder
                .medArbeidskategori(KodeverkMapper.mapArbeidskategoriFraDto(grunnlag.getArbeidskategoriDto()))
                .medDekningsgradProsent(grunnlag.getDekningsgradProsent())
                .medGraderingProsent(grunnlag.getGraderingProsent())
                .medInntektsgrunnlagProsent(grunnlag.getInntektsgrunnlagProsent())
                .medOpprinneligIdentdato(grunnlag.getOpprinneligIdentDato())
                .medVedtaksDagsats(grunnlag.getVedtaksDagsats());
            grunnlag.getFordeling()
                .forEach(fordeling -> grunnlagBuilder.medYtelseStørrelse(mapYtelseStørrelse(fordeling)));
            return grunnlagBuilder.build();
        }

        private YtelseStørrelse mapYtelseStørrelse(FordelingDto fordeling) {
            if (fordeling == null) {
                return null;
            }
            var arbeidsgiver = fordeling.getArbeidsgiver();
            return YtelseStørrelseBuilder.ny()
                .medBeløp(fordeling.getBeløp())
                .medHyppighet(KodeverkMapper.mapInntektPeriodeTypeFraDto(fordeling.getHyppighet()))
                .medVirksomhet(arbeidsgiver == null ? null : new OrgNummer(arbeidsgiver.getIdent()))
                .build();
        }

    }

    static class MapTilDto {

        private List<FordelingDto> mapFordeling(List<YtelseStørrelse> ytelseStørrelse) {
            if (ytelseStørrelse == null || ytelseStørrelse.isEmpty()) {
                return Collections.emptyList();
            }
            return ytelseStørrelse.stream().map(this::tilFordeling).sorted(COMP_FORDELING).collect(Collectors.toUnmodifiableList());
        }

        private YtelserDto mapTilYtelser(AktørYtelse ay) {
            AktørIdPersonident person = new AktørIdPersonident(ay.getAktørId().getId());
            return new YtelserDto(person)
                .medYtelser(mapTilYtelser(ay.getAlleYtelser()));
        }

        private List<YtelseDto> mapTilYtelser(Collection<Ytelse> ytelser) {
            return ytelser.stream().map(this::tilYtelse).sorted(COMP_YTELSE).collect(Collectors.toList());
        }

        private YtelseGrunnlagDto mapYtelseGrunnlag(YtelseGrunnlag gr) {

            YtelseGrunnlagDto dto = new YtelseGrunnlagDto();
            gr.getArbeidskategori().ifPresent(ak -> dto.setArbeidskategoriDto(KodeverkMapper.mapArbeidskategoriTilDto(ak)));
            gr.getOpprinneligIdentdato().ifPresent(dto::setOpprinneligIdentDato);
            gr.getDekningsgradProsent().map(Stillingsprosent::getVerdi).ifPresent(dto::setDekningsgradProsent);
            gr.getGraderingProsent().map(Stillingsprosent::getVerdi).ifPresent(dto::setGraderingProsent);
            gr.getInntektsgrunnlagProsent().map(Stillingsprosent::getVerdi).ifPresent(dto::setInntektsgrunnlagProsent);
            gr.getVedtaksDagsats().map(Beløp::getVerdi).ifPresent(dto::setVedtaksDagsats);
            dto.setFordeling(mapFordeling(gr.getYtelseStørrelse()));
            return dto;
        }

        private FordelingDto tilFordeling(YtelseStørrelse ytelseStørrelse) {
            var organisasjon = ytelseStørrelse.getVirksomhet().map(o -> new Organisasjon(o.getId())).orElse(null);
            var inntektPeriodeType = KodeverkMapper.mapInntektPeriodeTypeTilDto(ytelseStørrelse.getHyppighet());
            var beløp = ytelseStørrelse.getBeløp().getVerdi();
            return new FordelingDto(organisasjon, inntektPeriodeType, beløp);
        }

        private YtelseDto tilYtelse(Ytelse ytelse) {

            var fagsystem = KodeverkMapper.mapFagsystemTilDto(ytelse.getKilde());
            var periode = new Periode(ytelse.getPeriode().getFomDato(), ytelse.getPeriode().getTomDato());
            var ytelseType = new YtelseType(ytelse.getRelatertYtelseType().getKode());
            var ytelseStatus = new YtelseStatus(ytelse.getStatus().getKode());
            var temaUnderkategori = KodeverkMapper.getBehandlingsTemaUnderkategori(ytelse.getBehandlingsTema());
            var dto = new YtelseDto(fagsystem, ytelseType, periode, ytelseStatus)
                .medSaksnummer(ytelse.getSaksnummer() == null ? null : ytelse.getSaksnummer().getVerdi());

            dto.medTemaUnderkategori(temaUnderkategori);

            ytelse.getYtelseGrunnlag().ifPresent(gr -> dto.setGrunnlag(mapYtelseGrunnlag(gr)));

            Comparator<AnvisningDto> compAnvisning = Comparator
                .comparing((AnvisningDto anv) -> anv.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(anv -> anv.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

            var anvisninger = ytelse.getYtelseAnvist().stream().map(this::map).sorted(compAnvisning).collect(Collectors.toList());
            dto.setAnvisninger(anvisninger);

            return dto;
        }

        private AnvisningDto map(YtelseAnvist ya) {
            var periode = new Periode(ya.getAnvistFOM(), ya.getAnvistTOM());
            var dto = new AnvisningDto(periode);
            ya.getBeløp().ifPresent(v -> dto.setBeløp(v.getVerdi()));
            ya.getDagsats().ifPresent(v -> dto.setDagsats(v.getVerdi()));
            ya.getUtbetalingsgradProsent().ifPresent(v -> dto.setUtbetalingsgrad(v.getVerdi()));
            return dto;
        }

        List<YtelserDto> map(Collection<AktørYtelse> aktørYtelser) {
            return aktørYtelser.stream().map(this::mapTilYtelser).collect(Collectors.toList());
        }

    }
}
