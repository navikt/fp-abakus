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
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.FpwsproxyKlient;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.konfig.Environment;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);
    private static final Set<YtelseType> LØNNSKOMP_FOR_YTELSER = Set.of(YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER);
    private final boolean isDev = Environment.current().isDev();
    private final boolean isLocal = Environment.current().isLocal();

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private InntektTjeneste inntektTjeneste;
    private FpwsproxyKlient fpwsproxyKlient;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;
    private LønnskompensasjonRepository lønnskompensasjonRepository;

    InnhentingSamletTjeneste() {
        //CDI
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,  // NOSONAR
                                    InntektTjeneste inntektTjeneste,
                                    InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste,
                                    LønnskompensasjonRepository lønnskompensasjonRepository,
                                    FpwsproxyKlient fpwsproxyKlient) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.inntektTjeneste = inntektTjeneste;
        this.fpwsproxyKlient = fpwsproxyKlient;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
        this.lønnskompensasjonRepository = lønnskompensasjonRepository;
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

    public List<InfotrygdYtelseGrunnlag> innhentInfotrygdGrunnlag(PersonIdent ident, IntervallEntitet periode) {
        if (isDev) {
            return innhentingInfotrygdTjeneste.getInfotrygdYtelserFailSoft(ident, periode);
        }
        return innhentingInfotrygdTjeneste.getInfotrygdYtelser(ident, periode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentSpokelseGrunnlag(PersonIdent ident, @SuppressWarnings("unused") IntervallEntitet periode) {
        if (isLocal) {
            return Collections.emptyList();
        }
        if (isDev) {
            return innhentingInfotrygdTjeneste.getSPøkelseYtelserFailSoft(ident, periode.getFomDato());
        }
        return innhentingInfotrygdTjeneste.getSPøkelseYtelser(ident, periode.getFomDato());
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentDagpengerAAP(PersonIdent ident, IntervallEntitet opplysningsPeriode) {
        var fom = opplysningsPeriode.getFomDato();
        var tom = opplysningsPeriode.getTomDato();
        var saker = fpwsproxyKlient.hentDagpengerAAP(ident, fom, tom);
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
