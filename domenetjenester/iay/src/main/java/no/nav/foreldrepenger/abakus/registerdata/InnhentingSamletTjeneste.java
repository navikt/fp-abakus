package no.nav.foreldrepenger.abakus.registerdata;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus.AVSLUTTET;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus.LØPENDE;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus.UNDER_BEHANDLING;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseType.ENGANGSSTØNAD;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseType.ENSLIG_FORSØRGER;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseType.FORELDREPENGER;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseType.PÅRØRENDESYKDOM;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseType.SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseType.SYKEPENGER;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import com.google.common.collect.Sets;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.Aggregator;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.Grunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.InfotrygdGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles.InfotrygdSakTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);
    private static final String REST_GJELDER = "fpabakus.infotrygd.rest";
    private ArbeidsforholdTjeneste arbeidsforhold;
    private AktørConsumer aktør;
    private InntektTjeneste inntekt;
    private InfotrygdTjeneste wsSak;
    private InfotrygdGrunnlag grunnlag;
    private InfotrygdSakTjeneste saker;
    private InfotrygdBeregningsgrunnlagTjeneste wsGrunnlag;
    private MeldekortTjeneste meldekortTjeneste;
    private Unleash unleash;

    InnhentingSamletTjeneste() {
        // CDI
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,
            AktørConsumer aktørConsumer, InntektTjeneste inntektTjeneste, InfotrygdTjeneste wsSak,
            InfotrygdBeregningsgrunnlagTjeneste wsGrunnlag,
            @Aggregator InfotrygdGrunnlag grunnlag,
            @Aggregator InfotrygdSakTjeneste saker,
            MeldekortTjeneste meldekortTjeneste, Unleash unleash) {
        this.arbeidsforhold = arbeidsforholdTjeneste;
        this.aktør = aktørConsumer;
        this.inntekt = inntektTjeneste;
        this.wsGrunnlag = wsGrunnlag;
        this.grunnlag = grunnlag;
        this.saker = saker;
        this.wsSak = wsSak;
        this.meldekortTjeneste = meldekortTjeneste;
        this.unleash = unleash;
    }

    public InntektsInformasjon getInntektsInformasjon(AktørId aktørId, Interval periode, InntektsKilde kilde) {
        FinnInntektRequest.FinnInntektRequestBuilder builder = FinnInntektRequest.builder(
                YearMonth.from(dato(periode.getStart())),
                YearMonth.from(dato(periode.getEnd())));

        builder.medAktørId(aktørId.getId());
        return inntekt.finnInntekt(builder.build(), kilde);
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId, Interval periode) {
        return arbeidsforhold.finnArbeidsforholdForIdentIPerioden(getFnrFraAktørId(aktørId), periode);
    }

    public List<InfotrygdSakOgGrunnlag> getSammenstiltSakOgGrunnlag(AktørId aktørId, Interval periode,
            boolean medGrunnlag) {
        var wsSaker = filtrerSaker(getInfotrygdSaker(aktørId, periode), medGrunnlag);
        var restSaker = filtrerSaker(saker.saker(getFnrFraAktørId(aktørId).getIdent(), dato(periode.getStart())),
                medGrunnlag);

        LOG.info("InfotrygdSak sammenstilling antall saker/vedtak: {}", wsSaker.size());
        if (medGrunnlag) {
            var rest = restSakOgGrunnlag(restSaker,
                    grunnlag.hentGrunnlag(aktørId, dato(periode.getStart()), dato(periode.getEnd())), periode);
            var ws = wsSakOgGrunnlag(wsSaker, wsGrunnlag(aktørId, periode), periode);
            return sammenlign(rest, ws);
        }

        return sammenlign(restSaker, wsSaker)
                .stream()
                .map(InfotrygdSakOgGrunnlag::new)
                .collect(toList());
    }

    private List<InfotrygdSak> filtrerSaker(List<InfotrygdSak> saker, boolean medGrunnlag) {
        return saker
                .stream()
                .filter(s -> skalLagresBetinget(s, medGrunnlag))
                .collect(toList());
    }

    private static boolean skalLagresBetinget(InfotrygdSak sak, boolean medGrunnlag) {
        if (ENSLIG_FORSØRGER.equals(sak.getYtelseType())) {
            return false;
        }
        if (LØPENDE.equals(sak.getYtelseStatus()) || AVSLUTTET.equals(sak.getYtelseStatus())) {
            return sak.erAvRelatertYtelseType(ENGANGSSTØNAD, SYKEPENGER, SVANGERSKAPSPENGER, FORELDREPENGER,
                    PÅRØRENDESYKDOM);
        }
        if (sak.erVedtak()) {
            if (sak.erAvRelatertYtelseType(ENGANGSSTØNAD, FORELDREPENGER)) {
                return true;
            }
            return medGrunnlag && sak.erAvRelatertYtelseType(SYKEPENGER, PÅRØRENDESYKDOM, SVANGERSKAPSPENGER);
        }
        return false;
    }

    private List<InfotrygdSak> getInfotrygdSaker(AktørId aktørId, Interval periode) {
        var restSaker = saker.saker(getFnrFraAktørId(aktørId).getIdent(), dato(periode.getStart()));
        var wsSaker = wsSak.finnSakListe(getFnrFraAktørId(aktørId).getIdent(), dato(periode.getStart()));
        return sammenlign(restSaker, wsSaker);
    }

    private List<YtelseBeregningsgrunnlag> wsGrunnlag(AktørId aktørId, Interval periode) {
        var grunnlag = wsGrunnlag.hentGrunnlagListeFull(aktørId, dato(periode.getStart()));
        LOG.info("InfotrygdBeregningsgrunnlag antall grunnlag: {}", grunnlag.size());
        return grunnlag;
    }

    private boolean matcherSakOgGrunnlag(InfotrygdSakOgGrunnlag sak, Grunnlag grunnlag) {
        if (sak.getGrunnlag().isPresent() || sak.getSak().getIverksatt() == null) {
            return false;
        }
        return grunnlag.getType().equals(sak.getSak().hentRelatertYtelseTypeForSammenstillingMedBeregningsgrunnlag())
                && grunnlag.getIdentdato().equals(sak.getSak().getIverksatt());
    }

    private boolean matcherSakOgGrunnlag(InfotrygdSakOgGrunnlag sak, YtelseBeregningsgrunnlag grunnlag) {
        if (sak.getGrunnlag().isPresent() || sak.getSak().getIverksatt() == null) {
            return false;
        }
        // Samme type (ITrygd tema+behandlingstema) og grunnlag/identdato =
        // sak/iverksattdato
        return grunnlag.getType().equals(sak.getSak().hentRelatertYtelseTypeForSammenstillingMedBeregningsgrunnlag())
                && grunnlag.getIdentdato().equals(sak.getSak().getIverksatt());
    }

    private List<InfotrygdSakOgGrunnlag> restSakOgGrunnlag(List<InfotrygdSak> saker, List<Grunnlag> grunnlag,
            Interval periode) {
        var sammenstilling = saker
                .stream()
                .map(InfotrygdSakOgGrunnlag::new)
                .collect(toList());

        LocalDate fom = dato(periode.getStart());
        for (var ettGrunnlag : grunnlag) {

            var funnet = match(sammenstilling, ettGrunnlag);
            if (funnet.isEmpty() && ettGrunnlag.getPeriode().getTom() != null
                    && !ettGrunnlag.getPeriode().getTom().isBefore(fom)) {
                InfotrygdSak sak = lagSakForYtelseUtenomSaksbasen(
                        new YtelseType(ettGrunnlag.getBehandlingsTema().getKode().name()),
                        new TemaUnderkategori(ettGrunnlag.getBehandlingsTema().getKode().name()),
                        ettGrunnlag.getIdentdato(),
                        DatoIntervallEntitet.fraOgMedTilOgMed(ettGrunnlag.getPeriode().getFom(),
                                ettGrunnlag.getPeriode().getTom()));
                InfotrygdSakOgGrunnlag isog = new InfotrygdSakOgGrunnlag(sak);
                isog.setGrunnlag(ettGrunnlag);
                sammenstilling.add(isog);
                Feilene.FACTORY
                        .manglerInfotrygdSak(ettGrunnlag.getBehandlingsTema().getKode().name(),
                                ettGrunnlag.getIdentdato().toString())
                        .log(LOG);
            }
        }
        return sammenstilling.stream()
                .filter(isog -> !(erAvsluttet(isog) && isog.getPeriode().getTomDato().isBefore(fom)))
                .filter(isog -> skalTypeLagresUansett(isog) || erEtterIverksatt(isog))
                .collect(toList());
    }

    private List<InfotrygdSakOgGrunnlag> wsSakOgGrunnlag(List<InfotrygdSak> saker,
            List<YtelseBeregningsgrunnlag> grunnlag, Interval periode) {
        var sammenstilling = saker
                .stream()
                .map(InfotrygdSakOgGrunnlag::new)
                .collect(toList());
        LocalDate fom = dato(periode.getStart());

        for (var ettGrunnlag : grunnlag) {
            var funnet = match(sammenstilling, ettGrunnlag);
            if (funnet.isEmpty() && ettGrunnlag.getTom() != null && !ettGrunnlag.getTom().isBefore(fom)) {
                InfotrygdSak sak = lagSakForYtelseUtenomSaksbasen(ettGrunnlag.getType(),
                        ettGrunnlag.getTemaUnderkategori(),
                        ettGrunnlag.getIdentdato(),
                        DatoIntervallEntitet.fraOgMedTilOgMed(ettGrunnlag.getFom(), ettGrunnlag.getTom()));
                InfotrygdSakOgGrunnlag isog = new InfotrygdSakOgGrunnlag(sak);
                isog.setGrunnlag(ettGrunnlag);
                sammenstilling.add(isog);
                Feilene.FACTORY
                        .manglerInfotrygdSak(ettGrunnlag.getType().toString(), ettGrunnlag.getIdentdato().toString())
                        .log(LOG);
            }
        }

        return sammenstilling.stream()
                .filter(isog -> !(erAvsluttet(isog) && isog.getPeriode().getTomDato().isBefore(fom)))
                .filter(isog -> skalTypeLagresUansett(isog) || erEtterIverksatt(isog))
                .collect(toList());
    }

    /* Faker sak fra Infotrygd når saken mangler i saksbasen, men finnes i vedtak */
    private InfotrygdSak lagSakForYtelseUtenomSaksbasen(YtelseType type, TemaUnderkategori temaUnderkategori,
            LocalDate identdato, DatoIntervallEntitet periode) {
        YtelseStatus tilstand = FPDateUtil.iDag().isBefore(periode.getTomDato()) ? LØPENDE
                : AVSLUTTET;
        return InfotrygdSak.InfotrygdSakBuilder.ny()
                .medIverksatt(identdato)
                .medRegistrert(identdato)
                .medYtelseType(type)
                .medTemaUnderkategori(temaUnderkategori)
                .medRelatertYtelseTilstand(tilstand)
                .medPeriode(periode)
                .build();
    }

    private Optional<InfotrygdSakOgGrunnlag> match(List<InfotrygdSakOgGrunnlag> saker, Grunnlag grunnlag) {
        Optional<InfotrygdSakOgGrunnlag> funnet = Optional.empty();
        for (InfotrygdSakOgGrunnlag sak : saker) {
            if (funnet.isEmpty() && matcherSakOgGrunnlag(sak, grunnlag)) {
                sak.setGrunnlag(grunnlag);
                funnet = Optional.of(sak);
                if (grunnlag.getPeriode().getFom() != null
                        && grunnlag.getPeriode().getFom().isBefore(sak.getPeriode().getFomDato())) {
                    LOG.info("Grunnlag med fom tidligere enn identdato: {}", grunnlag.getPeriode().getFom());
                }
                if (erAvsluttet(sak) && grunnlag.getPeriode().getTom() != null
                        && sak.getPeriode().getTomDato().isAfter(grunnlag.getPeriode().getTom())) {
                    sak.setPeriode(
                            DatoIntervallEntitet.fraOgMedTilOgMed(sak.getPeriode().getFomDato(),
                                    grunnlag.getPeriode().getTom()));
                }
            }
        }
        return funnet;
    }

    private Optional<InfotrygdSakOgGrunnlag> match(List<InfotrygdSakOgGrunnlag> saker,
            YtelseBeregningsgrunnlag grunnlag) {
        Optional<InfotrygdSakOgGrunnlag> funnet = Optional.empty();
        for (InfotrygdSakOgGrunnlag sak : saker) {
            if (funnet.isEmpty() && matcherSakOgGrunnlag(sak, grunnlag)) {
                sak.setGrunnlag(grunnlag);
                funnet = Optional.of(sak);
                if (grunnlag.getFom() != null && grunnlag.getFom().isBefore(sak.getPeriode().getFomDato())) {
                    LOG.info("Grunnlag med fom tidligere enn identdato: {}", grunnlag.getFom());
                }
                if (erAvsluttet(sak) && grunnlag.getTom() != null
                        && sak.getPeriode().getTomDato().isAfter(grunnlag.getTom())) {
                    sak.setPeriode(
                            DatoIntervallEntitet.fraOgMedTilOgMed(sak.getPeriode().getFomDato(), grunnlag.getTom()));
                }
            }
        }
        return funnet;
    }

    private PersonIdent getFnrFraAktørId(AktørId aktørId) {
        return aktør.hentPersonIdentForAktørId(aktørId.getId())
                .map(PersonIdent::new)
                .orElseThrow();
    }

    private boolean erAvsluttet(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        return AVSLUTTET.equals(infotrygdSakOgGrunnlag.getSak().getYtelseStatus());
    }

    private boolean erEtterIverksatt(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        YtelseStatus tilstand = infotrygdSakOgGrunnlag.getSak().getYtelseStatus();
        return LØPENDE.equals(tilstand) || AVSLUTTET.equals(tilstand);
    }

    private boolean skalTypeLagresUansett(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        YtelseType type = infotrygdSakOgGrunnlag.getSak().getYtelseType();
        return List.of(ENGANGSSTØNAD, FORELDREPENGER).contains(type);
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentYtelserTjenester(AktørId aktørId, Interval periode) {
        List<MeldekortUtbetalingsgrunnlagSak> saker = meldekortTjeneste.hentMeldekortListe(aktørId,
                dato(periode.getStart()),
                dato(periode.getEnd()));
        return filtrerYtelserTjenester(saker);
    }

    private List<MeldekortUtbetalingsgrunnlagSak> filtrerYtelserTjenester(List<MeldekortUtbetalingsgrunnlagSak> saker) {
        List<MeldekortUtbetalingsgrunnlagSak> filtrert = new ArrayList<>();
        for (MeldekortUtbetalingsgrunnlagSak sak : saker) {
            if (sak.getKravMottattDato() == null) {
                if (sak.getVedtakStatus() == null) {
                    InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("vedtak", sak.getSaksnummer()).log(LOG);
                } else {
                    InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("kravMottattDato", sak.getSaksnummer()).log(LOG);
                }
            } else if (UNDER_BEHANDLING.equals(sak.getYtelseTilstand())
                    && sak.getMeldekortene().isEmpty()) {
                InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("meldekort", sak.getSaksnummer()).log(LOG);
            } else if (sak.getVedtaksPeriodeFom() == null && sak.getMeldekortene().isEmpty()) {
                InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("vedtaksDato", sak.getSaksnummer()).log(LOG);
            } else if (sak.getVedtaksPeriodeTom() != null
                    && sak.getVedtaksPeriodeTom().isBefore(sak.getVedtaksPeriodeFom())
                    && sak.getMeldekortene().isEmpty()) {
                InnhentingFeil.FACTORY.ignorerArenaSakMedVedtakTomFørVedtakFom(sak.getSaksnummer()).log(LOG);
            } else {
                filtrert.add(sak);
            }
        }
        return filtrert;
    }

    private <T> List<T> sammenlign(List<T> rest, List<T> ws) {
        if (!rest.containsAll(ws)) {
            warn(rest, ws);
        }
        return unleash.isEnabled(REST_GJELDER) ? rest : ws;
    }

    private static <T> void warn(List<T> restSaker, List<T> wsSaker) {
        var rest = new HashSet<>(restSaker);
        var ws = new HashSet<>(wsSaker);
        LOG.warn("Forskjellig respons fra WS og REST. Fikk {} fra REST og {} fra WS", restSaker, wsSaker);
        LOG.warn("Elementer som ikke er tilstede i begge responser er {}", Sets.symmetricDifference(rest, ws));
        LOG.warn("Elementer fra REST men ikke fra WS {}", Sets.difference(rest, ws));
        LOG.warn("Elementer fra WS men ikke fra REST {}", Sets.difference(ws, rest));
    }

    private static LocalDate dato(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }
}
