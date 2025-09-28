package no.nav.foreldrepenger.abakus.registerdata;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Inntektstype;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.FpwsproxyKlient;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin.KelvinKlient;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.konfig.Environment;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);
    private static final boolean FAILSOFT_DEV = Environment.current().isDev();
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private InntektTjeneste inntektTjeneste;
    private FpwsproxyKlient fpwsproxyKlient;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;
    private LønnskompensasjonRepository lønnskompensasjonRepository;
    private KelvinKlient kelvinKlient;

    InnhentingSamletTjeneste() {
        //CDI
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,
                                    InntektTjeneste inntektTjeneste,
                                    InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste,
                                    LønnskompensasjonRepository lønnskompensasjonRepository,
                                    FpwsproxyKlient fpwsproxyKlient,
                                    KelvinKlient kelvinKlient) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.inntektTjeneste = inntektTjeneste;
        this.fpwsproxyKlient = fpwsproxyKlient;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
        this.lønnskompensasjonRepository = lønnskompensasjonRepository;
        this.kelvinKlient = kelvinKlient;
    }

    public Map<InntektskildeType, InntektsInformasjon> getInntektsInformasjon(AktørId aktørId, IntervallEntitet periode, Set<InntektskildeType> kilder) {
        return inntektTjeneste.finnInntekt(aktørId.getId(), YearMonth.from(periode.getFomDato()), YearMonth.from(periode.getTomDato()), kilder);
    }

    public List<Månedsinntekt> getLønnskompensasjon(AktørId aktørId, IntervallEntitet periode) {
        List<Månedsinntekt> resultat = new ArrayList<>();
        lønnskompensasjonRepository.hentLønnskompensasjonForIPeriode(aktørId, periode.getFomDato(), periode.getTomDato())
            .stream()
            .filter(lk -> lk.getBeløp().getVerdi().compareTo(BigDecimal.ZERO) > 0)
            .forEach(lk -> resultat.addAll(periodiserLønnskompensasjon(lk)));
        return resultat;
    }

    private List<Månedsinntekt> periodiserLønnskompensasjon(LønnskompensasjonVedtak vedtak) {
        return vedtak.getAnvistePerioder()
            .stream()
            .map(a -> new Månedsinntekt(Inntektstype.LØNN, YearMonth.from(a.getAnvistFom()),
                a.getBeløp().map(Beløp::getVerdi).orElse(BigDecimal.ZERO), null, vedtak.getOrgNummer().getId(), null))
            .filter(mi -> mi.beløp().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId,
                                                                                    PersonIdent ident,
                                                                                    IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforholdFrilans(AktørId aktørId,
                                                                                           PersonIdent ident,
                                                                                           IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdFrilansForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentInfotrygdGrunnlag(PersonIdent ident, IntervallEntitet periode) {
        if (FAILSOFT_DEV) {
            return innhentingInfotrygdTjeneste.getInfotrygdYtelserFailSoft(ident, periode);
        }
        return innhentingInfotrygdTjeneste.getInfotrygdYtelser(ident, periode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentSpokelseGrunnlag(PersonIdent ident, @SuppressWarnings("unused") IntervallEntitet periode) {
        return innhentingInfotrygdTjeneste.getSPøkelseYtelser(ident, periode.getFomDato());
    }

    public void innhentMaksimumAAP(PersonIdent ident, IntervallEntitet opplysningsPeriode, List<MeldekortUtbetalingsgrunnlagSak> arena) {
        try {
            var fom = opplysningsPeriode.getFomDato();
            var tom = opplysningsPeriode.getTomDato();
            var kArena = kelvinKlient.hentAAP(ident, fom, tom, arena.size());
            sammenligneArenaDirekteVsKelvin(arena, kArena);
        } catch (Exception e) {
            LOG.info("Maksimum AAP feil ved kall", e);
        }
    }

    private void sammenligneArenaDirekteVsKelvin(List<MeldekortUtbetalingsgrunnlagSak> arena, List<MeldekortUtbetalingsgrunnlagSak> kelvin) {
        var arenaMK = arena.stream().map(MeldekortUtbetalingsgrunnlagSak::getMeldekortene).flatMap(Collection::stream).collect(Collectors.toSet());
        var kelvinMK = kelvin.stream().map(MeldekortUtbetalingsgrunnlagSak::getMeldekortene).flatMap(Collection::stream).collect(Collectors.toSet());
        var vAIkkeK = arena.stream().filter(a -> kelvin.stream().noneMatch(a::likeNokVedtak))
            .map(MeldekortUtbetalingsgrunnlagSak::utskriftUtenMK).collect(Collectors.joining(", "));
        var vKIkkeA = kelvin.stream().filter(a -> arena.stream().noneMatch(a::likeNokVedtak))
            .map(MeldekortUtbetalingsgrunnlagSak::utskriftUtenMK).collect(Collectors.joining(", "));
        var mAIkkeK = arenaMK.stream().filter(a -> kelvinMK.stream().noneMatch(a::equals)).collect(Collectors.toSet());
        var mKIkkeA = kelvinMK.stream().filter(a -> arenaMK.stream().noneMatch(a::equals)).collect(Collectors.toSet());
        if (arena.isEmpty() && kelvin.isEmpty()) {
            return;
        } else if (arena.isEmpty() || kelvin.isEmpty()) {
            LOG.info("Maksimum AAP sammenligning ene er tom:  arena: {} mk {} kelvin: {} mk {}", vAIkkeK, mAIkkeK, vKIkkeA, mKIkkeA);
        } else if (arena.size() != kelvin.size() || arenaMK.size() != kelvinMK.size()) {
            LOG.info("Maksimum AAP sammenligning ulik størrelse:  arena: {} mk {} kelvin: {} mk {}", vAIkkeK, mAIkkeK, vKIkkeA, mKIkkeA);
        } else {
            var likeNokVedtak = arena.stream().allMatch(a -> kelvin.stream().anyMatch(a::likeNokVedtak));
            var likeMk = kelvinMK.containsAll(arenaMK);
            if (likeNokVedtak && likeMk) {
                LOG.info("Maksimum AAP sammenligning likt svar fra arena og AAP-api");
            } else {
                LOG.info("Maksimum AAP sammenligning lik størrelse ulikt innhold: arena: {} mk {} kelvin: {} mk {}", vAIkkeK, mAIkkeK, vKIkkeA, mKIkkeA);
            }
        }
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
                    loggArenaIgnorert("vedtak", sak.getSaksnummer(), sak.getMeldekortene().size());
                } else {
                    loggArenaIgnorert("kravMottattDato", sak.getSaksnummer(), sak.getMeldekortene().size());
                }
            } else if (YtelseStatus.UNDER_BEHANDLING.equals(sak.getYtelseTilstand()) && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("meldekort", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeFom() == null && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("vedtaksDato", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeTom() != null && sak.getVedtaksPeriodeTom().isBefore(sak.getVedtaksPeriodeFom()) && sak.getMeldekortene()
                .isEmpty()) {
                loggArenaTomFørFom(sak.getSaksnummer());
            } else {
                filtrert.add(sak);
            }
        }
        return filtrert;
    }

    private void loggArenaIgnorert(String ignorert, Saksnummer saksnummer, int antmeldekort) {
        // Ingen forekomster i loggen, men for sikkerhets skylt
        LOG.info("FP-112843 Ignorerer Arena-sak uten {}, saksnummer: {} antall meldekort {}", ignorert, saksnummer, antmeldekort);
    }


    private void loggArenaIgnorert(String ignorert, Saksnummer saksnummer) {
        LOG.info("FP-112843 Ignorerer Arena-sak uten {}, saksnummer: {}", ignorert, saksnummer);
    }

    private void loggArenaTomFørFom(Saksnummer saksnummer) {
        LOG.info("FP-597341 Ignorerer Arena-sak med vedtakTom før vedtakFom, saksnummer: {}", saksnummer);
    }

}
