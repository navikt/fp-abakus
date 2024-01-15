package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseArbeid;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.ArbeidskategoriReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.InntektPeriodeReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.RelatertYtelseStatusReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.TemaReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.InfotrygdGrunnlagAggregator;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Arbeidsforhold;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Periode;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Vedtak;
import no.nav.vedtak.felles.integrasjon.spokelse.Spøkelse;
import no.nav.vedtak.felles.integrasjon.spokelse.SykepengeVedtak;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class InnhentingInfotrygdTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingInfotrygdTjeneste.class);

    private static final Map<no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode, YtelseType> STØNADSKAT1_TIL_YTELSETYPE = Map.ofEntries(
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.UKJENT, YtelseType.UDEFINERT),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.FA, YtelseType.FORELDREPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.SP, YtelseType.SYKEPENGER),
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.TemaKode.BS, YtelseType.OMSORGSPENGER));

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
        Map.entry(no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.BehandlingstemaKode.PN, YtelseType.PLEIEPENGER_SYKT_BARN));

    private static final List<Duration> SPOKELSE_TIMEOUTS = List.of(Duration.ofMillis(100), Duration.ofMillis(250), Duration.ofMillis(500),
        Duration.ofMillis(2500), Duration.ofMillis(12), Duration.ofSeconds(60));

    private InfotrygdGrunnlagAggregator infotrygdGrunnlag;
    private Spøkelse spøkelse;

    InnhentingInfotrygdTjeneste() {
        // CDI
    }

    @Inject
    public InnhentingInfotrygdTjeneste(InfotrygdGrunnlagAggregator infotrygdGrunnlag, Spøkelse spøkelse) {
        this.infotrygdGrunnlag = infotrygdGrunnlag;
        this.spøkelse = spøkelse;
    }

    public List<InfotrygdYtelseGrunnlag> getInfotrygdYtelser(PersonIdent ident, IntervallEntitet periode) {
        LocalDate innhentFom = periode.getFomDato();
        List<Grunnlag> rest = infotrygdGrunnlag.hentAggregertGrunnlag(ident.getIdent(), innhentFom, periode.getTomDato());

        return mapTilInfotrygdYtelseGrunnlag(rest, innhentFom);
    }

    public List<InfotrygdYtelseGrunnlag> getInfotrygdYtelserFailSoft(PersonIdent ident, IntervallEntitet periode) {
        LocalDate innhentFom = periode.getFomDato();
        List<Grunnlag> rest = infotrygdGrunnlag.hentAggregertGrunnlagFailSoft(ident.getIdent(), innhentFom, periode.getTomDato());

        return mapTilInfotrygdYtelseGrunnlag(rest, innhentFom);
    }

    public static List<InfotrygdYtelseGrunnlag> mapTilInfotrygdYtelseGrunnlag(List<Grunnlag> rest, LocalDate innhentFom) {
        var mappedGrunnlag = rest.stream()
            .filter(g -> !YtelseType.UDEFINERT.equals(TemaReverse.reverseMap(g.tema().kode().name(), LOG)))
            .map(g -> restTilInfotrygdYtelseGrunnlag(g, innhentFom))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return mappedGrunnlag;
    }

    private static InfotrygdYtelseGrunnlag restTilInfotrygdYtelseGrunnlag(Grunnlag grunnlag, LocalDate innhentFom) {
        if (grunnlag.iverksatt() == null || grunnlag.identdato() == null || !grunnlag.iverksatt().equals(grunnlag.identdato())) {
            LOG.info("Infotrygd ny mapper avvik iverksatt {} vs identdato {}", grunnlag.iverksatt(), grunnlag.identdato());
        }
        LocalDate brukIdentdato = grunnlag.identdato() != null ? grunnlag.identdato() : grunnlag.iverksatt();
        if (brukIdentdato == null) {
            return null;
        }
        Periode fraSaksdata = utledPeriode(grunnlag.iverksatt(), grunnlag.opphørFom(), grunnlag.registrert());
        if (grunnlag.opphørFom() != null) {
            LOG.info("Infotrygdgrunnlag: OpphørFom: {}, Iverksatt: {}, Registrert: {}, Periode: {}", grunnlag.opphørFom(), grunnlag.iverksatt(),
                grunnlag.registrert(), grunnlag.periode() == null ? "IKKE SATT" : grunnlag.periode());

        }
        Periode brukPeriode = grunnlag.periode() != null ? grunnlag.periode() : fraSaksdata;
        Integer dekningsgrad = grunnlag.dekningsgrad() != null ? grunnlag.dekningsgrad().prosent() : null;
        Arbeidskategori arbeidskategori =
            grunnlag.kategori() == null ? Arbeidskategori.UGYLDIG : ArbeidskategoriReverse.reverseMap(grunnlag.kategori().kode().getKode(), LOG);
        YtelseStatus brukStatus = mapYtelseStatus(grunnlag);
        // Ignorer gamle vedtak
        if (brukPeriode.tom().isBefore(innhentFom) && grunnlag.vedtak()
            .stream()
            .map(Vedtak::periode)
            .map(Periode::tom)
            .noneMatch(innhentFom::isBefore)) {
            return null;
        }

        var grunnlagBuilder = InfotrygdYtelseGrunnlag.getBuilder()
            .medYtelseType(bestemYtelseType(grunnlag))
            .medYtelseStatus(brukStatus)
            .medVedtattTidspunkt(brukIdentdato.atStartOfDay())
            .medVedtaksPeriodeFom(brukPeriode.fom())
            .medVedtaksPeriodeTom(brukPeriode.tom())
            .medArbeidskategori(arbeidskategori)
            .medDekningsgrad(dekningsgrad)
            .medGradering(grunnlag.gradering())
            .medFødselsdatoBarn(grunnlag.fødselsdatoBarn())
            .medOpprinneligIdentdato(grunnlag.opprinneligIdentdato());

        grunnlag.arbeidsforhold().stream().map(InnhentingInfotrygdTjeneste::arbeidsforholdTilInfotrygdYtelseArbeid).forEach(grunnlagBuilder::leggTilArbeidsforhold);

        grunnlag.vedtak()
            .stream()
            .map(v -> new InfotrygdYtelseAnvist(v.periode().fom(), v.periode().tom(), new BigDecimal(v.utbetalingsgrad()), v.arbeidsgiverOrgnr(),
                v.erRefusjon(), v.dagsats() != null ? BigDecimal.valueOf(v.dagsats()) : null))
            .forEach(grunnlagBuilder::leggTillAnvistPerioder);

        return grunnlagBuilder.build();
    }

    private static YtelseStatus mapYtelseStatus(Grunnlag grunnlag) {
        if (grunnlag.status() == null) {
            if (grunnlag.opphørFom() != null) {
                return YtelseStatus.AVSLUTTET;
            }
            if (grunnlag.iverksatt() != null || grunnlag.identdato() != null) {
                return YtelseStatus.LØPENDE;
            }
            return YtelseStatus.UNDER_BEHANDLING;
        }
        return RelatertYtelseStatusReverse.reverseMap(grunnlag.status().kode().name(), LOG);
    }

    private static InfotrygdYtelseArbeid arbeidsforholdTilInfotrygdYtelseArbeid(Arbeidsforhold arbeidsforhold) {
        InntektPeriodeType inntektPeriode =
            arbeidsforhold.inntektsperiode() == null ? InntektPeriodeType.UDEFINERT : InntektPeriodeReverse.reverseMap(
                arbeidsforhold.inntektsperiode().kode().name(), LOG);
        BigDecimal inntekt = arbeidsforhold.inntekt() != null ? new BigDecimal(arbeidsforhold.inntekt()) : null;
        return new InfotrygdYtelseArbeid(arbeidsforhold.orgnr().orgnr(), inntekt, inntektPeriode, arbeidsforhold.refusjon(),
            arbeidsforhold.refusjonTom());
    }

    private static Periode utledPeriode(LocalDate iverksatt, LocalDate opphoerFomDato, LocalDate registrert) {
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

    private static LocalDate localDateMinus1Virkedag(LocalDate opphoerFomDato) {
        LocalDate dato = opphoerFomDato.minusDays(1);
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            dato = opphoerFomDato.minusDays(1L + dato.getDayOfWeek().getValue() - DayOfWeek.FRIDAY.getValue());
        }
        return dato;
    }

    private static YtelseType bestemYtelseType(Grunnlag grunnlag) {
        YtelseType kategori2 =
            grunnlag.behandlingstema() == null ? YtelseType.UDEFINERT : STØNADSKAT2_TIL_YTELSETYPE.getOrDefault(grunnlag.behandlingstema().kode(),
                YtelseType.UDEFINERT);
        if (!YtelseType.UDEFINERT.equals(kategori2)) {
            return kategori2;
        }
        LOG.info("Infotrygd ukjent stønadskategori 2");
        YtelseType kategori1 =
            grunnlag.tema() == null ? YtelseType.UDEFINERT : STØNADSKAT1_TIL_YTELSETYPE.getOrDefault(grunnlag.tema().kode(), YtelseType.UDEFINERT);
        if (!YtelseType.UDEFINERT.equals(kategori1)) {
            return kategori1;
        }
        LOG.info("Infotrygd ukjent stønadskategori 1 og 2");
        return YtelseType.UDEFINERT;
    }

    public List<InfotrygdYtelseGrunnlag> getSPøkelseYtelser(PersonIdent ident, LocalDate fom) {
        var it = SPOKELSE_TIMEOUTS.iterator();
        while (it.hasNext()) {
            try {
                var timeout = it.next();
                var før = System.nanoTime();
                var vedtak = spøkelse.hentGrunnlag(ident.getIdent(), fom, timeout);
                var etter = System.nanoTime();
                LOG.info("Spøkelse antall {} timeout {} svartid {}", vedtak.size(), timeout, (etter - før) / 1000000); // Log millis
                return mapSpøkelseTilInfotrygdYtelseGrunnlag(vedtak);
            } catch (Exception e) {
                if (!it.hasNext()) {
                    throw e;
                }
            }
        }
        return List.of();
    }

    public List<InfotrygdYtelseGrunnlag> getSPøkelseYtelserFailSoft(PersonIdent ident, LocalDate fom) {
        try {
            List<SykepengeVedtak> rest = spøkelse.hentGrunnlagFailSoft(ident.getIdent(), fom);

            return mapSpøkelseTilInfotrygdYtelseGrunnlag(rest);
        } catch (Exception e) {
            LOG.info("abakus spokelse noe gikk feil", e);
            return Collections.emptyList();
        }
    }

    private List<InfotrygdYtelseGrunnlag> mapSpøkelseTilInfotrygdYtelseGrunnlag(List<SykepengeVedtak> rest) {
        return rest.stream().map(this::spokelseTilInfotrygdYtelseGrunnlag).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private InfotrygdYtelseGrunnlag spokelseTilInfotrygdYtelseGrunnlag(SykepengeVedtak grunnlag) {
        LocalDate min = grunnlag.utbetalingerNonNull()
            .stream()
            .map(SykepengeVedtak.SykepengeUtbetaling::fom)
            .min(Comparator.naturalOrder())
            .orElse(null);
        LocalDate max = grunnlag.utbetalingerNonNull()
            .stream()
            .map(SykepengeVedtak.SykepengeUtbetaling::tom)
            .max(Comparator.naturalOrder())
            .orElse(null);
        if (min == null) {
            return null;
        }

        Periode brukPeriode = new Periode(min, max == null ? min : max);

        var grunnlagBuilder = InfotrygdYtelseGrunnlag.getBuilder()
            .medYtelseType(YtelseType.SYKEPENGER)
            .medYtelseStatus(YtelseStatus.AVSLUTTET)
            .medVedtaksreferanse(grunnlag.vedtaksreferanse().trim())
            .medVedtattTidspunkt(grunnlag.vedtattTidspunkt() != null ? grunnlag.vedtattTidspunkt() : LocalDateTime.now())
            .medVedtaksPeriodeFom(brukPeriode.fom())
            .medVedtaksPeriodeTom(brukPeriode.tom());

        grunnlag.utbetalingerNonNull()
            .stream()
            .map(v -> new InfotrygdYtelseAnvist(v.fom(), v.tom(), v.gradScale2(), null, null, null))
            .forEach(grunnlagBuilder::leggTillAnvistPerioder);

        return grunnlagBuilder.build();
    }

}
