package no.nav.foreldrepenger.abakus.registerdata;

import static no.nav.vedtak.feil.LogLevel.INFO;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

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
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag.YtelseBeregningsgrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);
    private static final String REST_GJELDER = "fpabakus.infotrygd.rest";

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private AktørConsumer aktørConsumer;
    private InntektTjeneste inntektTjeneste;
    private InfotrygdTjeneste infotrygdTjeneste;
    private InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste;
    private MeldekortTjeneste meldekortTjeneste;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;
    private Unleash unleash;

    InnhentingSamletTjeneste() {
        //CDI
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,  // NOSONAR
                                    AktørConsumer aktørConsumer, InntektTjeneste inntektTjeneste, InfotrygdTjeneste infotrygdTjeneste,
                                    InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste,
                                    InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste,
                                    MeldekortTjeneste meldekortTjeneste, Unleash unleash) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.aktørConsumer = aktørConsumer;
        this.inntektTjeneste = inntektTjeneste;
        this.infotrygdBeregningsgrunnlagTjeneste = infotrygdBeregningsgrunnlagTjeneste;
        this.infotrygdTjeneste = infotrygdTjeneste;
        this.meldekortTjeneste = meldekortTjeneste;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
        this.unleash = unleash;
    }

    public InntektsInformasjon getInntektsInformasjon(AktørId aktørId, Interval periode, InntektsKilde kilde) {
        FinnInntektRequest.FinnInntektRequestBuilder builder = FinnInntektRequest.builder(YearMonth.from(LocalDateTime.ofInstant(periode.getStart(), ZoneId.systemDefault())),
            YearMonth.from(LocalDateTime.ofInstant(periode.getEnd(), ZoneId.systemDefault())));

        builder.medAktørId(aktørId.getId());

        return inntektTjeneste.finnInntekt(builder.build(), kilde);
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId, Interval opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(getFnrFraAktørId(aktørId), opplysningsPeriode);
    }

    private PersonIdent getFnrFraAktørId(AktørId aktørId) {
        return aktørConsumer.hentPersonIdentForAktørId(aktørId.getId()).map(PersonIdent::new).orElseThrow();
    }

    public List<InfotrygdYtelseGrunnlag> innhentRest(AktørId aktørId, Interval periode) {
        if (unleash != null && unleash.isEnabled(REST_GJELDER, true)) {
            var ident = getFnrFraAktørId(aktørId);
            return innhentingInfotrygdTjeneste.getInfotrygdYtelser(ident, periode);
        }
        return Collections.emptyList();
    }

    public boolean sammenlignWsRest(List<InfotrygdSakOgGrunnlag> ws, List<InfotrygdYtelseGrunnlag> rest) {
        return innhentingInfotrygdTjeneste.sammenlignGrunnlagKilder(ws, rest);
    }


    public List<InfotrygdSakOgGrunnlag> getSammenstiltSakOgGrunnlag(AktørId aktørId, Interval opplysningsPeriode, boolean medGrunnlag) {
        final List<InfotrygdSak> infotrygdSakList = filtrerSaker(getInfotrygdSaker(aktørId, opplysningsPeriode), medGrunnlag);
        LOGGER.info("Infotrygd Sak : {}", infotrygdSakList);
        if (medGrunnlag) {
            List<YtelseBeregningsgrunnlag> alleGrunnlag = getInfotrygdBeregningsgrunnlag(aktørId, opplysningsPeriode);
            LOGGER.info("Infotrygd Beregningsgrunnlag : {}", alleGrunnlag);
            return sammenstillSakOgGrunnlag(infotrygdSakList, alleGrunnlag, opplysningsPeriode);
        }

        return infotrygdSakList.stream().map(InfotrygdSakOgGrunnlag::new).collect(Collectors.toList());
    }

    private List<InfotrygdSak> filtrerSaker(List<InfotrygdSak> infotrygdSaker, boolean medGrunnlag) {
        if (medGrunnlag) {
            return infotrygdSaker.stream()
                .filter(this::skalLagresIfbmGrunnlag).collect(Collectors.toList());
        } else {
            return infotrygdSaker.stream()
                .filter(this::skalLagres).collect(Collectors.toList());
        }
    }

    private boolean skalLagres(InfotrygdSak infotrygdSak) {
        return skalLagresBetinget(infotrygdSak, false);
    }

    private boolean skalLagresIfbmGrunnlag(InfotrygdSak infotrygdSak) {
        return skalLagresBetinget(infotrygdSak, true);
    }

    private boolean skalLagresBetinget(InfotrygdSak infotrygdSak, boolean medGrunnlag) {
        if (YtelseType.ENSLIG_FORSØRGER.equals(infotrygdSak.getYtelseType())) {
            return false;
        }
        if (YtelseStatus.LØPENDE.equals(infotrygdSak.getYtelseStatus()) || YtelseStatus.AVSLUTTET.equals(infotrygdSak.getYtelseStatus())) {
            return infotrygdSak.erAvRelatertYtelseType(YtelseType.ENGANGSSTØNAD, YtelseType.SYKEPENGER,
                YtelseType.SVANGERSKAPSPENGER, YtelseType.FORELDREPENGER, YtelseType.PÅRØRENDESYKDOM);
        } else if (infotrygdSak.erVedtak()) {
            if (infotrygdSak.erAvRelatertYtelseType(YtelseType.ENGANGSSTØNAD, YtelseType.FORELDREPENGER)) {
                return true;
            }
            return medGrunnlag && infotrygdSak.erAvRelatertYtelseType(YtelseType.SYKEPENGER,
                YtelseType.PÅRØRENDESYKDOM, YtelseType.SVANGERSKAPSPENGER);
        }
        return false;
    }

    private List<InfotrygdSak> getInfotrygdSaker(AktørId aktørId, Interval opplysningsPeriode) {
        return infotrygdTjeneste.finnSakListe(getFnrFraAktørId(aktørId).getIdent(), LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate());
    }

    private List<YtelseBeregningsgrunnlag> getInfotrygdBeregningsgrunnlag(AktørId aktørId, Interval opplysningsPeriode) {
        return infotrygdBeregningsgrunnlagTjeneste.hentGrunnlagListeFull(aktørId,
            LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate());
    }

    private boolean matcherSakOgGrunnlag(InfotrygdSakOgGrunnlag sak, YtelseBeregningsgrunnlag grunnlag) {
        if (sak.getGrunnlag().isPresent() || sak.getSak().getIverksatt() == null) {
            return false;
        }
        // Samme type (ITrygd tema+behandlingstema) og grunnlag/identdato = sak/iverksattdato
        return grunnlag.getType().equals(sak.getSak().hentRelatertYtelseTypeForSammenstillingMedBeregningsgrunnlag())
            && grunnlag.getIdentdato().equals(sak.getSak().getIverksatt());
    }

    private List<InfotrygdSakOgGrunnlag> sammenstillSakOgGrunnlag(List<InfotrygdSak> saker, List<YtelseBeregningsgrunnlag> grunnlagene,
                                                                  Interval opplysningsPeriode) {
        List<InfotrygdSakOgGrunnlag> sammenstilling = saker.stream().map(InfotrygdSakOgGrunnlag::new).collect(Collectors.toList());
        LocalDate periodeFom = LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate();

        for (YtelseBeregningsgrunnlag grunnlag : grunnlagene) {
            Optional<InfotrygdSakOgGrunnlag> funnet = settSammenSakMatchendeGrunnlag(sammenstilling, grunnlag);
            if (funnet.isEmpty() && grunnlag.getTom() != null && !grunnlag.getTom().isBefore(periodeFom)) {
                InfotrygdSak sak = lagSakForYtelseUtenomSaksbasen(grunnlag.getType(), grunnlag.getTemaUnderkategori(), grunnlag.getIdentdato(),
                    DatoIntervallEntitet.fraOgMedTilOgMed(grunnlag.getFom(), grunnlag.getTom()));
                InfotrygdSakOgGrunnlag isog = new InfotrygdSakOgGrunnlag(sak);
                isog.setGrunnlag(grunnlag);
                sammenstilling.add(isog);
                InnhentingSamletTjeneste.Feilene.FACTORY.manglerInfotrygdSak(grunnlag.getType().toString(), grunnlag.getIdentdato().toString()).log(LOGGER);
            }
        }

        return sammenstilling.stream()
            .filter(isog -> !(erAvsluttet(isog) && isog.getPeriode().getTomDato().isBefore(periodeFom)))
            .filter(isog -> skalTypeLagresUansett(isog) || erEtterIverksatt(isog))
            .collect(Collectors.toList());
    }

    /* Faker sak fra Infotrygd når saken mangler i saksbasen, men finnes i vedtak */
    private InfotrygdSak lagSakForYtelseUtenomSaksbasen(YtelseType type, TemaUnderkategori temaUnderkategori, LocalDate identdato, DatoIntervallEntitet periode) {
        YtelseStatus tilstand = FPDateUtil.iDag().isBefore(periode.getTomDato()) ? YtelseStatus.LØPENDE : YtelseStatus.AVSLUTTET;
        return InfotrygdSak.InfotrygdSakBuilder.ny()
            .medIverksatt(identdato)
            .medRegistrert(identdato)
            .medYtelseType(type)
            .medTemaUnderkategori(temaUnderkategori)
            .medRelatertYtelseTilstand(tilstand)
            .medPeriode(periode)
            .build();
    }

    private Optional<InfotrygdSakOgGrunnlag> settSammenSakMatchendeGrunnlag(List<InfotrygdSakOgGrunnlag> sakene, YtelseBeregningsgrunnlag grunnlag) {
        Optional<InfotrygdSakOgGrunnlag> funnet = Optional.empty();
        for (InfotrygdSakOgGrunnlag sak : sakene) {
            if (funnet.isEmpty() && matcherSakOgGrunnlag(sak, grunnlag)) {
                sak.setGrunnlag(grunnlag);
                funnet = Optional.of(sak);
                if (grunnlag.getFom() != null && grunnlag.getFom().isBefore(sak.getPeriode().getFomDato())) {
                    LOGGER.info("Grunnlag med fom tidligere enn identdato: {}", grunnlag.getFom());
                }
                if (erAvsluttet(sak) && grunnlag.getTom() != null && sak.getPeriode().getTomDato().isAfter(grunnlag.getTom())) {
                    sak.setPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(sak.getPeriode().getFomDato(), grunnlag.getTom()));
                }
            }
        }
        return funnet;
    }

    private boolean erAvsluttet(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        return YtelseStatus.AVSLUTTET.equals(infotrygdSakOgGrunnlag.getSak().getYtelseStatus());
    }

    private boolean erEtterIverksatt(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        YtelseStatus tilstand = infotrygdSakOgGrunnlag.getSak().getYtelseStatus();
        return YtelseStatus.LØPENDE.equals(tilstand) || YtelseStatus.AVSLUTTET.equals(tilstand);
    }

    private boolean skalTypeLagresUansett(InfotrygdSakOgGrunnlag infotrygdSakOgGrunnlag) {
        YtelseType type = infotrygdSakOgGrunnlag.getSak().getYtelseType();
        return List.of(YtelseType.ENGANGSSTØNAD, YtelseType.FORELDREPENGER).contains(type);
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentYtelserTjenester(AktørId aktørId, Interval opplysningsPeriode) {
        List<MeldekortUtbetalingsgrunnlagSak> saker = meldekortTjeneste.hentMeldekortListe(aktørId,
            LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault()).toLocalDate(),
            LocalDateTime.ofInstant(opplysningsPeriode.getEnd(), ZoneId.systemDefault()).toLocalDate());
        return filtrerYtelserTjenester(saker);
    }

    private List<MeldekortUtbetalingsgrunnlagSak> filtrerYtelserTjenester(List<MeldekortUtbetalingsgrunnlagSak> saker) {
        List<MeldekortUtbetalingsgrunnlagSak> filtrert = new ArrayList<>();
        for (MeldekortUtbetalingsgrunnlagSak sak : saker) {
            if (sak.getKravMottattDato() == null) {
                if (sak.getVedtakStatus() == null) {
                    InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("vedtak", sak.getSaksnummer()).log(LOGGER);
                } else {
                    InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("kravMottattDato", sak.getSaksnummer()).log(LOGGER);
                }
            } else if (YtelseStatus.UNDER_BEHANDLING.equals(sak.getYtelseTilstand()) && sak.getMeldekortene().isEmpty()) {
                InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("meldekort", sak.getSaksnummer()).log(LOGGER);
            } else if (sak.getVedtaksPeriodeFom() == null && sak.getMeldekortene().isEmpty()) {
                InnhentingFeil.FACTORY.ignorerArenaSakInfoLogg("vedtaksDato", sak.getSaksnummer()).log(LOGGER);
            } else if (sak.getVedtaksPeriodeTom() != null && sak.getVedtaksPeriodeTom().isBefore(sak.getVedtaksPeriodeFom())
                && sak.getMeldekortene().isEmpty()) {
                InnhentingFeil.FACTORY.ignorerArenaSakMedVedtakTomFørVedtakFom(sak.getSaksnummer()).log(LOGGER);
            } else {
                filtrert.add(sak);
            }
        }
        return filtrert;
    }

    interface Feilene extends DeklarerteFeil {
        InnhentingSamletTjeneste.Feilene FACTORY = FeilFactory.create(InnhentingSamletTjeneste.Feilene.class);

        @TekniskFeil(feilkode = "FP-074125", feilmelding = "Mangler Infotrygdsak for Infotrygdgrunnlag av type %s identdato %s", logLevel = INFO)
        Feil manglerInfotrygdSak(String type, String dato);
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
        LOGGER.warn("Forskjellig respons fra WS og REST. Fikk {} fra REST og {} fra WS", restSaker, wsSaker);
        //LOGGER.warn("Elementer som ikke er tilstede i begge responser er {}", Sets.symmetricDifference(rest, ws));
        //LOGGER.warn("Elementer fra REST men ikke fra WS {}", Sets.difference(rest, ws));
        //LOGGER.warn("Elementer fra WS men ikke fra REST {}", Sets.difference(ws, rest));
    }

    private static LocalDate dato(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }
}
