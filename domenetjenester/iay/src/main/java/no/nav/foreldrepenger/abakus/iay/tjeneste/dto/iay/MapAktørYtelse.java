package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collection;
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
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.AnvisningDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.FordelingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelseDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelseGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelserDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MapAktørYtelse {
    static class MapFraDto {
        private InntektArbeidYtelseAggregatBuilder aggregatBuilder;
        private AktørId aktørId;

        MapFraDto(AktørId aktørId, InntektArbeidYtelseAggregatBuilder aggregatBuilder) {
            this.aktørId = aktørId;
            this.aggregatBuilder = aggregatBuilder;
        }

        public List<AktørYtelseBuilder> map(Collection<YtelserDto> dtos) {
            return dtos.stream().map(this::mapAktørYtelse).collect(Collectors.toUnmodifiableList());
        }

        private AktørYtelseBuilder mapAktørYtelse(YtelserDto dto) {
            var builder = aggregatBuilder.getAktørYtelseBuilder(aktørId);
            dto.getYtelser().forEach(ytelseDto -> builder.leggTilYtelse(mapYtelse(ytelseDto)));
            return builder;
        }

        private TemaUnderkategori mapBehandlingsTema(no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.TemaUnderkategori temaUnderkategori) {
            return new TemaUnderkategori(temaUnderkategori.getKode());
        }

        private no.nav.foreldrepenger.abakus.typer.Fagsystem mapFagSystem(Fagsystem fagsystemDto) {
            return new no.nav.foreldrepenger.abakus.typer.Fagsystem(fagsystemDto.getKode());
        }

        private DatoIntervallEntitet mapPeriode(Periode periode) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom());
        }

        private YtelseBuilder mapYtelse(YtelseDto ytelseDto) {
            var ytelseBuilder = YtelseBuilder.oppdatere(Optional.empty());
            var behandlingsTema = mapBehandlingsTema(ytelseDto.getTemaUnderkategori());
            ytelseBuilder
                .medYtelseGrunnlag(mapYtelseGrunnlag(ytelseDto.getGrunnlag(), ytelseBuilder.getGrunnlagBuilder()))
                .medYtelseType(mapYtelseType(ytelseDto.getYtelseType()))
                .medBehandlingsTema(behandlingsTema)
                .medKilde(mapFagSystem(ytelseDto.getFagsystemDto()))
                .medPeriode(mapPeriode(ytelseDto.getPeriode()))
                .medSaksnummer(new Saksnummer(ytelseDto.getSaksnummer()))
                .medStatus(mapYtelseStatus(ytelseDto.getStatus()));
            ytelseDto.getAnvisninger()
                .forEach(anvisning -> ytelseBuilder.medYtelseAnvist(mapYtelseAnvist(anvisning, ytelseBuilder.getAnvistBuilder())));
            return ytelseBuilder;
        }

        private YtelseAnvist mapYtelseAnvist(AnvisningDto anvisning, YtelseAnvistBuilder anvistBuilder) {
            return anvistBuilder
                .medAnvistPeriode(mapPeriode(anvisning.getPeriode()))
                .medBeløp(anvisning.getBeløp())
                .medDagsats(anvisning.getDagsats())
                .medUtbetalingsgradProsent(anvisning.getUtbetalingsgrad())
                .build();
        }

        private YtelseGrunnlag mapYtelseGrunnlag(YtelseGrunnlagDto grunnlag, YtelseGrunnlagBuilder grunnlagBuilder) {
            grunnlagBuilder
                .medArbeidskategori(grunnlag.getArbeidskategoriDto().getKode())
                .medDekningsgradProsent(grunnlag.getDekningsgradProsent())
                .medGraderingProsent(grunnlag.getGraderingProsent())
                .medInntektsgrunnlagProsent(grunnlag.getInntektsgrunnlagProsent())
                .medOpprinneligIdentdato(grunnlag.getOpprinneligIdentDato());
            grunnlag.getFordeling()
                .forEach(fordeling -> grunnlagBuilder.medYtelseStørrelse(mapYtelseStørrelse(fordeling)));
            return grunnlagBuilder.build();
        }

        private no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus mapYtelseStatus(YtelseStatus ytelseStatus) {
            return new no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus(ytelseStatus.getKode());
        }

        private YtelseStørrelse mapYtelseStørrelse(FordelingDto fordeling) {
            return YtelseStørrelseBuilder.ny()
                    .medBeløp(fordeling.getBeløp())
                    .medHyppighet(fordeling.getHyppighet().getKode())
                    .medVirksomhet(new OrgNummer(fordeling.getArbeidsgiver().getIdent()))
                    .build();
        }

        private no.nav.foreldrepenger.abakus.kodeverk.YtelseType mapYtelseType(YtelseType type) {
            return new no.nav.foreldrepenger.abakus.kodeverk.YtelseType(type.getKode());
        }

    }

    static class MapTilDto {

        private List<FordelingDto> mapFordeling(List<YtelseStørrelse> ytelseStørrelse) {
            return ytelseStørrelse.stream().map(this::tilFordeling).collect(Collectors.toUnmodifiableList());
        }

        private YtelserDto mapTilYtelser(AktørYtelse ay) {
            AktørIdPersonident person = new AktørIdPersonident(ay.getAktørId().getId());
            return new YtelserDto(person)
                    .medYtelser(mapTilYtelser(ay.getYtelser()));
        }

        private List<YtelseDto> mapTilYtelser(Collection<Ytelse> ytelser) {
            return ytelser.stream().map(this::tilYtelse).collect(Collectors.toList());
        }

        private YtelseGrunnlagDto mapYtelseGrunnlag(YtelseGrunnlag gr) {
            YtelseGrunnlagDto dto = new YtelseGrunnlagDto();
            gr.getArbeidskategori().ifPresent(ak -> dto.setArbeidskategoriDto(new Arbeidskategori(ak.getKode())));
            gr.getOpprinneligIdentdato().ifPresent(dto::setOpprinneligIdentDato);
            gr.getDekningsgradProsent().map(Stillingsprosent::getVerdi).ifPresent(dto::setDekningsgradProsent);
            gr.getGraderingProsent().map(Stillingsprosent::getVerdi).ifPresent(dto::setGraderingProsent);
            gr.getInntektsgrunnlagProsent().map(Stillingsprosent::getVerdi).ifPresent(dto::setInntektsgrunnlagProsent);
            dto.setFordeling(mapFordeling(gr.getYtelseStørrelse()));
            return dto;
        }

        private FordelingDto tilFordeling(YtelseStørrelse ytelseStørrelse) {
            var organisasjon = ytelseStørrelse.getVirksomhet().map(o -> new Organisasjon(o.getId())).orElse(null);
            var inntektPeriodeType = new InntektPeriodeType(ytelseStørrelse.getHyppighet().getKode());
            var beløp = ytelseStørrelse.getBeløp().getVerdi();
            return new FordelingDto(organisasjon, inntektPeriodeType, beløp);
        }

        private YtelseDto tilYtelse(Ytelse ytelse) {
            var fagsystem = new Fagsystem(ytelse.getKilde().getKode());
            var periode = new Periode(ytelse.getPeriode().getFomDato(), ytelse.getPeriode().getTomDato());
            var ytelseType = new YtelseType(ytelse.getRelatertYtelseType().getKode());
            var ytelseStatus = new YtelseStatus(ytelse.getStatus().getKode());

            var dto = new YtelseDto(fagsystem, ytelseType, periode, ytelseStatus, ytelse.getSaksnummer().getVerdi());
            ytelse.getYtelseGrunnlag().ifPresent(gr -> dto.setGrunnlag(mapYtelseGrunnlag(gr)));

            return dto;
        }

        List<YtelserDto> map(Collection<AktørYtelse> aktørYtelser) {
            return aktørYtelser.stream().map(this::mapTilYtelser).collect(Collectors.toList());
        }

    }
}
