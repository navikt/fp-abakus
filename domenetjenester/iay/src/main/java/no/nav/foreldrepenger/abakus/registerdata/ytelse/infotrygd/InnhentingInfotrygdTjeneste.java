package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseArbeid;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.ArbeidskategoriReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.InntektPeriodeReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.RelatertYtelseStatusReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.TemaReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.TemaUnderkategoriReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.InfotrygdGrunnlagAggregator;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Arbeidsforhold;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Periode;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class InnhentingInfotrygdTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingInfotrygdTjeneste.class);

    private static final Map<no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode, YtelseType> STØNADSKAT1_TIL_YTELSETYPE = Map.ofEntries(
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.UKJENT, YtelseType.UDEFINERT),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.FA, YtelseType.FORELDREPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.SP, YtelseType.SYKEPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.BS, YtelseType.OMSORGSPENGER)
    );

    private static final Map<no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode, YtelseType> STØNADSKAT2_TIL_YTELSETYPE = Map.ofEntries(
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.UKJENT, YtelseType.UDEFINERT),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.AP, YtelseType.FORELDREPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.FP, YtelseType.FORELDREPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.FU, YtelseType.FORELDREPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.FØ, YtelseType.FORELDREPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.SV, YtelseType.SVANGERSKAPSPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.SP, YtelseType.SYKEPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.OM, YtelseType.OMSORGSPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.OP, YtelseType.OPPLÆRINGSPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.PP, YtelseType.PLEIEPENGER_NÆRSTÅENDE),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.PB, YtelseType.PLEIEPENGER_SYKT_BARN),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.PI, YtelseType.PLEIEPENGER_SYKT_BARN),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.PN, YtelseType.PLEIEPENGER_SYKT_BARN)
    );

    private InfotrygdGrunnlagAggregator grunnlag;

    InnhentingInfotrygdTjeneste() {
        // CDI
    }

    @Inject
    public InnhentingInfotrygdTjeneste(InfotrygdGrunnlagAggregator grunnlag) {
        this.grunnlag = grunnlag;
    }

    public List<InfotrygdYtelseGrunnlag> getInfotrygdYtelser(PersonIdent ident, Interval periode) {
        List<Grunnlag> rest = grunnlag.hentAggregertGrunnlag(ident.getIdent(), dato(periode.getStart()), dato(periode.getEnd()));

        return mapTilInfotrygdYtelseGrunnlag(rest);
    }

    public List<InfotrygdYtelseGrunnlag> getInfotrygdYtelserFailSoft(PersonIdent ident, Interval periode) {
        List<Grunnlag> rest = grunnlag.hentAggregertGrunnlagFailSoft(ident.getIdent(), dato(periode.getStart()), dato(periode.getEnd()));

        return mapTilInfotrygdYtelseGrunnlag(rest);
    }

    private List<InfotrygdYtelseGrunnlag> mapTilInfotrygdYtelseGrunnlag(List<Grunnlag> rest) {
        var mappedGrunnlag = rest.stream()
            .filter(g -> !YtelseType.UDEFINERT.equals(TemaReverse.reverseMap(g.getTema().getKode().name(), LOG)))
            .map(this::restTilInfotrygdYtelseGrunnlag)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (!mappedGrunnlag.isEmpty()) {
            LOG.info("Infotrygd abacus mapped grunnlag {}", mappedGrunnlag);
        }
        return mappedGrunnlag;
    }

    private InfotrygdYtelseGrunnlag restTilInfotrygdYtelseGrunnlag(Grunnlag grunnlag) {
        if (grunnlag.getIverksatt() == null || grunnlag.getIdentdato() == null || !grunnlag.getIverksatt().equals(grunnlag.getIdentdato())) {
            LOG.info("Infotrygd ny mapper avvik iverksatt {} vs identdato {}", grunnlag.getIverksatt(), grunnlag.getIdentdato());
        }
        LocalDate brukIdentdato = grunnlag.getIdentdato() != null ? grunnlag.getIdentdato() : grunnlag.getIverksatt();
        if (brukIdentdato == null) {
            return null;
        }
        Periode fraSaksdata = utledPeriode(grunnlag.getIverksatt(), grunnlag.getOpphørFom(), grunnlag.getRegistrert());
        Periode brukPeriode = grunnlag.getPeriode() != null ? grunnlag.getPeriode() : fraSaksdata;
        Integer dekningsgrad = grunnlag.getDekningsgrad() != null ? grunnlag.getDekningsgrad().getProsent() : null;
        Arbeidskategori arbeidskategori = grunnlag.getKategori() == null ? Arbeidskategori.UGYLDIG :
            ArbeidskategoriReverse.reverseMap(grunnlag.getKategori().getKode().getKode(), LOG);
        TemaUnderkategori tuk = grunnlag.getBehandlingsTema() == null ? TemaUnderkategori.UDEFINERT :
            TemaUnderkategoriReverse.reverseMap(grunnlag.getBehandlingsTema().getKode().name());
        YtelseStatus brukStatus = mapYtelseStatus(grunnlag);

        var grunnlagBuilder = InfotrygdYtelseGrunnlag.getBuilder()
            .medYtelseType(bestemYtelseType(grunnlag))
            .medTemaUnderkategori(tuk)
            .medYtelseStatus(brukStatus)
            .medIdentdato(brukIdentdato)
            .medVedtaksPeriodeFom(brukPeriode.getFom())
            .medVedtaksPeriodeTom(brukPeriode.getTom())
            .medArbeidskategori(arbeidskategori)
            .medDekningsgrad(dekningsgrad)
            .medGradering(grunnlag.getGradering())
            .medFødselsdatoBarn(grunnlag.getFødselsdatoBarn())
            .medOpprinneligIdentdato(grunnlag.getOpprinneligIdentdato());

        grunnlag.getArbeidsforhold().stream()
            .map(this::arbeidsforholdTilInfotrygdYtelseArbeid)
            .forEach(grunnlagBuilder::leggTilArbeidsforhold);

        grunnlag.getVedtak().stream()
            .map(v -> new InfotrygdYtelseAnvist(v.getPeriode().getFom(), v.getPeriode().getTom(), new BigDecimal(v.getUtbetalingsgrad())))
            .forEach(grunnlagBuilder::leggTillAnvistPerioder);

        return grunnlagBuilder.build();
    }

    private YtelseStatus mapYtelseStatus(Grunnlag grunnlag) {
        if (grunnlag.getStatus() == null) {
            if (grunnlag.getOpphørFom() != null)
                return YtelseStatus.AVSLUTTET;
            if (grunnlag.getIverksatt() != null || grunnlag.getIdentdato() != null)
                return YtelseStatus.LØPENDE;
            return YtelseStatus.UNDER_BEHANDLING;
        }
        return RelatertYtelseStatusReverse.reverseMap(grunnlag.getStatus().getKode().name(), LOG);
    }

    private InfotrygdYtelseArbeid arbeidsforholdTilInfotrygdYtelseArbeid(Arbeidsforhold arbeidsforhold) {
        InntektPeriodeType inntektPeriode = arbeidsforhold.getInntektperiode() == null ? InntektPeriodeType.UDEFINERT :
            InntektPeriodeReverse.reverseMap(arbeidsforhold.getInntektperiode().getKode().name(), LOG);
        BigDecimal inntekt= arbeidsforhold.getInntekt() != null ? new BigDecimal(arbeidsforhold.getInntekt()) : null;
        return new InfotrygdYtelseArbeid(arbeidsforhold.getOrgnr().getOrgnr(),
            inntekt, inntektPeriode, arbeidsforhold.getRefusjon());
    }

    private static LocalDate dato(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }

    private Periode utledPeriode(LocalDate iverksatt, LocalDate opphoerFomDato, LocalDate registrert) {
        if (opphoerFomDato != null) {
            LocalDate tomFraOpphørFom = localDateMinus1Virkedag(opphoerFomDato);
            if (tomFraOpphørFom.isAfter(iverksatt)) {
                return new Periode(iverksatt, tomFraOpphørFom);
            } else {
                return new Periode(iverksatt, iverksatt);
            }
        } else {
            if (iverksatt != null) {
                return new Periode(iverksatt, Tid.TIDENES_ENDE);
            }
            return new Periode(registrert, registrert);
        }
    }

    private LocalDate localDateMinus1Virkedag(LocalDate opphoerFomDato) {
        LocalDate dato = opphoerFomDato.minusDays(1);
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            dato = opphoerFomDato.minusDays(1L + dato.getDayOfWeek().getValue() - DayOfWeek.FRIDAY.getValue());
        }
        return dato;
    }

    private YtelseType bestemYtelseType(Grunnlag grunnlag) {
        YtelseType kategori2 = grunnlag.getBehandlingsTema() == null ? YtelseType.UDEFINERT :
            STØNADSKAT2_TIL_YTELSETYPE.getOrDefault(grunnlag.getBehandlingsTema().getKode(), YtelseType.UDEFINERT);
        if (!YtelseType.UDEFINERT.equals(kategori2))
            return kategori2;
        LOG.info("Infotrygd ukjent stønadskategori 2");
        YtelseType kategori1 = grunnlag.getTema() == null ? YtelseType.UDEFINERT :
            STØNADSKAT1_TIL_YTELSETYPE.getOrDefault(grunnlag.getTema().getKode(), YtelseType.UDEFINERT);
        if (!YtelseType.UDEFINERT.equals(kategori1))
            return kategori1;
        LOG.info("Infotrygd ukjent stønadskategori 1 og 2");
        return YtelseType.UDEFINERT;
    }


}
