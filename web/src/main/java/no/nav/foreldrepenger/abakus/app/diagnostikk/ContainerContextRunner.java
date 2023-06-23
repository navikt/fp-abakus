package no.nav.foreldrepenger.abakus.app.diagnostikk;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;

import no.nav.foreldrepenger.abakus.app.diagnostikk.dumps.RegisterInnhentingDump;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

/**
 * Kjører et kall på en egen tråd med Kontekst som en prosesstask. Kan benyttes til å kalle med system kontekst videre internt.
 * NB: ikke bruk som convenience utenfor dump.
 */
@Dependent
public class ContainerContextRunner {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        var t = new Thread(r);
        t.setDaemon(true);
        t.setName(RegisterInnhentingDump.class.getSimpleName() + "-thread");
        return t;
    });

    @Inject
    public ContainerContextRunner() {
        // CDI
    }

    public static ContainerContextRunner createRunner() {
        return CDI.current().select(ContainerContextRunner.class).get();
    }

    public static <T> Future<T> doRun(Kobling kobling, Callable<T> call) {
        var saksnummer = kobling.getSaksnummer();

        return EXECUTOR.submit((() -> {
            T result;
            var requestContext = CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
            requestContext.activate();
            var runner = ContainerContextRunner.createRunner();
            try {
                LOG_CONTEXT.add("koblingId", kobling.getId());
                LOG_CONTEXT.add("saksnummer", saksnummer);
                result = runner.submit(call);
            } finally {
                LOG_CONTEXT.remove("saksnummer");
                LOG_CONTEXT.remove("koblingId");
                CDI.current().destroy(runner);
                requestContext.deactivate();
            }
            return result;
        }));

    }

    @Transactional
    private <T> T submit(Callable<T> call) throws Exception {
        KontekstHolder.setKontekst(BasisKontekst.forProsesstask());
        var result = call.call();
        KontekstHolder.fjernKontekst();
        return result;
    }

}
