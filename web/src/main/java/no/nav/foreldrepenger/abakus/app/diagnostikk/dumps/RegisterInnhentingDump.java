package no.nav.foreldrepenger.abakus.app.diagnostikk.dumps;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DebugDump;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpKontekst;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;
import no.nav.foreldrepenger.abakus.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingFellesTjenesteImpl;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.AaregRestKlient;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.InfotrygdGrunnlagAggregator;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.spokelse.SpokelseKlient;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.loginmodule.ContainerLogin;

@ApplicationScoped
@YtelseTypeRef
public class RegisterInnhentingDump implements DebugDump {


    private static final Collection<InntektskildeType> INNTEKTSKILDER = IAYRegisterInnhentingFellesTjenesteImpl.ELEMENT_TIL_INNTEKTS_KILDE_MAP.values();

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName(RegisterInnhentingDump.class.getSimpleName() + "-thread");
        return t;
    });

    private InntektTjeneste inntektTjeneste;
    private MeldekortTjeneste meldekortTjeneste; // TODO

    private AaregRestKlient aaregKlient;
    private InfotrygdGrunnlagAggregator infotrygdGrunnlag;
    private SpokelseKlient spokelseKlient;
    private ObjectWriter writer;
    private final String prefiks = "register-innhenting";

    public RegisterInnhentingDump() {
        //
    }

    @Inject
    public RegisterInnhentingDump(AaregRestKlient aaregKlient,
                                  InntektTjeneste inntektTjeneste,
                                  MeldekortTjeneste meldekortTjeneste,
                                  InfotrygdGrunnlagAggregator infotrygdGrunnlag,
                                  SpokelseKlient spokelseKlient) {
        this.aaregKlient = aaregKlient;
        this.inntektTjeneste = inntektTjeneste;
        this.meldekortTjeneste = meldekortTjeneste;
        this.infotrygdGrunnlag = infotrygdGrunnlag;
        this.spokelseKlient = spokelseKlient;
        this.writer = new JacksonJsonConfig().getObjectMapper().writerWithDefaultPrettyPrinter();
    }

    @Override
    public List<DumpOutput> dump(DumpKontekst dumpKontekst) {
        try {

            var data = submit(dumpKontekst, k -> dumpRegister(k))
                .get(20, TimeUnit.SECONDS);

            return data;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return List.of(new DumpOutput(prefiks + "-ERROR.txt", sw.toString()));
        }
    }

    private List<DumpOutput> dumpRegister(DumpKontekst kontekst) {
        var periode = kontekst.getKobling().getOpplysningsperiode();
        var aktørId = kontekst.getKobling().getAktørId();
        var ident = kontekst.getIdent();
        var fom = periode.getFomDato();
        var tom = periode.getTomDato();
        var dumps = new ArrayList<DumpOutput>();

        dumps.addAll(innhentYtelser(ident, fom, tom));

        dumps.addAll(innhentAareg(ident, periode));

        dumps.addAll(innhentInntekt(aktørId, periode));

        return dumps;
    }

    private List<DumpOutput> innhentInntekt(AktørId aktørId, IntervallEntitet periode) {
        var dumps = new ArrayList<DumpOutput>();
        var fom = periode.getFomDato();
        var tom = periode.getTomDato();
        INNTEKTSKILDER.forEach(inntektsKilde -> {
            var request = FinnInntektRequest.builder(YearMonth.from(fom), YearMonth.from(tom))
                .medAktørId(aktørId.getId())
                .build();

            dumps.add(dumpJsonOutput(prefiks + "-inntekt-" + inntektsKilde.getKode(), () -> inntektTjeneste.finnInntektRaw(request, inntektsKilde)));
        });
        return dumps;
    }

    private List<DumpOutput> innhentAareg(PersonIdent ident, IntervallEntitet periode) {
        var dumps = new ArrayList<DumpOutput>();
        var fom = periode.getFomDato();
        var tom = periode.getTomDato();
        dumps.add(dumpJsonOutput(prefiks + "-aareg-arbeid", () -> aaregKlient.finnArbeidsforholdForArbeidstaker(ident.getIdent(), fom, tom)));
        dumps.add(dumpJsonOutput(prefiks + "-aareg-frilans", () -> aaregKlient.finnArbeidsforholdForFrilanser(ident.getIdent(), fom, tom)));
        return dumps;
    }

    private List<DumpOutput> innhentYtelser(PersonIdent ident, LocalDate fom, LocalDate tom) {
        var dumps = new ArrayList<DumpOutput>();
        dumps.add(dumpJsonOutput(prefiks + "-sp", () -> spokelseKlient.hentGrunnlag(ident.getIdent())));

        infotrygdGrunnlag.hentAggregertGrunnlag(ident.getIdent(), fom, tom)
            .forEach(g -> {
                dumps.add(dumpJsonOutput(prefiks + "-infotrygd-" + g.getTema() + "-" + g.getBehandlingsTema(), () -> g));
            });
        return dumps;
    }

    private DumpOutput dumpJsonOutput(String navn, Callable<Object> kall) {
        try {
            var res = kall.call();
            return new DumpOutput(navn + ".json", writer.writeValueAsString(res));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return new DumpOutput(navn + "-ERROR.txt", sw.toString());
        }

    }

    private Future<List<DumpOutput>> submit(DumpKontekst kontekst, Function<DumpKontekst, List<DumpOutput>> call) {

        var future = EXECUTOR.submit(() -> {
            LOG_CONTEXT.add("saksnummer", kontekst.getSaksnummer().getVerdi());
            LOG_CONTEXT.add("ytelseType", kontekst.getYtelseType().getKode());

            var containerLogin = new ContainerLogin();
            try {
                containerLogin.login();
                var result = call.apply(kontekst);
                return result;
            } finally {
                containerLogin.logout();
                LOG_CONTEXT.remove("saksnummer");
                LOG_CONTEXT.remove("ytelseType");
            }

        });
        return future;
    }
}
