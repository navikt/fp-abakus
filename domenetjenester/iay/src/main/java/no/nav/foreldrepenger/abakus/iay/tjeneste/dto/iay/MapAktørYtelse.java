package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import no.nav.foreldrepenger.abakus.domene.iay.AktørYtelse;
import no.nav.foreldrepenger.abakus.domene.iay.Ytelse;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseStørrelse;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.FordelingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelseDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelseGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.ytelse.v1.YtelserDto;

public class MapAktørYtelse {
    private class MapFraDto {

        public List<AktørYtelse> map(Collection<YtelserDto> aktørYtelser) {
            // FIXME Map AktørYtelse på vei inn
            throw new UnsupportedOperationException("Not Yet Implemented");
        }
        
    }
    private class MapTilDto {

        private List<FordelingDto> mapFordeling(List<YtelseStørrelse> ytelseStørrelse) {
            return ytelseStørrelse.stream().map(this::tilFordeling).collect(Collectors.toList());
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

    public List<YtelserDto> mapTilDto(Collection<AktørYtelse> aktørYtelser) {
        return new MapTilDto().map(aktørYtelser);
    }

    public List<AktørYtelse> mapFraDto(Collection<YtelserDto> aktørYtelser) {
        return new MapFraDto().map(aktørYtelser);
    }
}
