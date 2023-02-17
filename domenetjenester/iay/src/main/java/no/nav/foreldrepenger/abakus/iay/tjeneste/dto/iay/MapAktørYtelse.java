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
import no.nav.abakus.iaygrunnlag.ytelse.v1.AnvisningDto;
import no.nav.abakus.iaygrunnlag.ytelse.v1.AnvistAndelDto;
import no.nav.abakus.iaygrunnlag.ytelse.v1.FordelingDto;
import no.nav.abakus.iaygrunnlag.ytelse.v1.YtelseDto;
import no.nav.abakus.iaygrunnlag.ytelse.v1.YtelseGrunnlagDto;
import no.nav.abakus.iaygrunnlag.ytelse.v1.YtelserDto;
import no.nav.foreldrepenger.abakus.domene.iay.AktørYtelse;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.Ytelse;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvist;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvistAndel;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelse;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class MapAktørYtelse {
    private static final Comparator<YtelseDto> COMP_YTELSE = Comparator.comparing(YtelseDto::getSaksnummer,
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getYtelseType() == null ? null : dto.getYtelseType().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getTemaUnderkategori() == null ? null : dto.getTemaUnderkategori().getKode(),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<FordelingDto> COMP_FORDELING = Comparator.comparing(
            (FordelingDto dto) -> dto.getArbeidsgiver() == null ? null : dto.getArbeidsgiver().getIdent(),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getHyppighet() == null ? null : dto.getHyppighet().getKode(), Comparator.nullsLast(Comparator.naturalOrder()));


    static class MapTilDto {

        private List<FordelingDto> mapFordeling(List<YtelseStørrelse> ytelseStørrelse) {
            if (ytelseStørrelse == null || ytelseStørrelse.isEmpty()) {
                return Collections.emptyList();
            }
            return ytelseStørrelse.stream().map(this::tilFordeling).sorted(COMP_FORDELING).collect(Collectors.toUnmodifiableList());
        }

        private YtelserDto mapTilYtelser(AktørYtelse ay) {
            AktørIdPersonident person = new AktørIdPersonident(ay.getAktørId().getId());
            return new YtelserDto(person).medYtelser(mapTilYtelser(ay.getAlleYtelser()));
        }

        private List<YtelseDto> mapTilYtelser(Collection<Ytelse> ytelser) {
            return ytelser.stream().map(this::tilYtelse).sorted(COMP_YTELSE).collect(Collectors.toList());
        }

        private YtelseGrunnlagDto mapYtelseGrunnlag(YtelseGrunnlag gr) {

            YtelseGrunnlagDto dto = new YtelseGrunnlagDto();
            gr.getArbeidskategori().ifPresent(dto::setArbeidskategoriDto);
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
            var inntektPeriodeType = ytelseStørrelse.getHyppighet();
            var beløp = ytelseStørrelse.getBeløp().getVerdi();
            return new FordelingDto(organisasjon, inntektPeriodeType, beløp, ytelseStørrelse.getErRefusjon());
        }

        private YtelseDto tilYtelse(Ytelse ytelse) {

            var fagsystem = ytelse.getKilde();
            var periode = new Periode(ytelse.getPeriode().getFomDato(), ytelse.getPeriode().getTomDato());
            var ytelseType = ytelse.getRelatertYtelseType();
            var ytelseStatus = ytelse.getStatus();
            var temaUnderkategori = ytelse.getBehandlingsTema();
            var dto = new YtelseDto(fagsystem, ytelseType, periode, ytelseStatus).medSaksnummer(
                ytelse.getSaksreferanse() == null ? null : ytelse.getSaksreferanse().getVerdi()).medVedtattTidspunkt(ytelse.getVedtattTidspunkt());

            dto.medTemaUnderkategori(temaUnderkategori);

            ytelse.getYtelseGrunnlag().ifPresent(gr -> dto.setGrunnlag(mapYtelseGrunnlag(gr)));

            Comparator<AnvisningDto> compAnvisning = Comparator.comparing((AnvisningDto anv) -> anv.getPeriode().getFom(),
                    Comparator.nullsFirst(Comparator.naturalOrder()))
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
            if (ya.getYtelseAnvistAndeler() != null) {
                dto.setAndeler(ya.getYtelseAnvistAndeler().stream().map(this::mapAndel).toList());
            }
            return dto;
        }


        List<YtelserDto> map(Collection<AktørYtelse> aktørYtelser) {
            return aktørYtelser.stream().map(this::mapTilYtelser).collect(Collectors.toList());
        }

        private AnvistAndelDto mapAndel(YtelseAnvistAndel aa) {
            return new AnvistAndelDto(aa.getArbeidsgiver().map(this::mapAktør).orElse(null), aa.getArbeidsforholdRef().getReferanse(),
                aa.getDagsats().getVerdi(), aa.getUtbetalingsgradProsent().getVerdi(), aa.getRefusjonsgradProsent().getVerdi(),
                aa.getInntektskategori());
        }

        private Aktør mapAktør(Arbeidsgiver arbeidsgiverEntitet) {
            return arbeidsgiverEntitet.erAktørId() ? new AktørIdPersonident(arbeidsgiverEntitet.getAktørId().getId()) : new Organisasjon(
                arbeidsgiverEntitet.getOrgnr().getId());
        }

    }
}
