package no.nav.foreldrepenger.abakus.app.diagnostikk;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.Transactional;

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

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName(RegisterInnhentingDump.class.getSimpleName() + "-thread");
        return t;
    });

    @Inject
    public ContainerContextRunner() {
    }

    public static ContainerContextRunner createRunner() {
        return CDI.current().select(ContainerContextRunner.class).get();
    }

    public static <T> Future<T> doRun(Kobling kobling, Callable<T> call) {
        var saksnummer = kobling.getSaksnummer();

        var future = EXECUTOR.submit((() -> {
            T result;
            var requestContext = CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
            requestContext.activate();
            var runner = ContainerContextRunner.createRunner();
            try {
                LOG_CONTEXT.add("saksnummer", saksnummer);
                result = runner.submit(call);
            } finally {
                LOG_CONTEXT.remove("saksnummer");
                CDI.current().destroy(runner);
                requestContext.deactivate();
            }
            return result;
        }));

        return future;

    }

    @Transactional
    private <T> T submit(Callable<T> call) throws Exception {
        KontekstHolder.setKontekst(BasisKontekst.forProsesstask());
        var result = call.call();
        KontekstHolder.fjernKontekst();
        return result;
    }

}
