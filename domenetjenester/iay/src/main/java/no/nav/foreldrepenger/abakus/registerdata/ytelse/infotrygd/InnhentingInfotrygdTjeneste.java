package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd;

import static no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus.AVSLUTTET;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus.LØPENDE;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.ArbeidskategoriReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.InntektPeriodeReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.TemaReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.TemaUnderkategoriReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps.YtelseTypeReverse;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.InfotrygdYtelseArbeid;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.Grunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.Periode;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.felles.InfotrygdGrunnlagAggregator;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class InnhentingInfotrygdTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingInfotrygdTjeneste.class);
    private static final Set<YtelseType> YTELSER_STØTTET = Set.of(YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER, YtelseType.SYKEPENGER, YtelseType.PÅRØRENDESYKDOM);

    private InfotrygdGrunnlagAggregator grunnlag;

    InnhentingInfotrygdTjeneste() {
        // CDI
    }

    @Inject
    public InnhentingInfotrygdTjeneste(InfotrygdGrunnlagAggregator grunnlag) {
        this.grunnlag = grunnlag;
    }

    public List<InfotrygdYtelseGrunnlag> getInfotrygdYtelser(PersonIdent ident, Interval periode) {
        try {
            List<Grunnlag> rest = grunnlag.hentGrunnlag(ident.getIdent(), dato(periode.getStart()), dato(periode.getEnd()));

            var mappedGrunnlag = rest.stream()
                .filter(g -> !YtelseType.UDEFINERT.equals(TemaReverse.reverseMap(g.getTema().getKode().name(), LOG)))
                .map(this::restTilInfotrygdYtelseGrunnlag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            if (!mappedGrunnlag.isEmpty()) {
                LOG.info("Infotrygd abacus mapped grunnlag {}", mappedGrunnlag.toString());
            }
            return mappedGrunnlag;
        } catch (Exception e) {
            LOG.info("Infotrygd abacus ny mapper ukjent feil", e);
            return Collections.emptyList();
        }
    }

    private InfotrygdYtelseGrunnlag restTilInfotrygdYtelseGrunnlag(Grunnlag grunnlag) {
        YtelseStatus brukStatus = grunnlag.getOpphørFom() != null ? AVSLUTTET : (grunnlag.getIdentdato() != null ? LØPENDE : YtelseStatus.UDEFINERT);
        Periode fraSaksdata = utledPeriode(grunnlag.getIverksatt(), grunnlag.getOpphørFom(), grunnlag.getRegistrert());
        if (grunnlag.getOpphørFom() != null && !fraSaksdata.equals(grunnlag.getPeriode())) {
            LOG.info("Infotrygd ny mapper ulike perioder utledet: opphørFom {} vs periode {}", fraSaksdata, grunnlag.getPeriode());
        }
        if (grunnlag.getIverksatt() == null || grunnlag.getIdentdato() == null || !grunnlag.getIverksatt().equals(grunnlag.getIdentdato())) {
            LOG.info("Infotrygd ny mapper avvik iverksatt {} vs identdato {}", grunnlag.getIverksatt(), grunnlag.getIdentdato());
        }
        LocalDate brukIdentdato = grunnlag.getIdentdato() != null ? grunnlag.getIdentdato() : grunnlag.getIverksatt();
        if (brukIdentdato == null) {
            return null;
        }
        Periode brukPeriode = grunnlag.getPeriode() != null ? grunnlag.getPeriode() : fraSaksdata;
        TemaUnderkategori tuk = TemaUnderkategoriReverse.reverseMap(grunnlag.getBehandlingsTema().getKode().name(), LOG);

        var grunnlagBuilder = InfotrygdYtelseGrunnlag.getBuilder()
            .medYtelseType(YtelseTypeReverse.reverseMap(tuk, LOG))
            .medTemaUnderkategori(tuk)
            .medYtelseStatus(brukStatus)
            .medIdentdato(brukIdentdato)
            .medVedtaksPeriodeFom(brukPeriode.getFom())
            .medVedtaksPeriodeTom(brukPeriode.getTom())
            .medArbeidskategori(ArbeidskategoriReverse.reverseMap(grunnlag.getKategori().getKode().getKode(), LOG))
            .medDekningsgrad(grunnlag.getDekningsgrad().getProsent())
            .medGradering(grunnlag.getGradering())
            .medFødselsdatoBarn(grunnlag.getFødselsdatoBarn())
            .medOpprinneligIdentdato(grunnlag.getOpprinneligIdentdato());

        grunnlag.getArbeidsforhold().stream()
            .map(a -> new InfotrygdYtelseArbeid(a.getOrgnr().getOrgnr(), a.getInntekt() != null ? new BigDecimal(a.getInntekt()) : null,
                InntektPeriodeReverse.reverseMap(a.getInntektperiode().getKode().name(), LOG), a.getRefusjon()))
            .forEach(grunnlagBuilder::leggTilArbeidsforhold);

        grunnlag.getVedtak().stream()
            .map(v -> new InfotrygdYtelseAnvist(v.getPeriode().getFom(), v.getPeriode().getTom(), new BigDecimal(v.getUtbetalingsgrad())))
            .forEach(grunnlagBuilder::leggTillAnvistPerioder);

        return grunnlagBuilder.build();
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

    public static boolean sammenlignGrunnlagKilder(List<InfotrygdYtelseGrunnlag> ws, List<InfotrygdYtelseGrunnlag> rest) {
        // Likt innhold så equals ikke går bananas
        var mappedRest = rest.stream().map(InnhentingInfotrygdTjeneste::nyttGrunnlagTilNyttMedRedusertInnhold).filter(Objects::nonNull).collect(Collectors.toList());
        boolean sammenligning = mappedRest.equals(ws);
        if (!sammenligning) {
            LOG.info("Infotrygd mapper avvik mellom ws {} og rest {}", ws, mappedRest);
        }
        return sammenligning;
    }

    public static List<InfotrygdYtelseGrunnlag> mapISoG(List<InfotrygdSakOgGrunnlag> sog) {
        try {
            return sog.stream()
                .filter(g -> YTELSER_STØTTET.contains(g.getSak().getYtelseType()))
                .filter(g -> g.getGrunnlag().isPresent())
                .map(InnhentingInfotrygdTjeneste::isogTilNyttGrunnlag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.info("Infotrygd ny mapper fra ISoG ukjent feil", e);
            return Collections.emptyList();
        }

    }

    private static InfotrygdYtelseGrunnlag isogTilNyttGrunnlag(InfotrygdSakOgGrunnlag grunnlag) {
        var grunnlagBuilder = InfotrygdYtelseGrunnlag.getBuilder()
            .medYtelseType(grunnlag.getSak().getYtelseType())
            .medTemaUnderkategori(grunnlag.getSak().getTemaUnderkategori())
            .medYtelseStatus(grunnlag.getSak().getYtelseStatus())
            .medIdentdato(grunnlag.getSak().getIverksatt())
            .medVedtaksPeriodeFom(grunnlag.getPeriode().getFomDato())
            .medVedtaksPeriodeTom(grunnlag.getPeriode().getTomDato())
            .medArbeidskategori(grunnlag.getGrunnlag().map(YtelseBeregningsgrunnlag::getArbeidskategori).orElse(Arbeidskategori.UDEFINERT));

        grunnlag.getGrunnlag().ifPresent(grunnlagO -> grunnlagO.getArbeidsforhold().stream()
            .map(a -> new InfotrygdYtelseArbeid(a.getOrgnr(), a.getInntektForPerioden(), a.getInntektPeriodeType(), null))
            .forEach(grunnlagBuilder::leggTilArbeidsforhold));

        grunnlag.getGrunnlag().ifPresent(grunnlagO -> grunnlagO.getVedtak().stream()
            .map(v -> new InfotrygdYtelseAnvist(v.getFom(), v.getTom(), v.getUtbetalingsgrad() != null ? new BigDecimal(v.getUtbetalingsgrad()) : null))
            .forEach(grunnlagBuilder::leggTillAnvistPerioder));

        return grunnlagBuilder.build();
    }

    private static InfotrygdYtelseGrunnlag nyttGrunnlagTilNyttMedRedusertInnhold(InfotrygdYtelseGrunnlag grunnlag) {
        try {
            var grunnlagBuilder = InfotrygdYtelseGrunnlag.getBuilder()
                .medYtelseType(grunnlag.getYtelseType())
                .medTemaUnderkategori(grunnlag.getTemaUnderkategori())
                .medYtelseStatus(grunnlag.getYtelseStatus())
                .medIdentdato(grunnlag.getIdentdato())
                .medVedtaksPeriodeFom(grunnlag.getVedtaksPeriodeFom())
                .medVedtaksPeriodeTom(grunnlag.getVedtaksPeriodeTom())
                .medArbeidskategori(grunnlag.getKategori());

            grunnlag.getArbeidsforhold().stream()
                .map(a -> new InfotrygdYtelseArbeid(a.getOrgnr(), a.getInntekt(), a.getInntektperiode(), null))
                .forEach(grunnlagBuilder::leggTilArbeidsforhold);

            grunnlag.getUtbetaltePerioder().stream()
                .map(v -> new InfotrygdYtelseAnvist(v.getUtbetaltFom(), v.getUtbetaltTom(), v.getUtbetalingsgrad()))
                .forEach(grunnlagBuilder::leggTillAnvistPerioder);

            return grunnlagBuilder.build();
        }  catch (Exception e) {
            LOG.info("Infotrygd ny mapper til redusert rest ukjent feil", e);
            return null;
        }
    }

}
