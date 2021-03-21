package no.nav.foreldrepenger.abakus.registerdata;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.util.env.Cluster;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);

    private static final Set<YtelseType> LØNNSKOMP_FOR_YTELSER = Set.of(YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER);

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private InntektTjeneste inntektTjeneste;
    private MeldekortTjeneste meldekortTjeneste;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;
    private LønnskompensasjonRepository lønnskompensasjonRepository;
    private boolean isDev;
    private boolean isProd;

    InnhentingSamletTjeneste() {
        //CDI
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,  // NOSONAR
                                    InntektTjeneste inntektTjeneste,
                                    InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste,
                                    LønnskompensasjonRepository lønnskompensasjonRepository,
                                    MeldekortTjeneste meldekortTjeneste) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.inntektTjeneste = inntektTjeneste;
        this.meldekortTjeneste = meldekortTjeneste;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
        this.lønnskompensasjonRepository = lønnskompensasjonRepository;
        this.isDev = Cluster.DEV_FSS.equals(Environment.current().getCluster());
        this.isProd = Cluster.PROD_FSS.equals(Environment.current().getCluster());
    }

    public InntektsInformasjon getInntektsInformasjon(AktørId aktørId, IntervallEntitet periode, InntektskildeType kilde, YtelseType ytelse) {
        FinnInntektRequest.FinnInntektRequestBuilder builder = FinnInntektRequest.builder(YearMonth.from(periode.getFomDato()),
            YearMonth.from(periode.getTomDato()));

        builder.medAktørId(aktørId.getId());

        return inntektTjeneste.finnInntekt(builder.build(), kilde, ytelse);
    }

    public boolean skalInnhenteLønnskompensasjon(Kobling kobling, @SuppressWarnings("unused") InntektskildeType kilde) {
        return LØNNSKOMP_FOR_YTELSER.contains(kobling.getYtelseType());
    }

    public List<Månedsinntekt> getLønnskompensasjon(AktørId aktørId, IntervallEntitet periode) {
        List<Månedsinntekt> resultat = new ArrayList<>();
        lønnskompensasjonRepository.hentLønnskompensasjonForIPeriode(aktørId, periode.getFomDato(), periode.getTomDato()).stream()
            .filter(lk -> lk.getBeløp().getVerdi().compareTo(BigDecimal.ZERO) > 0)
            .forEach(lk -> resultat.addAll(periodiserLønnskompensasjon(lk)));
        return resultat;
    }

    private List<Månedsinntekt> periodiserLønnskompensasjon(LønnskompensasjonVedtak vedtak) {
        return vedtak.getAnvistePerioder().stream()
            .map(a -> new Månedsinntekt.Builder()
                .medMåned(YearMonth.from(a.getAnvistFom()))
                .medBeløp(a.getBeløp().map(Beløp::getVerdi).orElse(BigDecimal.ZERO))
                .medArbeidsgiver(vedtak.getOrgNummer().getId())
                .medYtelse(false)
                .build())
            .filter(mi -> mi.getBeløp().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId, PersonIdent ident, IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforholdFrilans(AktørId aktørId, PersonIdent ident, IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdFrilansForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    private boolean envUnstable() {
        return isDev;
    }

    public List<InfotrygdYtelseGrunnlag> innhentInfotrygdGrunnlag(AktørId aktørId, PersonIdent ident, IntervallEntitet periode) {
        if (envUnstable()) {
            return innhentingInfotrygdTjeneste.getInfotrygdYtelserFailSoft(ident, periode);
        }
        return innhentingInfotrygdTjeneste.getInfotrygdYtelser(ident, periode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentSpokelseGrunnlag(AktørId aktørId, PersonIdent ident, @SuppressWarnings("unused") IntervallEntitet periode) {
        if (!isProd) {
            return Collections.emptyList();
        }
        return innhentingInfotrygdTjeneste.getSPøkelseYtelser(ident);
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentYtelserTjenester(AktørId aktørId, IntervallEntitet opplysningsPeriode) {
        List<MeldekortUtbetalingsgrunnlagSak> saker = meldekortTjeneste.hentMeldekortListe(aktørId,
            opplysningsPeriode.getFomDato(), opplysningsPeriode.getTomDato());
        return filtrerYtelserTjenester(saker);
    }

    private List<MeldekortUtbetalingsgrunnlagSak> filtrerYtelserTjenester(List<MeldekortUtbetalingsgrunnlagSak> saker) {
        List<MeldekortUtbetalingsgrunnlagSak> filtrert = new ArrayList<>();
        for (MeldekortUtbetalingsgrunnlagSak sak : saker) {
            if (sak.getKravMottattDato() == null) {
                if (sak.getVedtakStatus() == null) {
                    loggArenaIgnorert("vedtak", sak.getSaksnummer());
                } else {
                    loggArenaIgnorert("kravMottattDato", sak.getSaksnummer());
                }
            } else if (YtelseStatus.UNDER_BEHANDLING.equals(sak.getYtelseTilstand()) && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("meldekort", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeFom() == null && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("vedtaksDato", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeTom() != null && sak.getVedtaksPeriodeTom().isBefore(sak.getVedtaksPeriodeFom())
                && sak.getMeldekortene().isEmpty()) {
                loggArenaTomFørFom(sak.getSaksnummer());
            } else {
                filtrert.add(sak);
            }
        }
        return filtrert;
    }

    private void loggArenaIgnorert(String ignorert, Saksnummer saksnummer) {
        LOGGER.info("FP-112843 Ignorerer Arena-sak uten {}, saksnummer: {}", ignorert, saksnummer);
    }

    private void loggArenaTomFørFom(Saksnummer saksnummer) {
        LOGGER.info("FP-597341 Ignorerer Arena-sak med vedtakTom før vedtakFom, saksnummer: {}", saksnummer);
    }

}
